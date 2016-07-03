package com.sam_chordas.android.stockhawk.ui.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.activities.MyStocksActivity;

/**
 * Created by Kartik_ch on 6/12/2016.
 */
public class MyQuoteWidgetFactory implements RemoteViewsService.RemoteViewsFactory, Loader.OnLoadCompleteListener<Cursor> {
    private static final int ID_CURSOR_LOADER = 47;

    private static final String TAG = MyQuoteWidgetFactory.class.getSimpleName();

    private Context mContext;
    private int mAppWidgetId;

    //quote items
    private String[] mTestArr = new String[]{"This", "is", "a", "dummy", "list", "view", "on", "a", "widget"};

    private CursorLoader mCursorLoader;

    private Cursor mCursor;

    public MyQuoteWidgetFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        mCursorLoader = new CursorLoader(mContext, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
        mCursorLoader.registerListener(ID_CURSOR_LOADER, this);
        mCursorLoader.startLoading();
    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }

        if (mCursorLoader != null) {
            mCursorLoader.cancelLoad();
        }
    }

    @Override
    public int getCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews row =
                new RemoteViews(mContext.getPackageName(), R.layout.list_item_widget);

        mCursor.moveToPosition(i);
        row.setTextViewText(R.id.text_view_stock_name, mCursor.getString(mCursor.getColumnIndex("symbol")));
        row.setTextViewText(R.id.text_view_stock_price, mCursor.getString(mCursor.getColumnIndex("bid_price")));

        int color = -1;
        if (mCursor.getInt(mCursor.getColumnIndex("is_up")) == 1) {
            color = ContextCompat.getColor(mContext, R.color.md_green_500);
        } else {
            color = ContextCompat.getColor(mContext, R.color.md_red_500);
        }

        row.setTextColor(R.id.text_view_stock_price, color);

        Intent intent = new Intent();
        Bundle extras = new Bundle();
        intent.putExtra(MyStocksActivity.ARG_STOCK_NAME,
                mCursor.getString(mCursor.getColumnIndex("symbol")));
        extras.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        intent.putExtras(extras);
        row.setOnClickFillInIntent(R.id.relative_layout_stock_container, intent);

        return row;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor cursor) {
        mCursor = cursor;
        Log.d(TAG, "onLoadComplete: " + cursor.getCount());

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(
                new ComponentName(mContext, MyQuoteListWidgetProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list_view_quotes);
    }
}
