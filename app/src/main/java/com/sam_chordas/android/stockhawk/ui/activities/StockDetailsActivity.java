package com.sam_chordas.android.stockhawk.ui.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.fragments.StockDetailsFragment;
import com.sam_chordas.android.stockhawk.utils.LocaleUtil;
import com.sam_chordas.android.stockhawk.utils.SharedPrefUtil;

public class StockDetailsActivity extends AppCompatActivity {
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_details);

        bindViews();

        initToolbar();

        initDetailsFragment();
    }

    private void bindViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initDetailsFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frame_layout_stock_details,
                StockDetailsFragment.newInstance(getIntent().getExtras()),
                StockDetailsFragment.class.getSimpleName());
        fragmentTransaction.commit();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        LocaleUtil.changeLocale(this,
                new SharedPrefUtil(this).getString(MyStocksActivity.ARG_LOCALE_TYPE, LocaleUtil.LOCALE_EN));

        setContentView(R.layout.activity_stock_details);

        bindViews();

        initToolbar();

        initDetailsFragment();
    }
}
