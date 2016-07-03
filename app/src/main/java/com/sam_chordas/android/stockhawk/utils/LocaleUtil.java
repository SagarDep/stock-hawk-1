package com.sam_chordas.android.stockhawk.utils;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

/**
 * Created by Kartik_ch on 6/25/2016.
 */
public class LocaleUtil {
    public static final String LOCALE_EN = "en";
    public static final String LOCALE_AR = "ar";

    /**
     * <p>Change locale of the application.</p>
     * <p>Source: http://stackoverflow.com/a/9173571</p>
     *
     * @param context Context of the application
     * @param locale  Locale you want to put
     */
    public static void changeLocale(Context context, LOCALE locale) {
        String languageToLoad = LOCALE_EN;

        switch (locale) {
            case EN:
                languageToLoad = LOCALE_EN;
                break;
            case AR:
                languageToLoad = LOCALE_AR;
                break;
        }

        Locale newLocale = new Locale(languageToLoad);
        Locale.setDefault(newLocale);
        Configuration config = new Configuration();
        config.locale = newLocale;
        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());
    }

    /**
     * <p>Change locale of the application.</p>
     * <p>Source: http://stackoverflow.com/a/9173571</p>
     *
     * @param context Context of the application
     * @param locale  Locale you want to put
     */
    public static void changeLocale(Context context, String locale) {
        Locale newLocale = new Locale(locale);
        Locale.setDefault(newLocale);
        Configuration config = new Configuration();
        config.locale = newLocale;
        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());
    }

    public enum LOCALE {
        EN,
        AR
    }
}
