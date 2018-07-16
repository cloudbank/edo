package com.droidteahouse.edo.db

import android.arch.persistence.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class Converters {

/*
  @TypeConverter
  fun imagesItemListToString(someObjects: List<ImagesItem>): String {
    val gson = Gson()
    return gson.toJson(someObjects)
  }


  @TypeConverter
  fun stringToItemsList(data: String): List<ImagesItem> {
    if (data == null) {
      return Collections.emptyList()
    }
    val listType = object : TypeToken<List<ImagesItem>>() {
    }.type
    val gson = Gson()
    return gson.fromJson(data, listType)
  }
*/

}