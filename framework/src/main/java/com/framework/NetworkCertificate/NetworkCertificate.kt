package com.framework.NetworkCertificate

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import okhttp3.CertificatePinner

object NetworkCertificate {
     fun certificatePinner(): CertificatePinner{
        val jsonString = FirebaseRemoteConfig.getInstance().getString("network_security_config")
        if(jsonString.isNotEmpty()){
            val pinList = Gson().fromJson(jsonString, NetworkCertificateModule::class.java)
            val pinner = CertificatePinner.Builder()
            for(singlePin in pinList) {
                pinner.add(singlePin.domain,"sha256/"+singlePin.pin)
            }
            return pinner.build()
        }
        return CertificatePinner.Builder().build()
    }

}