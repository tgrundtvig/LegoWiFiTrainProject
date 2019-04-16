// Motor pin definitions
#define IS_LEFT_SWITCH false 
#define MOTOR_PIN_1 D1 // IN1 on the ULN2003 driver
#define MOTOR_PIN_2 D2 // IN2 on the ULN2003 driver
#define MOTOR_PIN_3 D3 // IN3 on the ULN2003 driver
#define MOTOR_PIN_4 D5 // IN4 on the ULN2003 driver
#define LED_PIN LED_BUILTIN
#define WIFI_RESET_PIN D6

#include <Arduino.h>
#include "LegoRailroadSwitch.h"

LegoRailroadSwitch legoSwitch(MOTOR_PIN_1, MOTOR_PIN_2, MOTOR_PIN_3, MOTOR_PIN_4, WIFI_RESET_PIN, LED_PIN, IS_LEFT_SWITCH);

void setup()
{
  Serial.begin(115200);
}

void loop()
{
  legoSwitch.update(millis());
}