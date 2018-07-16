package com.droidteahouse.edo.vo

import com.google.gson.annotations.SerializedName

data class Info(
        @SerializedName("totalrecordsperquery") val totalrecordsperquery: Int = 0,
        @SerializedName("totalrecords") val totalrecords: Int = 0,
        @SerializedName("pages") val pages: Int = 0,
        @SerializedName("page") val page: Int = 0,
        @SerializedName("next") val next: String = ""
)