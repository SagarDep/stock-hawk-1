package com.sam_chordas.android.stockhawk.ui.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by Kartik_ch on 6/12/2016.
 */
public class MyQuoteWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MyQuoteWidgetFactory(getApplicationContext(),
                intent);
    }
}
