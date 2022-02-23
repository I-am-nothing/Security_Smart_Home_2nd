#ifndef ENCRYPTION_H_
#define ENCRYPTION_H_

String mrand(size_t n);
String UUID();
/*
struct Encryption {
  String key;
  String value;
};
*/
void encrypt (const String key, String value, JsonObject object);
//Encryption encrypt (String value);
//void encrypt (DynamicJsonDocument doc);
String decrypt (String key, String value);
//String decrypt (Encryption encryption);
String decrypt(JsonObject object);

#endif //ENCRYPTION_H_
//2021/7/20
