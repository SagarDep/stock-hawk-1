package com.sam_chordas.android.stockhawk.ui.activities;

import android.app.LoaderManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.melnykov.fab.FloatingActionButton;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.RestUtils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;
import com.sam_chordas.android.stockhawk.ui.adapters.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.ui.widget.MyQuoteListWidgetProvider;
import com.sam_chordas.android.stockhawk.utils.LocaleUtil;
import com.sam_chordas.android.stockhawk.utils.NetworkConnectionUtil;
import com.sam_chordas.android.stockhawk.utils.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.utils.SharedPrefUtil;

public class MyStocksActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener, RecyclerViewItemClickListener.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    public static final String ARG_STOCK_NAME = "stock_name";
    public static final String ARG_LOCALE_TYPE = "locale_type";
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */

    private static final int CURSOR_LOADER_ID = 0;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private Intent mServiceIntent;
    private ItemTouchHelper mItemTouchHelper;
    private QuoteCursorAdapter mCursorAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;
    private RelativeLayout mRelativeLayoutErrScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocaleUtil.changeLocale(this,
                new SharedPrefUtil(this).getString(ARG_LOCALE_TYPE, LocaleUtil.LOCALE_EN));

        setContentView(R.layout.activity_my_stocks);

        bindViews();

        // The intent service is for executing immediate pulls from the Yahoo API
        // GCMTaskService can only schedule tasks, they cannot execute immediately
        if (savedInstanceState == null) {
            initInitialService();
        }

        initRecyclerView();

        //associate the current recycler view with fab
        mFab.attachToRecyclerView(mRecyclerView);

        //add some listeners
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this, this));
        mFab.setOnClickListener(this);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        //get title
        mTitle = getTitle();

        initPeriodicTasks();
    }

    private void bindViews() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mRelativeLayoutErrScreen = (RelativeLayout) findViewById(R.id.relative_layout_err_screen);
    }

    private void initInitialService() {
        mServiceIntent = new Intent(this, StockIntentService.class);
        // Run the initialize task service so that some stocks appear upon an empty database
        mServiceIntent.putExtra("tag", "init");
        if (NetworkConnectionUtil.isNetworkAvailable(this)) {
            startService(mServiceIntent);
        } else {
            if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
            }

            NetworkConnectionUtil.showNetworkUnavailableDialog(this);
        }
    }

    private void initRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        mCursorAdapter = new QuoteCursorAdapter(this, null);
        mRecyclerView.setAdapter(mCursorAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    private void initPeriodicTasks() {
        if (NetworkConnectionUtil.isNetworkAvailable(MyStocksActivity.this)) {
            long period = 3600L;
            long flex = 10L;
            String periodicTag = "periodic";

            // create a periodic task to pull stocks once every hour after the app has been opened. This
            // is so Widget data stays up to date.
            PeriodicTask periodicTask = new PeriodicTask.Builder()
                    .setService(StockTaskService.class)
                    .setPeriod(period)
                    .setFlex(flex)
                    .setTag(periodicTag)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setRequiresCharging(false)
                    .build();
            // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
            // are updated.
            GcmNetworkManager.getInstance(this).schedule(periodicTask);
        }
    }

    //normal click listener
    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.fab:
                if (NetworkConnectionUtil.isNetworkAvailable(this)) {
                    addNewStock();
                } else {
                    NetworkConnectionUtil.showNetworkUnavailableDialog(this);
                }
                break;
        }
    }

    //recycler view item click listener
    @Override
    public void onItemClick(View v, int position) {
        Cursor cursor = mCursorAdapter.getCursor();
        if (!cursor.moveToPosition(position)) {
            throw new IllegalStateException("Could not move Cursor to position: " + position);
        }
        String stockName = cursor.getString(cursor.getColumnIndex("symbol"));

        //redirect user to stock details activity
        Intent intent = new Intent(this, StockDetailsActivity.class);
        intent.putExtra(ARG_STOCK_NAME, stockName);
        startActivity(intent);
    }

    //called when swipe refresh layout is set to refresh state
    @Override
    public void onRefresh() {
        //recall the service, so that we can refresh the current data
        initInitialService();
    }

    private void addNewStock() {
        new MaterialDialog.Builder(MyStocksActivity.this).title(R.string.symbol_search)
                .content(R.string.content_test)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        // On FAB click, receive user input. Make sure the stock doesn't already exist
                        // in the DB and proceed accordingly
                        Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                                new String[]{QuoteColumns.SYMBOL}, QuoteColumns.SYMBOL + "= ?",
                                new String[]{input.toString()}, null);
                        if (c.getCount() != 0) {
                            Toast toast =
                                    Toast.makeText(MyStocksActivity.this, "This stock is already saved!",
                                            Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                            toast.show();
                            return;
                        } else {
                            // Add the stock to DB
                            mServiceIntent.putExtra("tag", "add");
                            mServiceIntent.putExtra("symbol", input.toString());
                            startService(mServiceIntent);
                        }
                    }
                })
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_stocks, menu);
        restoreActionBar();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_change_language) {
            onChangeLanguage();
        } else if (id == R.id.action_about) {
            onAbout();
        } else if (id == R.id.action_change_units) {
            // this is for changing stock changes from percent value to dollar value
            RestUtils.showPercent = !RestUtils.showPercent;
            this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
        }

        return super.onOptionsItemSelected(item);
    }

    private void onChangeLanguage() {
        new MaterialDialog.Builder(this)
                .title(R.string.change_language)
                .items(R.array.languages_arr)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        switch (which) {
                            case 0:
                                new SharedPrefUtil(MyStocksActivity.this).saveString(ARG_LOCALE_TYPE, LocaleUtil.LOCALE_EN);
                                break;
                            case 1:
                                new SharedPrefUtil(MyStocksActivity.this).saveString(ARG_LOCALE_TYPE, LocaleUtil.LOCALE_AR);
                                break;
                        }
                        Intent intent = new Intent(MyStocksActivity.this, MyStocksActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        return false;
                    }
                })
                .positiveText(R.string.change)
                .negativeText(R.string.cancel)
                .build()
                .show();
    }

    private void onAbout() {
        Toast.makeText(MyStocksActivity.this, "Not yet implemented!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This narrows the return to only the stocks that are most current.
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
        mCursorAdapter.swapCursor(data);
        if (mCursorAdapter.getItemCount() != 0) {
            mRelativeLayoutErrScreen.setVisibility(View.GONE);
        }

        // update all the available widgets
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

        Intent intent = new Intent(this, MyQuoteListWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that
        // source: http://stackoverflow.com/a/7738687
        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(getApplicationContext(), MyQuoteListWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
