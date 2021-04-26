package com.boost.presignin.rest

object EndPoints {

    //TODO NFX API WITH FLOAT
    const val WITH_FLOATS_BASE = "https://api.withfloats.com/"

    //TODO NFX API WITH FLOAT TWO
    const val WITH_FLOATS_TWO_BASE = "https://api2.withfloats.com/"
    const val CREATE_MERCHANT_PROFILE = "/user/v9/floatingPoint/CreateMerchantProfile"
    const val VERIFY_LOGIN = "/discover/v1/floatingPoint/verifyLogin"
    const val CHANGE_PASSWORD = "/discover/v1/floatingpoint/changePassword"
    const val FORGET_PASSWORD = "/Discover/v1/floatingpoint/forgotPassword"
    const val LOG_OUT = "/Discover/v1/floatingpoint/notification/unregisterChannel"
    const val CONNECT_MERCHANT_AUTH_PROVIDER = "/user/v9/floatingPoint/ConnectMerchantAuthProvider"
    const val CHECK_MOBILE_IS_REGISTERED = "/discover/v1/floatingpoint/CheckIfMobileIsRegistered"
    const val GET_FP_DETAILS_BY_PHONE = "/discover/v1/floatingPoint/getfpdetailsbynumber"
    const val SEND_OTP_INDIA = "/discover/v1/floatingpoint/SendOTPIndia"
    const val VERIFY_OTP = "/discover/v1/floatingpoint/VerifyOTP"
    const val FP_LIST_REGISTERED_MOBILE = "/discover/v1/floatingpoint/GetFPListforRegisteredMobile"
    const val GET_FP_DETAILS = "/Discover/v3/floatingPoint/nf-app/{fpid}"
    const val POST_BUSINESS_DOMAIN_URL = "discover/v1/floatingPoint/verifyUniqueTag"
    const val POST_BUSINESS_DOMAIN_SUGGEST = "discover/v1/floatingPoint/suggestTag"
    const val PUT_CREATE_BUSINESS_URL = "discover/v5/FloatingPoint/create"
    const val POST_ACTIVATE_PURCHASED_ORDER = "Payment/v9/floatingpoint/ActivatePurchaseOrder"
}

