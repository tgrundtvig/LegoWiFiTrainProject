/*
  Author: Tobias Grundtvig
  This code is used to run at the modified Lego train switch, to control it over WiFi.
*/

#include <RemoteDevice.h>
#include <AccelStepper.h>


#define DEVICE_TYPE 1 //1 -> SWITCH
#define DEVICE_VERSION 1
#define DEVICE_TYPE_NAME "LEFT_SWITCH"
#define SWITCH_DIRECTION 5 //5 = LEFT SWITCH, 6 = RIGHT SWITCH
#define MAX_PACKAGE_SIZE 2


#define HALFSTEP 8

// Motor pin definitions
#define motorPin1  D1     // IN1 on the ULN2003 driver 1
#define motorPin2  D2     // IN2 on the ULN2003 driver 1
#define motorPin3  D3     // IN3 on the ULN2003 driver 1
#define motorPin4  D5     // IN4 on the ULN2003 driver 1

// Initialize with pin sequence IN1-IN3-IN2-IN4 for using the AccelStepper with 28BYJ-48
AccelStepper stepper1(HALFSTEP, motorPin1, motorPin3, motorPin2, motorPin4);

//State of switch
//0 -> Unknown position
//1 -> Train goes left
//2 -> Train goes right
//3 -> Moving to the left position
//4 -> Moving to the right position
int state = 0;
int nextState = 0;
int moveRange = 500;
uint8_t data[MAX_PACKAGE_SIZE];

int initState = 0;

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
  switch(initState)
  {
    case 0:
      stepper1.moveTo(-moveRange);
      Serial.println("Going to init 1");
      initState = 1;   
      return false;
    case 1:
      if(stepper1.distanceToGo() == 0)
      {
        Serial.println("Going to init 2");
        initState = 2;
        stepper1.moveTo(moveRange);
      }
      return false;
    case 2:
      if(stepper1.distanceToGo() == 0)
      {
        Serial.println("Going to init 3");
        initState = 3;
        stepper1.moveTo(-moveRange);
      }
      return false;
    case 3:
      if(stepper1.distanceToGo() == 0)
      {
        Serial.println("Going to init 4");
        initState = 4;
        state = 1;
        nextState = 1;
      }
      return false;
    case 4:
      data[0] = SWITCH_DIRECTION;
      data[1] = state;
      if(device.sendPackage(data,2))
      {
        Serial.println("Initialization package sent");
        return true;
      }
      return false;        
    default:
      Serial.print("Invalid initState: ");
      Serial.println(initState);
      return false;
  }
}

void setup()
{
  Serial.begin(115200);
  delay(10);

  pinMode(D6, INPUT_PULLUP);
  pinMode(D4, OUTPUT);     // Initialize the LED_BUILTIN pin as an output
  digitalWrite(D4, HIGH);
  
  //initialize switch
  stepper1.setMaxSpeed(800.0);
  stepper1.setAcceleration(800.0);
  stepper1.setSpeed(400);
  delay(1000);
  
  Serial.println("Start WiFi connection...");
  // Connect to WiFi network
  
  //WiFiManager
  //Local intialization. Once its business is done, there is no need to keep it around
  boolean resetWiFi = !digitalRead(D6);
  device.start("192.168.43.125",3377,resetWiFi);
}

void loop()
{
  stepper1.run();
  device.update();

  if(nextState != state && stepper1.distanceToGo() == 0)
  {
    digitalWrite(D4, LOW);
    state = nextState;
    data[0] = state;
    device.sendPackage(data, 1);
  }
}

void onPackageReceived(uint8_t data[], uint16_t size)
{
  switch(data[0])
  {
    case 0:
      data[0] = state;
      device.sendPackage(data, 1);
      break;
    case 1:
      if(state != 1 && nextState != 1)
      {
        nextState = 1;
        state = 3;
        stepper1.moveTo(-moveRange);
      }
      break;
    case 2:
      if(state != 2 && nextState != 2)
      {
        nextState = 2;
        state = 4;
        stepper1.moveTo(moveRange);
      }
      break;
    default:
      Serial.print("Unknown command: ");
      Serial.println(data[0]);
  }
}
