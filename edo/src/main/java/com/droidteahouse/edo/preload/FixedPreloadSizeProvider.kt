package com.droidteahouse.edo.preload

/**
 * A [com.bumptech.glide.ListPreloader.PreloadSizeProvider] with a fixed width and height.
 *
 * @param <T> The type of the model the size should be provided for.
</T> */
class FixedPreloadSizeProvider<T>
/**
 * Constructor for a PreloadSizeProvider with a fixed size.
 *
 * @param width  The width of the preload size in pixels.
 * @param height The height of the preload size in pixels.
 */
(width: Int, height: Int) : ListPreloaderHasher.PreloadSizeProvider<T> {

    private val size: IntArray

    init {
        this.size = intArrayOf(width, height)
    }

    // It's better to take on the risk that callers may mutate the array when there isn't any reason
    // for them to do so than it the performance overhead of copying the array with every call.
    override fun getPreloadSize(item: T, adapterPosition: Int, itemPosition: Int): IntArray? {
        return size
    }
}
