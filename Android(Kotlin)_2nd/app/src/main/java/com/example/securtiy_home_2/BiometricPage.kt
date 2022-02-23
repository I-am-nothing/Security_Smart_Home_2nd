package com.example.securtiy_home_2

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import java.util.concurrent.Executor

class BiometricPage : Fragment() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_biometric_page, container, false)

        executor = ContextCompat.getMainExecutor(view.context)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int,
                                                   errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    useBiometricLogin()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)

                    Navigation.findNavController(view).navigate(R.id.action_biometricPage_to_main_Page)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    useBiometricLogin()
                }
            })

        useBiometricLogin()

        return view
    }

    private fun useBiometricLogin() {
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for Security_home")
            .setSubtitle("Log in using your biometric credential")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL or BIOMETRIC_WEAK)
            .build()
        biometricPrompt.authenticate(promptInfo)
    }
}