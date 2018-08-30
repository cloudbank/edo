package com.droidteahouse.edo.vo

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import com.droidteahouse.edo.db.Converters
import com.google.gson.annotations.SerializedName

@Entity(tableName = "artObjects") //indices = arrayOf(Index(value = "hash", unique = true)))
data class ArtObject(


        @SerializedName("primaryimageurl") var url: String = "",


        @SerializedName("objectid") var objectid: Int = 0,

        @PrimaryKey(autoGenerate = true)
        @Transient
        @ColumnInfo(name = "id")
        var id: Int = 0,

        @TypeConverters(Converters::class)
        @SerializedName("people") var people: List<People> = listOf(),
        @SerializedName("title") var title: String = "") {

    var page = 0

    //@todo add a deserializer
    //unique default
    // var hash: Int = 0
}