package com.jahanwalsh.meerkat;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.squareup.picasso.Transformation;

/**
 * Created by Guest on 6/21/17.
 */

public class MyTransform implements Transformation {

    @Override
    public Bitmap transform(Bitmap source) {
        final Bitmap copy = source.copy(source.getConfig(), true);
        source.recycle();
        final Canvas canvas = new Canvas(copy);
        canvas.drawColor(0);
        return copy;
    }

    @Override
    public String key() {
        return "darken";
    }


}
