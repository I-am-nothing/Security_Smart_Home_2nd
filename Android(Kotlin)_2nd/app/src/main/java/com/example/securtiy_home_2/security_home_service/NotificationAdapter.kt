package com.example.securtiy_home_2.security_home_service

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.securtiy_home_2.R
import com.example.securtiy_home_2.security_home_service.model.Notification

class NotificationAdapter(private val context: Context, private val arrayList: ArrayList<Notification>): RecyclerView.Adapter<NotificationAdapter.NotificationHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationAdapter.NotificationHolder {

        val itemHolder = LayoutInflater.from(parent.context).inflate(R.layout.custom_recycler_view, parent, false)

        return NotificationHolder(itemHolder)
    }

    override fun onBindViewHolder(holder: NotificationAdapter.NotificationHolder, position: Int) {
        val notification = arrayList[position]

        holder.titleTv.text = notification.Command_ID.toString()
        holder.datetimeTv.text = notification.DateTime.toString()
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    class NotificationHolder(v: View): RecyclerView.ViewHolder(v){
        var titleTv: TextView = v.findViewById(R.id.title2_tv)
        var datetimeTv: TextView = v.findViewById(R.id.datetime_tv)
    }
}