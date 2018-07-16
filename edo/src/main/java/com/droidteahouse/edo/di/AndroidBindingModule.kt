package com.droidteahouse.edo.di

import com.droidteahouse.edo.ui.ArtActivity

import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Binds all activity sub-components within the app.
 */
@Module
abstract class AndroidBindingModule {

  @ContributesAndroidInjector
  internal abstract fun contributesArtActivity(): ArtActivity
}