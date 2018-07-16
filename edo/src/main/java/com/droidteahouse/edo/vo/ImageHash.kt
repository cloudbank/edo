package com.droidteahouse.edo.vo

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull

//@Entity(tableName = "imageHashes", indices = arrayOf(Index(value = "hash", unique = true)))
data class ImageHash(
    @PrimaryKey
    @NonNull
    var id: String = "",
    var hash: Int = 0)

