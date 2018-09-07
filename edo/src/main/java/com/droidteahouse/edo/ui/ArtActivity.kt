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
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
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
import kotlinx.android.synthetic.main.activity_art.*
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
    @Inject
    @field:Named("ids")
    lateinit var spIds: SharedPreferences
    var onsavedstate = false

    private var mLayoutManager: LinearLayoutManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        //@todo check if this is always true and if needed for config?  VM should be intact
        if (savedInstanceState != null) {
            Log.d("oncreate", "onSaveInstanceState")
            ArtViewModel.idcache = savedInstanceState.getIntArray("idcache")
            ArtViewModel.bits = savedInstanceState.getInt("bits")

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
            ArtViewModel.idcache = savedInstanceState?.getString("idcache")?.map({ it.toInt() })!!.toIntArray()
            ArtViewModel.bits = savedInstanceState?.getInt("bits")
            onsavedstate = true
        }
    }
    */

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d("ONSAVEINSTANCESTATE", "ONSAVEINSTANCESTATE")
        outState.putInt("bits", ArtViewModel.bits)
        outState.putIntArray("idcache", ArtViewModel.idcache)

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

                    if (!spIds.contains("hashVisible")) {
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


    override fun onResume() {
        super.onResume()
        if (!onsavedstate) {
            if (spIds.contains("bits") && ArtViewModel.bits == 0) {
                ArtViewModel.bits = spIds.getInt("bits", 0)
            }
            //@todo           //------>@todo  @todo
            //if (spIds.contains("idcache") && (ArtViewModel.idcache[0] == 0)) {
            //   ArtViewModel.idcache = spIds.getString("idcache", "0")
        }


        onsavedstate = false
    }

    override fun onDestroy() {
        super.onDestroy()
        //@todo persist  via flatbuffer
        Log.d("ONDESTROY", "ONDESTROY")

    }

    //  onStop is better but I moved it here to help w data loss on forced exits
    override fun onStop() {
        super.onStop()
        //@todo persist  via flatbuffer
        Log.d("ONSTOP", "ONSTOP")
        // if (ArtViewModel.bits != 0) spIds.edit().putInt("bits", ArtViewModel.bits).commit()
        // if (ArtViewModel.idcache[0] != 0) spIds.edit().putString("idcache", ArtViewModel.idcache.contentToString()).commit()


    }

    override fun onPause() {
        super.onPause()
        //@todo persist  via flatbuffer
        Log.d("ONPAUSE", "ONPAUSE")
        if (ArtViewModel.bits != 0) spIds.edit().putInt("bits", ArtViewModel.bits).commit()
        if (ArtViewModel.idcache[0] != 0) spIds.edit().putString("idcache", ArtViewModel.idcache.contentToString()).commit()


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


}