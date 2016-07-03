package com.sam_chordas.android.stockhawk.utils;

import java.math.BigDecimal;

/**
 * Created by Kartik_ch on 6/26/2016.
 */
public class MathUtils {
    /**
     * Round to certain number of decimals
     * <p>Source: http://stackoverflow.com/a/8911683</p>
     *
     * @param d
     * @param decimalPlace
     * @return
     */
    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
}
