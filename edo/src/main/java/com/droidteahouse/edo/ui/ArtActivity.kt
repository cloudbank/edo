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
import com.droidteahouse.edo.*
import com.droidteahouse.edo.repository.NetworkState
import com.droidteahouse.edo.vo.ArtObject
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_art.*
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

        // val modelProvider = ArtActivity.MyPreloadModelProvider(this, artViewModel, executor, sp)
        var preloader = RecyclerViewPreloader(
                glide, modelProvider, FixedPreloadSizeProvider(75, 75), 10, spIds)
        rvArt?.addOnScrollListener(preloader)
        artViewModel.artObjects.observe(this, Observer<PagedList<ArtObject>> {
            // if (it?.size!! > 0) {  //into STARTED w out data  bugfix idea for STARTED && list.size > 0
            //do we need to have these here or can we get away without to debug
            if ((it?.size?.compareTo(0)!! > 0) and (it.size.compareTo(10) == 0) && !hashVisible) {
                hashVisible = true
                modelProvider.hashVisible(it.subList(0, 3), spIds)
            }
            modelProvider.objects = it.toMutableList(

            )
            adapter.submitList(it)


        })
        //check top of page 1 as well, and check pages  1 w add

        artViewModel.networkState.observe(this, Observer
        {
            adapter.setNetworkState(it)
        })

        //rvArt?.smoothScrollToPosition(0)


    }

    private fun configRV() {
        rvArt?.setHasFixedSize(true)
        rvArt?.isDrawingCacheEnabled = true
        rvArt?.setItemViewCacheSize(9)
        rvArt?.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvArt?.layoutManager = mLayoutManager

        //
        //  rvArt?.smoothScrollToPosition(0)
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

        //set objects
        var objects: MutableList<ArtObject>? = mutableListOf()
        //2 level cache  vs db vs SP

        //before onscroll
        fun hashVisible(sublist: List<ArtObject>, spIds: SharedPreferences): Unit {
            //has the first 3
            Log.d("MyPreloadModelProvider", "hashVisible" + sublist.size + spIds)
            sublist.sortedBy { it.id }
            for (i in 0 until sublist.size) {
                val art: ArtObject = sublist.get(i)
                spIds.edit().putString(art.id.toString(), "1").commit()
                val rb = getPreloadRequestBuilder(art) as RequestBuilder<Any>
                hashImage(rb, art)
            }


        }


        override fun getPreloadItems(position: Int): MutableList<ArtObject> {
            Log.d("MyPreloadModelProvider", "getPreloadItems" + objects?.size)
            //hashAndPreload(objects)
            //need to get a range that works
            if (objects?.isEmpty()!! || position >= objects?.size!!) {
                return mutableListOf()
            } else {
                return Collections.singletonList(objects?.get(position))
            }
        }


        override fun getPreloadRequestBuilder(art: ArtObject): GlideRequest<Drawable> {
            // don't calculate the current end of row compared to the beginning of the next row    //Log.d("MyPreloadModelProvider", "getPreloadRequestBuilder")
            return GlideApp.with(context).load(art.url).centerCrop()
        }


        override fun hashImage(requestBuilder: RequestBuilder<Any>, item: ArtObject) {
            executor.execute {
                val start = System.nanoTime()
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
                //
                lateinit var bmd: BitmapDrawable
                //can I guarantee it is in the cache

                try {
                    bmd = requestBuilder.submit(9, 8).get() as BitmapDrawable
                } catch (e: Exception) {
                    //@todo get timeout down
                    // java.net.SocketTimeoutException(timeout)
                    Log.e("MyPreloadModelProvider", "exception" + e + item.id)
                    artViewModel.delete(item)
                    return@execute

                }
                val b = bmd.bitmap
                val bmpGrayscale = Bitmap.createBitmap(9, 8, Bitmap.Config.ARGB_8888)
                val paint = Paint()
                val cm = ColorMatrix()
                cm.setSaturation(0.0f)
                val f = ColorMatrixColorFilter(cm)
                paint.colorFilter = f
                val c = Canvas(bmpGrayscale)
                c.drawBitmap(b, 0.0f, 0.0f, paint)
                val pix = IntArray(72)
                bmpGrayscale.getPixels(pix, 0, 9, 0, 0, 9, 8)
                // val dbb = ByteBuffer.allocateDirect(72 * 4)

                // val sm = SharedMemory.create("pix", 4 * 64)
                //direct buffer into native copyPixelsToBuffer
                // val dbb = sm.map((OsConstants.PROT_READ or OsConstants.PROT_WRITE or OsConstants.PROT_EXEC), 0, 64 * 4)   //gray scale pixels, as signed ints, 2^8=256 possible tones of gray for each channel
                //bmpGrayscale.copyPixelsToBuffer(dbb)
                // Log.d("MyPreloadModelProvider", "direct: " + dbb.isDirect)
                val hash = dhash1(pix)
                //SharedMemory.unmap(dbb)
//@todo  threads don't just die in android
                //clean(dbb)
                Log.d("MyPreloadModelProvider", "hash" + item.id + " :: " + hash.toString())

                if (sp.contains(hash.toString())) {
                    artViewModel.delete(item)
                    Log.d("MyPreloadModelProvider", "****duplicate" + item.id + " :: " + hash)
                } else {
                    sp.edit().putString(hash.toString(), "1").commit()
                }


                /*
               try {
                  // artViewModel.update(item)
                   //new table for hashes and ids
                   // artViewModel.insertHash(ImageHash(item.id, hash))  // dont update the pagedlist again and aagin
                   //catch (e: Exception) {//android.database.sqlite.SQLiteConstraintException
               } catch (e: Exception) {
                   Log.d("MyPreloadModelProvider", "hashImages found duplicate" + item.id + e)
                   //datasource updates the pagedlist in time for now
                   artViewModel.delete(item)
               }
               */
                Log.d("MyPreloadModelProvider", "Time::: " + (System.nanoTime() - start).toString())
            }


        }

/*
        // 01 12 23 34 45 56 67 78 89 1011 1213 1415    1617 1819 2021 2223    24-31    32-39 40-47   48-55  56-633
        //skip 2 save 2 skip two save two compare
        private fun dhash(bb: ByteBuffer): Long {
            var hash: Long = 0
            bb.rewind()
            val ib = bb.asIntBuffer()
            //ib.flip()
            //72
            //also could get into byte[] 0-71
            while (ib.hasRemaining()) {
                val i = ib.position()

                if ((i + 1) % 9 == 0) {
                    continue;
                }

                var bit = (ib.get(i).compareTo(ib.get(i + 1))).compareToInt()
                hash = hash shl 1 or bit
            }

            return hash
        }


        inline fun Int.compareToInt() = if (this < 0) 1L else 0L

        inline fun Int.asColorUInt() = this and 0xff
*/

        //with 72 pixel array
        private fun dhash1(pixels: IntArray): Int {
            var width = 9
            var height = 8
            var hash = 0

            for (pixelOffset in 0 until width * height) {
                if ((pixelOffset + 1) % width == 0) {
                    continue
                }

                var bit = (pixels[pixelOffset].asColorUInt() < pixels[pixelOffset + 1].asColorUInt()).toInt()
                hash = hash shl 1 or bit
            }


            return hash
        }


        inline fun Boolean.toInt() = if (this) 1 else 0

        inline fun Int.asColorUInt() = this and 0xff

/*
        external fun dhash(array: IntArray): Int


              companion object {

          // Used to load the 'native-lib' library on application startup.
          init {
              //  System.loadLibrary("native-lib")
          }


      } */


    }
}
