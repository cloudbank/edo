package com.droidteahouse.edo.ui

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


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedList
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.bumptech.glide.RequestBuilder
import com.droidteahouse.edo.*
import com.droidteahouse.edo.repository.NetworkState
import com.droidteahouse.edo.util.Util
import com.droidteahouse.edo.vo.ArtObject
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_art.*
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import java.util.concurrent.ExecutorService
import javax.inject.Inject
import javax.inject.Named


/**
 *
 */
class ArtActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val artViewModel: ArtViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)[ArtViewModel::class.java]
    }
    @Inject
    lateinit var modelProvider: ArtActivity.MyPreloadModelProvider<ArtObject>
    @Inject
    @field:Named("ids")
    lateinit var spIds: SharedPreferences


    private var mLayoutManager: LinearLayoutManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_art)
        createViews()
        initSwipeToRefresh()
    }


    private fun createViews() {
        toolbar.setTitleTextColor(resources.getColor(R.color.colorPrimary))
        setSupportActionBar(toolbar)
        supportActionBar?.title = "e d o"

        val glide = GlideApp.with(this)
        val adapter = ArtObjectAdapter(glide) {
            artViewModel.retry()
        }
        //@todo rv opts, gridlayout with paging and preload
        rvArt.adapter = adapter
        configRV()

        var preloader = RecyclerViewPreloader(
                glide, modelProvider, FixedPreloadSizeProvider(55, 55), 10)

        rvArt?.addOnScrollListener(preloader)
        if (artViewModel.artObjects.value?.size?.compareTo(0) !== 0) { //avoid 0 size onstart PR
            artViewModel.artObjects.observe(this, Observer<PagedList<ArtObject>> {

                //@todo  needs generalization and onsavedinstancestate for reclaim w small list SSOT db
                if (it?.size?.compareTo(0)!! > 0) {

                    if (spIds.getString("hashVisible", "").equals("")) {
                        spIds.edit().putString("hashVisible", "true").commit()

                        ArtViewModel.stashVisible((it.subList(0, 4).map { it.id }.toIntArray()))
                        modelProvider.hashVisible(it.subList(0, 4))

                        //@todo try on real device to tweak this modelProvider . hashVisible (it.subList(0, 4))

                        //SystemClock.sleep(4000)
                        setTheme(R.style.AppTheme)

                    }
                    adapter.submitList(it)
                    modelProvider.objects = it.toMutableList()

                }
            })
        }

        rvArt?.smoothScrollToPosition(0)
        artViewModel.networkState.observe(this, Observer
        {
            adapter.setNetworkState(it)
        })


    }


    override fun onStart() {
        super.onStart()
        if (spIds.contains("bitset") && ArtViewModel.bits == 0) {
            ArtViewModel.bitset.put(0, spIds.getInt("bitset", 0))
        }
    }


    //  onStop is better but I moved it here to help w data loss on forced exits
    override fun onStop() {
        super.onStop()
        //@todo persist  via protobuf

        spIds.edit().putInt("bitset", ArtViewModel.bits).commit()

    }

    private fun configRV() {
        rvArt?.setHasFixedSize(true)
        rvArt?.isDrawingCacheEnabled = true
        rvArt?.setItemViewCacheSize(9)
        rvArt?.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvArt?.layoutManager = mLayoutManager

        val itemDecor = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        itemDecor.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider)!!)
        val addItemDecoration = rvArt?.addItemDecoration(itemDecor)

    }

    private fun initSwipeToRefresh() {
        artViewModel.refreshState.observe(this, Observer {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener {
            artViewModel.refresh()
        }
    }


    //setOnFlingListener
    class MyPreloadModelProvider<T> @Inject constructor(var context: Context, var artViewModel: ArtViewModel, @Named("hashExec") var executor: ExecutorService, @Named("hashes") var sp: SharedPreferences) : ListPreloaderHasher.PreloadModelProvider<ArtObject> {
        var objects: MutableList<ArtObject>? = mutableListOf()
        //@todo garbage free vs SP

        //before onscroll
        fun hashVisible(sublist: List<ArtObject>): Unit {
            //has the first 3
            Log.d("MyPreloadModelProvider", "hashVisible" + sublist.size + ArtViewModel.Companion.idcache)
            //sublist.sortedBy { it.id }
            for (i in 0 until sublist.size) {
                val art: ArtObject = sublist.get(i)
                //spIds.edit().putString(art.id.toString(), "1").commit()
                //done in stash visible
                Log.d("MyPreloadModelProvider", "hashVisible::::" + art.id)
                val rb = getPreloadRequestBuilder(art) as RequestBuilder<Any>
                hashImage(rb, art)
            }
        }

        override fun getPreloadItems(position: Int): MutableList<ArtObject> {
            if (objects?.isEmpty()!! || position >= objects?.size!!) {
                return mutableListOf()
            } else {
                return Collections.singletonList(objects?.get(position))
            }
        }

        override fun getPreloadRequestBuilder(art: ArtObject): GlideRequest<Drawable> {
            return GlideApp.with(context).load(art.url).centerCrop()
        }


        override fun hashImage(requestBuilder: RequestBuilder<Any>, item: ArtObject) {
            executor.execute {
                val start = System.nanoTime()
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)

                lateinit var bmd: BitmapDrawable
                Log.d("MyPreloadModelProvider", "url:  " + item.url)
                try {
                    //@todo do we need weakref here for target see RequestTracker, not include in cache as bitmap
                    bmd = requestBuilder.submit().get() as BitmapDrawable
                } catch (e: Exception) {
                    //@todo get timeout
                    // java.net.SocketTimeoutException(timeout)
                    Log.e("MyPreloadModelProvider", "exception" + e + item.id + ":::" + item.url)
                    // artViewModel.delete(item)
                    return@execute
                }
                var b = bmd.bitmap
                //bitmap pool?  work on freeing memory here
                val width = b.width
                val height = b.height
                var bb = ByteBuffer.allocateDirect((width * height) * 4)
                bb.order(ByteOrder.nativeOrder())
                b.copyPixelsToBuffer(bb)
                //@todo
                /*try {
                    if (b != null && !b.isRecycled) {
                        b.setPixels(null, 0, 0, 0, 0, 0, 0)
                        b.recycle()
                    }
                } catch (e: Exception) {
                    Log.e("MyPreloadModelProvider", "exception" + e + item.id + ":::" + item.url)
                }*/

                bb.rewind()
                var ib = bb.asIntBuffer()
                var hash = (nativeDhash(ib, 9, 8, width, height))
//big endian from little JNI automatic? overflow  BigInteger
                hash = hash and 0xFFFFFFFF
                //timestamp that it returns
                val bc = Util.bitCount(hash)
                // ArtViewModel.bitset.put(0, (ArtViewModel.bitset[0] or (1 shl (bc - 1))))
                bb = null
                ib = null
                // --->8DIR100
                //is this enough of a lock
                synchronized(this) {

                    if (sp.contains(hash.toString())) {
                        artViewModel.delete(item)
                        Log.d("MyPreloadModelProvider", "****exact duplicate" + item.id + " :: " + hash.toString())
                        ArtViewModel.bits = ArtViewModel.bits or (1 shl (bc - 1))
                        ArtViewModel.bitset.put(0, ArtViewModel.bits)
                        sp.edit().putString(hash.toString(), "1").commit()
                        val s = sp.getStringSet(bc.toString(), mutableSetOf())
                        sp.edit().putStringSet(bc.toString(), s.plus(hash.toString())).commit()

                    } else if (((ArtViewModel.bits and (1 shl (bc - 1)) == (1 shl (bc - 1))) || (ArtViewModel.bits and (1 shl (bc)) == (1 shl (bc))) || ((bc > 1) && (ArtViewModel.bits and (1 shl (bc - 2)) == (1 shl (bc - 2))))) &&
                            (withinThree(hash, bc, sp))) {
                        artViewModel.delete(item)
                        Log.d("MyPreloadModelProvider", "****non exact duplicate" + item.id + " :: " + hash.toString())
                        ArtViewModel.bits = ArtViewModel.bits or (1 shl (bc - 1))
                        ArtViewModel.bitset.put(0, ArtViewModel.bits)
                        sp.edit().putString(hash.toString(), "1").commit()
                        val s = sp.getStringSet(bc.toString(), mutableSetOf())
                        sp.edit().putStringSet(bc.toString(), s.plus(hash.toString())).commit()

                    } else {

                        sp.edit().putString(hash.toString(), "1").commit()
                        ArtViewModel.bits = ArtViewModel.bits or (1 shl (bc - 1))
                        ArtViewModel.bitset.put(0, ArtViewModel.bits)
                        val s = sp.getStringSet(bc.toString(), mutableSetOf())
                        sp.edit().putStringSet(bc.toString(), s.plus(hash.toString())).commit()
                        //return@execute
                        //make volatile memcache of hashes as well
                    }
                }


                //  Log.d("MyPreloadModelProvider", "Time::: " + )
                //for crashes and force close but slows me down some more
                //@todo detect a force close or global exception
                // sp.edit().putInt("bitset", ArtViewModel.bitset[0]).commit()

                Log.d("MyPreloadModelProvider", "hash" + item.id + " :: " + hash.toString() + ":::time::" + (System.nanoTime() - start).toString() + "--" + bc.toString())
            }

        }


        //@todo shutdown threadpool when reached last image


        fun withinThree(hash: Long, bc: Int, sp: SharedPreferences): Boolean {
            //Util  bitCounts into oh arraylist and check for this in native code
            var s = sp.getStringSet(bc.toString(), mutableSetOf())
            s.addAll(sp.getStringSet((bc + 1).toString(), mutableSetOf()))
            s.addAll(sp.getStringSet((bc - 1).toString(), mutableSetOf()))
            for (st in s) {
                if (Util.bitCount(hash xor java.lang.Long.parseLong(st)) <= 3) {
                    return true
                }
            }
            return false
        }

        external fun nativeDhash(b: Buffer, nw: Int, nh: Int, ow: Int, oh: Int): Long

        companion object {

            init {
                System.loadLibrary("native-lib")
            }

        }

    }
}
