#ifndef ASYNCWEBSERVER_H_
#define ASYNCWEBSERVER_H_

#include <ESPAsyncWebServer.h>
#include "Encryption.h"
#include "ConfigData.h"
#include "Server.h"

extern String ssid;
extern String password;
extern ConfigData configData;

void sendRequest(AsyncWebServerRequest * request, bool success, int status);
bool WiFiSetup();
void softAPSetup(const char *_ssid, const char *_password);
void webServerSetup();

#endif /* ASYNCWEBSERVER_H_ */
