package com.droidteahouse.edo.db

import android.arch.persistence.room.TypeConverter
import com.droidteahouse.edo.vo.People
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*


class Converters {

    @TypeConverter
    fun imagesItemListToString(someObjects: List<People>): String {
        val gson = Gson()
        return gson.toJson(someObjects)
    }


    @TypeConverter
    fun stringToItemsList(data: String): List<People> {
        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object : TypeToken<List<People>>() {
        }.type
        val gson = Gson()
        return gson.fromJson(data, listType)
    }



}