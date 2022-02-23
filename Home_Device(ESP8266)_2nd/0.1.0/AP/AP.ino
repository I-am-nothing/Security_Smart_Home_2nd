#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <WiFiClient.h>
//#include <Hash.h>
//#include <ESPAsyncTCP.h>
#include <ESPAsyncWebServer.h>
#include <ArduinoJson.h>
#include "ConfigData.h"
#include "Encryption.h"

const char* ssid_AP = "XDD-Light";
const char* password_AP = "A65A6C8B-4819-483D-97E2-E0FFC5C6FE61";
char ssid_STA[64];
char password_STA[64];
char deviceId[40];
const char* serverName = "54.234.67.26";
const int port = 8080;
const char *path = "/Security_Home_2/home_device/new_device";
const unsigned long intervalTime = 250;
unsigned long previousTime = millis();

// Create AsyncWebServer object on port 80
AsyncWebServer server(port);
ConfigData configData;
WiFiClient client;
IPAddress ip(192, 168, 87, 87);
IPAddress gateway(192, 168, 0, 1);
IPAddress subnet(255, 255, 255, 0);
IPAddress dns(192, 168, 0, 1);

bool WiFiSetup()
{
  Serial.printf("connecting to %s ", ssid_STA);
  WiFi.begin(ssid_STA, password_STA);
  int count = 0;
  while (WiFi.status() != WL_CONNECTED)
  {
    if (count == 40)
      return false;
    Serial.print(F("."));
    delay(250);
    count++;
    yield();
  }
  Serial.print(F("connected "));
  Serial.println(WiFi.localIP());
  return true;
}

bool newId() {
  if (WiFi.status() != WL_CONNECTED && !WiFiSetup())
    return false;
  
  bool ret = false;
  DynamicJsonDocument doc(1024);
  encrypt(UUID(), F("01643D3E-3452-4640-9784-DDBFA4CBA50E"), doc.createNestedObject(F("typeId")));
  encrypt(UUID(), String(ESP.getChipId()), doc.createNestedObject(F("chipId")));
  encrypt(UUID(), WiFi.macAddress(), doc.createNestedObject(F("macAddress")));
  
  String jsonStr = "";
  serializeJson(doc, jsonStr);
  Serial.println(F("sendData:"));
  serializeJsonPretty(doc, Serial);
  Serial.println();
  
  HTTPClient http;
  http.begin(client, serverName, port, path, false);
  http.addHeader(F("Content-Type"), F("application/json"));
  int httpCode = http.POST(jsonStr);
  Serial.printf("httpCode: %d\n", httpCode);
  if (httpCode == 200) {
    Serial.println(F("Success"));
    //receive
    deserializeJson(doc, http.getString());
    Serial.println(F("receiveData:"));
    serializeJsonPretty(doc, Serial);
    Serial.println();
    doc["status"]["value"] = decrypt(doc["status"]);
    doc["message"]["Device_ID"]["value"] = decrypt(doc["message"]["Device_ID"]);
    configData.setDeviceId(decrypt(doc["message"]["Device_ID"]).c_str());
    doc["message"]["Name"]["value"] = decrypt(doc["message"]["Name"]);
    configData.setName(decrypt(doc["message"]["Name"]).c_str());
    doc["message"]["Detail"]["value"] = decrypt(doc["message"]["Detail"]);
    configData.setDetail(decrypt(doc["message"]["Detail"]).c_str());
    EEPROM.commit();
    Serial.println(F("decrypedData:"));
    serializeJsonPretty(doc, Serial);
    Serial.println();
    ret = true;
  }
  http.end();
  return ret;
}

void softAPSetup() {
  Serial.print(F("Setting AP (Access Point)…"));
  // Remove the password parameter, if you want the AP (Access Point) to be open
  WiFi.softAPConfig(ip, gateway, subnet);
  WiFi.softAP(ssid_AP, password_AP);

  IPAddress IP = WiFi.softAPIP();
  Serial.print(F("AP IP address: "));
  Serial.println(IP);
}

void sendRequest(AsyncWebServerRequest * request, bool success, int status) {
  if (success) {
    DynamicJsonDocument doc(1024);
    encrypt(UUID(), String(status), doc.createNestedObject(F("status")));
    encrypt(UUID(), "success", doc.createNestedObject(F("message")));
    Serial.println(F("sendData:"));
    serializeJsonPretty(doc, Serial);
    Serial.println();
    String jsonBuffer = "";
    serializeJson(doc, jsonBuffer);
    request -> send(200, F("application/json"), jsonBuffer);
  } else {
    request -> send(400, F("text/plain"), F("Bad Request"));
  }
}

void webServerSetup() {
  server.onRequestBody([](AsyncWebServerRequest * request, uint8_t *data, size_t len, size_t index, size_t total) {
    
      if (request -> url() == F("/") && request -> method() == HTTP_POST) {
        Serial.println(F("test"));
        //receive
        //send
        sendRequest(request, true, 1);
      }
      
      else if (request -> url() == F("/newWiFi") && request -> method() == HTTP_POST) {
        Serial.println(F("New WiFi"));
        bool success = false;
        int status = 0;
        //receive
        char jsonBuffer[len + 1];
        strncpy(jsonBuffer, (const char*)data, len);
        jsonBuffer[len] = '\0';
        Serial.println(jsonBuffer);
        DynamicJsonDocument doc(1024);
        deserializeJson(doc, jsonBuffer);
        Serial.println(F("reveiceData:"));
        serializeJsonPretty(doc, Serial);
        Serial.println();
        if (doc["Ssid"] && doc["Password"]) {
          success = true;
          strcpy(ssid_STA, decrypt(doc["Ssid"]).c_str());
          strcpy(password_STA, decrypt(doc["Password"]).c_str());
          configData.setSsid(ssid_STA);
          configData.setPassword(password_STA);
          EEPROM.commit();
        }
        //send
        if (success) {
          if (ssid_STA) {
            if (WiFiSetup()) {
              status = 1;
            } else {
              status = 3;
            }
          } else {
            status = 2;
          }
        }
        sendRequest(request, success, status);
      }
      
      else if (request -> url() == "/getDeviceId" && request -> method() == HTTP_POST) {
        Serial.println(F("getDeviceId"));
        bool success = false;
        int status = 0;
        //receive
        char jsonBuffer[len + 1];
        strncpy(jsonBuffer, (const char*)data, len);
        jsonBuffer[len] = '\0';
        Serial.println(jsonBuffer);
        DynamicJsonDocument doc(1024);
        deserializeJson(doc, jsonBuffer);
        Serial.println(F("reveiceData:"));
        serializeJsonPretty(doc, Serial);
        Serial.println();
        if (doc["status"] && decrypt(doc["status"]) == "418") {
          success = true;
          //send
          if (WiFiSetup()) {
            if (newId()) {
              status = 1;
            } else {
              status = 3;
            }
          } else {
            status = 2;
          }
        }
        sendRequest(request, success, status);
      }
      else {
        Serial.println(F("Not Found!"));
        request -> send(404, F("text/plain"), F("Not Found!"));
      }
  });

  server.onNotFound([](AsyncWebServerRequest* request){
    request -> send(400, F("text/plain"), F("Empty Body"));
  });
  
  // Start server
  server.begin();
}

void setup(){
  // Serial port for debugging purposes
  Serial.begin(115200);
  while(!Serial);
  EEPROM.begin(512);
  srand(time(NULL));
  pinMode(2, OUTPUT);
  configData.configLoad();
  //configData.getDeviceId(deviceId);
  WiFi.mode(WIFI_AP_STA);

  softAPSetup();
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
  delay(100);
}
