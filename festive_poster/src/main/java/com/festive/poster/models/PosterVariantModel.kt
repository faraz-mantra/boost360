package com.festive.poster.models

import com.google.gson.annotations.SerializedName

data class PosterVariantModel(
    @SerializedName("active")
    val active: Boolean?,
    @SerializedName("svgUrl")
    var svgUrl: String?,
    @SerializedName("name")
    val name: String?
)