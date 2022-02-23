package com.example.securtiy_home_2

import android.graphics.Canvas
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.securtiy_home_2.databinding.FragmentControlPageBinding
import com.example.securtiy_home_2.security_home_service.HouseAdapter
import com.example.securtiy_home_2.security_home_service.RoomAdapter
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class ControlPage : Fragment() {

    private var _binding: FragmentControlPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentControlPageBinding.inflate(inflater, container, false)

        //house
        val houseRecyclerView = binding.houseRecyclerView
        var houseAdapter: HouseAdapter? = null
        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                current: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                val currentPosition = current.adapterPosition
                val targetPosition = target.adapterPosition
                // move item in `fromPos` to `toPos` in adapter.
                //Collections.swap(list, sourcePosition, targetPosition)
                houseAdapter?.notifyItemMoved(currentPosition, targetPosition)
                return true;// true if moved, false otherwise
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

        })
        touchHelper.attachToRecyclerView(houseRecyclerView)
        houseAdapter = HouseAdapter(touchHelper)
        val houseLayoutManager = GridLayoutManager(context, 1, LinearLayoutManager.HORIZONTAL, false)
        houseRecyclerView.adapter = houseAdapter
        houseRecyclerView.layoutManager = houseLayoutManager

        val roomRecyclerView = binding.roomRecyclerView
        val roomAdapter = RoomAdapter(requireContext())
        val roomLayoutManager = GridLayoutManager(context, 1, LinearLayoutManager.VERTICAL, false)
        roomRecyclerView.adapter = roomAdapter
        roomRecyclerView.layoutManager = roomLayoutManager

        binding.floatingActionButton.setOnClickListener{
            Navigation.findNavController(it).navigate(R.id.action_controlPage_to_setupHomeDevicePage)
        }

        return binding.root
    }
}