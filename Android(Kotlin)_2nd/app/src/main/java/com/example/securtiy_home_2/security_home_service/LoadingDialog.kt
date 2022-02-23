package com.example.securtiy_home_2.security_home_service

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.fragment.app.Fragment
import com.example.securtiy_home_2.R
import java.util.*
import kotlin.concurrent.schedule

class LoadingDialog(val fragment: Fragment) {

    private lateinit var dialog: Dialog

    fun startLoading(){
        dialog = Dialog(fragment.requireContext())
        dialog.setContentView(R.layout.progress_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        val surfIv: ImageView = dialog.findViewById(R.id.surf_2_iv)

        val rotate = RotateAnimation(0f, 360f, 100f, 150f)
        rotate.interpolator = PathInterpolatorCompat.create(0.8f,0f,0.2f,1f)
        rotate.duration = 750

        Timer().schedule(0, 750){
            surfIv.startAnimation(rotate)
        }

        dialog.show()
    }

    fun loadingOk(){
        dialog.dismiss()
    }
}