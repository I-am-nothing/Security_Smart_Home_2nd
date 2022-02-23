package com.example.securtiy_home_2

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Interpolator
import android.opengl.Visibility
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.navigation.Navigation
import com.example.securtiy_home_2.databinding.FragmentLoginPageBinding

import com.example.securtiy_home_2.R.*
import com.example.securtiy_home_2.security_home_service.*
import com.google.android.material.snackbar.Snackbar
import org.w3c.dom.Text
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.abs

class LoginPage : Fragment(), TextWatcher{

    private var _binding: FragmentLoginPageBinding? = null
    private val binding get() = _binding!!
    private var loginFragment = LoginFragment.Welcome
    private var httpProgress = false
    private lateinit var fadeIn: AlphaAnimation
    private lateinit var fadeOut: AlphaAnimation
    private lateinit var rotate: RotateAnimation
    private lateinit var communicator: Communicator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginPageBinding.inflate(inflater, container, false)

        communicator = activity as Communicator

        fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 300

        fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.duration = 300

        rotate = RotateAnimation(0f, 360f, 100f, 100f)
        rotate.interpolator = PathInterpolatorCompat.create(0.8f,0f,0.2f,1f)
        rotate.duration = 750

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.accountTb.addTextChangedListener(this)
        binding.passwordTb.addTextChangedListener(this)
        binding.nameTb.addTextChangedListener(this)
        binding.confirmPasswordTb.addTextChangedListener(this)

        binding.loginBtn.setOnClickListener{
            if(loginFragment == LoginFragment.LogIn){
                if(checkData()){
                    binding.loginBtn.isEnabled = false
                    httpProgress = false
                    runSurf()
                    SecurityHomeService(view.context).login(binding.accountTb.text.toString(), binding.passwordTb.text.toString(), {
                        when(it){
                            LoginStatus.NoAccount -> {
                                Snackbar.make(view, "Account or password incorrect", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show()
                                binding.loginBtn.isEnabled = true
                                httpProgress = true
                            }
                            LoginStatus.Success -> {
                                communicator.updateUserLocalData()

                                SecurityHomeService(view.context).deviceLogin({ loginStatus ->
                                    httpProgress = true
                                    when(loginStatus){
                                        LoginStatus.Success -> {
                                            communicator.showDialog("Use Biometric Log In", "Because of Security, you must need to use biometric log in"){
                                                Navigation.findNavController(view).navigate(R.id.action_loginPage_to_biometricPage)
                                            }
                                        }
                                        LoginStatus.NoDevice -> Navigation.findNavController(view).navigate(R.id.action_loginPage_to_newDevicePage)
                                    }
                                }, { networkStatus ->
                                    binding.loginBtn.isEnabled = true
                                    httpProgress = true
                                    networkError(networkStatus)
                                })
                            }
                        }
                    }, {
                        binding.loginBtn.isEnabled = true
                        httpProgress = true
                        networkError(it)
                    })
                }
                else{
                    Snackbar.make(view, "Invalid Data: Please check your data in text box", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show()
                }
            }
            else{
                binding.container.transitionToState(R.id.motion_login)
                activity?.window?.navigationBarColor = Color.WHITE
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.titleTv.text = "Welcome\nBack"
                    binding.loginBtn.setBackgroundResource(drawable.custom_btn_1_background)
                    binding.loginBtn.setTextColor(ContextCompat.getColor(view.context, color.white))
                    binding.signUpBtn.setBackgroundResource(drawable.custom_btn_2_background)
                    binding.signUpBtn.setTextColor(ContextCompat.getColor(view.context, color.black_overlay))
                    if(loginFragment != LoginFragment.Welcome){
                        loginFragment = LoginFragment.LogIn
                        checkData()
                    }
                    loginFragment = LoginFragment.LogIn
                }, 250)
            }
        }

        binding.signUpBtn.setOnClickListener {
            if (loginFragment == LoginFragment.SignUp) {
                if (checkData()) {
                    binding.signUpBtn.isEnabled = false
                    httpProgress = false
                    runSurf()
                    SecurityHomeService(view.context).register(binding.accountTb.text.toString(),
                        binding.passwordTb.text.toString(),
                        binding.nameTb.text.toString(),
                        {
                            httpProgress = true
                            when (it) {
                                LoginStatus.AccountExist -> {
                                    Snackbar.make(view, "This account is already exist", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show()
                                    binding.signUpBtn.isEnabled = true
                                }
                                LoginStatus.Success -> {
                                    Snackbar.make(view, "Welcome " + binding.nameTb.text.toString(), Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show()
                                    communicator.updateUserLocalData()

                                    communicator.showDialog("Use Biometric Log In", "Because of Security, you must need to use biometric log in"){
                                        Navigation.findNavController(view).navigate(R.id.action_loginPage_to_biometricPage)
                                    }
                                }
                            }
                        },
                        {
                            binding.signUpBtn.isEnabled = true
                            httpProgress = true
                            networkError(it)
                        })
                } else {
                    Snackbar.make(
                        view,
                        "Invalid Data: Please check your data in text box",
                        Snackbar.LENGTH_SHORT
                    )
                        .setAction("Action", null).show()
                }
            } else {
                binding.container.transitionToState(R.id.motion_signup)
                activity?.window?.navigationBarColor = Color.WHITE
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.titleTv.text = "Create\nAccount"
                    binding.loginBtn.setBackgroundResource(drawable.custom_btn_2_background)
                    binding.loginBtn.setTextColor(
                        ContextCompat.getColor(view.context, color.black_overlay)
                    )
                    binding.signUpBtn.setBackgroundResource(drawable.custom_btn_1_background)
                    binding.signUpBtn.setTextColor(
                        ContextCompat.getColor(view.context, color.white)
                    )
                    if (loginFragment != LoginFragment.Welcome) {
                        loginFragment = LoginFragment.SignUp
                        checkData()
                    }
                    loginFragment = LoginFragment.SignUp
                }, 250)
            }
        }

        binding.forgetTv.setOnClickListener {

        }

        binding.surfIv.setOnClickListener {

        }
    }

    private fun networkError(networkStatus: NetworkStatus){
        view?.let {
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
        }
    }

    private fun runSurf(){
        Thread{
            Timer().schedule(0, 750){
                if(httpProgress){
                    this.cancel()
                }
                binding.surfIv.startAnimation(rotate)
            }
        }.run()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    override fun afterTextChanged(s: Editable?) {
        checkData().toString()
    }

    private fun checkData(): Boolean{
        var hint = ""

        when(loginFragment){
            LoginFragment.LogIn -> {
                if(binding.accountTb.text.isEmpty()){
                    hint += "Email: Must not be empty\n"
                }
                if(binding.passwordTb.text.isEmpty()){
                    hint += "Password: Must not be empty"
                }
            }
            LoginFragment.SignUp -> {
                if(binding.nameTb.text.length < 2){
                    hint += "Name: At least 2 characters\n"
                }
                else if(binding.nameTb.text.length > 20){
                    hint += "Name: Is too Long :(\n"
                }
                if (TextUtils.isEmpty(binding.accountTb.text) || !android.util.Patterns.EMAIL_ADDRESS.matcher(binding.accountTb.text).matches()) {
                    hint += "Email: Can't find this email\n"
                }
                hint += when {
                    binding.passwordTb.text.length < 8 -> {
                        "Password: At least 8 characters"
                    }
                    binding.passwordTb.text.length > 20 -> {
                        "Password: Is too Long :("
                    }
                    binding.passwordTb.text.toString() != binding.confirmPasswordTb.text.toString() -> {
                        "Confirm Password: Is not the same"
                    }
                    else -> ""
                }
            }
        }
        binding.hintTv.text = hint

        if (!(binding.accountTb.text.isEmpty() && binding.passwordTb.text.isEmpty() && (loginFragment == LoginFragment.LogIn || (binding.nameTb.text.isEmpty() && binding.confirmPasswordTb.text.isEmpty())))){
            if (hint.isEmpty()) {
                if (binding.hintLayout.alpha == 1f) {
                    binding.hintLayout.startAnimation(fadeOut)
                    binding.hintLayout.alpha = 0f
                }
            }
            else {
                if (binding.hintLayout.alpha == 0f) {
                    binding.hintLayout.startAnimation(fadeIn)
                    binding.hintLayout.alpha = 1f
                }
            }
        }

        return hint.isEmpty()
    }
}