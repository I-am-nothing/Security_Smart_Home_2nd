package com.example.securtiy_home_2.security_home_service

import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.securtiy_home_2.R
import com.google.android.material.snackbar.Snackbar

class HouseAdapter(private val touchHelper: ItemTouchHelper): RecyclerView.Adapter<HouseAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.house_row, parent, false))
    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val houseRow: ConstraintLayout = holder.itemView.findViewById(R.id.house_row)
        val houseName: TextView = holder.itemView.findViewById(R.id.house_row_house_name)
        val dragAnchor: ImageView = holder.itemView.findViewById(R.id.house_row_dragAnchor)
        houseName.text = "HOUSE${position + 1}"

        dragAnchor.setOnTouchListener { v, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN)
                touchHelper.startDrag(holder)
            false
        }

        houseRow.setOnLongClickListener {
            Log.e("xdd", "longClick")
            false
        }
    }


}