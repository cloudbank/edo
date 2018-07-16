package com.droidteahouse.edo.vo

import com.google.gson.annotations.SerializedName

data class EdoObjects(
        @SerializedName("info") val info: Info = Info(),
        @SerializedName("records") val records: List<ArtObject> = listOf()
)