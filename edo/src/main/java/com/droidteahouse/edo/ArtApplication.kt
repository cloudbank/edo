package com.droidteahouse.edo

import android.util.Log
import com.droidteahouse.edo.di.DaggerAppComponent

import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import java.nio.ByteBuffer

class ArtApplication : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().application(this).build()
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("APP", "APP")


        //get all urls and start service that goes to finish
    }

    companion object {
        val bitset = ByteBuffer.allocateDirect(4).asIntBuffer()
    }

}
//https://stackoverflow.com/questions/19398827/understanding-ontrimmemory-int-level
/*
  /This my introduce OutOfMemoryException if you don't handle register and removal quiet well, better to replace it with weak reference
  private static List<IMemoryInfo> memInfoList = new ArrayList<AppContext.IMemoryInfo>();

  public static abstract interface IMemoryInfo {
    public void goodTimeToReleaseMemory();
  }

  @Override
  public void onTrimMemory(int level) {
    super.onTrimMemory(level);
//don't compare with == as intermediate stages also can be reported, always better to check >= or <=
    if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
      try {
        // Activity at the front will get earliest than activity at the
        // back
        for (int i = memInfoList.size() - 1; i >= 0; i--) {
          try {
            memInfoList.get(i).goodTimeToReleaseMemory();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   *
   * @param implementor
   *            interested listening in memory events
   */
  public static void registerMemoryListener(IMemoryInfo implementor) {
    memInfoList.add(implementor);
  }

  public static void unregisterMemoryListener(IMemoryInfo implementor) {
    memInfoList.remove(implementor);
  }
*/

