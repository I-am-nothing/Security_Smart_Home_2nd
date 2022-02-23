package com.example.securtiy_home_2.security_home_service.model

import android.net.ConnectivityManager
import android.net.NetworkInfo

class UserData() {
    var accountToken: Any? = null
    var deviceId: Any? = null
    var name: Any? = null

    constructor(userdata: UserData) : this() {
        accountToken = userdata.accountToken
        deviceId = userdata.deviceId
        name = userdata.name
    }
}