/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.droidteahouse.edo.db

import android.arch.paging.DataSource
import android.arch.persistence.room.*
import com.droidteahouse.edo.vo.ArtObject

@Dao
interface ArtDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(artItems: List<ArtObject>)

  @Query("SELECT * FROM artObjects ORDER BY page ASC, id")
  fun artObjects(): DataSource.Factory<Int, ArtObject>


  @Query("SELECT MAX(page) + 1 FROM artObjects")
  fun getNextPageInArt(): Int

  @Update
  fun update(item: ArtObject)

  @Delete
  fun delete(item: ArtObject)

/*
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertHash(hashItem: ImageHash)

  @Query("SELECT hash FROM imageHashes WHERE id = :id")
  fun getHash(id: String): Int
*/


}