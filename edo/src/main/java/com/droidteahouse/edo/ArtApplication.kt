package com.droidteahouse.edo

import android.util.Log
import com.droidteahouse.edo.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.paperdb.Paper


class ArtApplication : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().application(this).build()
    }

    override fun onCreate() {
        super.onCreate()
        Paper.init(this)
        Log.d("APP", "APP")

    }




}