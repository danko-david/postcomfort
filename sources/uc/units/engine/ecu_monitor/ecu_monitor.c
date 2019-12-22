
#include <avr/io.h>
#include <avr/wdt.h>
#include <util/delay.h>

#include <avr/interrupt.h>
#include <avr/sleep.h>

#include "ub_app_wrapper.h"
#include "rpc.h"


/*
This unit measures:
- lambda value
- gas pedal 
- gasoline injection time and count

TODO measure also:
- vehicle speed
- engine RPM
- coolant temperature
- mass of air flow

And publishes periodically after X ms

packet layout:
X:X:dt{uint32_t}:lambda{uint16_t}:TPS{uint16_t}:injections{uint16_t}:inject_time{uint32_t}

*/


/********************************* App section ********************************/

volatile uint16_t report_dt_millis = 0;

//when report enabled send the actual and fake lambda value
// half secounds repetly

uint32_t t_sample_prev;
uint32_t t_publish_next;

volatile uint32_t tmp_open_start;

volatile uint32_t t_open_all;
volatile uint16_t n_opens;

bool iprev;

void init()
{
	DDRB = DDRD = 0xff;
	ADCSRA = (1<<ADEN) | 7;//enable and prescale = 128 (16MHz/128 = 125kHz)
	
	DDRB &= ~(1 << DDB4);// Clear the PB0 pin
	// PB0 (PCINT0 pin) is now an input


	PCICR |= (1 << PCIE0);// set PCIE0 to enable PCMSK0 scan
	PCMSK0 |= (1 << PB4);// set PCINT0 to trigger an interrupt on state change 
	
	//TODO set relay and SPI resistor outputs
}

//reads 10 bit ADC from A0
uint16_t read_adc(uint8_t channel)
{
	ADMUX = (1<<REFS0) | (1<<REFS1) | (channel & 0x0f);//use internal 1.1V reference and select input
	ADCSRA |= (1<<ADSC);//start the conversion
	while (ADCSRA & (1<<ADSC));//wait for end of conversion
	return ADCW;
}

void set_next_publish_time()
{
	t_sample_prev = micros();
	t_publish_next = t_sample_prev + ((uint32_t)report_dt_millis)*1000l;
}

void start_measure_publish(uint16_t millis)
{
	//forces the interrupt handler to accept ony the next port openeing
	//and populate the variables
	report_dt_millis = millis;
	iprev = true;
	t_open_all = 0;
	n_opens = 0;
	set_next_publish_time();
}

union to_bytes
{
	uint32_t _32;
	struct
	{
		uint16_t _16_0;
		uint16_t _16_1;
	};
	struct
	{
		uint8_t _8_0;
		uint8_t _8_1;
		uint8_t _8_2;
		uint8_t _8_3;
	};
};


//pin 12 change
ISR(PCINT0_vect)
{
	if(0 == report_dt_millis)
	{
		return;
	}
	
	bool inow = (PINB & (1 << PINB4));
	
	//injector change happened
	if(inow != iprev)
	{
		//reverse logic, when low the port is opened we start measuring the time
		if(inow)
		{
			//closing port
			t_open_all += micros() - tmp_open_start;
		}
		else
		{
			//opening port
			tmp_open_start = micros();
			++n_opens;
		}
		
		iprev = inow;
	}
}


void loop()
{
	if(0 != report_dt_millis)
	{
		if(t_publish_next <= micros())
		{
			//publish the values
			union to_bytes b;
			
			uint32_t dt = micros() - t_sample_prev;
			
			uint32_t ot = t_open_all;
			t_open_all = 0;
			uint8_t on = n_opens;
			n_opens = 0;
			
			uint16_t lambda = read_adc(0);
			uint16_t tp = read_adc(1);
			
			set_next_publish_time();
			
			uint8_t data[14];
			
			b._32 = dt;
			data[0] = b._8_3;
			data[1] = b._8_2;
			data[2] = b._8_1;
			data[3] = b._8_0;
			
			b._16_0 = lambda;
			data[4] = b._8_1;
			data[5] = b._8_0;
			
			b._16_0 = tp;
			data[6] = b._8_1;
			data[7] = b._8_0;
			
			b._16_0 = on;
			data[8] = b._8_1;
			data[9] = b._8_0;
			
			b._32 = ot;
			data[10] = b._8_3;
			data[11] = b._8_2;
			data[12] = b._8_1;
			data[13] = b._8_0;

			//construct package
			send_packet(-32, 10, data, 14);
		}	
	}
}

//set values: 

//32:0 => setReport_dt_millis

void packet_received(struct rpc_request* req)
{
	++req->procPtr;
	uint8_t* data = req->payload + req->procPtr;
	uint8_t len = req->size - req->procPtr;
	if(0 == len || 32 != data[-1])
	{
		return;
	}

	if(0 == data[0])
	{
		if(len == 3)
		{
			union to_bytes b;
			b._8_1 = data[1];
			b._8_0 = data[2];
			start_measure_publish(b._16_0);
			
			il_reply(req, 1, 0); 
		}
		else
		{
			il_reply(req, 1, EINVAL); 
		}
	}
}

void setup()
{
	init();
	register_packet_dispatch(packet_received);
	start_measure_publish(250);
}

