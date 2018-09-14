package com.droidteahouse.edo.preload


import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.ArrayMap
import android.util.Log
import com.bumptech.glide.RequestBuilder
import com.droidteahouse.edo.GlideApp
import com.droidteahouse.edo.GlideRequest
import com.droidteahouse.edo.preload.ListPreloaderHasher.PreloadModelProvider
import com.droidteahouse.edo.ui.ArtViewModel
import com.droidteahouse.edo.util.Util
import com.droidteahouse.edo.vo.ArtObject
import io.paperdb.Paper
import kotlinx.coroutines.experimental.*
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyPreloadModelProvider<T> @Inject constructor(var context: Context, var artViewModel: ArtViewModel) : PreloadModelProvider<T> {
    var objects: MutableList<ArtObject>? = mutableListOf()
    //val counterContext = newSingleThreadContext("CounterContext")


    external fun nativeDhash(b: Buffer, nw: Int, nh: Int, ow: Int, oh: Int): Long

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override suspend fun check(id: Int, item: ArtObject, preloadRequestBuilder: RequestBuilder<Any>) {

        if (!Cache.hasId(id)) {
            Log.d("HASHER", "MyPreloadModelProvider id not in cache:::" + id)
            Cache.putIdInCache(id)
            hashImage(preloadRequestBuilder, item)
        }

    }

    //singleton
    object Cache {
        val companionContext = newSingleThreadContext("stc")


        @Volatile
        var idcache = IntArray((4000 shr 5))   //125

        fun stashVisible(list: IntArray) {
            GlobalScope.launch(companionContext, CoroutineStart.DEFAULT, null, {
                for (i in list) {
                    stashId(i)
                }
            })
            GlobalScope.launch(companionContext, CoroutineStart.DEFAULT, null, {
                Paper.book().write("ids", idcache)
            })


        }

        suspend fun putIdInCache(id: Int) {

            // if (idcache.isEmpty()) {
            //     idcache = getIdCache()
            // }
            //@todo want these in parallel
            GlobalScope.launch(companionContext, CoroutineStart.DEFAULT, null, {
                stashId(id)
            })
            GlobalScope.launch(companionContext, CoroutineStart.DEFAULT, null, {
                Paper.book().write("ids", idcache)
            })

        }

        fun hasId(id: Int): Boolean {
            //  if (idcache.isEmpty()) {
            //     idcache = getIdCache()
            //  }
            return fetchId(id)
        }

        suspend fun getIdCache(): IntArray {
            return GlobalScope.async(companionContext, CoroutineStart.DEFAULT, null, { Paper.book().read("ids", (IntArray(4000 shr 5))) }).await()
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

        ///--------------------hashes
        @RequiresApi(Build.VERSION_CODES.KITKAT)
        //@todo what for 14-18
        @Volatile
        var hashcache = ArrayMap<Int, HashSet<Long>>(4000 shr 5)

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        //@todo what for 14-18
        fun getHashCache(): ArrayMap<Int, HashSet<Long>> {
            return Paper.book().read("hashes", (ArrayMap<Int, HashSet<Long>>(4000 shr 5)))
        }

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        fun putHashInCache(bc: Int, hash: Long) {
            //@todo want these in parallel
            //  if (hashcache.isEmpty()) {
            //     hashcache = getHashCache()
            // }//how to make it resume in this
            // launch(companionContext) {
            stashHash(bc, hash)
            //}
            //launch(companionContext) {
            Paper.book().write("hashes", hashcache)
            //}
        }


        @RequiresApi(Build.VERSION_CODES.KITKAT)
        fun hasHash(bc: Int, hash: Long): Boolean {
            checkHashCache()
            return fetchHash(bc, hash)
        }

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        fun checkHashCache(): Unit {
            if (hashcache.isEmpty()) {
                //  launch(companionContext) {
                hashcache = Paper.book().read("hashes", (ArrayMap<Int, HashSet<Long>>(4000 shr 5)))
            }
            // }
        }


        private fun fetchHash(bc: Int, hash: Long): Boolean {
            var result: Boolean = false
            if (!hashcache.contains(bc)) {
                result = false
            } else if (hashcache.get(bc) != null && hashcache.get(bc)!!.contains(hash)) {
                result = true
            }
            return result
        }

        private fun stashHash(bc: Int, hash: Long): Unit {
            val list = hashcache.get(bc) ?: hashSetOf<Long>()
            list.add(hash)
            hashcache.put(bc, list)
        }


        init {
            System.loadLibrary("native-lib")
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
//called from bg thread in activity
    suspend fun hashVisible(sublist: List<ArtObject>): Unit {
        //has the first 3
        MyPreloadModelProvider.Cache.stashVisible(sublist.map { it.id }.toIntArray())

        Log.d("MyPreloadModelProvider", "hashVisible" + sublist.size + MyPreloadModelProvider.Cache.idcache)
        //sublist.sortedBy { it.id }
        for (i in 0 until sublist.size) {
            val art = sublist.get(i)
            Log.d("MyPreloadModelProvider", "hashVisible::::" + art.id)
            val rb = getPreloadRequestBuilder(art as T) as RequestBuilder<Any>
            hashImage(rb, art)
        }
    }

    override fun getPreloadItems(position: Int): MutableList<T> {
        if (objects?.isEmpty()!! || position >= objects?.size!!) {
            return mutableListOf()
        } else {
            return Collections.singletonList((objects?.get(position) as T))
        }
    }

    override fun getPreloadRequestBuilder(art: T): GlideRequest<Drawable> {
        art as ArtObject
        return GlideApp.with(context).load(art.url).centerCrop()
    }

    //called from bg thread in either listhasher or activity
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override suspend fun hashImage(requestBuilder: RequestBuilder<Any>, item: ArtObject) {
        // val job = launch(stcContext) {
        val start = System.nanoTime()
        try {
            //@todo do we need weakref here for target see RequestTracker, not include in cache as bitmap
            var bmd = requestBuilder.submit().get() as BitmapDrawable

            var b = bmd.bitmap
            //bitmap pool?  work on freeing memory here
            val width = b.width
            val height = b.height

            var bb = ByteBuffer.allocateDirect((width * height) * 4)
            bb.order(ByteOrder.nativeOrder())
            b.copyPixelsToBuffer(bb)
            bb.rewind()
            var ib = bb.asIntBuffer()
            var hash = nativeDhash(ib, 9, 8, width, height)
            hash = hash and 0xFFFFFFFF
            val bc = Util.bitCount(hash)
            bb = null
            ib = null

            Log.d("MyPreloadModelProvider", "hash" + item.objectid + "***" + item.id + " :: " + hash)
            if (Cache.hasHash(bc, hash)) {
                artViewModel.delete(item)
                Log.d("MyPreloadModelProvider", "****found exact duplicate" + item.id + " :: " + hash.toString() + ";;" + item.objectid)
            } else if (nearDuplicate(bc, hash)) {
                artViewModel.delete(item)
                Log.d("MyPreloadModelProvider", "****found near duplicate" + item.id + " :: " + hash.toString() + ";;" + item.objectid)
            }
            setBitsAndHash(bc, hash)
            Log.d("MyPreloadModelProvider", "hash" + item.objectid + "***" + item.id + " :: " + hash.toString() + ":::time::" + (System.nanoTime() - start).div(1_000_000_000F).toFloat().toString() + "--" + bc.toString())
        } catch (e: Exception) {
            // java.net.SocketTimeoutException(timeout)
            Log.e("MyPreloadModelProvider", "exception" + e + item.id + ":::")
            // artViewModel.delete(item)
            Cache.companionContext.cancel()


        }
        // }

    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    suspend fun setBitsAndHash(bc: Int, hash: Long) {
        // MyPreloadModelProvider.putBits(1 shl (bc - 1))
        Cache.putHashInCache(bc, hash)
    }

    fun nearDuplicate(bc: Int, hash: Long) =
    // ((bits and (1 shl (bc - 1)) == (1 shl (bc - 1))) || (bits and (1 shl (bc)) == (1 shl (bc))) || ((bc > 1) && (bits and (1 shl (bc - 2)) == (1 shl (bc - 2))))) &&
            (withinThree(hash, bc))


    fun withinThree(hash: Long, bc: Int): Boolean {
        val s = Cache.hashcache.get(bc) ?: hashSetOf<Long>()
        s.addAll(Cache.hashcache.get(bc - 1).orEmpty())
        s.addAll(Cache.hashcache.get(bc + 1).orEmpty())

        for (st in s) {
            if ((Util.bitCount(hash xor st)) <= 3) {
                return true
            }
        }

        return false
    }


    //@todo try without nsative again


    fun cleanUp() {
        Cache.companionContext.close()
    }
}



