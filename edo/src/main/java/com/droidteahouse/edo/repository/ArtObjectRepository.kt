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

package com.droidteahouse.edo.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.LivePagedListBuilder
import android.support.annotation.MainThread
import android.util.Log
import com.droidteahouse.edo.db.ArtDb
import com.droidteahouse.edo.vo.ArtObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation that uses a database PagedList + a boundary callback to return a
 * listing that loads in pages.
 *      // create a boundary callback which will observe when the user reaches to the edges of
// the list and update the database with extra data.


// we are using a mutable live data to trigger refresh requests which eventually calls
// refresh method and gets a new live data. Each refresh request by the user becomes a newly
// dispatched data in refreshTrigger

 */
@Singleton
class ArtObjectRepository @Inject constructor(
        var boundaryCallback: ArtBoundaryCallback,
        var db: ArtDb) {

    var listing: Listing<ArtObject>

    init {
        val builder: LivePagedListBuilder<Int, ArtObject> by lazy {
            LivePagedListBuilder(db.artDao().artObjects(), 10)
                    .setBoundaryCallback(boundaryCallback)
        }
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger, {
            //@todo fixo
            refresh(1)
        })
        listing =
                Listing(
                        pagedList = builder.build(),
                        networkState = boundaryCallback.networkState,
                        retry = {
                            boundaryCallback.helper.retryAllFailed()
                        },
                        refresh = {
                            refreshTrigger.value = null
                        },
                        refreshState = refreshState
                )

        boundaryCallback.handleResponse = this::insertResultIntoDb


    }

    /**
     * Inserts the response into the database while also assigning page to items.
     */
    fun insertResultIntoDb(list: List<ArtObject>) {

        list.let { results ->
            //
            db.runInTransaction {

                var nextPage = db.artDao().getNextPageInArt()
                if (nextPage == 0) nextPage = 1
                val filtered = results.filter { it.url.length > 0 }

                var items = filtered.map { item ->
                    item.page = nextPage
                    item.url += "?height=100&width=100"
                    //item.hash = item.id //init before hash
                    item
                }.toMutableList()
                if (nextPage == 1) {
                    items.sortBy { it.id }
                    var clone1 = items.get(0)
                    clone1.title = "[dupicate 0 removed for] : " + clone1.title
                    val f3 = clone1.copy()
                    f3.id = f3.id - 1
                    f3.page = nextPage
                    f3.title = "copy of 0 " + f3.title
                    items.add(1, f3)

                    var clone2 = items.get(4)
                    clone2.title = "[dupicate removed for 4] : " + clone2.title
                    val f4 = clone2.copy()
                    f4.id = (f4.id * 10) + 1
                    f4.page = nextPage
                    f4.title = "copy of 4 " + f4.title
                    items.add(5, f4)


                    var clone3 = clone2.copy()
                    clone3.id = 1
                    clone3.title = "assets1"
                    //val photoUri = Uri.fromFile(File("file:///android_asset/402004259.jpeg"))
                    clone3.page = nextPage
                    clone3.url = "file:///android_asset/402004259.jpeg"
                    items.add(0, clone3)

                    var clone4 = clone2.copy()
                    clone4.id = 2
                    clone4.title = "assets2"
                    //val photoUri = Uri.fromFile(File("file:///android_asset/402004259.jpeg"))
                    clone4.page = nextPage
                    clone4.url = "file:///android_asset/BAA1Nb5.jpg"
                    items.add(1, clone4)

                }
                Log.d("REPO", "items insert starting" + items.size + ";;" + nextPage)
                //@todo when insert it calls onchange--need to protect from extra calls to network?
                //Paging data source is getting next page?  check page
                db.artDao().insert(items)

            }
        }
    }


    /**
     * When refresh is called, we simply run a fresh network request and when it arrives, clear
     * the database table and insert all new items in a transaction.
     * <p>
     * Since the PagedList already uses a database bound data source, it will automatically be
     * updated after the database transaction is finished.
     */
    //@todo not needed until search impl
    @MainThread
    private fun refresh(page: Int): LiveData<NetworkState> {

        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        /*
        artAPI.japaneseDesign(page).enqueue(
                object : Callback<ArtObjectsBak> {
                    override fun onFailure(call: Call<ArtObjectsBak>, t: Throwable) {
                        // retrofit calls this on main thread so safe to call set value
                        networkState.value = NetworkState.error(t.message)
                    }

                    override fun onResponse(
                            call: Call<ArtObjectsBak>,
                            response: Response<ArtObjectsBak>) {
                        ioExecutor.execute {
                            db.runInTransaction {
                                //db.artDao().deleteByPage(page)
                                insertResultIntoDb(response.body())
                            }
                            // since we are in bg thread now, post the result.
                            networkState.postValue(NetworkState.LOADED)
                        }
                    }
                }
        )*/
        networkState.postValue(NetworkState.LOADED)
        return networkState
    }

    /**
     * Returns japanese design objects.
     */
    @MainThread
    fun getArtObjects(): Listing<ArtObject> {

        //@todo inject or cache
        return listing
    }
/*
  fun insertHash(item: ImageHash) {
    db.artDao().insertHash(item)

  }

  fun getHash(item: String): Int {
    return db.artDao().getHash(item)

  }
  */


    fun delete(item: ArtObject) {
        db.artDao().delete(item)
    }

    fun update(item: ArtObject) {
        db.artDao().update(item)

    }
}

