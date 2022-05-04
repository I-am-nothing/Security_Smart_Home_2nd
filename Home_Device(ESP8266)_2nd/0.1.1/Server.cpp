#include "Server.h"

extern String ssid;
extern ConfigData configData;
String deviceId;
bool hasUpdate = false;
static WiFiClient client;
static const char *serverName = "54.234.67.26";
static const int serverPort = 80;
static const char *typeId = "01643D3E-3452-4640-9784-DDBFA4CBA50E";
static const char *version = "0.0.1";

bool newId() {
  Serial.println(F("newId"));
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println(F("No WiFi"));
    return false;
  }
  
  bool ret = false;
  //send
  DynamicJsonDocument doc(1024);
  encrypt(typeId, doc.createNestedObject(F("typeId")));
  encrypt(String(ESP.getChipId()), doc.createNestedObject(F("chipId")));
  encrypt(WiFi.macAddress(), doc.createNestedObject(F("macAddress")));
  
  String jsonStr = "";
  serializeJson(doc, jsonStr);
  Serial.println(F("sendData:"));
  serializeJsonPretty(doc, Serial);
  Serial.println();

  Serial.println(F("POST"));
  HTTPClient http;
  http.begin(client, serverName, serverPort, "/Security_Home_2/home_device/new_device", false);
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
    deviceId = decrypt(doc["message"]["Device_ID"]);
    configData.setDeviceId(decrypt(doc["message"]["Device_ID"]));
    doc["message"]["Name"]["value"] = decrypt(doc["message"]["Name"]);
    configData.setName(decrypt(doc["message"]["Name"]));
    doc["message"]["Detail"]["value"] = decrypt(doc["message"]["Detail"]);
    configData.setDetail(decrypt(doc["message"]["Detail"]));
    configData.configSave();
    Serial.println(F("decrypedData:"));
    serializeJsonPretty(doc, Serial);
    Serial.println();
    ret = true;
  }
  http.end();
  return ret;
}

int checkVersion() {
  Serial.println(F("checkVersion"));
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println(F("No WiFi"));
    return -1;
  }
  
  int ret = -1;
  //send
  DynamicJsonDocument doc(1024);
  encrypt(version, doc.createNestedObject(F("Version_ID")));
  encrypt(typeId, doc.createNestedObject(F("Type_ID")));
  encrypt(ssid, doc.createNestedObject(F("Wifi_Ssid")));
  encrypt(WiFi.localIP().toString(), doc.createNestedObject(F("Wifi_Ip")));
  encrypt(deviceId, doc.createNestedObject(F("Device_ID")));
  
  String jsonStr = "";
  serializeJson(doc, jsonStr);
  Serial.println(F("sendData:"));
  serializeJsonPretty(doc, Serial);
  Serial.println();

  Serial.println(F("POST"));
  HTTPClient http;
  http.begin(client, serverName, serverPort, "/Security_Home_2/version/home_device", false);
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
    doc["message"]["value"] = decrypt(doc["message"]);
    Serial.println(F("decrypedData:"));
    serializeJsonPretty(doc, Serial);
    Serial.println();
    ret = decrypt(doc["status"]).toInt();
    if (ret == 2)
      hasUpdate = true;
  }
  http.end();
  return ret;
}

bool update() {
  Serial.println(F("update"));
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println(F("No WiFi"));
    return false;
  }

  //send
  DynamicJsonDocument doc(1024);
  encrypt(version, doc.createNestedObject("Version_ID"));
  encrypt(typeId, doc.createNestedObject("Type_ID"));
  String jsonStr = "";
  serializeJson(doc, jsonStr);
  Serial.println(F("sendData:"));
  serializeJsonPretty(doc, Serial);
  Serial.println("uploading...");
  ESPhttpUpdate.update(client, serverName, serverPort, "/Security_Home_2/version/home_device/update", jsonStr, version);
  Serial.println("uplaod finished!!!");
  return true;
}
