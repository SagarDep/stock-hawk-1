package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import com.sam_chordas.android.stockhawk.data.QuoteHistoryModel;
import com.sam_chordas.android.stockhawk.ui.activities.MyStocksActivity;
import com.sam_chordas.android.stockhawk.ui.fragments.StockDetailsFragment;
import com.sam_chordas.android.stockhawk.utils.DayUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Kartik_ch on 5/22/2016.
 */
public class StockDetailsIntentService extends IntentService implements Callback {
    public static final String ARG_ERROR = "error";
    public static final String ARG_RESPONSE = "response";
    public static final String ARG_DATA = "data";
    private static final String YAHOO_QUERY_MAIN = "select * from yahoo.finance.historicaldata";
    private static final String WHERE = "where";
    private static final String SYMBOL_IN = "symbol in";
    private static final String AND = "and";
    private static final String START_DATE = "startDate";
    private static final String END_DATE = "endDate";
    private static final String EQUALS = "=";
    private static final String URL_SCHEME = "https";
    private static final String URL_AUTHORITY = "query.yahooapis.com";
    private static final String URL_VERSION = "v1";
    private static final String URL_PUBLIC = "public";
    private static final String URL_YQL = "yql";
    private static final String URL_PARAM_Q = "q";
    private static final String URL_PARAM_FORMAT = "format";
    private static final String URL_PARAM_ENV = "env";
    private static final String URL_PARAM_CALLBACK = "callback";
    private static final String JSON = "json";
    private static final String ENV_DATA = "store://datatables.org/alltableswithkeys";
    private static final String SPACE = " ";
    //json params
    private static final String PARAM_QUERY = "query";
    private static final String PARAM_COUNT = "count";
    private static final String PARAM_RESULTS = "results";
    private static final String PARAM_QUOTE = "quote";
    private static final String PARAM_SYMBOL = "Symbol";
    private static final String PARAM_DATE = "Date";
    private static final String PARAM_OPEN = "Open";
    private static final String PARAM_HIGH = "High";
    private static final String PARAM_LOW = "Low";
    private static final String PARAM_CLOSE = "Close";
    private static final String PARAM_VOLUME = "Volume";
    private static final String PARAM_ADJ_CLOSE = "Adj_Close";

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final String TAG = StockDetailsFragment.class.getSimpleName();

    private ResultReceiver mResultReceiver;

    public StockDetailsIntentService() {
        super(StockDetailsIntentService.class.getName());
    }

    public StockDetailsIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(StockDetailsIntentService.class.getSimpleName(), "Stock Details Intent Service");

        mResultReceiver = intent.getParcelableExtra(StockDetailsResultReceiver.RECEIVER);

        Bundle args = intent.getExtras();
        String stockName = args.getString(MyStocksActivity.ARG_STOCK_NAME);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -8);
        String startDate = new SimpleDateFormat(DATE_FORMAT, Locale.US).format(calendar.getTime());
        String endDate = new SimpleDateFormat(DATE_FORMAT, Locale.US).format(new Date());

        String query = new StringBuilder()
                .append(YAHOO_QUERY_MAIN + SPACE)
                .append(WHERE + SPACE)
                .append(SYMBOL_IN + SPACE)
                .append("('" + stockName + "')" + SPACE)
                .append(AND + SPACE)
                .append(START_DATE + EQUALS)
                .append("'" + startDate + "'" + SPACE)
                .append(AND + SPACE)
                .append(END_DATE + EQUALS + SPACE)
                .append("'" + endDate + "'")
                .toString();

        Uri.Builder builder = new Uri.Builder()
                .scheme(URL_SCHEME)
                .authority(URL_AUTHORITY)
                .appendPath(URL_VERSION)
                .appendPath(URL_PUBLIC)
                .appendPath(URL_YQL)
                .appendQueryParameter(URL_PARAM_Q, query)
                .appendQueryParameter(URL_PARAM_FORMAT, JSON)
                .appendQueryParameter(URL_PARAM_ENV, ENV_DATA)
                .appendQueryParameter(URL_PARAM_CALLBACK, "");

        String url = builder.build().toString();

        Log.e(TAG, url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(this);
    }

    @Override
    public void onFailure(Call call, IOException e) {
        Log.e(TAG, e.getMessage(), e);
        Bundle args = new Bundle();
        args.putBoolean(ARG_ERROR, true);
        args.putString(ARG_RESPONSE, e.getMessage());
        mResultReceiver.send(StockDetailsResultReceiver.RECEIVER_ID, args);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        String responseStr = response.body().string();
        Log.e(TAG, "Response from server: " + responseStr);

        Bundle args = new Bundle();

        //parse json retrieved from server
        try {
            JSONObject jsonObjectResponse = new JSONObject(responseStr);

            JSONObject jsonObjectQuery = jsonObjectResponse.getJSONObject(PARAM_QUERY);

            int count = jsonObjectQuery.getInt(PARAM_COUNT);

            if (count == 0) {
                args.putBoolean(ARG_ERROR, true);
                args.putString(ARG_RESPONSE, "no_history_available");
                mResultReceiver.send(StockDetailsResultReceiver.RECEIVER_ID, args);
                return;
            }

            JSONObject jsonObjectResults = jsonObjectQuery.getJSONObject(PARAM_RESULTS);

            JSONArray jsonArrayQuote = jsonObjectResults.getJSONArray(PARAM_QUOTE);

            ArrayList<QuoteHistoryModel> quoteHistoryModels = new ArrayList<>();
            for (int i = 0; i < jsonArrayQuote.length(); i++) {
                JSONObject jsonObjectQuote = jsonArrayQuote.getJSONObject(i);
                String symbol = jsonObjectQuote.getString(PARAM_SYMBOL);
                String date = jsonObjectQuote.getString(PARAM_DATE);
                String open = jsonObjectQuote.getString(PARAM_OPEN);
                String high = jsonObjectQuote.getString(PARAM_HIGH);
                String low = jsonObjectQuote.getString(PARAM_LOW);
                String close = jsonObjectQuote.getString(PARAM_CLOSE);
                String volume = jsonObjectQuote.getString(PARAM_VOLUME);
                String adjClose = jsonObjectQuote.getString(PARAM_ADJ_CLOSE);

                QuoteHistoryModel quoteHistoryModel = new QuoteHistoryModel();
                quoteHistoryModel.setSymbol(symbol);

                String customizedDate = null;
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(DayUtils.DATE_FORMAT_2, Locale.getDefault());
                    Date dateObj = sdf.parse(date);
                    customizedDate = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(dateObj);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                quoteHistoryModel.setDate(customizedDate);

                quoteHistoryModel.setOpen(open);
                quoteHistoryModel.setHigh(high);
                quoteHistoryModel.setLow(low);
                quoteHistoryModel.setClose(close);
                quoteHistoryModel.setVolume(volume);
                quoteHistoryModel.setAdjClose(adjClose);

                quoteHistoryModels.add(quoteHistoryModel);
            }

            args.putBoolean(ARG_ERROR, false);
            args.putParcelableArrayList(ARG_DATA, quoteHistoryModels);
            mResultReceiver.send(StockDetailsResultReceiver.RECEIVER_ID, args);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
            args.putBoolean(ARG_ERROR, true);
            args.putString(ARG_RESPONSE, e.getMessage());
            mResultReceiver.send(StockDetailsResultReceiver.RECEIVER_ID, args);
        }
    }
}
