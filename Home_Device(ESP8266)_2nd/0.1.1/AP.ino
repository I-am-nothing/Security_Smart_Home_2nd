#include "AsyncWebserver.h"

unsigned long previousTime;
const unsigned long intervalTime = 5000;

void setup(){
  Serial.begin(115200);
  while(!Serial);
  EEPROM.begin(512);
  srand(millis());
  pinMode(2, OUTPUT);
  configData.begin();
  deviceId = configData.getDeviceId();
  ssid = configData.getSsid();
  password = configData.getPassword();
  WiFi.mode(WIFI_AP_STA);
  if (WiFiSetup())
    if (checkVersion() == 0)
      update();
  //wdt_enable(WDTO_8S);

  softAPSetup("XDD-Light", "A65A6C8B-4819-483D-97E2-E0FFC5C6FE61");
  webServerSetup();

  Serial.println(F("Setup OK"));
  digitalWrite(2, LOW);
}
 
void loop(){
  if (WiFi.softAPgetStationNum()) {
    if (millis() - previousTime > intervalTime) {
      previousTime = millis();
      digitalWrite(2, !digitalRead(2));
    }
  }
  else {
    digitalWrite(2, LOW);
  }
  if (WiFi.status() != WL_CONNECTED && ssid != "") {
    Serial.print(F("try to connect to "));
    Serial.println(ssid);
    WiFiSetup();
  }
  if (deviceId == "") {
    if (millis() - previousTime > intervalTime) {
      previousTime = millis();
      if (WiFi.status() == WL_CONNECTED) {
        if (newId()) {
          Serial.println("success");
        } else {
          Serial.println("failed");
          //deviceId = "A65A6C8B-4819-483D-97E2-E0FFC5C6FE61";
        }
      }
    }
  }

  
  delay(250);
}
