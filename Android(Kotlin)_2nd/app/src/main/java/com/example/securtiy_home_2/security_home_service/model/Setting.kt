package com.example.securtiy_home_2.security_home_service.model

import com.example.securtiy_home_2.security_home_service.SettingDetail
import com.example.securtiy_home_2.security_home_service.SettingType

class Setting(
    var icon: Int,
    var title: String,
    var message: String?,
    var settingType: SettingType,
    var settingDetail: SettingDetail? = null,
    var arrayList: ArrayList<Any> = ArrayList()
){
    constructor(setting: Setting) : this(setting.icon, setting.title, setting.message, setting.settingType, setting.settingDetail, setting.arrayList)
}