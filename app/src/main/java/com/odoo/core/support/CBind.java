package com.odoo.core.support;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public final class CBind {


    public static void setImage(View view, Bitmap bitmap) {
        if (view instanceof ImageView && bitmap != null) {
            ((ImageView) view).setImageBitmap(bitmap);
        }
    }

    public static void setImage(View view, int res_id) {
        if (view instanceof ImageView) {
            ((ImageView) view).setImageResource(res_id);
        }
    }

    public static void setText(View view, String value) {
        if (view instanceof TextView) {
            ((TextView) view).setText(value);
        }
    }
}
