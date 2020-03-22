package com.droidteahouse.edo.vo

import com.google.gson.annotations.SerializedName

data class Image(
        @SerializedName("height") val height: Int = 0,
        @SerializedName("iiifbaseuri") val iiifbaseuri: String = "",
        @SerializedName("baseimageurl") val baseimageurl: String = "",
        @SerializedName("width") val width: Int = 0,
        @SerializedName("publiccaption") val publiccaption: Any = Any(),
        @SerializedName("idsid") val idsid: Int = 0,
        @SerializedName("displayorder") val displayorder: Int = 0,
        @SerializedName("format") val format: String = "",
        @SerializedName("copyright") val copyright: String = "",
        @SerializedName("imageid") val imageid: Int = 0,
        @SerializedName("renditionnumber") val renditionnumber: String = ""
)