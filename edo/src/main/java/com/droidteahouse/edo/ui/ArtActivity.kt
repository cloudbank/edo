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
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedList
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DecodeFormat
import com.droidteahouse.edo.*
import com.droidteahouse.edo.repository.NetworkState
import com.droidteahouse.edo.vo.ArtObject
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_art.*
import java.util.*
import java.util.concurrent.Executor
import javax.inject.Inject


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
    lateinit var ioExecutor: Executor


    private var mLayoutManager: LinearLayoutManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_art)
        createViews()
        initSwipeToRefresh()
        //wave hands while we look for duplicates

        //initSearch()
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
        val modelProvider = ArtActivity.MyPreloadModelProvider(this, artViewModel, ioExecutor)
        val sizeProvider = FixedPreloadSizeProvider<ArtObject>(100, 100)//@todo seems better than async cb????
        val preloader = RecyclerViewPreloader(
                glide, modelProvider, sizeProvider, 10)
        rvArt?.addOnScrollListener(preloader)
        artViewModel.artObjects.observe(this, Observer<PagedList<ArtObject>> {
            // if (it?.size!! > 0) {  //into STARTED w out data  bugfix idea for STARTED && list.size > 0
            //do we need to have these here or can we get away without to debug
            modelProvider.objects = it?.toMutableList()
            adapter.submitList(it)
            // }
        })
        //check top of page 1 as well, and check pages  1 w add
        //progress overlay for each new page? or just page 0
        rvArt?.smoothScrollToPosition(10)
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

        //rvArt?.smoothScrollToPosition(0)
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

    class MyPreloadModelProvider(val context: Context, val artViewModel: ArtViewModel, val ioExecutor: Executor) : ListPreloaderHasher.PreloadModelProvider<ArtObject> {

        //set objects
        var objects: MutableList<ArtObject>? = mutableListOf()


        override fun getExecutor(): Executor {
            return ioExecutor
        }

        override fun getViewModel(): ViewModel {
            return artViewModel
        }

        override fun getPreloadItems(position: Int): MutableList<ArtObject> {
            Log.d("MyPreloadModelProvider", "getPreloadItems" + objects?.size)
            if (objects?.isEmpty()!! || position >= objects?.size!!) {
                return mutableListOf()
            } else {
                return Collections.singletonList(objects?.get(position))
            }
        }

        //
        //@todo@WorkerThread  and generalize the type here for item
        override fun hashImage(requestBuilder: RequestBuilder<Any>, item: ArtObject) {
            // if ((item.id.equals("18731719") || item.id.equals("18731721")) || item.id.equals("18731723")) {
            //can we bulk update
            //@todo try with first 3 images
            ioExecutor.execute {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
                val bmd = requestBuilder.submit(9, 8).get() as BitmapDrawable
                val b = bmd.bitmap
                val pix = IntArray(72)
                b.getPixels(pix, 0, 9, 0, 0, 9, 8)
                val bmpGrayscale = Bitmap.createBitmap(9, 8, Bitmap.Config.ARGB_8888)
                val c = Canvas(bmpGrayscale)
                val paint = Paint()
                val cm = ColorMatrix()
                cm.setSaturation(0.0f)
                val f = ColorMatrixColorFilter(cm)
                paint.colorFilter = f
                c.drawBitmap(b, 0.0f, 0.0f, paint)
                bmpGrayscale.getPixels(pix, 0, 9, 0, 0, 9, 8)
                item.hash = dhash(pix)
                //this is the first chance to get the cached image but also, happens with scroll
                Log.d("MyPreloadModelProvider", "hashImages" + item.id + " :: " + dhash(pix))
                try {
                    artViewModel.update(item)
                    //new table for hashes and ids
                    // artViewModel.insertHash(ImageHash(item.id, hash))  // dont update the pagedlist again and aagin
                    //catch (e: Exception) {//android.database.sqlite.SQLiteConstraintException
                } catch (e: Exception) {
                    Log.d("MyPreloadModelProvider", "hashImages found duplicate" + item.id + e)
                    //datasource updates the pagedlist in time for now
                    artViewModel.delete(item)
                }
            }


        }
        //does a row hash only instead of row | col hash.  32 bits is plenty for 3K images not to collide
        //and it is faster for Android needs....will it give false positives

        private fun dhash(pixels: IntArray): Int {
            var width = 9
            var height = 8
            var hash = 0

            for (pixelOffset in 0 until width * height) {
                if ((pixelOffset + 1) % width == 0) {
                    // don't calculate the current end of row compared to the beginning of the next row
                    continue
                }
                var bit = (pixels[pixelOffset].asColorUInt() < pixels[pixelOffset + 1].asColorUInt()).toInt()
                hash = hash shl 1 or bit
            }

            return hash
        }

        inline fun Boolean.toInt() = if (this) 1 else 0
        inline fun Int.asColorUInt() = this and 0xff


        override fun getPreloadRequestBuilder(art: ArtObject): GlideRequest<Drawable> {
            //Log.d("MyPreloadModelProvider", "getPreloadRequestBuilder")
            return GlideApp.with(context).load(art?.url)
                    .format(DecodeFormat.PREFER_ARGB_8888).centerCrop()


        }


        external fun stringFromJNI(): String
        external fun hashFromJNI(): Int

        companion object {

            // Used to load the 'native-lib' library on application startup.
            init {
                System.loadLibrary("native-lib")
            }


        }


    }
}
