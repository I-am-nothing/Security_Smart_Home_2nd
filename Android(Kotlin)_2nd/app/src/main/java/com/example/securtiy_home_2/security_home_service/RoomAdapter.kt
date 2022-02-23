package com.example.securtiy_home_2.security_home_service

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.securtiy_home_2.R

class RoomAdapter(private val context: Context): RecyclerView.Adapter<RoomAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.room_row, parent, false))
    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val roomName: TextView = holder.itemView.findViewById(R.id.room_row_room_name)
        val deviceRecyclerView: RecyclerView = holder.itemView.findViewById(R.id.room_row_device_recyclerView)
        roomName.text = "ROOM${position + 1}"

        var deviceAdapter: DeviceAdapter? = null
        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
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
                deviceAdapter?.notifyItemMoved(currentPosition, targetPosition)
                return true;// true if moved, false otherwise
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

        })
        touchHelper.attachToRecyclerView(deviceRecyclerView)
        deviceAdapter = DeviceAdapter(touchHelper)
        val gridLayoutManager = GridLayoutManager(context, 3, LinearLayoutManager.VERTICAL, false)
        deviceRecyclerView.adapter = deviceAdapter
        deviceRecyclerView.layoutManager = gridLayoutManager
    }
}