package com.droidteahouse.edo.vo

import com.google.gson.annotations.SerializedName

data class Color(
        @SerializedName("percent") val percent: Double = 0.0,
        @SerializedName("spectrum") val spectrum: String = "",
        @SerializedName("color") val color: String = "",
        @SerializedName("css3") val css3: String = "",
        @SerializedName("hue") val hue: String = ""
)