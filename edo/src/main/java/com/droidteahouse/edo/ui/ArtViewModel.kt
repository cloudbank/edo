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
import android.os.Build
import com.droidteahouse.edo.repository.ArtObjectRepository
import com.droidteahouse.edo.vo.ArtObject
import java.nio.ByteBuffer
import java.util.*
import javax.inject.Inject

/**
 * VM
 */

class ArtViewModel @Inject constructor(
        var repository: ArtObjectRepository) : ViewModel() {

    companion object {
        //config change proof
        @Volatile
        var bitset = ByteBuffer.allocateDirect(4).asIntBuffer()
        @Volatile
        var bits = bitset.get(0)

        @Synchronized
        fun putInt(i: Int) {
            bitset.put(0, i)

        }

        //Bitset v int[] v boolean[] v byte[]
        //I could get this from total records
        //EdoObjects.info.totalrecords
        //4000 items roughly in this canned search  --make bitset and hide it in direct buffer
        @Volatile
        // public var ids = IntArray(4000 shr 5)
        var idcache = ByteBuffer.allocateDirect(4 * (4000 shr 5)).asIntBuffer()
        //@todo maybe create directbufferprefs class
        //private val rwlock = ReentrantReadWriteLock(), stampedlock, optimistic lock

        fun stashVisible(list: IntArray) {
            //synchronized(idcache) {
            for (i in list) {
                stashId(i)
            }
            //}
        }

        fun putIdInCache(id: Int) {

            //synchronized(idcache) {
            stashId(id)
            // }

        }

        fun hasId(id: Int): Boolean {
            return fetchId(id)
        }

        private fun fetchId(id: Int): Boolean {
            val word = id shr 5
            val bit = id and 0x1F   //mod 32
            return (idcache[word] and (1 shl bit)) != 0
        }

        private fun stashId(id: Int): Unit {
            val word = id shr 5
            val bit = id and 0x1F
            idcache.put(word, idcache[word] or (1 shl bit))
        }


        //3562 diff hashes if all unique
        //EdoObjects.info.totalrecords
        @Volatile
        var hashes = BitSet(4000)
        var hashCache = ByteBuffer.allocateDirect(4000 shr 3)
////
////1) parcelable bitset, IntArray
//// 2) gson complex object
/////3)


        fun getHashesFromBB(): BitSet {

            hashCache.rewind()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                return BitSet.valueOf(hashCache)
            } else {
                val b = BitSet()
                while (hashCache.hasRemaining()) {
                    b.set(hashCache.int)
                }
                return b
            }

        }


        fun setHashesInBB() {
            hashCache.clear()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                hashCache.put(hashes.toByteArray())
            } else {
                val ba = ByteArray(4000 shr 3)
                var index = 0
                var i = 0
                while (i >= 0) {
                    ba[index++] = hashes.nextSetBit(i++).toByte()

                }
                hashCache.put(ba)
            }
        }


    }


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


    fun delete(item: ArtObject) {
        //dicey @todo no guarantee on bg thread and not mutable list
        //not sure how this affects adapter, may need to do this in UI
        // repoResult.pagedList.value?.remove(item)
        // artObjects = repoResult.pagedList
        repository.delete(item)
    }


}