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
import javax.inject.Inject


/**
 * VM
 */

class ArtViewModel @Inject constructor(
        var repository: ArtObjectRepository) : ViewModel() {


    companion object {
        //config change proof
        //@Volatile
        // var bitset = ByteBuffer.allocateDirect(4).asIntBuffer()

        @Volatile
        var bits: Int = 0
        // var bits = bitset.get(0)
/*
        @Synchronized
        fun putInt(i: Int) {
            bitset.put(0, i)

        }

    */    //EdoObjects.info.totalrecords 3562
        //https://www.badlogicgames.com/wordpress/?p=2367
        //
        @Volatile
        var idcache = IntArray((4000 shr 5))

        //var idcache = newDisposableByteBuffer(4 * (4000 shr 5))
        // var idcache = ByteBuffer.allocateDirect(4 * (4000 shr 5)).asIntBuffer()
        //@todo maybe create directbufferprefs class
        //private val rwlock = ReentrantReadWriteLock(), stampedlock, optimistic lock

        fun stashVisible(list: IntArray) {
            //synchronized(idcache) {
            for (i in list) {
                stashId(i)
            }
            //spIds.edit().putString("idcache", ArtViewModel.idcache.contentToString()).commit()

            //}
        }

        fun putIdInCache(id: Int) {
            //synchronized(idcache) {
            stashId(id)
            // spIds.edit().    ("idcache", ArtViewModel.idcache.contentToString()).commit()

            // }

        }

        fun hasId(id: Int): Boolean {
            //spIds.get("idcache")
            return fetchId(id)
            //ActivityManager
        }

        private fun fetchId(id: Int): Boolean {
            val word = id shr 5
            val bit = id and 0x1F   //mod 32
            return (idcache[word] and (1 shl bit)) != 0
        }

        private fun stashId(id: Int): Unit {
            val word = id shr 5
            val bit = id and 0x1F
            idcache[word] = idcache[word] or (1 shl bit)
        }


        //@Volatile
        //var hashcache = ByteBuffer.allocateDirect(4 * (4000 shr 5)).asIntBuffer()
        //@todo maybe create directbufferprefs class
        //private val rwlock = ReentrantReadWriteLock(), stampedlock, optimistic lock

        /*  fun stashVisible(list: IntArray) {
              synchronized(idcache) {
              for (i in list) {
                  stashId(i)
              }
              }
          }

          fun putIdInCache(id: Int) {
              synchronized(idcache) {
              stashId(id)
               }

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
    */
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

/*
    var unsafeBuffers = listOf(idcache, hashcache)
    var allocatedUnsafe = 0
    fun disposeUnsafeByteBuffer(buffer: ByteBuffer) {
        val size = buffer.capacity()
        synchronized(unsafeBuffers) {
            if (!unsafeBuffers.contains(buffer))
                throw IllegalArgumentException("buffer not allocated with newUnsafeByteBuffer or already disposed")
        }
        allocatedUnsafe -= size
        freeMemory(buffer)
    }

    fun isUnsafeByteBuffer(buffer: ByteBuffer): Boolean {
        synchronized(unsafeBuffers) {
            return unsafeBuffers.contains(buffer)
        }
    }

    /** Allocates a new direct ByteBuffer from native heap memory using the native byte order. Needs to be disposed with
     * [.disposeUnsafeByteBuffer].  */
    fun newUnsafeByteBuffer(numBytes: Int): ByteBuffer {
        val buffer = newDisposableByteBuffer(numBytes)
        buffer.order(ByteOrder.nativeOrder())
        allocatedUnsafe += numBytes
        synchronized(unsafeBuffers) {
            unsafeBuffers.plus(buffer)
        }
        return buffer
    }

    /** Frees the memory allocated for the ByteBuffer, which MUST have been allocated via [.newUnsafeByteBuffer]
     * or in native code.  */
    private external fun freeMemory(buffer: ByteBuffer)  /*
		free(buffer);
	 */

    private external fun newDisposableByteBuffer(numBytes: Int): ByteBuffer  /*
		return env->NewDirectByteBuffer((char*)malloc(numBytes), numBytes);
	*/
	*
	* */

}