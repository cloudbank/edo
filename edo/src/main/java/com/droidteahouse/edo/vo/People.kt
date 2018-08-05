package com.droidteahouse.edo.vo

import com.google.gson.annotations.SerializedName

data class People(
        @SerializedName("name") var name: String = ""
)