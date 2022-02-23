package com.example.securtiy_home_2.security_home_service

interface Communicator {
    fun updateUserLocalData()
    fun checkPermissions(result: ((CheckPermission) -> Unit))
    fun checkPermission(permission: String, result: (CheckPermission) -> Unit)
    fun checkPermissions(permissions: Array<String>, result: (CheckPermission) -> Unit)
    fun showDialog(title: String, description: String, actionOk: () -> Unit)
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
}