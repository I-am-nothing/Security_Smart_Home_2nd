package com.example.securtiy_home_2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.example.securtiy_home_2.databinding.FragmentSetupHomeDevicePageBinding
import java.util.*
import kotlin.concurrent.schedule
import android.net.ConnectivityManager
import android.net.Network

import android.net.NetworkCapabilities

import android.net.NetworkRequest
import android.os.*
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavAction
import androidx.navigation.Navigation
import com.example.securtiy_home_2.security_home_service.*
import com.example.securtiy_home_2.security_home_service.model.Encryption
import com.example.securtiy_home_2.security_home_service.model.HomeDeviceType
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.net.Socket
import java.util.concurrent.Executors


class SetupHomeDevicePage : Fragment() {

    private var _binding: FragmentSetupHomeDevicePageBinding? = null
    private val binding get() = _binding!!

    private var wifiStatus = false
    private var wifiSsid = ""
    private var setupHomeDeviceFragment = SetupHomeDeviceFragment.NoWifi

    private lateinit var network: Network
    private lateinit var wifiManager: WifiManager
    private lateinit var communicator: Communicator
    private lateinit var intentFilter: IntentFilter
    private lateinit var wifiScanReceiver: BroadcastReceiver
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetupHomeDevicePageBinding.inflate(inflater, container, false)

        wifiManager = requireActivity().getSystemService(Context.WIFI_SERVICE) as WifiManager

        communicator = activity as Communicator

        binding.lottieLayerName.playAnimation()

        wifiReceive()

        Timer().schedule(0, 200) {
            try{
                checkWifiLoop()
            }
            catch (e: Exception){
                this.cancel()
            }
        }

        SecurityHomeService(binding.root.context).getHomeDeviceType{
            networkError(it)
        }

        checkWifi()

        intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)

        binding.settingTv.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }

        binding.cancelTv.setOnClickListener{
            Navigation.findNavController(binding.root).popBackStack()
        }

        binding.turnOnBtn.setOnClickListener {
            when(setupHomeDeviceFragment){
                SetupHomeDeviceFragment.NoWifi -> startActivity(Intent(Settings.Panel.ACTION_WIFI))
                SetupHomeDeviceFragment.DeviceFound -> {
                    binding.container2.transitionToState(R.id.setDeviceWifi)
                    setupHomeDeviceFragment = SetupHomeDeviceFragment.SetDeviceWifi
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.title3Tv.text = "Set Up Home Device Wi-Fi"
                        binding.subTitle2Tv.text = "Enter the password. Then, press CONTINUE"
                        binding.ssidTb.setText(wifiSsid)
                        binding.lottieLayerName.setAnimation(R.raw.device_set_wifi)
                        binding.lottieLayerName.playAnimation()
                    }, 150)
                }
                SetupHomeDeviceFragment.SetDeviceWifi -> {
                    when {
                        binding.ssidTb.text.isEmpty() -> {
                            Snackbar.make(binding.root, "Ssid must not be empty.", Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show()
                        }
                        binding.passwordTb.text.isEmpty() -> {
                            Snackbar.make(binding.root, "Password must not be empty.", Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show()
                        }
                        else -> {
                            binding.container2.transitionToState(R.id.DeviceConnectWifi)
                            setupHomeDeviceFragment = SetupHomeDeviceFragment.DeviceConnectWifi
                            Handler(Looper.getMainLooper()).postDelayed({
                                binding.title3Tv.text = "Home Device Connect to Wi-Fi"
                                binding.subTitle2Tv.text = "Waiting the Home Device Connect to Wifi"
                            }, 150)
                            deviceNewWifi()
                        }
                    }
                }
            }

        }

        return binding.root
    }

    private fun deviceNewWifi(){
        val body = JSONObject().put("Ssid", SecurityHomeEncryption().encrypt(binding.ssidTb.text.toString()).toJson())
            .put("Password", SecurityHomeEncryption().encrypt(binding.passwordTb.text.toString()).toJson())

        SecurityHomeService().homeDeviceCommunicate(network, "http://192.168.87.87:8080/newWiFi", body, {
            Log.e("A~ json string", it.toString())
            when(SecurityHomeEncryption().decrypt(Gson().fromJson(it["status"].toString(), Encryption::class.java))){
                "1" -> {
                    Log.e("A~ ", "yeah")
                    /*binding.container2.transitionToState(R.id.DeviceConnectWifi)
                    setupHomeDeviceFragment = SetupHomeDeviceFragment.DeviceConnectWifi
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.title3Tv.text = "Home Device Found"
                        binding.subTitle2Tv.text = "Make sure the home device is flashing. Then, press CONTINUE"
                        binding.lottieLayerName.setAnimation(R.raw.not_open_wifi)
                        binding.lottieLayerName.playAnimation()
                    }, 150)*/
                    getDeviceId()
                }
            }
        }, {
            Log.e("A~ ", "failed")
        })
    }

    private fun getDeviceId(){
        val body = JSONObject().put("status", SecurityHomeEncryption().encrypt("418").toJson())

        Handler(Looper.getMainLooper()).postDelayed({
            SecurityHomeService().homeDeviceCommunicate(network, "http://192.168.87.87:8080/getDeviceId", body, {
                Log.e("A~ json string", it.toString())
                when(SecurityHomeEncryption().decrypt(Gson().fromJson(it["status"].toString(), Encryption::class.java))){
                    "1" -> {
                        Log.e("A~ ", "yeah")
                        /*binding.container2.transitionToState(R.id.deviceFound)
                        setupHomeDeviceFragment = SetupHomeDeviceFragment.DeviceFound
                        Handler(Looper.getMainLooper()).postDelayed({
                            binding.turnOnBtn.text = "Continue"
                            binding.title3Tv.text = "Home Device Found"
                            binding.subTitle2Tv.text = "Make sure the home device is flashing. Then, press CONTINUE"
                            binding.lottieLayerName.setAnimation(R.raw.not_open_wifi)
                            binding.lottieLayerName.playAnimation()
                        }, 150)*/

                    }
                }
            }, {
                Log.e("A~ ", "failed")
            })
        }, 7500)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun wifiReceive(){
        wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                findDevice()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun findDevice(){
        val results = wifiManager.scanResults
        Timer().schedule(0, 200){
            if(SecurityHomeService().homeDeviceType() != null){
                for (result in results) {
                    for(device in SecurityHomeService().homeDeviceType()!!){
                        if(result.SSID == device.Ap_Ssid.toString()){
                            Log.e("A~ ", "You find the XDD Device: ${result.SSID}")
                            connectDeviceWifi(device)
                            this.cancel()
                        }
                    }
                }
                this.cancel()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectDeviceWifi(device: HomeDeviceType){
        try {
            val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
                .setSsid(device.Ap_Ssid.toString())
                .setWpa2Passphrase(device.Ap_Password.toString())
                .build()

            val networkRequest: NetworkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(wifiNetworkSpecifier)
                .build()

            connectivityManager =
                requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(net: Network) {
                    super.onAvailable(net)
                    Log.e("A~ ", "Connect to ${device.Ap_Ssid.toString()}")

                    network = net

                    deviceFound()
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                }
            }

            connectivityManager.requestNetwork(networkRequest, networkCallback)
        }
        catch (e: Exception){

        }
    }

    private fun deviceFound(){
        val body = JSONObject()

        SecurityHomeService().homeDeviceCommunicate(network, "http://192.168.87.87:8080/", body, {
            Log.e("A~ json string", it.toString())
            when(SecurityHomeEncryption().decrypt(Gson().fromJson(it["status"].toString(), Encryption::class.java))){
                "1" -> {
                    binding.container2.transitionToState(R.id.deviceFound)
                    setupHomeDeviceFragment = SetupHomeDeviceFragment.DeviceFound
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.turnOnBtn.text = "Continue"
                        binding.title3Tv.text = "Home Device Found"
                        binding.subTitle2Tv.text = "Make sure the home device is flashing. Then, press CONTINUE"
                        binding.lottieLayerName.setAnimation(R.raw.device_found)
                        binding.lottieLayerName.playAnimation()
                    }, 150)
                    /*Handler(Looper.getMainLooper()).postDelayed({
                        connectivityManager.unregisterNetworkCallback(networkCallback)
                        requireActivity().unregisterReceiver(wifiScanReceiver)
                    }, 5000)*/
                }
            }
        }, {
            Log.e("A~ ", "failed")
        })
    }

    private fun checkWifi(){
        if(wifiManager.isWifiEnabled){
            val info = wifiManager.connectionInfo
            setupHomeDeviceFragment = SetupHomeDeviceFragment.DiscoverDevice
            wifiSsid = info.ssid.trim('"')
            Log.e("A~ ", info.ssid)
            binding.container2.transitionToState(R.id.discoverDevice)
            Handler(Looper.getMainLooper()).postDelayed({
                binding.title3Tv.text = "Discovering The Home Device"
            }, 150)
            wifiManager.startScan()
        }
    }

    private fun checkWifiLoop(){
        if (wifiStatus != wifiManager.isWifiEnabled) {
            wifiStatus = wifiManager.isWifiEnabled
            if (wifiStatus) {
                setupHomeDeviceFragment = SetupHomeDeviceFragment.DiscoverDevice
                binding.container2.transitionToState(R.id.discoverDevice)
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.title3Tv.text = "Discovering The Home Device"
                    binding.subTitle2Tv.text = "Connecting to the device may cause network disconnection"
                    binding.lottieLayerName.setAnimation(R.raw.discover_home_device)
                    binding.lottieLayerName.playAnimation()
                }, 150)
                wifiManager.startScan()
            }
            else{
                binding.container2.transitionToState(R.id.noWifi)
                setupHomeDeviceFragment = SetupHomeDeviceFragment.NoWifi
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.title3Tv.text = "Turn On Wi-Fi"
                    binding.subTitle2Tv.text = "Wi-Fi is needed to communicate with home devices"
                    binding.lottieLayerName.setAnimation(R.raw.not_open_wifi)
                    binding.lottieLayerName.playAnimation()
                }, 150)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        try{
            requireActivity().unregisterReceiver(wifiScanReceiver)
        }
        catch (e: Exception){

        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().registerReceiver(wifiScanReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        try{
            connectivityManager.unregisterNetworkCallback(networkCallback)
            requireActivity().unregisterReceiver(wifiScanReceiver)
        }
        catch (e: Exception){

        }
    }

    private fun networkError(networkStatus: NetworkStatus) {
        view?.let {
            when (networkStatus) {
                NetworkStatus.NoOpenInternet -> {
                    Snackbar.make(
                        it,
                        "No Open Internet: Please open the internet",
                        Snackbar.LENGTH_SHORT
                    )
                        .setAction("Action", null).show()
                }
                NetworkStatus.ServerError -> {
                    Snackbar.make(
                        it,
                        "Server Error: Please wait a second and try again",
                        Snackbar.LENGTH_SHORT
                    )
                        .setAction("Action", null).show()
                }
            }
        }
    }

    private fun client(){
        Executors.newSingleThreadExecutor().execute{
            val socket = Socket("192.168.87.87", 81)
            val scanner = Scanner(socket.getInputStream())
            val printWriter = PrintWriter(socket.getOutputStream())
            Timer().schedule(0, 1000){
                printWriter.println("XDD Hello World")
            }
            while (scanner.hasNextLine()){
                Log.d("A~ ", scanner.nextLine())
            }
        }
    }
}