/*
  Author: Tobias Grundtvig
  This code is used to run at the modified Lego train locomotive, to control it over WiFi.
*/
#include <RemoteDevice.h>

#define DEVICE_TYPE 2 //2 -> Locomotive
#define DEVICE_VERSION 2
#define DEVICE_TYPE_NAME "Locomotive"
#define MAX_PACKAGE_SIZE 8
#define PING_TIME 3000

#define PWMA D1
#define PWMB D2
#define DA D3
#define DB D4
#define HALL_SENSOR_A D5
#define HALL_SENSOR_B D6
#define WIFI_RESET_PIN D7
//ToDo: detect these automatically
#define MIN_SPEED 512
#define MAX_SPEED 1023 

/*
#define MASK_0 0x000000FF
#define MASK_1 0x0000FF00
#define MASK_2 0x00FF0000
#define MASK_3 0xFF000000
*/
uint8_t data[MAX_PACKAGE_SIZE];


long lastSpeedUpdate = 0;

// keeping track of direction
uint8_t curDirection = 0;
bool waitForDirectionChange = false;
long waitForDirectionTime = 0;

//Keeping track position
long absolutePosition = 0;
long goalPosition = 0;
long lastPosition = 0;
volatile int deltaCount = 0;

//Keeping track of magnets
volatile bool onMagnet = false;
volatile unsigned long enterMagnetTime = 0;
volatile unsigned long onMagnetTime = 0;


uint16_t curSpeed = 0;
bool packageSent = false;

void onPackageReceived(uint8_t data[], uint16_t size);
bool onSendInitializationPackage();

uint16 minSpeed = 0;

RemoteDevice device(  onPackageReceived,
                      onSendInitializationPackage,
                      (uint32_t) DEVICE_TYPE,
                      (uint32_t) DEVICE_VERSION,
                      (char*) DEVICE_TYPE_NAME,
                      (uint16_t) MAX_PACKAGE_SIZE,
                      (uint32_t) PING_TIME );


void setup()
{
  Serial.begin(115200);
  delay(10);
  Serial.println("Starting setup()...");
  pinMode(LED_BUILTIN, OUTPUT);     // Initialize the LED_BUILTIN pin as an output
  pinMode(PWMA, OUTPUT);
  pinMode(PWMB, OUTPUT);
  pinMode(DA, OUTPUT);
  pinMode(DB, OUTPUT);
  pinMode(HALL_SENSOR_A, INPUT_PULLUP);
  pinMode(HALL_SENSOR_B, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(HALL_SENSOR_A),onHallSensorA,FALLING);
  attachInterrupt(digitalPinToInterrupt(HALL_SENSOR_B),onHallSensorB,FALLING);
  
  digitalWrite(LED_BUILTIN, HIGH);
  Serial.println("Start WiFi connection...");
  // Connect to WiFi network
  boolean resetWiFi = !digitalRead(WIFI_RESET_PIN);
  device.start("192.168.43.125",3377,resetWiFi);
}

bool onSendInitializationPackage()
{
  return sendState();
}

void loop()
{
  //Get frame time
  long curTime = millis();
  digitalWrite(LED_BUILTIN, !onMagnet);
  //Update device...
  device.update(curTime);
  
  //Update position, if it has changed
  if(deltaCount != 0)
  {
    updatePosition();
  }

  if(!packageSent)
  {
    packageSent = sendState();
    if(!packageSent)
    {
      curSpeed = 0;
      applySpeed();
      return;
    }
  }
  if(!device.isConnected())
  {
    curSpeed = 0;
    applySpeed();
    return;
  }

  //Are we changing direction?
  if(changingDirection(curTime)) return;
  
  //Update speed if it has been too long time or if position has changed
  if(curTime - lastSpeedUpdate > 1000 || absolutePosition != lastPosition)
  {
    updateSpeed(curTime);
  }
  
  applySpeed();
}

void updatePosition()
{
  absolutePosition += deltaCount;
  deltaCount = 0;
  packageSent = false; //Sent new package
}

bool changingDirection(long curTime)
{
  if(!waitForDirectionChange) return false;
  if(curTime - waitForDirectionTime < 2000) return true;
  waitForDirectionChange = false;
  if(curDirection == 0)
  {
    curDirection = 1;
    if(onMagnet)
    {
      if(!digitalRead(HALL_SENSOR_A))
      {
        onMagnet = false;
      }
      else
      {
        ++deltaCount;
      }
    }
    else // !onMagnet
    {
      if(!digitalRead(HALL_SENSOR_B))
      {
        if(!digitalRead(HALL_SENSOR_A))
        {
          --deltaCount;
        }
        else
        {
          onMagnet = true;
        }
      }
    }
  }
  else //curDirection == 1
  {
    curDirection = 0;
    if(onMagnet)
    {
      if(!digitalRead(HALL_SENSOR_B))
      {
        onMagnet = false;
      }
      else
      {
        --deltaCount;
      }
    }
    else // !onMagnet
    {
      if(!digitalRead(HALL_SENSOR_A))
      {
        if(!digitalRead(HALL_SENSOR_B))
        {
          ++deltaCount;
        }
        else
        {
          onMagnet = true;
        }
      }
    }
  }
  return false;
}

void updateSpeed(long curTime)
{
  //Check if we need to change direction...
  if(curSpeed == 0 && goalPosition > absolutePosition && curDirection == 1)
  {
    waitForDirectionChange = true;
    waitForDirectionTime = curTime;
    return;
  }

  if(curSpeed == 0 && goalPosition < absolutePosition && curDirection == 0)
  {
    waitForDirectionChange = true;
    waitForDirectionTime = curTime;
    return;
  }

  //Are we at the position
  if(goalPosition == absolutePosition)
  {
    curSpeed = 0;
    lastPosition = absolutePosition;
    return;
  }

  //Should we update the speedsetting?
  if(absolutePosition != lastPosition || curTime - lastSpeedUpdate > 1000)
  {
    lastSpeedUpdate = curTime;
    if(absolutePosition == lastPosition) //We seem to be stopped
    {
      ++curSpeed;
      if(curSpeed > 10) curSpeed = 10;
      return;
    }
    if(curDirection == 0)
    {
      if(goalPosition - absolutePosition > curSpeed)
      {
        ++curSpeed;
        if(curSpeed > 10) curSpeed = 10;
      }
      else
      {
        --curSpeed;
        if(curSpeed < 1) curSpeed = 1;
      }
    }
    else if(curDirection == 1)
    {
      if(absolutePosition - goalPosition > curSpeed)
      {
        ++curSpeed;
        if(curSpeed > 10) curSpeed = 10;
      }
      else
      {
        --curSpeed;
        if(curSpeed < 1) curSpeed = 1;
      }
    }
    lastPosition = absolutePosition;
  }  
}

void onPackageReceived(uint8_t data[], uint16_t size)
{
  Serial.println("Package received!");
  long newTargetPosition = data[0];
  newTargetPosition <<= 8;
  newTargetPosition += data[1];
  newTargetPosition <<= 8;
  newTargetPosition += data[2];
  newTargetPosition <<= 8;
  newTargetPosition += data[3];
  Serial.print("Got a new target position: ");
  Serial.println(newTargetPosition);
  goalPosition = newTargetPosition;
}

bool sendState()
{
  data[0] = absolutePosition >> 24;
  data[1] = absolutePosition >> 16;
  data[2] = absolutePosition >> 8;
  data[3] = absolutePosition;
  data[4] = onMagnetTime >> 24;
  data[5] = onMagnetTime >> 16;
  data[6] = onMagnetTime >> 8;
  data[7] = onMagnetTime;
  return device.sendPackage(data, 8);
}

void applySpeed()
{
  int speedValue = 0;
  if(curSpeed > 0)
  {
    if(curSpeed == 10)
    {
      speedValue = MAX_SPEED;
    }
    else
    {
      int d = (MAX_SPEED - MIN_SPEED) / 9;
      speedValue = MIN_SPEED + (curSpeed-1)*d;
    }
  }
  digitalWrite(DA, curDirection);
  analogWrite(PWMA, speedValue);
}

void onHallSensorA()
{
  if(curDirection == 0 && !onMagnet)
  {
    onMagnet = true;
    enterMagnetTime = millis();
  }
  else if(curDirection == 1 && onMagnet)
  {
    onMagnet = false;
    onMagnetTime = millis() - enterMagnetTime;
    --deltaCount;
  }
}

void onHallSensorB()
{
  if(curDirection == 1 && !onMagnet)
  {
    onMagnet = true;
    enterMagnetTime = millis();
  }
  else if(curDirection == 0 && onMagnet)
  {
    onMagnet = false;
    onMagnetTime = millis() - enterMagnetTime;
    ++deltaCount;
  }
}
