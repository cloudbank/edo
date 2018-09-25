package com.droidteahouse.edo.preload


import android.support.v7.widget.RecyclerView
import android.util.Log
import com.bumptech.glide.RequestManager
import com.bumptech.glide.integration.recyclerview.RecyclerToListViewScrollListener
import com.droidteahouse.edo.preload.ListPreloaderHasher.PreloadModelProvider
import com.droidteahouse.edo.preload.ListPreloaderHasher.PreloadSizeProvider

/**
 * Loads a few resources ahead in the direction of scrolling in any [RecyclerView] so that
 * images are in the memory cache just before the corresponding view in created in the list. Gives
 * the appearance of an infinitely large image cache, depending on scrolling speed, cpu speed, and
 * cache size.
 *
 *
 *
 *  Must be added as a listener to the [RecyclerView] using
 * [RecyclerView.addOnScrollListener], or have its
 * corresponding methods called from another
 * [android.support.v7.widget.RecyclerView.OnScrollListener] to function.
 *
 *
 *
 *  This class only works with [android.support.v7.widget.LinearLayoutManager] and
 * subclasses of [android.support.v7.widget.LinearLayoutManager].
 *
 * @param <T> The type of the model being displayed in the [RecyclerView].
</T> */

/**
 * Constructor that accepts interfaces for providing the dimensions of images to preload, the list
 * of models to preload for a given position, and the request to use to load images.
 *
 * @param preloadModelProvider     Provides models to load and requests capable of loading them.
 * @param preloadDimensionProvider Provides the dimensions of images to load.
 * @param maxPreload               Maximum number of items to preload.
 */
class RecyclerViewPreloader<T>
(requestManager: RequestManager,
 preloadModelProvider: PreloadModelProvider<T>,
 preloadDimensionProvider: PreloadSizeProvider<T>, maxPreload: Int) : RecyclerView.OnScrollListener() {

    private val recyclerScrollListener: RecyclerToListViewScrollListener


    init {
        val listPreloader = ListPreloaderHasher(requestManager, preloadModelProvider,
                preloadDimensionProvider, maxPreload)
        recyclerScrollListener = RecyclerToListViewScrollListener(listPreloader)
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        recyclerScrollListener.onScrolled(recyclerView, dx, dy)
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        Log.d("RecyclerViewPreloader", "onScrollStateChanged " + newState)
//        recyclerScrollListener.onScrollStateChanged(recyclerView, newState)
    }


}

