package com.droidteahouse.edo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;

import java.security.MessageDigest;

public class GrayscaleTransformation implements Transformation<Bitmap> {

  private BitmapPool mBitmapPool;

  public GrayscaleTransformation(BitmapPool pool) {
    mBitmapPool = pool;
  }

  @Override
  public Resource<Bitmap> transform(Context context, Resource<Bitmap> resource, int outWidth, int outHeight) {
    Bitmap source = resource.get();

    int width = source.getWidth();
    int height = source.getHeight();

    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

    Canvas canvas = new Canvas(bitmap);
    ColorMatrix saturation = new ColorMatrix();
    saturation.setSaturation(0f);
    Paint paint = new Paint();
    paint.setColorFilter(new ColorMatrixColorFilter(saturation));
    canvas.drawBitmap(source, 0, 0, paint);
    source.recycle();

    return BitmapResource.obtain(bitmap, mBitmapPool);
  }


  @Override
  public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {

  }
}