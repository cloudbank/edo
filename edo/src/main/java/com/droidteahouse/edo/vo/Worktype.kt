package com.droidteahouse.edo.vo

import com.google.gson.annotations.SerializedName

data class Worktype(
        @SerializedName("worktypeid") val worktypeid: String = "",
        @SerializedName("worktype") val worktype: String = ""
)