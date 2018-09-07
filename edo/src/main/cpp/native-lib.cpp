#include <jni.h>
#include <stdlib.h>


/**3 stepsisters algo
 * scales the image using the fastest, simplest algorithm called "nearest neighbor, greyscales,
 * and fingerprints all in one*/
extern "C" JNIEXPORT jlong JNICALL
Java_com_droidteahouse_edo_preload_MyPreloadModelProvider_nativeDhash(
        JNIEnv *env, jobject obj, jobject db, jint newWidth, jint newHeight, jint oldWidth,
        jint oldHeight) {

    jint *iBuf = (jint *) env->GetDirectBufferAddress(db);
    jint *newBitmapPixels = new jint[newWidth * newHeight];
    jint x2, y2;
    jint index = 0;
    jlong hash = 0;
    //buffer has been allocated for size already on java side
    for (jint y = 0; y < newHeight; ++y) {
        for (jint x = 0; x < newWidth; ++x) {
            x2 = x * oldWidth / newWidth;
            if (x2 < 0)
                x2 = 0;
            else if (x2 >= oldWidth)
                x2 = oldWidth - 1;
            y2 = y * oldHeight / newHeight;
            if (y2 < 0)
                y2 = 0;
            else if (y2 >= oldHeight)
                y2 = oldHeight - 1;
            newBitmapPixels[index] = iBuf[((y2 * oldWidth) + x2)];
            //same as : newBitmapPixels[(y * newWidth) + x] = previousData[(y2 * oldWidth) + x2];
            if (index > 0) {
                if ((index) % newWidth != 0) {
                    jint pixel2 = newBitmapPixels[index];
                    jint pixel = newBitmapPixels[index - 1];
                    //little endian
                    pixel2 = (pixel2 & 0xff) * 0.299 + ((pixel2 >> 8) & 0xff) * 0.587 +
                             ((pixel2 >> 16) & 0xff) * 0.114;
                    pixel = (pixel & 0xff) * 0.299 + ((pixel >> 8) & 0xff) * 0.587 +
                            ((pixel >> 16) & 0xff) * 0.114;

                    //could store the gray pixels here to use with colhash and rotations

                    hash |= ((pixel) < (pixel2));
                    hash <<= 1L;
                }

            }
            index++;
        }

    }

    //this happens anyway but try to move it along
    env->DeleteLocalRef(db);
    delete (newBitmapPixels);

    return hash;
}


/** Frees the memory allocated for the ByteBuffer, which MUST have been allocated via [.newUnsafeByteBuffer]
     * or in native code.  */
//add the code for a C++ object to hold the ptr and possibly to update within native
extern "C" JNIEXPORT void JNICALL
Java_com_droidteahouse_edo_ui_ArtViewModel_free(JNIEnv *env, jobject obj, jobject buffer) {

    free(buffer);
}
//whart if I create a global ref in this class and set it to the address of this buffer
//then method to get it from native?  use it to get the buffer with address??? is this method available?
extern "C" JNIEXPORT jobject JNICALL
Java_com_droidteahouse_edo_ui_ArtViewModel_newDisposableByteBuffer(JNIEnv *env, jobject obj,
                                                                   jint numBytes) {

    //set ibuf as glob ref w getter method in native
    //1.bundle for process stop  2. if gc'd get it again from native heap 3.get it from flatbuffer
    //or set buffer as glob and operate on here
    jobject buffer = env->NewDirectByteBuffer((char *) malloc(numBytes), numBytes);
    jint *iBuf = (jint *) env->GetDirectBufferAddress(buffer);
    // make global ref
    return buffer;

}
//89                                     //1718          //2627    //3536  //4445  //5354 //6263  //71n072
//01 12 23 34 45 56 67 78 --910 1011 1112 1213 1314 1415 1516 1617 --1819 1920
/**3 stepsisters algo
 * scales the image using the fastest, simplest algorithm called "nearest neighbor, greyscales,
 * and fingerprints all in one*/
extern "C" JNIEXPORT jlong JNICALL
Java_com_droidteahouse_edo_preload_MyPreloadModelProvider_nativeDhashBilinear(
        JNIEnv *env, jobject obj, jobject db, jint w2, jint h2, jint w,
        jint h) {
    jint *iBuf = (jint *) env->GetDirectBufferAddress(db);
    //jint *newBitmapPixels = new jint[w2 * h2];
    jint graypixel;
    //int[] temp = new int[w2 * h2];
    int a, b, c, d, x, y, index;
    float x_ratio = ((float) (w - 1)) / w2;
    float y_ratio = ((float) (h - 1)) / h2;
    float x_diff, y_diff, blue, red, green;
    int offset = 0;
    jlong hash = 0;
    for (int i = 0; i < h2; i++) {
        for (int j = 0; j < w2; j++) {
            x = (int) (x_ratio * j);
            y = (int) (y_ratio * i);
            x_diff = (x_ratio * j) - x;
            y_diff = (y_ratio * i) - y;
            index = (y * w + x);
            a = iBuf[index];
            b = iBuf[index + 1];
            c = iBuf[index + w];
            d = iBuf[index + w + 1];
//little endian
// red element
// Yb = Ab(1-w)(1-h) + Bb(w)(1-h) + Cb(h)(1-w) + Db(wh)
            red = (a & 0xff) * (1 - x_diff) * (1 - y_diff) + (b & 0xff) * (x_diff) * (1 - y_diff) +
                  (c & 0xff) * (y_diff) * (1 - x_diff) + (d & 0xff) * (x_diff * y_diff);

// green element
// Yg = Ag(1-w)(1-h) + Bg(w)(1-h) + Cg(h)(1-w) + Dg(wh)
            green = ((a >> 8) & 0xff) * (1 - x_diff) * (1 - y_diff) +
                    ((b >> 8) & 0xff) * (x_diff) * (1 - y_diff) +
                    ((c >> 8) & 0xff) * (y_diff) * (1 - x_diff) +
                    ((d >> 8) & 0xff) * (x_diff * y_diff);

// blue element
// Yr = Ar(1-w)(1-h) + Br(w)(1-h) + Cr(h)(1-w) + Dr(wh)
            blue = ((a >> 16) & 0xff) * (1 - x_diff) * (1 - y_diff) +
                   ((b >> 16) & 0xff) * (x_diff) * (1 - y_diff) +
                   ((c >> 16) & 0xff) * (y_diff) * (1 - x_diff) +
                   ((d >> 16) & 0xff) * (x_diff * y_diff);


            if (offset > 0 && offset % w2 != 0) {
                int graypixel2 = red * 0.299 + green * 0.587 + blue * 0.114;
                hash |= ((graypixel) < (graypixel2));
                hash <<= 1L;
                offset++;
                graypixel = graypixel2;
            } else if (offset == 0) {
                graypixel = red * 0.299 + green * 0.587 + blue * 0.114;


            }
/*
            temp[offset++] =
                    0xff000000 | // hardcode alpha
                    ((((int) red) << 16) & 0xff0000) |
                    ((((int) green) << 8) & 0xff00) |
                    ((int) blue);
                    */
        }
    }
    //this happens anyway but try to move it along
    env->DeleteLocalRef(db);
    return hash;
}
/*
store the address on the c++ obj globally, get it from c side via native calls
JNIEXPORT jlong JNICALL Java_com_eclipsesource_v8_V8__createIsolate
        (JNIEnv *env, jobject v8) {
    return reinterpret_cast<jlong>(new V8Runtime());
}
JNIEXPORT void JNICALL Java_com_eclipsesource_v8_V8__performReset
        (JNIEnv *env, jobject v8, jlong handle) {
    reinterpret_cast<V8Runtime>(handle)->Reset(...);
}
 */






