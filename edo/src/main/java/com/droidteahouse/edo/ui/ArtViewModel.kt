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

package com.droidteahouse.edo.ui

import android.arch.lifecycle.ViewModel
import com.droidteahouse.edo.repository.ArtObjectRepository
import com.droidteahouse.edo.vo.ArtObject
import com.droidteahouse.edo.vo.ImageHash
import javax.inject.Inject

/**
 * VM
 */

class ArtViewModel @Inject constructor(
    var repository: ArtObjectRepository) : ViewModel() {

  val repoResult = repository.getArtObjects()


  var artObjects = repoResult.pagedList
  val networkState = repoResult.networkState
  val refreshState = repoResult.refreshState


  fun refresh() {
    repoResult.refresh.invoke()
  }


  fun retry() {
    repoResult.retry.invoke()
  }

  fun update(item: ArtObject) {
    repository.update(item)


  }
/*
  fun insertHash(item: ImageHash) {
    repository.insertHash(item)


  }


  fun getHash(item: String): Int {
    return repository.getHash(item)

  }
  */

  fun delete(item: ArtObject) {
    //dicey @todo no guarantee on bg thread and not mutable list
    //not sure how this affects adapter, may need to do this in UI
    // repoResult.pagedList.value?.remove(item)
    // artObjects = repoResult.pagedList
    repository.delete(item)
  }

}