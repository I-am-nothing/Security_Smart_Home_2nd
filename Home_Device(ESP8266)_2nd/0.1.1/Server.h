#ifndef SERVER_H_
#define SERVER_H_

#include <Arduino.h>
#include <ArduinoJson.h>
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include "ESP8266httpUpdate.h"
#include "Encryption.h"
#include "ConfigData.h"

extern String deviceId;
extern bool hasUpdate;

bool newId();
int checkVersion();
bool update();

#endif /* SERVER_H_ */
