#ifndef ENCRYPTION_H_
#define ENCRYPTION_H_

#include <Arduino.h>
#include <ArduinoJson.h>

String mrand(size_t n);
String UUID();

void encrypt (String value, JsonObject object);

String decrypt (String key, String value);
String decrypt(JsonObject object);

#endif /* ENCRYPTION_H_ */
//2021/8/16
