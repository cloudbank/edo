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

import android.arch.paging.PagedList
import android.arch.paging.PagingRequestHelper
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.annotation.MainThread
import android.support.annotation.RequiresApi
import android.util.Log
import android.widget.Toast
import com.droidteahouse.edo.api.ArtAPI
import com.droidteahouse.edo.util.createStatusLiveData
import com.droidteahouse.edo.vo.ArtObject
import com.droidteahouse.edo.vo.EdoObjects
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject
import javax.inject.Singleton


/**
 * This boundary callback gets notified when user reaches to the edges of the list such that the
 * database cannot provide any more data.
 * <p>
 * The boundary callback might be called multiple times for the same direction so it does its own
 * rate limiting using the PagingRequestHelper class.
 */
@Singleton
class ArtBoundaryCallback @Inject constructor(
        var ioExecutor: Executor,
        var webservice: ArtAPI,
        var threadpool: ThreadPoolExecutor,  //@todo
        var context: Context
) : PagedList.BoundaryCallback<ArtObject>() {
  lateinit var handleResponse: (List<ArtObject>) -> Unit
  val helper = PagingRequestHelper(ioExecutor)
  val networkState = helper.createStatusLiveData()

  /**
   * Database returned 0 items. We should query the backend for more items.
   */
  @MainThread
  override fun onZeroItemsLoaded() {
    helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
      webservice.japaneseDesign(
              page = "1")
          //per_page = networkPageSize)
          .enqueue(createWebserviceCallback(it))
    }
  }

  /**
   * User reached to the end of the list.
   */
  @MainThread
  override fun onItemAtEndLoaded(itemAtEnd: ArtObject) {
    helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
      webservice.japaneseDesign(
          page = (itemAtEnd.page + 1).toString())
          //per_page = networkPageSize)
          .enqueue(createWebserviceCallback(it))
    }
  }

  /**
   * every time it gets new items, boundary callback simply inserts them into the database and
   * paging library takes care of refreshing the list if necessary.
   */
  private fun insertItemsIntoDb(
      response: List<ArtObject>,
      it: PagingRequestHelper.Request.Callback) {
    ioExecutor.execute {
      android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
      handleResponse(response)
      it.recordSuccess()
    }
  }

  override fun onItemAtFrontLoaded(itemAtFront: ArtObject) {
    // ignored, since we only ever append to what's in the DB
  }

  private fun createWebserviceCallback(it: PagingRequestHelper.Request.Callback)
      : Callback<EdoObjects> {
    return object : Callback<EdoObjects> {
      override fun onFailure(
              call: Call<EdoObjects>,
              t: Throwable) {
        it.recordFailure(t)
      }

      @RequiresApi(Build.VERSION_CODES.O)
      override fun onResponse(
              call: Call<EdoObjects>,
              response: Response<EdoObjects>) =
          try {
            insertItemsIntoDb(response.body()?.records!!, it)
          } catch (e: Exception) {
            Log.e("ABCB", "HTTP ERROR " + e)
            Handler(Looper.getMainLooper()).post(Runnable {
              //server down 999
              Toast.makeText(context, "API is down, please try again later", Toast.LENGTH_LONG).show()

            })
            System.exit(0)
          }
    }
  }

}
/*
  //@todo
  @RequiresApi(Build.VERSION_CODES.O)
  fun hashAndPreload(objects: List<ArtObject>?): List<ArtObject> {
    val start = System.currentTimeMillis()


    val ioExecutor = Executors.newFixedThreadPool(10);

    // Submit and execute 100 threads!
    for (o in objects!!) {
       var i=1
      ioExecutor.execute(object : Runnable {
        public override fun run() {
          System.out.println("thread..." );
          //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
          //inject glide and load--load cache AND get bitmap
          /* glide
               .asBitmap()
               .load(artObject.images?.get(0)?.n?.url).submit()
               .into(Target() )*/
          val bitmap = GlideApp.with(context).asBitmap()
              .load(o.images?.get(0)?.n?.url).into(8, 8).get()

            val hash = 31 //or a higher prime at your choice
            for(int x  from  bmp.getWidth(); x++){
            for (int y = 0; y < bmp.getHeight(); y++){
            hash *= (bmp.getPixel(x,y) + 31);

          }
          }
            return hash;
          }
          //o.hash = md5Hash.toString()

          //load all caches as Drawable
          //GlideApp.with(context).load(bitmap)

        }
      })
    }

    System.out.println("Waiting...");

    // No more threads can be submitted to the ioExecutor service!
    ioExecutor.shutdown();

    // Blocks until all 100 submitted threads have finished!
    ioExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);

    System.out.println("Done" + objects +"  :  " +System.currentTimeMillis().minus(start));
    return  objects

  }*/
