
#include <Arduino.h>
#include <LegoLocomotive.h>

#define MOTOR_PWM_PIN D1
#define MOTOR_DIR_PIN D3
#define HALL_SENSOR_A_PIN D5
#define HALL_SENSOR_B_PIN D6
#define WIFI_RESET_PIN D7
#define LED_PIN LED_BUILTIN

LegoLocomotive legoLocomotive(MOTOR_PWM_PIN, MOTOR_DIR_PIN, HALL_SENSOR_A_PIN, HALL_SENSOR_B_PIN, WIFI_RESET_PIN, LED_PIN);

void setup()
{
  Serial.begin(115200);
}

void loop()
{
  legoLocomotive.update(millis());
}