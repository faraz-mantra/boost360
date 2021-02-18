package com.appservice.model.account


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PanDetailsN(
    @SerializedName("DocumentContent")
    var documentContent: String? = null,
    @SerializedName("DocumentName")
    var documentName: String? = null,
    @SerializedName("FileType")
    var fileType: String? = null,
    @SerializedName("Name")
    var name: String? = null,
    @SerializedName("Number")
    var number: String? = null
): Serializable