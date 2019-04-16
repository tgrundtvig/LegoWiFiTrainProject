#include <Arduino.h>
#include "TestDevice.h"

TestDevice device(D6, LED_BUILTIN);

void setup()
{
  Serial.begin(115200);
}

void loop()
{
  device.update(millis());
}