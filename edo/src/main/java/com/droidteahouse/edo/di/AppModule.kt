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
package com.droidteahouse.edo.di

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import com.droidteahouse.edo.api.ArtAPI
import com.droidteahouse.edo.db.ArtDao
import com.droidteahouse.edo.db.ArtDb
import dagger.Module
import dagger.Provides
import dagger.Reusable
import kotlinx.coroutines.experimental.ThreadPoolDispatcher
import kotlinx.coroutines.experimental.newSingleThreadContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Named
import javax.inject.Singleton


@Module(includes = arrayOf(ViewModelModule::class))
class AppModule {


    @Provides
    @Singleton
    internal fun provideContext(application: Application): Context {
        return application
    }


    @Reusable
    @Provides
    internal fun provideArtService(): ArtAPI {
        val okHttpClient = OkHttpClient.Builder()
                // .addInterceptor(logger)
                // .addNetworkInterceptor(HeaderInterceptor())
                //  .authenticator(TokenAuthenticator())
                .build()
        return Retrofit.Builder()
                .baseUrl("https://api.harvardartmuseums.org".trim())
                .client(okHttpClient)
                // .addConverterFactory(GsonConverterFactory.create(GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ArtAPI::class.java)
    }

    //this is an expensive operation so, we would want a singleton object.
    @Singleton
    @Provides
    internal fun provideDb(application: Application): ArtDb {
        return Room.databaseBuilder(application, ArtDb::class.java, "edo.db").fallbackToDestructiveMigration().build()
    }

    //https://stackoverflow.com/questions/16316890/when-to-shutdown-executorservice-in-android-application
    @Provides
    @Reusable
    @Named("repoExec")
    internal fun provideRepoIOExecutor(): ExecutorService {
        return Executors.newCachedThreadPool()
    }

    @Provides
    @Reusable
    @Named("stc")
    internal fun provideSTC(): ThreadPoolDispatcher {
        return newSingleThreadContext("CounterContext")
    }


    @Provides
    @Singleton
    internal fun provideDao(db: ArtDb): ArtDao {
        return db.artDao()
    }


}



