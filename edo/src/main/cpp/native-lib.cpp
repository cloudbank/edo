#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <boost/functional/hash.hpp>


extern "C" JNIEXPORT jstring

JNICALL
Java_com_droidteahouse_edo_ui_ArtActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT std::size_t
JNICALL
Java_com_droidteahouse_edo_ui_ArtActivity_hashFromJNI(
        JNIEnv *env,
        jobject /* this */) {

    int arr[10] = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};

    return boost::hash_range(arr, arr + 10);
}


/*
extern "C" JNIEXPORT int
JNICALL
Java_com_droidteahouse_edo_ListPreloaderHasher_hashBitmap(JNIEnv *env, jobject obj, jarray pixels) {
  AndroidBitmapInfo  info;
  uint32_t          *pixels;
  int                ret;

  AndroidBitmap_getInfo(env, obj, &info);

  if(info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
    __android_log_write(ANDROID_LOG_ERROR,"NATIVE","Bitmap format is not RGBA_8888!");
    //return false;
  }
  if ((ret = AndroidBitmap_lockPixels(env, obj, reinterpret_cast<void **>(&pixels)) < 0)) {
    __android_log_write(ANDROID_LOG_ERROR,"NATIVE", "AndroidBitmap_lockPixels() failed ! error=%d");
  }
  //AndroidBitmap_lockPixels(env, bitmap, reinterpret_cast<void **>(&pixels));
  uint32_t* src = (uint32_t*) pixels;


  std::size_t result = boost::hash_range(pixels,64 );
// Now you can use the pixel array 'pixels', which is in RGBA format


/*
 * https://stackoverflow.com/questions/5231599/is-there-any-way-to-pass-a-java-array-to-c-through-jni-without-making-a-copy-of
 *
 */ //DOUbkleBuffer
/*

  //AndroidBitmap_unlockPixels(env,bitmap);
  return result;

}
*/
extern "C" JNIEXPORT jint
JNICALL
Java_com_droidteahouse_edo_ui_ArtActivity_00024MyPreloadModelProvider_dhash(JNIEnv *env,
                                                                            jobject /* this */,
                                                                            jintArray pixels) {

    // initializations, declarations, etc
    jint *c_array;
    jint i = 0;
    jint hash = 0;

    // get a pointer to the array
    c_array = env->GetIntArrayElements(pixels, 0);
    // do some exception checking
    if (c_array == NULL) {
        return -1; /* exception occurred */
    }
    //jsize len = env->GetArrayLength(arr);
    // do stuff to the array
    for (i = 0; i < 72; i++) {
        if ((i + 1) % 9 == 0) {
            // don't calculate the current end of row compared to the beginning of the next row
            continue;
        }
        jint bit = (c_array[i] & 0xff) < (c_array[i + 1] & 0xff);
        hash = hash << 1 | bit;

    }

    // release the memory so java can have it again
    env->ReleaseIntArrayElements(pixels, c_array, 0);

    // return something, or not.. it's up to you
    return hash;
}



//https://developer.android.com/ndk/guides/stable_apis#jnigraphics
