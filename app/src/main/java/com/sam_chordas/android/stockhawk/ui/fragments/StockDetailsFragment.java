package com.sam_chordas.android.stockhawk.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteHistoryModel;
import com.sam_chordas.android.stockhawk.service.StockDetailsIntentService;
import com.sam_chordas.android.stockhawk.service.StockDetailsResultReceiver;
import com.sam_chordas.android.stockhawk.ui.activities.MyStocksActivity;
import com.sam_chordas.android.stockhawk.ui.adapters.QuoteHistoryRecyclerAdapter;
import com.sam_chordas.android.stockhawk.utils.NetworkConnectionUtil;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class StockDetailsFragment extends Fragment implements StockDetailsResultReceiver.Receiver {
    public static final String TAG = StockDetailsFragment.class.getSimpleName();

    private LineChartView mLineChartViewStockPrices;
    private RecyclerView mRecyclerViewQuoteHistory;
    //private MaterialProgressBar mMaterialProgressBar;
    private RelativeLayout mRelativeLayoutErrScreen;
    private RelativeLayout mRelativeLayoutLoading;

    private QuoteHistoryRecyclerAdapter mQuoteHistoryRecyclerAdapter;

    private ArrayList<QuoteHistoryModel> mQuoteHistoryModels;

    private Menu mMenu;

    public static StockDetailsFragment newInstance(Bundle args) {
        StockDetailsFragment fragment = new StockDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_details, container, false);
        bindViews(view);
        return view;
    }

    private void bindViews(View view) {
        mLineChartViewStockPrices = (LineChartView) view.findViewById(R.id.line_chart_stock_prices);
        mRecyclerViewQuoteHistory = (RecyclerView) view.findViewById(R.id.recycler_view_quote_history);
        //mMaterialProgressBar= (MaterialProgressBar) view.findViewById(R.id.progress_loading);
        mRelativeLayoutErrScreen = (RelativeLayout) view.findViewById(R.id.relative_layout_err_screen);
        mRelativeLayoutLoading = (RelativeLayout) view.findViewById(R.id.relative_layout_loading);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (NetworkConnectionUtil.isNetworkAvailable(getActivity())) {
            mRelativeLayoutLoading.setVisibility(View.VISIBLE);
        }
        loadStockDetails();
        initToolbar();
    }

    private void initToolbar() {
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setTitle(getArguments().getString(MyStocksActivity.ARG_STOCK_NAME));
    }

    /**
     * this implementation on result receiver is used from here: http://javatechig.com/android/creating-a-background-service-in-android
     */
    private void loadStockDetails() {
        StockDetailsResultReceiver receiver = new StockDetailsResultReceiver(new Handler());
        receiver.setReceiver(this);
        Intent intent = new Intent(Intent.ACTION_SYNC, null, getActivity(), StockDetailsIntentService.class);

        /* Send optional extras to StockDetailsIntentService */
        String stockName = getArguments().getString(MyStocksActivity.ARG_STOCK_NAME);
        intent.putExtra(MyStocksActivity.ARG_STOCK_NAME, stockName);
        intent.putExtra("receiver", receiver);
        intent.putExtra("requestId", StockDetailsResultReceiver.RECEIVER_ID);

        getActivity().startService(intent);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        // check if current activity is available or not
        if (getActivity() != null) {
            boolean error = resultData.getBoolean(StockDetailsIntentService.ARG_ERROR);
            String response = resultData.getString(StockDetailsIntentService.ARG_RESPONSE);
            mQuoteHistoryModels = resultData.getParcelableArrayList(StockDetailsIntentService.ARG_DATA);

            if (error) {
                onFailure(response);
            } else {
                try {
                    onSuccess(mQuoteHistoryModels);
                } catch (ParseException e) {
                    Log.e(TAG, e.getMessage(), e);
                    onFailure(e.getMessage());
                }
            }
        }
    }

    private void onSuccess(final ArrayList<QuoteHistoryModel> quoteHistoryModels) throws ParseException {
        if (mMenu != null &&
                mMenu.findItem(R.id.action_refresh).getActionView() != null) {
            mMenu.findItem(R.id.action_refresh).setActionView(null);
        }

        // for some reason material progress bar's visibility won't be set gone after
        // an orientation change
        mRelativeLayoutLoading.setVisibility(View.GONE);

        mRelativeLayoutErrScreen.setVisibility(View.GONE);

        for (int i = 0; i < quoteHistoryModels.size(); i++) {
            quoteHistoryModels.get(i).setType(QuoteHistoryRecyclerAdapter.TYPE_LISTING);
        }

        QuoteHistoryModel quoteHistoryModelData = new QuoteHistoryModel();
        quoteHistoryModelData.setType(QuoteHistoryRecyclerAdapter.TYPE_DATA);

        QuoteHistoryModel quoteHistoryModelGraph = new QuoteHistoryModel();
        quoteHistoryModelGraph.setType(QuoteHistoryRecyclerAdapter.TYPE_GRAPH);

        QuoteHistoryModel quoteHistoryModelTitle = new QuoteHistoryModel();
        quoteHistoryModelTitle.setType(QuoteHistoryRecyclerAdapter.TYPE_TITLE);

        quoteHistoryModels.add(0, quoteHistoryModelData);
        quoteHistoryModels.add(1, quoteHistoryModelGraph);
        quoteHistoryModels.add(2, quoteHistoryModelTitle);

        //update recycler view
        mQuoteHistoryRecyclerAdapter = new QuoteHistoryRecyclerAdapter(quoteHistoryModels);
        mRecyclerViewQuoteHistory.setAdapter(mQuoteHistoryRecyclerAdapter);
    }

    private void onFailure(String response) {
        mRelativeLayoutLoading.setVisibility(View.GONE);

        if (mQuoteHistoryRecyclerAdapter == null || mQuoteHistoryRecyclerAdapter.getItemCount() == 0) {
            mRelativeLayoutErrScreen.setVisibility(View.VISIBLE);
        }

        Toast.makeText(getActivity(), "Error: " + true + ", Response: " + response, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_stock_details, menu);
        mMenu = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_refresh:
                if (NetworkConnectionUtil.isNetworkAvailable(getActivity())) {
                    item.setActionView(R.layout.progress_view);
                    loadStockDetails();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
