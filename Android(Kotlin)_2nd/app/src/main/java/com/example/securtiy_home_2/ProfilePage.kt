package com.example.securtiy_home_2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.securtiy_home_2.databinding.FragmentMainPageBinding
import com.example.securtiy_home_2.databinding.FragmentProfilePageBinding
import com.example.securtiy_home_2.security_home_service.NotificationAdapter
import com.example.securtiy_home_2.security_home_service.SettingAdapter
import com.example.securtiy_home_2.security_home_service.SettingDetail
import com.example.securtiy_home_2.security_home_service.SettingType
import com.example.securtiy_home_2.security_home_service.model.Notification
import com.example.securtiy_home_2.security_home_service.model.Setting

class ProfilePage : Fragment() {

    private var _binding: FragmentProfilePageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilePageBinding.inflate(inflater, container, false)

        val gridLayoutManager = GridLayoutManager(context, 1, LinearLayoutManager.VERTICAL, false)
        binding.settingRv.layoutManager = gridLayoutManager

        val arrayList = ArrayList<Setting>()
        arrayList.add(Setting(R.drawable.surf, "Edit Profile", null, SettingType.Normal, SettingDetail.EditProfile))
        arrayList.add(Setting(R.drawable.phone_login_verify, "Phone Login Verify", null, SettingType.Normal, SettingDetail.PhoneVerify))
        arrayList.add(Setting(R.drawable.email_verify, "Email verify", null, SettingType.Normal, SettingDetail.EmailVerify))
        arrayList.add(Setting(R.drawable.change_password, "Change Password", null, SettingType.Normal,SettingDetail.ChangePassword))
        arrayList.add(Setting(R.drawable.about_us, "About Us", null, SettingType.Normal,SettingDetail.AboutUs))
        arrayList.add(Setting(R.drawable.donate_us, "Donate Us", null, SettingType.Normal,SettingDetail.DonateUs))
        val adapter = SettingAdapter(binding.root.context, R.layout.fragment_profile_page, arrayList)
        binding.settingRv.adapter = adapter

        binding.editProfileTv.setOnClickListener{
            val bundle = Bundle()
            bundle.putSerializable("settingDetail", SettingDetail.EditProfile)
            bundle.putString("title", binding.editProfileTv.text.toString())
            Navigation.findNavController(it).navigate(R.id.action_profilePage_to_settingDetailPge, bundle)
        }

        return binding.root
    }
}