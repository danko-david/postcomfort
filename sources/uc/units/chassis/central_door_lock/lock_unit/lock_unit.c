
#include <avr/io.h>
#include <avr/wdt.h>
#include <util/delay.h>


#include <avr/interrupt.h>
#include <avr/sleep.h>


#include "ub_app_wrapper.h"
#include "rpc.h"
/********************************* App section ********************************/

//get state
//get operation: 
//move state (1 lock, 0 unlock)

volatile bool prev_lock = false;
volatile bool prev_unlock = false;

volatile uint32_t lastCmd = 0;
volatile uint8_t cmd = 0;

void packet_received(struct rpc_request* req)
{
	++req->procPtr;
	uint8_t* data = req->payload + req->procPtr;
	uint8_t ep = req->size - req->procPtr;
	if(0 == ep || 32 != data[-1])
	{
		return;
	}

	if(0 == data[0])
	{
		il_reply(req, 1, (prev_lock?2:0)| (prev_unlock?1:0));
	}
	else if(1 == data[0])
	{
		il_reply(req, 1, cmd);
	}
	else if(2 == data[0])
	{
		lastCmd = micros();
		cmd = data[1];
		il_reply(req, 1, data[1]);
	}
}

void releaseOperation()
{
	//reset command
	cmd = 0;
	PORTD &= ~(_BV(PD2) | _BV(PD3));
}
/*
void go_sleep()
{
	set_sleep_mode(SLEEP_MODE_IDLE);
	wdt_disable();
	sleep_enable();
    sei();
    sleep_cpu();
    sleep_disable();
}*/

bool isLocked()
{
	return prev_lock & !prev_unlock;
}

bool isUnlocked()
{
	return !prev_lock & prev_unlock;
}

void loop()
{
	bool lck = PIND & _BV(PD4);
	bool ulck = PIND & _BV(PD5);
	bool send = false;

	if((prev_lock != lck) || (prev_unlock != ulck))
	{
		if(lck != ulck)
		{
			prev_lock = lck;
			prev_unlock = ulck;
			uint8_t send = (lck?2:0)| (ulck?1:0);
			send_packet(-32, 0, &send, 1);
			send = true;
		}
	}
	
	if(cmd != 0)
	{
		if((lastCmd + 500000) < micros())
		{
			releaseOperation();
			return;
		}
	
		if(1 == cmd)
		{
			if(isLocked())
			{
				cmd = 3;
				lastCmd = micros();
				return;
			}
			else
			{
				PORTD &= ~_BV(PD3);
				PORTD |= _BV(PD2);
			}
		}
		else if(2 == cmd)
		{
			if(isUnlocked())
			{
				cmd = 3;
				lastCmd = micros();
				return;
			}
			else
			{
				PORTD &= ~_BV(PD2);
				PORTD |= _BV(PD3);
			}
		}
		else if(3 == cmd)
		{
			if((lastCmd + 100000) < micros())
			{
				releaseOperation();
			}
		}
		else
		{
			releaseOperation();
		}
	}
}

void setup()
{
	cmd = 0;
	DDRD |= _BV(PD2) | _BV(PD3);
	DDRD &= ~(_BV(PD4) | _BV(PD5));
	register_packet_dispatch(packet_received);
}

