package com.example.securtiy_home_2

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.MediaDrm
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.securtiy_home_2.security_home_service.*
import com.example.securtiy_home_2.security_home_service.model.UserData
import com.google.gson.Gson
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule
import kotlin.concurrent.thread
import kotlin.concurrent.timerTask


class MainActivity : AppCompatActivity(), Communicator {

    private var permissionReturn = false
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.USE_BIOMETRIC,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.hide()

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        SecurityHomeService(getUserLocalData())
    }

    private fun getUserLocalData(): UserData {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val json = sharedPref.getString("UserLocalData", "")
        val userLocalData = Gson().fromJson(json, UserData::class.java)
        val uuid = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)
        val wvDrm = MediaDrm(uuid)
        val deviceUniqueId = wvDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
        var userData = UserData()

        if(userLocalData != null){
            userData = SecurityHomeEncryption().decrypt(userLocalData, UserData::class.java)
        }
        userData.deviceId = String(Base64.getEncoder().encode(deviceUniqueId))

        return userData
    }

    override fun updateUserLocalData() {
        val userLocalData = SecurityHomeService().getUserData()
        userLocalData.deviceId = null
        val prefsEditor = getPreferences(Context.MODE_PRIVATE).edit()
        val encryptionData: UserData = SecurityHomeEncryption().encrypt(userLocalData, UserData::class.java)
        val json = Gson().toJson(encryptionData)

        Log.d("xdd", json)

        prefsEditor.putString("UserLocalData", json)
        prefsEditor.apply()
    }

    override fun checkPermissions(result: ((CheckPermission) -> Unit)){
        checkPermissions(permissions){
            result(it)
        }
    }

    private fun checkPermission(permissions: Array<String>): ArrayList<String>{
        val losePermissions = ArrayList<String>()
        for(per in permissions){
            if(ContextCompat.checkSelfPermission(this, per) != PackageManager.PERMISSION_GRANTED){
                losePermissions.add(per)
            }
        }

        return losePermissions
    }

    private fun runThread(result: () -> Unit){
        Handler(Looper.getMainLooper()).postDelayed({
            if(!permissionReturn){
                runThread {
                    result()
                }
            }
            else{
                result()
            }
        }, 250)
    }

    override fun checkPermissions(permissions: Array<String>, result: (CheckPermission) -> Unit){
        val losePermissions = checkPermission(permissions)

        if(losePermissions.size == 0){
            result(CheckPermission.Success)
        }
        else{
            permissionReturn = false
            var str = "Some functions will not be available if you don't allow it."
            losePermissions.forEach {
                str += "\n" + it.split(".").last()
            }

            showDialog("Request Permissions", str){
                ActivityCompat.requestPermissions(this, losePermissions.toTypedArray(),87)

                runThread{
                    if(checkPermission(permissions).size == 0){
                        result(CheckPermission.Success)
                    }
                    else{
                        result(CheckPermission.Failed)
                    }
                }
            }
        }
    }

    override fun showDialog(title: String, description: String, actionOk: () -> Unit){
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(title)
        alertDialog.setMessage(description)
        alertDialog.setIcon(R.mipmap.ic_launcher)
        alertDialog.setCancelable(false)
        alertDialog.setPositiveButton("OK") { _, _ ->
            actionOk()
        }
        alertDialog.show()
    }

    override fun checkPermission(permission: String, result: (CheckPermission) -> Unit){
        checkPermissions(arrayOf(permission)){
            result(it)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionReturn = true
    }
}