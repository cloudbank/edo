package com.droidteahouse.edo.di

import android.app.Application
import com.droidteahouse.edo.ArtApplication
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton


@Singleton  //@todo research
@Component(modules = arrayOf(AndroidSupportInjectionModule::class, AppModule::class, AndroidBindingModule::class))
interface AppComponent : AndroidInjector<ArtApplication> {
  override fun inject(app: ArtApplication)

  @Component.Builder
  interface Builder {
    //Binding an instance is equivalent to passing an instance to a module constructor and providing
    //*that instance, but is often more efficient.
    @BindsInstance
    fun application(application: Application): AppComponent.Builder

    fun build(): AppComponent
  }


}



