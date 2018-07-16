package com.droidteahouse.edo.di

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.droidteahouse.edo.ui.ArtViewModel
import com.droidteahouse.edo.ui.ArtViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
  //the default factory only works with default constructor
  @Binds
  @IntoMap
  @ViewModelKey(ArtViewModel::class)
  abstract fun bindArtViewModel(artViewModel: ArtViewModel): ViewModel

  @Binds
  abstract fun bindViewModelFactory(factory: ArtViewModelFactory): ViewModelProvider.Factory
}
