package com.example.securtiy_home_2.security_home_service

import android.util.Log
import com.example.securtiy_home_2.security_home_service.model.Encryption
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.TypeAdapter
import com.google.gson.internal.Primitives
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.experimental.xor
import kotlin.math.log
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf
import kotlin.reflect.KMutableProperty1 as KMutableProperty11

class SecurityHomeEncryption {

    fun <T> encrypt(model: Any, dataType: Class<*>): T{
        return modelRun(model, dataType, EncryptionStatus.Encryption) as T
    }

    fun encrypt(value: String): Encryption{
        return encrypt(value.toByteArray())
    }

    private fun encrypt(encryptText: ByteArray): Encryption{
        val uuid = UUID.randomUUID().toString()
        val uuidByte = uuid.toByteArray()

        var j = 0
        for(i in encryptText.indices){
            encryptText[i] = encryptText[i].xor(uuidByte[j])

            j++
            if(j == uuidByte.size){
                j = 0
            }
        }

        j = uuidByte.size-1
        for(i in encryptText.indices){
            encryptText[i] = encryptText[i].xor(uuidByte[j])
            j--

            if(j == -1){
                j = uuidByte.size-1
            }
        }

        return Encryption(uuid, String(encryptText))
    }

    fun <T> decrypt(model: Any, dataType: Class<*>): T{
        return modelRun(model, dataType, EncryptionStatus.Decryption) as T
    }

    fun decrypt(encryption: Encryption): String{
        val uuidByte = encryption.key.toByteArray()
        val decryptText = encryption.value.toByteArray()

        var j = uuidByte.size-1
        for(i in decryptText.indices){
            decryptText[i] = decryptText[i].xor(uuidByte[j])
            j--

            if(j == -1){
                j = uuidByte.size-1
            }
        }

        j = 0
        for(i in decryptText.indices){
            decryptText[i] = decryptText[i].xor(uuidByte[j])
            j++

            if(j == uuidByte.size){
                j = 0
            }
        }

        return String(decryptText)
    }

    private fun modelRun(model: Any, dataType: Class<*>, status: EncryptionStatus): Any{
        dataType.declaredFields.forEach { fieldValue ->
            val prop = model.javaClass.kotlin.memberProperties.first{ it.name == fieldValue.name }

            with(prop.get(model)){
                if(this != null){
                    when(status){
                        EncryptionStatus.Encryption -> (prop as KMutableProperty11<Any, Encryption>).set(model, encrypt(this as String))
                        EncryptionStatus.Decryption -> {
                            val encryption = Gson().fromJson(Gson().toJson(prop.get(model)), Encryption::class.java)
                            (prop as KMutableProperty11<Any, String>).set(model, decrypt(encryption))
                        }
                    }
                }
            }
        }

        return model
    }
}