package com.droidteahouse.edo.vo

import com.google.gson.annotations.SerializedName

data class SeeAlso(
        @SerializedName("id") val id: String = "",
        @SerializedName("type") val type: String = "",
        @SerializedName("format") val format: String = "",
        @SerializedName("profile") val profile: String = ""
)