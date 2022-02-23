package com.example.securtiy_home_2.security_home_service

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.securtiy_home_2.R

class DeviceAdapter(private val touchHelper: ItemTouchHelper): RecyclerView.Adapter<DeviceAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.device_row, parent, false))
    }

    override fun getItemCount(): Int {
        return 6
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val deviceName: TextView = holder.itemView.findViewById(R.id.device_row_name_tv)
        val dragAnchor: ImageView = holder.itemView.findViewById(R.id.device_row_dragAnchor)
        deviceName.text = "DEVICE${position + 1}"
        dragAnchor.setOnTouchListener { v, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                touchHelper.startDrag(holder)
            }
            false
        }
    }
}