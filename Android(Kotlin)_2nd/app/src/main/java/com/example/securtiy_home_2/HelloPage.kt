package com.example.securtiy_home_2

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import com.example.securtiy_home_2.databinding.FragmentHelloPageBinding
import com.example.securtiy_home_2.databinding.FragmentLoginPageBinding
import com.example.securtiy_home_2.security_home_service.Communicator
import com.example.securtiy_home_2.security_home_service.LoginStatus
import com.example.securtiy_home_2.security_home_service.NetworkStatus
import com.example.securtiy_home_2.security_home_service.SecurityHomeService
import com.example.securtiy_home_2.security_home_service.model.Version
import com.google.android.material.snackbar.Snackbar
import kotlin.math.log
import kotlin.system.exitProcess

class HelloPage : Fragment() {

    private var _binding: FragmentHelloPageBinding? = null
    private val binding get() = _binding!!
    private lateinit var communicator: Communicator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelloPageBinding.inflate(inflater, container, false)

        activity?.window?.navigationBarColor = Color.parseColor("#DAFDF1")

        communicator = activity as Communicator

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        SecurityHomeService(view.context).checkVersion { status, body ->
            val version: Version
            var message = ""
            if(body != null){
                version = body as Version
                message = "Version_ID: " + version.Version_ID.toString() + "\nTitle: " + version.Title.toString() + "\nDetail: " + version.Detail.toString()
            }

            Handler(Looper.getMainLooper()).postDelayed({
                when (status) {
                    NetworkStatus.Update -> communicator.showDialog("Available updates can be downloaded", message) {  }
                    NetworkStatus.ForceUpdate -> communicator.showDialog("Available updates must be downloaded", message){
                        exitProcess(-1)
                    }
                    NetworkStatus.NoOpenInternet -> {
                        Snackbar.make(view, "No Open Internet: Please open the internet", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show()
                    }
                    NetworkStatus.ServerError -> {
                        Snackbar.make(view, "Server Error: Please wait a second and try again", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show()
                    }
                }
                login()
            }, 500)
        }
    }

    private fun login(){
        val data = SecurityHomeService().getUserData()

        communicator.checkPermissions {
            view?.let {
                if(data.accountToken == null){
                    Navigation.findNavController(it).navigate(R.id.action_helloPage_to_loginPage)
                }
                else{
                    SecurityHomeService(it.context).deviceLogin({ loginStatus ->
                        when(loginStatus){
                            LoginStatus.Success -> Navigation.findNavController(it).navigate(R.id.action_helloPage_to_biometricPage)
                            LoginStatus.NoDevice -> Navigation.findNavController(it).navigate(R.id.action_helloPage_to_newDevicePage)
                        }
                    },{ networkStatus ->
                        when (networkStatus) {
                            NetworkStatus.NoOpenInternet -> {
                                Snackbar.make(it, "No Open Internet: Please open the internet", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show()
                            }
                            NetworkStatus.ServerError -> {
                                Snackbar.make(it, "Server Error: Please wait a second and try again", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show()
                            }
                        }
                        if(data.accountToken != null){
                            Navigation.findNavController(it).navigate(R.id.action_helloPage_to_biometricPage)
                        }
                    })

                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}