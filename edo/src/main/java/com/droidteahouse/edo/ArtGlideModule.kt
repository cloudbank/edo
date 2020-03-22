package com.droidteahouse.edo

import android.content.Context
import android.util.Log
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.module.AppGlideModule


@GlideModule
class ArtGlideModule : AppGlideModule() {

  override fun applyOptions(context: Context, builder: GlideBuilder) {
    val diskCacheSizeBytes = Long.MAX_VALUE //@todo tweaking from 800 MB
    builder.setDiskCache(InternalCacheDiskCacheFactory(context, diskCacheSizeBytes))
    Log.d("ArtGlideModule", "apply options")
  }
}
