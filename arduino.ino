#include <SoftwareSerial.h>
#include <pt.h>
#include <dht.h>

#define DHT11_PIN 3

SoftwareSerial mySerial(11, 12); // RX, TX
dht DHT;

int in0 = analogRead(A0);

struct pt hilo1; //Abre hilo
struct pt hilo2;
struct pt hilo3;

float humedad; //Guarda el valor de la humedad tierra
float h; //Guarda el valor de la humedad
float te; //Guarda el valor de la temperatura
char Incoming_value = 0; //Guarda valor entrante del hardware
char stateRelay = 'b'; //Si el estado es "b" esta apagado
char stateRelay1 = 'b';

const int in_relevador = 2; //Declaro el puerto digital del relay
const int in_relevador1 = 4;

void setup() {
  Serial.begin(9600);
  mySerial.begin(9600);
  
  PT_INIT(&hilo1);
  PT_INIT(&hilo2);
  PT_INIT(&hilo3);
  pinMode(in_relevador, OUTPUT);
  pinMode(in_relevador1, OUTPUT); 
  digitalWrite(in_relevador, HIGH);
  digitalWrite(in_relevador1, HIGH);
}

void loop() {
  if(mySerial.available() >0)  
  {
    Incoming_value = mySerial.read(); //Lee los datos enviados por la app y los guarda en la variable Incoming_value
    Serial.print(Incoming_value);        //Imprime valor leido en el serial
    Serial.print("\n");
    if(Incoming_value == 'a'){            //Si el valor es "a" lo prendera
      digitalWrite(in_relevador, LOW); 
      stateRelay = 'a';
    }
    else if(Incoming_value == 'b' ){       //Si el valor es "b" lo apagara
      digitalWrite(in_relevador, HIGH);
      stateRelay = 'b';   
    }  
    else if(Incoming_value == 'c'){       //Si el valor es "c" lo prendera
      digitalWrite(in_relevador1, LOW);
      stateRelay1 = 'a';   
    }
    else if(Incoming_value == 'd'){       //Si el valor es "d" lo apagara
      digitalWrite(in_relevador1, HIGH);  
      stateRelay1 = 'b';    
    }
  } 
  sendStateRelay(&hilo3);
  readSensors();
  sendAndroidValues(&hilo2);
}

void readSensors()
{
  //Humedad tierra
  float read = analogRead(in0);
  humedad = map(read,0 , 1023, 100,0);
  //Temperatura y humedad
  dht11(&hilo1);
}

void sendAndroidValues(struct pt *pt){
  PT_BEGIN(pt);
  static long t = 0;
  
  //puts # before the values so our app knows what to do with the data
  mySerial.print('#');
  mySerial.print(h);
  mySerial.print('+');
  mySerial.print(te);
  mySerial.print('+');
  mySerial.print(humedad);
  mySerial.print('~'); //used as an end of transmission character - used in app for string length
  //mySerial.println();
  
  t = millis();
  PT_WAIT_UNTIL(pt, (millis()-t)>1000);
  PT_END(pt);
}

void sendStateRelay(struct pt *pt){
  PT_BEGIN(pt);
  static long t = 0;
  
  //puts # before the values so our app knows what to do with the data
  mySerial.print('E');
  mySerial.print(stateRelay);
  mySerial.print('+');
  mySerial.print(stateRelay1);
  mySerial.print('-'); //used as an end of transmission character - used in app for string length
  
  t = millis();
  PT_WAIT_UNTIL(pt, (millis()-t)>400);
  PT_END(pt);
}

void dht11(struct pt *pt){
  PT_BEGIN(pt);
  static long t = 0;
  DHT.read11(DHT11_PIN);
  te = DHT.temperature;
  h = DHT.humidity;
  t = millis();
  PT_WAIT_WHILE(pt, (millis()-t)<2000);
  PT_END(pt);
}
