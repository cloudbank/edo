package com.droidteahouse.edo.vo

import com.google.gson.annotations.SerializedName

data class People(
        @SerializedName("alphasort") val alphasort: String = "",
        @SerializedName("birthplace") val birthplace: Any = Any(),
        @SerializedName("name") val name: String = "",
        @SerializedName("prefix") val prefix: String = "",
        @SerializedName("personid") val personid: Int = 0,
        @SerializedName("gender") val gender: String = "",
        @SerializedName("role") val role: String = "",
        @SerializedName("displayorder") val displayorder: Int = 0,
        @SerializedName("culture") val culture: String = "",
        @SerializedName("displaydate") val displaydate: Any = Any(),
        @SerializedName("deathplace") val deathplace: Any = Any(),
        @SerializedName("displayname") val displayname: String = ""
)