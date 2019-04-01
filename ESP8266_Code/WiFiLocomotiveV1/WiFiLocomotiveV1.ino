/*
  Author: Tobias Grundtvig
  This code is used to run at the modified Lego train locomotive, to control it over WiFi.
*/
#include <RemoteDevice.h>

#define DEVICE_TYPE 2 //2 -> Locomotive
#define DEVICE_VERSION 1
#define DEVICE_TYPE_NAME "Locomotive"
#define MAX_PACKAGE_SIZE 7

#define PWMA D1
#define PWMB D2
#define DA D3
#define DB D4
#define HALL_SENSOR_A D5
#define HALL_SENSOR_B D7
#define WIFI_RESET_PIN D6
/*
#define MASK_0 0x000000FF
#define MASK_1 0x0000FF00
#define MASK_2 0x00FF0000
#define MASK_3 0xFF000000
*/
uint8_t data[MAX_PACKAGE_SIZE];

uint32 totalCount = 2147483648;
volatile int deltaCount = 0;
volatile bool onMagnet = false;
volatile unsigned long enterMagnetTime = 0;
volatile unsigned long onMagnetTime = 0;
uint8_t curDirection = 0;
uint16_t curSpeed = 0;
bool packageSent = false;
bool sendNewState = false;
bool updateSpeedAndDirection = false;

//Test stuff
volatile bool onA = false;
volatile bool onB = false;

void onPackageReceived(uint8_t data[], uint16_t size);
bool onSendInitializationPackage();

RemoteDevice device(  onPackageReceived,
                      onSendInitializationPackage,
                      (uint32_t) DEVICE_TYPE,
                      (uint32_t) DEVICE_VERSION,
                      (char*) DEVICE_TYPE_NAME,
                      (uint16_t) MAX_PACKAGE_SIZE );

bool onSendInitializationPackage()
{
  return sendState();
}

bool sendState()
{
  data[0] = totalCount >> 24;
  data[1] = totalCount >> 16;
  data[2] = totalCount >> 8;
  data[3] = totalCount;
  data[4] = curDirection;
  data[5] = curSpeed >> 8;
  data[6] = curSpeed;
  return device.sendPackage(data, 7);
}

void setup()
{
  Serial.begin(115200);
  delay(10);

  pinMode(LED_BUILTIN, OUTPUT);     // Initialize the LED_BUILTIN pin as an output
  pinMode(PWMA, OUTPUT);
  pinMode(PWMB, OUTPUT);
  pinMode(DA, OUTPUT);
  pinMode(DB, OUTPUT);
  pinMode(HALL_SENSOR_A, INPUT_PULLUP);
  pinMode(HALL_SENSOR_B, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(HALL_SENSOR_A),onHallSensorA,RISING);
  attachInterrupt(digitalPinToInterrupt(HALL_SENSOR_B),onHallSensorB,RISING);
  

  
  Serial.println("Start WiFi connection...");
  // Connect to WiFi network
  
  //WiFiManager
  //Local intialization. Once its business is done, there is no need to keep it around
  boolean resetWiFi = !digitalRead(WIFI_RESET_PIN);
  device.start("192.168.43.125",3377,resetWiFi);
}

void loop()
{
  if(onA)
  {
    Serial.println("onA");
    onA = false;
  }
  if(onB)
  {
    Serial.println("onB");
    onB = false;
  }
  device.update();
  if(!device.isConnected())
  {
    curSpeed = 0;
    updateSpeedAndDirection = true;
  }
  if(deltaCount > 0 || !packageSent)
  {
    totalCount += deltaCount;
    deltaCount = 0;
    sendNewState = true;
    Serial.print("Updating position to: ");
    Serial.println(totalCount);
  }
  if(sendState)
  {
    packageSent = sendState();
    if(!packageSent)
    {
      curSpeed = 0;
      updateSpeedAndDirection = true;
    }
    else
    {
      sendNewState = false;
    }
  }
  if(updateSpeedAndDirection)
  {
    digitalWrite(DA, curDirection);
    analogWrite(PWMA, curSpeed);
  }
}

void onPackageReceived(uint8_t data[], uint16_t size)
{
  uint8_t newDirection = data[0];
  uint16_t newSpeed = data[1];
  newSpeed <<= 8;
  newSpeed += data[2];
  if(newDirection != curDirection && curSpeed == 0)
  {
    sendNewState = true;
    curDirection = newDirection;
    if(curDirection == 0)
    {
      if(digitalRead(HALL_SENSOR_A))
      {
        onMagnet = true;
      }
      if(digitalRead(HALL_SENSOR_B))
      {
        onMagnet = false;
      }
    }
    else //curDirection == 1
    {
      if(digitalRead(HALL_SENSOR_B))
      {
        onMagnet = true;
      }
      if(digitalRead(HALL_SENSOR_A))
      {
        onMagnet = false;
      }
    }
  }
}

void onHallSensorA()
{
  onA = true;
  if(curDirection == 0)
  {
    onMagnet = true;
    enterMagnetTime = millis();
  }
  else //curDirection == 1
  {
    if(onMagnet)
    {
      onMagnet = false;
      onMagnetTime = millis() - enterMagnetTime;
      --deltaCount;
    }
  }
}

void onHallSensorB()
{
  onB=true;
  if(curDirection == 1)
  {
    onMagnet = true;
    enterMagnetTime = millis();
  }
  else //curDirection == 0
  {
    if(onMagnet)
    {
      onMagnet = false;
      onMagnetTime = millis() - enterMagnetTime;
      ++deltaCount;
    }
  }
}
