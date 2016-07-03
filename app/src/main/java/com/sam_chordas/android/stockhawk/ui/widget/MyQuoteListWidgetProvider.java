package com.sam_chordas.android.stockhawk.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.activities.MyStocksActivity;
import com.sam_chordas.android.stockhawk.ui.activities.StockDetailsActivity;

/**
 * Created by Kartik_ch on 6/5/2016.
 * This widget is inspired from https://github.com/commonsguy/cw-advandroid/tree/master/AppWidget/LoremWidget
 */
public class MyQuoteListWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        int numberOfWidgetsAvail = appWidgetIds.length;

        for (int i = 0; i < numberOfWidgetsAvail; i++) {
            int appWidgetId = appWidgetIds[i];

            Intent widgetServiceIntent = new Intent(context, MyQuoteWidgetService.class);
            widgetServiceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            widgetServiceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            widgetServiceIntent.setData(Uri.parse(widgetServiceIntent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.layout_widget);

            views.setRemoteAdapter(R.id.list_view_quotes, widgetServiceIntent);

            views.setEmptyView(R.id.list_view_quotes, R.id.text_view_stock_unavailable);

            Intent clickIntentMyStocks = new Intent(context, MyStocksActivity.class);
            PendingIntent clickPendingIntentMyStocks = PendingIntent
                    .getActivity(context, 0,
                            clickIntentMyStocks,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.text_view_stock_unavailable, clickPendingIntentMyStocks);

            Intent clickIntentMyStockDetails = new Intent(context, StockDetailsActivity.class);
            PendingIntent clickPendingIntentMyStockDetails = PendingIntent
                    .getActivity(context, 0,
                            clickIntentMyStockDetails,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.list_view_quotes, clickPendingIntentMyStockDetails);

            appWidgetManager.updateAppWidget(appWidgetIds[i], views);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
