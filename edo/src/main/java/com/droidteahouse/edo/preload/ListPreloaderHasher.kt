package com.droidteahouse.edo.preload

import android.util.Log
import android.widget.AbsListView
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.BaseTarget
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.util.Synthetic
import com.bumptech.glide.util.Util
import com.droidteahouse.edo.vo.ArtObject
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.launch
import java.util.*


/**
 * Loads a few resources ahead in the direction of scrolling in any [AbsListView] so that
 * images are in the memory cache just before the corresponding view in created in the list. Gives
 * the appearance of an infinitely large image cache, depending on scrolling speed, cpu speed, and
 * cache size.
 *
 *
 *
 *  Must be put using
 * [AbsListView.setOnScrollListener], or have its
 * corresponding methods called from another [android.widget.AbsListView.OnScrollListener] to
 * function.
 *
 * @param <T> The type of the model being displayed in the list.
</T> */

//inject artviewmodel

/**
 * Constructor for [ListPreloaderHasher] that accepts interfaces for providing
 * the dimensions of images to preload, the list of models to preload for a given position, and
 * the request to use to load images.
 *
 * @param preloadModelProvider     Provides models to load and requests capable of loading them.
 * @param preloadDimensionProvider Provides the dimensions of images to load.
 * @param maxPreload               Maximum number of items to preload.
 */
class ListPreloaderHasher<T>
(private val requestManager: RequestManager,
 private val preloadModelProvider: ListPreloaderHasher.PreloadModelProvider<T>,
 private val preloadDimensionProvider: ListPreloaderHasher.PreloadSizeProvider<T>, private val maxPreload: Int) : AbsListView.OnScrollListener {
    private val preloadTargetQueue: ListPreloaderHasher.PreloadTargetQueue
    private var lastEnd: Int = 0
    private var lastStart: Int = 0
    private var lastFirstVisible = -1
    private var totalItemCount: Int = 0
    private var isIncreasing = true
    //val counterContext = newSingleThreadContext("CounterContext")


    init {
        preloadTargetQueue = ListPreloaderHasher.PreloadTargetQueue(maxPreload + 1)
    }

    override fun onScrollStateChanged(absListView: AbsListView, scrollState: Int) {
        // Do nothing.
    }

    //@todo refactor this
    override fun onScroll(absListView: AbsListView?, firstVisible: Int, visibleCount: Int,
                          totalCount: Int) {
        totalItemCount = totalCount

        if (firstVisible > lastFirstVisible) {
            preload(firstVisible + visibleCount, true)
        } else if (firstVisible < lastFirstVisible) {
            preload(firstVisible, false)
        }
        lastFirstVisible = firstVisible
    }

    private fun preload(start: Int, increasing: Boolean) {
        if (isIncreasing != increasing) {
            isIncreasing = increasing
            cancelAll()
        }
        preload(start, start + if (increasing) maxPreload else -maxPreload)
    }

    //here
    private fun preload(from: Int, to: Int) {
        var start: Int
        var end: Int
        if (from < to) {
            start = Math.max(lastEnd, from)
            end = to
        } else {
            start = to
            end = Math.min(lastStart, from)
        }
        end = Math.min(totalItemCount, end)
        start = Math.min(totalItemCount, Math.max(0, start))

        if (from < to) {
            // Increasing
            for (i in start until end) {
                preloadAdapterPosition(preloadModelProvider.getPreloadItems(i), i, true)
            }
        } else {
            // Decreasing
            for (i in end - 1 downTo start) {
                preloadAdapterPosition(preloadModelProvider.getPreloadItems(i), i, false)
            }
        }

        lastStart = start
        lastEnd = end
    }

    private fun preloadAdapterPosition(items: List<T>, position: Int, isIncreasing: Boolean) {
        val numItems = items.size
        //  Log.d("HASHER", "preloading starting" + items.size());
        if (isIncreasing) {
            for (i in 0 until numItems) {
                preloadItem(items[i], position, i)
            }
        } else {
            for (i in numItems - 1 downTo 0) {
                preloadItem(items[i], position, i)
            }
        }
    }

    private fun preloadItem(item: T?, position: Int, perItemPosition: Int) {
        if (item == null) {
            return
        }

        val dimensions = preloadDimensionProvider.getPreloadSize(item, position, perItemPosition)
                ?: return
        val preloadRequestBuilder = preloadModelProvider.getPreloadRequestBuilder(item) as RequestBuilder<Any>?
                ?: return
        val id = (item as ArtObject).id
//@todo put on bg thread again
        Log.d("HASHER", "Loading " + item.id + "::::" + item.objectid)
        CoroutineScope(MyPreloadModelProvider.Cache.companionContext).launch {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
            if (!MyPreloadModelProvider.Cache.hasId(item.id)) {
                MyPreloadModelProvider.Cache.putIdInCache(item.id)
                preloadModelProvider.hashImage(preloadRequestBuilder, item as ArtObject)
            }
        }

        preloadRequestBuilder.into(preloadTargetQueue.next(dimensions[0], dimensions[1]))

    }
    private fun cancelAll() {
        for (i in 0 until maxPreload) {
            requestManager.clear(preloadTargetQueue.next(0, 0))
        }
    }


    /**
     * An implementation of MyPreloadModelProvider should provide all the models that should be
     * preloaded.
     *
     * @param <U> The type of the model being preloaded.
    </U> */
    interface PreloadModelProvider<U> {

        fun check(id: Int, artObject: ArtObject, preloadRequestBuilder: RequestBuilder<Any>)
        fun hashImage(requestBuilder: RequestBuilder<Any>, item: ArtObject)

        /**
         * Returns a [List] of models that need to be loaded for the list to display adapter items
         * in positions between `start` and `end`.
         *
         *
         *
         * A list of any size can be returned so there can be multiple models per adapter position.
         *
         *
         *
         * Every model returned by this method is expected to produce a valid [RequestBuilder]
         * in [.getPreloadRequestBuilder]. If that's not possible for any set of models,
         * avoid including them in the [List] returned by this method.
         *
         *
         *
         * Although it's acceptable for the returned [List] to contain `null` models,
         * it's best to filter them from the list instead of adding `null` to avoid unnecessary
         * logic and expanding the size of the [List]
         *
         * @param position The adapter position.
         */
        fun getPreloadItems(position: Int): List<U>

        /**
         * Returns a [RequestBuilder] for a given item on which
         * [RequestBuilder.load]} has been called or `null` if no valid load can be
         * started.
         *
         *
         *
         * For the preloader to be effective, the [RequestBuilder] returned here must use
         * exactly the same size and set of options as the [RequestBuilder] used when the ``View``
         * is bound. You may need to specify a size in both places to ensure that the width and height
         * match exactly. If so, you can use
         * [com.bumptech.glide.request.RequestOptions.override] to do so.
         *
         *
         *
         * The target and context will be provided by the preloader.
         *
         *
         *
         * If [RequestBuilder.load] is not called by this method, the preloader will
         * trigger a [RuntimeException]. If you don't want to load a particular item or position,
         * filter it from the list returned by [.getPreloadItems].
         *
         * @param item The model to load.
         */
        fun getPreloadRequestBuilder(item: U): RequestBuilder<*>?


    }

    /**
     * An implementation of PreloadSizeProvider should provide the size of the view in the list where
     * the resources will be displayed.
     *
     * @param <T> The type of the model the size should be provided for.
    </T> */
    interface PreloadSizeProvider<T> {

        /**
         * Returns the size of the view in the list where the resources will be displayed in pixels in
         * the format [x, y], or `null` if no size is currently available.
         *
         *
         *
         * Note - The dimensions returned here must precisely match those of the view in the list.
         *
         *
         *
         * If this method returns `null`, then no request will be started for the given item.
         *
         * @param item A model
         */
        fun getPreloadSize(item: T, adapterPosition: Int, perItemPosition: Int): IntArray?
    }

    private class PreloadTargetQueue// The loop is short and the only point is to create the objects.
    internal constructor(size: Int) {
        private val queue: Queue<PreloadTarget>

        init {
            queue = Util.createQueue(size)

            for (i in 0 until size) {
                queue.offer(ListPreloaderHasher.PreloadTarget())
            }
        }

        fun next(width: Int, height: Int): ListPreloaderHasher.PreloadTarget {
            val result = queue.poll()
            queue.offer(result)
            result.photoWidth = width
            result.photoHeight = height
            return result
        }
    }

    private class PreloadTarget @Synthetic
    internal constructor() : BaseTarget<Any>() {
        @Synthetic
        internal var photoHeight: Int = 0
        @Synthetic
        internal var photoWidth: Int = 0

        override fun onResourceReady(resource: Any,
                                     transition: Transition<in Any>?) {
            //  Log.d("HASHER ", "resource: " + resource);
        }

        override fun getSize(cb: SizeReadyCallback) {
            cb.onSizeReady(photoWidth, photoHeight)
        }

        override fun removeCallback(cb: SizeReadyCallback) {
            // Do nothing because we don't retain references to SizeReadyCallbacks.
        }
    }
}

