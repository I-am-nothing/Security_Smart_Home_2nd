package com.example.securtiy_home_2.security_home_service

enum class NetworkStatus{
    Success,
    Update,
    ForceUpdate,
    NoOpenInternet,
    ServerError,
    Failed
}

enum class EncryptionStatus{
    Encryption,
    Decryption
}

enum class LoginStatus{
    Success,
    NoAccount,
    NoDevice,
    AccountExist
}

enum class LoginFragment{
    Welcome,
    SignUp,
    LogIn
}

enum class CheckPermission{
    Success,
    Failed
}

enum class SetupHomeDeviceFragment{
    NoWifi,
    DiscoverDevice,
    DeviceFound,
    SetDeviceWifi,
    DeviceConnectWifi
}

enum class SettingDetail{
    EditProfile,
    EmailVerify,
    AboutUs,
    ChangePassword,
    PhoneVerify,
    DonateUs
}

enum class SettingType{
    Normal,
    TextBox,
    DateSelector,
    Password,
    Text,
    Selector
}
