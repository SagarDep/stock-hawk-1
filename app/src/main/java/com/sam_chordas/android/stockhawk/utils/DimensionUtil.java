package com.sam_chordas.android.stockhawk.utils;

import android.content.res.Resources;

/**
 * Created by Kartik_ch on 2/14/2016.
 */
public class DimensionUtil {
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
}
