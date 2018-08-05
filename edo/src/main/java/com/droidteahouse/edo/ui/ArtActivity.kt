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

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedList
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.SystemClock
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
    var hashVisible = false


    private var mLayoutManager: LinearLayoutManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_art)
        createViews()
        initSwipeToRefresh()
    }

    //@todo refactor and opts
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
                glide, modelProvider, FixedPreloadSizeProvider(55, 55), 10, spIds)
        rvArt?.addOnScrollListener(preloader)
        if (artViewModel.artObjects.value?.size?.compareTo(0) !== 0) { //avoid 0 size onstart PR
            artViewModel.artObjects.observe(this, Observer<PagedList<ArtObject>> {

                //@todo  needs generalization and onsavedinstancestate for reclaim w small list SSOT db
                if ((it?.size?.compareTo(0)!! > 0) and (it.size.compareTo(11) == 0) and !hashVisible) {
                    hashVisible = true
                    modelProvider.hashVisible(it.subList(0, 3), spIds)

                }
                modelProvider.objects = it.toMutableList(

                )
                adapter.submitList(it)


            })
        }
        SystemClock.sleep(400)
        setTheme(R.style.AppTheme)
        rvArt?.smoothScrollToPosition(0)
        artViewModel.networkState.observe(this, Observer
        {
            adapter.setNetworkState(it)
        })


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
        //queue of bytebuffers  companion object to re-use rather than create and GC--multithread access


        //before onscroll
        fun hashVisible(sublist: List<ArtObject>, spIds: SharedPreferences): Unit {
            //has the first 3
            Log.d("MyPreloadModelProvider", "hashVisible" + sublist.size + spIds)
            sublist.sortedBy { it.id }
            for (i in 0 until sublist.size) {
                val art: ArtObject = sublist.get(i)
                spIds.edit().putString(art.id.toString(), "1").commit()
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
                //
                lateinit var bmd: BitmapDrawable
                Log.d("MyPreloadModelProvider", "url:  " + item.url)
                try {
                    //@todo do we need weakref here for target see RequestTracker
                    bmd = requestBuilder.submit().get() as BitmapDrawable
                } catch (e: Exception) {
                    //@todo get timeout down
                    // java.net.SocketTimeoutException(timeout)
                    Log.e("MyPreloadModelProvider", "exception" + e + item.id + ":::" + item.url)
                    // artViewModel.delete(item)
                    return@execute

                }
                val b = bmd.bitmap
                val bb = ByteBuffer.allocateDirect((b.width * b.height) * 4)
                bb.order(ByteOrder.nativeOrder())
                b.copyPixelsToBuffer(bb)
                bb.rewind()
                val ib = bb.asIntBuffer()
                //Preallocate a static pool of direct ByteBuffers at startup,
                var hash = nativeDhash(ib, 9, 8, b.width, b.height)  //getIntField jni

//@todo  threads don't just die in android
                Log.d("MyPreloadModelProvider", "hash" + item.id + " :: " + hash.toString() + ":::" + Util.bitCount(hash))

                if (sp.contains(hash.toString())) {
                    artViewModel.delete(item)
                    Log.d("MyPreloadModelProvider", "****duplicate" + item.id + " :: " + hash.toString())
                } else {
                    sp.edit().putString(hash.toString(), "1").commit()
                }

                Log.d("MyPreloadModelProvider", "Time::: " + (System.nanoTime() - start).toString())
            }


        }


        external fun nativeDhash(b: Buffer, nw: Int, nh: Int, ow: Int, oh: Int): Long

        companion object {

            init {
                System.loadLibrary("native-lib")
            }


        }


    }
}
