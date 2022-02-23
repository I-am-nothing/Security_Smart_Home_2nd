#ifndef CONFIGDATA_H_
#define CONFIGDATA_H_

#include <EEPROM.h>
#include <Arduino.h>
// Handle the Application configuration parameters stored in EEPROM
// Access to these parameters is through the object properties only
//
struct configData_t
{
	uint8_t signature[2];
	uint8_t version;
	// application config data starts below
	char deviceId[40];
	char name[50];
	char detail[100];
  char ssid[64];
  char password[64];
};

class ConfigData
{
public:
	inline void getDeviceId(char *deviceId) {
	  //for (int i = 0; i < 40; i++)
      //deviceId[i] = _D.deviceId[i];
    strcpy(deviceId, _D.deviceId);
	};
	inline void setDeviceId(const char *deviceId) {
	  strcpy(_D.deviceId, deviceId);
	};
  inline void getName(char *name) {
    strcpy(name, _D.name);
  };
  inline void setName(const char *name) {
    strcpy(_D.name, name);
  };
  inline void getDetail(char *detail) {
    strcpy(detail, _D.detail);
  };
  inline void setDetail(const char *detail) {
    strcpy(_D.detail, detail);
  };
  inline void getSsid(char *ssid) {
    strcpy(ssid, _D.ssid);
  };
  inline void setSsid(const char *ssid) {
    strcpy(_D.ssid, ssid);
  };
  inline void getPassword(char *password) {
    strcpy(password, _D.password);
  };
  inline void setPassword(const char *password) {
    strcpy(_D.password, password);
  };
	
	void begin() { 
	  if (!configLoad()) {
	    configDefault();
	    configSave();
	  }
	};
  
  void configDefault(void) {
    _D.signature[0] = EEPROM_SIG[0];
    _D.signature[1] = EEPROM_SIG[1];
    _D.version = CONFIG_VERSION;
    strcpy(_D.deviceId, DEVICEID_DEFAULT);
    strcpy(_D.name, NAME_DEFAULT);
    strcpy(_D.detail, DETAIL_DEFAULT);
    strcpy(_D.ssid, SSID_DEFAULT);
    strcpy(_D.password, PASSWORD_DEFAULT);
  }
  
  bool configLoad(void) {
    EEPROM.get(EEPROM_ADDR, _D);
    if (_D.signature[0] != EEPROM_SIG[0] && _D.signature[1] != EEPROM_SIG[1])   
      return(false);
    // handle any version adjustments here
    if (_D.version != CONFIG_VERSION) {
      // do something here
    }
 
    // update version number to current
    _D.version = CONFIG_VERSION;
    return(true);
  }
   
   bool configSave(void) {
     EEPROM.put(EEPROM_ADDR, _D);
     return(true);
   }
   
private:
  const char DEVICEID_DEFAULT[40] = "392FA7C9-6BA9-FAF5-1E41-1A854C78977D";
  const char *NAME_DEFAULT = "";
  const char *DETAIL_DEFAULT = "";
  const char *SSID_DEFAULT = "";
  const char *PASSWORD_DEFAULT = "";
  const uint16_t EEPROM_ADDR = 0;
  const uint8_t EEPROM_SIG[2] = { 0xc1, 0xc8 };
  const uint8_t CONFIG_VERSION = 0;
  configData_t _D;
};

#endif //CONFIGDATA_H_
//2021/8/2
