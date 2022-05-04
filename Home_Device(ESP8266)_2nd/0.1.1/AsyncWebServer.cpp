#include "AsyncWebServer.h"

String ssid;
String password;
static AsyncWebServer server(8080);
ConfigData configData;

void sendRequest(AsyncWebServerRequest * request, int status, String message) {
  DynamicJsonDocument doc(1024);
  encrypt(String(status), doc.createNestedObject(F("status")));
  encrypt(message, doc.createNestedObject(F("message")));
  Serial.println(F("sendData:"));
  serializeJsonPretty(doc, Serial);
  Serial.println();
  String jsonBuffer = "";
  serializeJson(doc, jsonBuffer);
  request -> send(200, F("application/json"), jsonBuffer);
}

bool WiFiSetup()
{
  if (WiFi.status() == WL_CONNECTED) {
    Serial.println(F("connected"));
    return true;
  }
  if (ssid == "") {
    Serial.println(F("No ssid"));
    return false;
  }
  Serial.printf("connecting to %s ", ssid.c_str());
  WiFi.begin(ssid, password);
  int count = 0;
  while (WiFi.status() != WL_CONNECTED)
  {
    if (count == 40) {
      Serial.println(F("\nconnect failed"));
      return false;
   }
    Serial.print(F("."));
    delay(250);
    count++;
    ESP.wdtFeed();
  }
  Serial.print(F("connected "));
  Serial.println(WiFi.localIP());
  return true;
}

void softAPSetup(const char *_ssid, const char *_password) {
  Serial.print(F("Setting AP (Access Point)…"));
  IPAddress ip(192, 168, 87, 87);
  IPAddress gateway(192, 168, 0, 1);
  IPAddress subnet(255, 255, 255, 0);
  WiFi.softAPConfig(ip, gateway, subnet);
  WiFi.softAP(_ssid, _password);

  IPAddress IP = WiFi.softAPIP();
  Serial.print(F("AP IP address: "));
  Serial.println(IP);
}

void webServerSetup() {
  server.onRequestBody([](AsyncWebServerRequest * request, uint8_t *data, size_t len, size_t index, size_t total) {
    
      if (request -> url() == F("/") && request -> method() == HTTP_POST) {
        Serial.println(F("test"));
        //receive
        //send
        sendRequest(request, 1, "success");
      }
      
      else if (request -> url() == F("/newWiFi") && request -> method() == HTTP_POST) {
        Serial.println(F("New WiFi"));
        bool success = false;
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
        //Serial.println(doc["Ssid"]? "yes" : "no");
        //Serial.println(doc["Password"]? "yes" : "no");
        if (doc["Ssid"] && doc["Password"]) {                          //???
          success = true;
          ssid = decrypt(doc["Ssid"]);                          //TODO
          password = decrypt(doc["Password"]);
          Serial.printf("ssid = %s, length = %u\n", ssid.c_str(), ssid.length());
          Serial.printf("password = %s, length = %u\n", password.c_str(), ssid.length());
          configData.setSsid(ssid.c_str());
          configData.setPassword(password.c_str());
          configData.configSave();
        }
        //send
        if (success) {
          if (ssid != "") {
            sendRequest(request, 1, "success");
          } else {
            sendRequest(request, 0, "no ssid");
          }
        } else {
          request -> send(400, F("text/plain"), F("Bad Request"));
        }
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
          if (WiFi.status() == WL_CONNECTED) {
            if (deviceId != "") {
              sendRequest(request, 1, deviceId);
            } else {
              sendRequest(request, 2, "no deviceId");
            }
          } else {
            sendRequest(request, 0, "cannot connect to WiFi");
          }
          sendRequest(request, status, deviceId);
        } else {
          request -> send(400, F("text/plain"), F("Bad Request"));
        }
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
