package com.example.securtiy_home_2

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.securtiy_home_2.databinding.FragmentMainPageBinding
import com.example.securtiy_home_2.security_home_service.NetworkStatus
import com.example.securtiy_home_2.security_home_service.NotificationAdapter
import com.example.securtiy_home_2.security_home_service.SecurityHomeService
import com.example.securtiy_home_2.security_home_service.model.Notification
import com.google.android.material.snackbar.Snackbar
import java.util.*
import kotlin.collections.ArrayList

class MainPage : Fragment() {

    private var _binding: FragmentMainPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainPageBinding.inflate(inflater, container, false)

        val gridLayoutManager = GridLayoutManager(context, 1, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = gridLayoutManager

        val arrayList = ArrayList<Notification>()
        arrayList.add(Notification("XDD", "AAA", "BBB"))
        val adapter = NotificationAdapter(binding.root.context, arrayList)
        binding.recyclerView.adapter = adapter

        binding.roomControlBtn.setOnClickListener{
            Navigation.findNavController(binding.root).navigate(R.id.action_main_Page_to_controlPage)
        }

        binding.profileBtn.setOnClickListener{
            Navigation.findNavController(binding.root).navigate(R.id.action_mainPage_to_profilePage)
        }

        SecurityHomeService(requireContext()).getProfile({
            view?.let { view ->
                Snackbar.make(view, "Welcome ${it.Name.toString()}", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
            }

        }, {
            view?.let { view ->
                when(it){
                    NetworkStatus.NoOpenInternet -> {
                        Snackbar.make(view, "No Open Internet: Please open the internet", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show()
                    }
                    NetworkStatus.ServerError -> {
                        Snackbar.make(view, "Server Error: Please wait a second and try again", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show()
                    }
                    NetworkStatus.Failed -> {
                        Snackbar.make(view, "Device Login Failed", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show()
                    }
                }
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}