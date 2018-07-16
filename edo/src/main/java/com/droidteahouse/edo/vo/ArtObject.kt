package com.droidteahouse.edo.vo

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "artObjects", indices = arrayOf(Index(value = "hash", unique = true)))
data class ArtObject(


        @SerializedName("primaryimageurl") var url: String = "",


        @SerializedName("objectid") var objectid: Int = 0,

        @PrimaryKey
        @SerializedName("id") var id: Int = 0,
        @SerializedName("title") var title: String = "") {

        var page = 0
        //@todo add a deserializer
        //unique default
        var hash: Int = 0
}