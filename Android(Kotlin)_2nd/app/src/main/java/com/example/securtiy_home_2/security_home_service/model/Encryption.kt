package com.example.securtiy_home_2.security_home_service.model

import org.json.JSONObject

class Encryption (
    val key: String,
    val value: String
){
    fun toJson(): JSONObject {
        return JSONObject().put("key", key).put("value", value)
    }
}