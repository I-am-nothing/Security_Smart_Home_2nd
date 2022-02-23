package com.example.securtiy_home_2.security_home_service

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.securtiy_home_2.R
import com.example.securtiy_home_2.security_home_service.model.BaseParcelable
import com.example.securtiy_home_2.security_home_service.model.Setting
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory
import org.json.JSONObject

class SettingAdapter(private val context: Context, private val fragmentLayout: Int, private val arrayList: ArrayList<Setting>): RecyclerView.Adapter<SettingAdapter.SettingHolder>() {

    private val original = ArrayList<Setting>()

    init {
        for(i in arrayList){
            original.add(Setting(i))
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SettingAdapter.SettingHolder {
        val itemHolder = LayoutInflater.from(context).inflate(R.layout.custom_recycler_view_2, parent, false)

        return SettingHolder(itemHolder)
    }

    override fun onBindViewHolder(holder: SettingAdapter.SettingHolder, position: Int) {
        val setting = arrayList[position]

        holder.titleTv.text = setting.title
        holder.iconImg.setImageResource(setting.icon)

        when(setting.settingType){
            SettingType.TextBox -> {
                holder.editText.setText(setting.message)
                holder.editText.visibility = View.VISIBLE

                holder.editText.doAfterTextChanged {
                    arrayList[position].message = holder.editText.text.toString()
                }
            }
            SettingType.Selector -> {
                val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, setting.arrayList)
                holder.spinner.adapter = adapter
                holder.spinner.visibility = View.VISIBLE
                holder.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                        arrayList[holder.adapterPosition].message = pos.toString()
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
            SettingType.DateSelector -> {
                holder.editText.setText(setting.message)
                holder.editText.focusable = EditText.NOT_FOCUSABLE
                holder.editText.visibility = View.VISIBLE

                holder.editText.setOnClickListener{
                    val date = holder.editText.text.split('/')
                    val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                        holder.editText.setText("$month/$dayOfMonth/$year")
                        arrayList[position].message = "$month/$dayOfMonth/$year"
                    }

                    val dialog = if(date.size == 3){
                        DatePickerDialog(context, dateSetListener, date[2].toInt(), date[0].toInt(), date[1].toInt())
                    }else{
                        DatePickerDialog(context, dateSetListener, 2000, 1, 1)
                    }
                    dialog.show()
                }
            }
        }
        when(fragmentLayout){
            R.layout.fragment_profile_page -> {
                holder.layout.setOnClickListener{
                    val bundle = Bundle()
                    bundle.putSerializable("settingDetail", setting.settingDetail)
                    bundle.putString("title", setting.title)
                    Navigation.findNavController(it).navigate(R.id.action_profilePage_to_settingDetailPge, bundle)
                }
            }
            R.layout.fragment_setting_detail_pge -> {

            }
        }


    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    fun getSetValue(): JSONObject{
        val output = JSONObject()
        for(i in 0 until original.size){
            if(original[i].message != arrayList[i].message){
                output.put(original[i].title, arrayList[i].message)
            }
        }

        return output
    }

    class SettingHolder(v: View): RecyclerView.ViewHolder(v){
        var titleTv: TextView = v.findViewById(R.id.title_4_tv)
        var iconImg: ImageView = v.findViewById(R.id.icon_img)
        var layout: ConstraintLayout = v.findViewById(R.id.setting_ly)
        var editText: EditText = v.findViewById(R.id.edit_text)
        var spinner: Spinner = v.findViewById(R.id.spinner)
    }
}