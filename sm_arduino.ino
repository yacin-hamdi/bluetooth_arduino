#include "DHT.h"
#include <Servo.h>

#define DHTPIN A2 
#define DHTTYPE DHT11
DHT dht(DHTPIN, DHTTYPE);
int pompe = 4;
int ventilateur = 5;
int led3 = 9;
Servo myservo;

String value ;
char character;
void setup() {
  dht.begin();
  pinMode(pompe,OUTPUT);
  pinMode(ventilateur,OUTPUT);
  pinMode(led3,OUTPUT);
  Serial.begin(9600);

}

void loop() {
  float h = dht.readHumidity();
  float t = dht.readTemperature();
  
  if(Serial.available() > 0){
    character = Serial.read();
    if(character == '0'){
    digitalWrite(pompe, HIGH);
  }else if (character == '1'){
    digitalWrite(pompe, LOW);
  }else if (character == '2'){
    digitalWrite(ventilateur, HIGH);
  }else if (character == '3'){
    digitalWrite(ventilateur, LOW);
  }else if (character == '4'){
    digitalWrite(led3, HIGH);
  }else if (character == '5'){
    digitalWrite(led3, LOW);
  }
  }

  Serial.print(String(t)+":"+String(h)+"#");
  

}
