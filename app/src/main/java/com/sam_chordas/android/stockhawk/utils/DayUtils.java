package com.sam_chordas.android.stockhawk.utils;

import android.content.Context;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteHistoryGraphModel;
import com.sam_chordas.android.stockhawk.data.QuoteHistoryModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by Kartik_ch on 5/28/2016.
 */
public class DayUtils {
    public static final String DATE_FORMAT_2 = "yyyy-MM-dd";
    //private static final String[] DAYS_EN = new String[] {"", "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
    //private static final String[] DAYS_AR = new String[] {"", "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
    private static final String DATE_FORMAT = "dd/MM/yyyy";

    public static QuoteHistoryGraphModel getDaysWithQuotes(Context context, List<QuoteHistoryModel> quoteHistoryModels) throws ParseException {
        String[] daysArr = context.getResources().getStringArray(R.array.days_arr);

        Collections.reverse(quoteHistoryModels);

        String[] days = new String[quoteHistoryModels.size()];
        String[] dates = new String[quoteHistoryModels.size()];
        float[] open = new float[quoteHistoryModels.size()];
        float[] low = new float[quoteHistoryModels.size()];
        float[] high = new float[quoteHistoryModels.size()];
        float[] close = new float[quoteHistoryModels.size()];
        float[] adjClose = new float[quoteHistoryModels.size()];
        float[] volume = new float[quoteHistoryModels.size()];
        int maxClose = (int) Float.parseFloat(quoteHistoryModels.get(0).getClose());
        int minClose = (int) Float.parseFloat(quoteHistoryModels.get(0).getClose());

        for (int i = 0; i < quoteHistoryModels.size(); i++) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_2, Locale.getDefault());
            calendar.setTime(sdf.parse(quoteHistoryModels.get(i).getDate()));

            String day = daysArr[calendar.get(Calendar.DAY_OF_WEEK)];
            //day=day.substring(0, 3);
            String date = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(calendar.getTime());

            days[i] = day;
            dates[i] = date;

            QuoteHistoryModel quoteHistoryModel = quoteHistoryModels.get(i);
            open[i] = MathUtils.round(Float.parseFloat(quoteHistoryModel.getOpen()), 2);
            low[i] = MathUtils.round(Float.parseFloat(quoteHistoryModel.getLow()), 2);
            high[i] = MathUtils.round(Float.parseFloat(quoteHistoryModel.getHigh()), 2);
            close[i] = MathUtils.round(Float.parseFloat(quoteHistoryModel.getClose()), 2);
            adjClose[i] = MathUtils.round(Float.parseFloat(quoteHistoryModel.getAdjClose()), 2);
            volume[i] = MathUtils.round(Float.parseFloat(quoteHistoryModel.getVolume()), 2);

            if (maxClose < (int) close[i]) {
                maxClose = (int) close[i];
            } else {
                minClose = (int) close[i];
            }
        }

        QuoteHistoryGraphModel quoteHistoryGraphModel = new QuoteHistoryGraphModel();
        quoteHistoryGraphModel.setDays(days);
        quoteHistoryGraphModel.setDates(dates);
        quoteHistoryGraphModel.setOpen(open);
        quoteHistoryGraphModel.setLow(low);
        quoteHistoryGraphModel.setHigh(high);
        quoteHistoryGraphModel.setClose(close);
        quoteHistoryGraphModel.setAdjClose(adjClose);
        quoteHistoryGraphModel.setVolume(volume);
        quoteHistoryGraphModel.setMaxClose(maxClose);
        quoteHistoryGraphModel.setMinClose(minClose);

        return quoteHistoryGraphModel;
    }
}
