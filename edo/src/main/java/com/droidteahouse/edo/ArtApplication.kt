package com.droidteahouse.edo

import android.util.Log
import com.droidteahouse.edo.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

class ArtApplication : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().application(this).build()
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("APP", "APP")
    }




}


