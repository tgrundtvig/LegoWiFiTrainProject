#include <RemoteDevice.h>
#include <ESP8266WiFi.h>
#include <WiFiManager.h>

#define DEVICE_TYPE 0 //0 -> Debug and test device
#define DEVICE_VERSION 1
#define DEVICE_TYPE_NAME "TestDevice"
#define MAX_PACKAGE_SIZE 1024

RemoteDevice device(onPackageReceived, DEVICE_TYPE, DEVICE_VERSION, DEVICE_TYPE_NAME, MAX_PACKAGE_SIZE);

void setup()
{
  Serial.begin(115200);
  device.start("192.168.0.140",3377,false);
}

void loop()
{
  device.update();
}

void onPackageReceived(uint8_t data[], int size)
{
  Serial.print("Package received, size: ");
  Serial.println(size);
  Serial.println("Content:");
  for(int i = 0; i < size; ++i)
  {
    Serial.println(data[i]);
  }
}
