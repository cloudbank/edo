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
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.droidteahouse.edo.GlideApp
import com.droidteahouse.edo.R
import com.droidteahouse.edo.preload.FixedPreloadSizeProvider
import com.droidteahouse.edo.preload.MyPreloadModelProvider
import com.droidteahouse.edo.preload.RecyclerViewPreloader
import com.droidteahouse.edo.repository.NetworkState
import com.droidteahouse.edo.vo.ArtObject
import dagger.android.support.DaggerAppCompatActivity
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_art.*
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.ThreadPoolDispatcher
import kotlinx.coroutines.experimental.launch
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
    lateinit var modelProvider: MyPreloadModelProvider<ArtObject>

    //@todo instead of injection, use Cache stc ctx?
    @Inject
    @field:Named("activity")
    lateinit var stcContext: ThreadPoolDispatcher

    var onsavedstate = false

    private var mLayoutManager: LinearLayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            Log.d("oncreate", "onSaveInstanceState")
            MyPreloadModelProvider.Cache.idcache = savedInstanceState.getIntArray("idcache")
            // MyPreloadModelProvider.bits = savedInstanceState.getInt("bits")
            //@todo the arraymap ; for now the bg
            MyPreloadModelProvider.Cache.hashcache = Paper.book().read("hashes")

            onsavedstate = true
        }
        setContentView(R.layout.activity_art)
        createViews()
        initSwipeToRefresh()
    }
/*
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState != null) {
            Log.d("onRestoreInstanceState", "onRestoreInstanceState")
            onsavedstate = true
        }
    }
    */

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d("ONSAVEINSTANCESTATE", "ONSAVEINSTANCESTATE")
        // outState.putInt("bits", MyPreloadModelProvider.bits)
        outState.putIntArray("idcache", MyPreloadModelProvider.Cache.idcache)
        //@todo bg thread; arraymap to bundle
        Paper.book().write("hashes", MyPreloadModelProvider.Cache.hashcache)
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
        rvArt?.onFlingListener = (FlingListener())
        //val helper = LinearSnapHelper()
        //helper.attachToRecyclerView(rvArt)


        artViewModel.artObjects.observe(this, Observer<PagedList<ArtObject>> {
            if (artViewModel.artObjects.value?.size?.compareTo(0) !== 0) { //avoid 0 size onstart PR

                //@todo  needs generalization and onsavedinstancestate for reclaim w small list SSOT db
                //if (it?.size?.compareTo(0)!! > 0) {
                //CoroutineScope(stcContext).launch {
                if (!Paper.book().contains("hashVisible")) {


                    Paper.book().write("hashVisible", true)

                    CoroutineScope(MyPreloadModelProvider.Cache.companionContext).launch {
                        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
                        modelProvider.hashVisible(it!!.subList(0, 4))
                    }
                    //runOnUiThread {
                    setTheme(R.style.AppTheme)
                    //}

                }
                // }
//do I have to do a manual diff for paged list before 26?
                Log.d(".0artobject", ":::" + it?.get(0) + it?.size)
                adapter.submitList(it)
                modelProvider.objects = it?.toMutableList()

                //}


            }

        })

        //rvArt?.smoothScrollToPosition(0)
        artViewModel.networkState.observe(this, Observer
        {
            adapter.setNetworkState(it)
        })


    }


    override fun onResume() {
        super.onResume()
        if (!onsavedstate) {
/*
            if (Paper.book().contains("bits") && MyPreloadModelProvider.bits == 0) {
                launch(counterContext) {
                    MyPreloadModelProvider.bits = Paper.book().read("bits", 0)
                }
            }*/
            if (Paper.book().contains("ids") && MyPreloadModelProvider.Cache.idcache[0] == 0) {
                CoroutineScope(stcContext).launch {
                    MyPreloadModelProvider.Cache.idcache = Paper.book().read("ids")
                }
            }
            if (Paper.book().contains("hashes") && MyPreloadModelProvider.Cache.hashcache.isEmpty()) {
                CoroutineScope(stcContext).launch {
                    MyPreloadModelProvider.Cache.hashcache = Paper.book().read("hashes")
                }
            }

        }


        onsavedstate = false
    }

    override fun onDestroy() {
        super.onDestroy()
        //@todo where to really call this?
        modelProvider.cleanUp()
        Log.d("ONDESTROY", "ONDESTROY")

    }

    override fun onStop() {
        super.onStop()
        Log.d("ONSTOP", "ONSTOP")


    }

    override fun onPause() {
        super.onPause()
        Log.d("ONPAUSE", "ONPAUSE")
/*
        if (MyPreloadModelProvider.bits != 0) {
            launch(counterContext) {
                Paper.book().write("bits", MyPreloadModelProvider.bits)
            }
        }*/
        if (MyPreloadModelProvider.Cache.idcache[0] != 0) {
            CoroutineScope(stcContext).launch {
                Paper.book().write("ids", MyPreloadModelProvider.Cache.idcache)
            }
        }
        if (!MyPreloadModelProvider.Cache.hashcache.isEmpty()) {
            CoroutineScope(stcContext).launch {
                Paper.book().write("hashes", MyPreloadModelProvider.Cache.hashcache)
            }
        }


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

    class FlingListener : RecyclerView.OnFlingListener() {
        override fun onFling(x: Int, y: Int): Boolean {
            if (y > 400) {
                Log.d("fling speed", "fling speed= " + x + "::" + y)

                return true
            }
            return false
        }


    }


}