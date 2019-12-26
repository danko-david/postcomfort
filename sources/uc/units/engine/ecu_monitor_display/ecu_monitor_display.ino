/**
 * This is an arduino application that displays the ecu_monitor's
 * information on a 2x16 liquid crystal display.
 * To compile you need to have UARTBus arduino library installed
 * into you arduino environment.
 * Currently this library not available in the arduino library manager
 * so you have to export from the UARTBus project.
 * https://github.com/danko-david/uartbus
 * using script: ./scripts/export_arduino_lib.sh
 */

#include "ub_arduino.h"
#include <LiquidCrystal.h>

LiquidCrystal lcd(10,11, 2,3,4,5);

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

uint32_t extractU32(uint8_t * data, uint16_t from)
{
  union to_bytes b;
  b._8_3 = data[from+0];
  b._8_2 = data[from+1];
  b._8_1 = data[from+2];
  b._8_0 = data[from+3];
  return b._32;
}

uint16_t extractU16(uint8_t * data, uint16_t from)
{
  union to_bytes b;
  b._8_1 = data[from+0];
  b._8_0 = data[from+1];
  return b._16_0;
}

void print3(uint16_t val)
{
  if(val < 100)
  {
    lcd.print(" ");
  }
  if(val < 10)
  {
    lcd.print(" ");
  }
  lcd.print(val);
}

void clearDisplay()
{
    lcd.setCursor(0, 0);
    lcd.print("                ");
    lcd.setCursor(0, 1);
    lcd.print("                ");
}

void idleDisplay()
{
    lcd.setCursor(0, 0);
    lcd.print("  PostComfort   ");
    lcd.setCursor(0, 1);
    lcd.print("No engine signal ");
}

uint32_t lastDraw = 0;

/**
packet layout:
X:X:dt{uint32_t}:lambda{uint16_t}:TPS{uint16_t}:injections{uint16_t}:inject_time{uint32_t}
*/
void onPacketReceived(UartBus& bus, uint8_t* data, uint16_t size)
{
  digitalWrite(13, !digitalRead(13));
  //(vint)-32:X{sender_address}:10:data
  if
  (
    size > 16
  &&
    95 == data[0]
  &&
    10 == data[2]
  )
  {
    uint32_t dt = extractU32(data, 3);
    uint16_t lambda = extractU16(data, 7);
    uint16_t throttle = extractU16(data, 9);
    uint16_t injectCount = extractU16(data, 11);
    uint32_t injectTime = extractU32(data, 13);
    injectTime *= 100;

    clearDisplay();
    
    lcd.setCursor(0, 0);
    lcd.print("L ");
    print3(lambda/ 10);

    lcd.print(" TP ");
    print3(throttle/ 10);
    
    
    lcd.setCursor(0, 1);
    lcd.print("I ");
    print3(injectTime/dt);
    
    lcd.print("   (");
    print3(injectCount);
    lcd.print(")");

    lastDraw = micros();
  }
}

UartBus ub;

void serialEvent()
{
  ub.processIncomingStream();
}

void setup()
{
  ub.init(Serial, 115200, 48, onPacketReceived);
  lcd.begin(16, 2);
  idleDisplay();
  
  //set backlight
  analogWrite(6, 20);
}

void loop()
{
  ub.manage();
  if(lastDraw + 1000000 < micros())
  {
     idleDisplay();
     lastDraw = micros();
  }
}

