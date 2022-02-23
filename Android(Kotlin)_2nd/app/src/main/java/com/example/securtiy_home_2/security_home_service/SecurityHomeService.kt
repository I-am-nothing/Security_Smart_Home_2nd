package com.example.securtiy_home_2.security_home_service

import android.R.attr
import android.content.Context
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Network
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.securtiy_home_2.security_home_service.model.*
import com.google.gson.Gson
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import android.R.attr.data
import java.nio.charset.StandardCharsets


class SecurityHomeService {

    companion object {
        private const val VERSION = "0.0.1"
        private const val TIMEOUT = 3500L
        private const val SERVICE_LOCATION: String = "http://54.234.67.26:80/Security_Home_2"

        @JvmStatic
        private lateinit var userLocalData: UserData

        @JvmStatic
        private var homeDeviceType: List<HomeDeviceType>? = null

        @JvmStatic
        private var profile: Profile? = null
    }

    private var context: Context? = null

    constructor()

    constructor(userData: UserData){
        userLocalData = userData
    }

    constructor(context: Context){
        this.context = context
    }

    private fun checkInternet(): Boolean {
        val connectManager = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return connectManager.activeNetwork != null
    }

    fun getUserData(): UserData{
        return UserData(userLocalData)
    }

    fun homeDeviceType(): List<HomeDeviceType>?{
        return homeDeviceType
    }

    fun homeDeviceCommunicate(network: Network, url: String, body: JSONObject, response: (JSONObject) -> Unit, error: (NetworkStatus) -> Unit){
        Thread{
            with(network.openConnection(URL(url)) as HttpURLConnection){
                requestMethod = "POST"
                doOutput = true
                addRequestProperty("Content-Type", "application/json; utf-8")
                addRequestProperty("Accept", "application/json")
                outputStream.write(body.toString().toByteArray(), 0, body.toString().toByteArray().size)
                BufferedReader(InputStreamReader(inputStream)).use {
                    val response = StringBuffer()

                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        response.append(inputLine)
                        inputLine = it.readLine()
                    }
                    Log.e("A~ ", response.toString())
                    it.close()
                    disconnect()
                    response(JSONObject(response.toString()))
                }
            }
        }.start()
    }

    fun getProfile(pf: (Profile) -> Unit, error: (NetworkStatus) -> Unit){
        if(profile != null){
            pf(profile!!)
        }
        else{
            val url = "${SERVICE_LOCATION}/mobile/profile"
            val body = JSONObject().put("AccountId", SecurityHomeEncryption().encrypt(userLocalData.accountToken.toString()).toJson())
                .put("DeviceId", SecurityHomeEncryption().encrypt(userLocalData.deviceId.toString()).toJson())

            httpPost(url, body, {
                val message = Gson().fromJson(it.toString(), ResponseProfile::class.java)
                if(SecurityHomeEncryption().decrypt(Gson().fromJson(message.status.toString(), Encryption::class.java)) != "1"){
                    error(NetworkStatus.Failed)
                }
                profile = SecurityHomeEncryption().decrypt(message.message[0], Profile::class.java)
                profile!!.Name = String(android.util.Base64.decode(profile!!.Name.toString(), android.util.Base64.DEFAULT))

                val imageBytes = android.util.Base64.decode(profile!!.Image.toString(), android.util.Base64.DEFAULT)
                Log.e("A~ ", (profile!!.Image.toString()))
                profile!!.Image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                pf(profile!!)
            }, {
                error(it)
            })
        }
    }

    fun getHomeDeviceType(error: (NetworkStatus) -> Unit){
        val url = "${SERVICE_LOCATION}/mobile/home_device_type"
        val body = JSONObject()

        httpPost(url, body, {
            Log.e("A~ ", it.toString())
            val message = Gson().fromJson(it.toString(), ResponseHomeDeviceType::class.java)
            if(SecurityHomeEncryption().decrypt(Gson().fromJson(message.status.toString(), Encryption::class.java)) != "1"){
                error(NetworkStatus.ServerError)
            }
            val deviceList = ArrayList<HomeDeviceType>()
            for(device in message.message){
                deviceList.add(SecurityHomeEncryption().decrypt(device, HomeDeviceType::class.java))
            }
            homeDeviceType = deviceList.toList()
        }, {
            error(it)
        })
    }

    fun deviceLogin(response: (LoginStatus) -> Unit, error: (NetworkStatus) -> Unit){
        val url = "${SERVICE_LOCATION}/mobile/device_login"
        val body = JSONObject().put("AccountId", SecurityHomeEncryption().encrypt(userLocalData.accountToken.toString()).toJson())
            .put("DeviceId", SecurityHomeEncryption().encrypt(userLocalData.deviceId.toString()).toJson())

        httpPost(url, body, {
            when(SecurityHomeEncryption().decrypt(Gson().fromJson(it["status"].toString(), Encryption::class.java))){
                "0" -> response(LoginStatus.NoDevice)
                "1" -> response(LoginStatus.Success)
            }
        }, {
            error(it)
        })
    }

    fun login(account: String, password: String, response: (LoginStatus) -> Unit, error: (NetworkStatus) -> Unit){
        val url = "${SERVICE_LOCATION}/mobile/login"
        val body = JSONObject().put("Account", SecurityHomeEncryption().encrypt(account).toJson())
            .put("Password", SecurityHomeEncryption().encrypt(password).toJson())

        httpPost(url, body, {
            when(SecurityHomeEncryption().decrypt(Gson().fromJson(it["status"].toString(), Encryption::class.java))){
                "0" -> response(LoginStatus.NoAccount)
                "1" -> {
                    userLocalData.accountToken = SecurityHomeEncryption()
                        .decrypt(Gson().fromJson(it.getJSONArray("message").getJSONObject(0)["Account_ID"].toString(), Encryption::class.java))

                    Log.d("A~ account token", userLocalData.accountToken.toString())
                    response(LoginStatus.Success)
                }
            }
        }, {
            error(it)
        })
    }

    fun register(account: String, password: String, name: String, response: (LoginStatus) -> Unit, error: (NetworkStatus) -> Unit){
        val url = "${SERVICE_LOCATION}/mobile/register"
        val body = JSONObject().put("Account", SecurityHomeEncryption().encrypt(account).toJson())
            .put("Password", SecurityHomeEncryption().encrypt(password).toJson())
            .put("Name", SecurityHomeEncryption().encrypt(Base64.getEncoder().encodeToString(name.toByteArray())).toJson())
            .put("DeviceId", SecurityHomeEncryption().encrypt((userLocalData.deviceId.toString())).toJson())

        httpPost(url, body, {
            when(SecurityHomeEncryption().decrypt(Gson().fromJson(it["status"].toString(), Encryption::class.java))){
                "0" -> response(LoginStatus.AccountExist)
                "1" -> {
                    userLocalData.name = name
                    userLocalData.accountToken = SecurityHomeEncryption()
                        .decrypt(Gson().fromJson(it.getJSONArray("message").getJSONObject(0)["Account_ID"].toString(), Encryption::class.java))
                    response(LoginStatus.Success)
                }
            }
        }, {
            error(it)
        })
    }

    fun checkVersion(response: (NetworkStatus, Any?) -> Unit) {
        val url = "${SERVICE_LOCATION}/version/android"
        val body = JSONObject().put("Version_ID", SecurityHomeEncryption().encrypt(VERSION).toJson())

        httpPost(url, body, {
            val status = Gson().fromJson(it["status"].toString(), Encryption::class.java)

            val decryptStatus = SecurityHomeEncryption().decrypt(status)

            if(decryptStatus == "1"){
                response(NetworkStatus.Success, null)
            }
            else{
                val versionStatus = if(decryptStatus == "2"){
                    NetworkStatus.Update
                }
                else{
                    NetworkStatus.ForceUpdate
                }
                val message: Version = Gson().fromJson(it["message"].toString(), Version::class.java)
                val decryptMessage: Version = SecurityHomeEncryption().decrypt(message, Version::class.java)

                response(versionStatus, decryptMessage)
            }
        }, {
            response(it, null)
        })
    }

    private fun httpPost(url: String, body: JSONObject, response: (JSONObject) -> Unit, error: (NetworkStatus) -> Unit) {
        if(checkInternet()){
            var status = false
            val queue = Volley.newRequestQueue(context)

            val request = JsonObjectRequest(Request.Method.POST, url, body,{
                status = true
                response(it)
            },{
                Log.e("A~", it.message.toString())
                if(!status) {
                    status = true
                    error(NetworkStatus.ServerError)
                }
            })
            queue.add(request)

            Handler(Looper.getMainLooper()).postDelayed({
                if(!status){
                    status = true
                    error(NetworkStatus.ServerError)
                }
            }, TIMEOUT)
        }
        else{
            error(NetworkStatus.NoOpenInternet)
        }
    }
}