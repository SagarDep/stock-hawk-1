package com.sam_chordas.android.stockhawk.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteHistoryGraphModel;
import com.sam_chordas.android.stockhawk.data.QuoteHistoryModel;
import com.sam_chordas.android.stockhawk.ui.activities.MyStocksActivity;
import com.sam_chordas.android.stockhawk.utils.DayUtils;
import com.sam_chordas.android.stockhawk.utils.DimensionUtil;
import com.sam_chordas.android.stockhawk.utils.LocaleUtil;
import com.sam_chordas.android.stockhawk.utils.SharedPrefUtil;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Kartik_ch on 5/29/2016.
 */
public class QuoteHistoryRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TYPE_DATA = "DATA";
    public static final String TYPE_GRAPH = "GRAPH";
    public static final String TYPE_TITLE = "TITLE";
    public static final String TYPE_LISTING = "LISTING";

    private static final int VIEW_TYPE_DATA = 0;
    private static final int VIEW_TYPE_GRAPH = 1;
    private static final int VIEW_TYPE_TITLE = 2;
    private static final int VIEW_TYPE_LISTING = 3;

    private ArrayList<QuoteHistoryModel> mQuoteHistoryModels;
    private int mSelectedDataPosition;

    public QuoteHistoryRecyclerAdapter(ArrayList<QuoteHistoryModel> quoteHistoryModels) {
        this.mQuoteHistoryModels = quoteHistoryModels;
    }

    public ArrayList<QuoteHistoryModel> getQuoteHistoryModels() {
        return mQuoteHistoryModels;
    }

    public void updateQuoteHistoryModels(ArrayList<QuoteHistoryModel> quoteHistoryModels) {
        this.mQuoteHistoryModels = quoteHistoryModels;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case VIEW_TYPE_DATA:
                View viewData = layoutInflater.inflate(R.layout.recycler_item_data, parent, false);
                viewHolder = new ViewHolderData(viewData);
                break;
            case VIEW_TYPE_GRAPH:
                View viewGraph = layoutInflater.inflate(R.layout.recycler_item_stock_graph, parent, false);
                viewHolder = new ViewHolderGraph(viewGraph);
                break;
            case VIEW_TYPE_TITLE:
                View viewTitle = layoutInflater.inflate(R.layout.recycler_item_quote_history, parent, false);
                viewHolder = new ViewHolderListing(viewTitle);
                break;
            case VIEW_TYPE_LISTING:
                View viewListing = layoutInflater.inflate(R.layout.recycler_item_quote_history, parent, false);
                viewHolder = new ViewHolderListing(viewListing);
                break;
        }
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        if (TextUtils.equals(mQuoteHistoryModels.get(position).getType(), TYPE_DATA)) {
            return VIEW_TYPE_DATA;
        } else if (TextUtils.equals(mQuoteHistoryModels.get(position).getType(), TYPE_GRAPH)) {
            return VIEW_TYPE_GRAPH;
        } else if (TextUtils.equals(mQuoteHistoryModels.get(position).getType(), TYPE_TITLE)) {
            return VIEW_TYPE_TITLE;
        } else if (TextUtils.equals(mQuoteHistoryModels.get(position).getType(), TYPE_LISTING)) {
            return VIEW_TYPE_LISTING;
        }
        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (TextUtils.equals(mQuoteHistoryModels.get(position).getType(), TYPE_DATA)) {
            configureDataViewHolder((ViewHolderData) holder, position);
        } else if (TextUtils.equals(mQuoteHistoryModels.get(position).getType(), TYPE_GRAPH)) {
            try {
                configureGraphViewHolder((ViewHolderGraph) holder, position);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (TextUtils.equals(mQuoteHistoryModels.get(position).getType(), TYPE_TITLE)) {
            configureTitleViewHolder((ViewHolderListing) holder, position);
        } else if (TextUtils.equals(mQuoteHistoryModels.get(position).getType(), TYPE_LISTING)) {
            configureListingViewHolder((ViewHolderListing) holder, position);
        }
    }

    private void configureDataViewHolder(ViewHolderData holder, int position) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder("");
        try {
            spannableStringBuilder = getSelectedData(holder.mContext);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.mTxtStockPrice.setText(spannableStringBuilder);
    }

    private void configureGraphViewHolder(ViewHolderGraph holder, int position) throws ParseException {
        List<QuoteHistoryModel> quoteHistoryModels = new ArrayList<>();
        for (int i = 3; i < mQuoteHistoryModels.size(); i++) {
            quoteHistoryModels.add(mQuoteHistoryModels.get(i));
        }

        final QuoteHistoryGraphModel quoteHistoryGraphModel = DayUtils.getDaysWithQuotes(holder.itemView.getContext(), quoteHistoryModels);

        // Data
        final LineSet dataset = new LineSet(quoteHistoryGraphModel.getDays(), quoteHistoryGraphModel.getClose());
        dataset.setColor(Color.parseColor("#b3b5bb"))
                .setFill(Color.parseColor("#2d374c"))
                .setDotsColor(Color.parseColor("#ffc755"))
                .setThickness(5);
        holder.mLineChartViewStockPrices.addData(dataset);

        // Chart
        holder.mLineChartViewStockPrices.setBorderSpacing(Tools.fromDpToPx(15))
                .setAxisBorderValues(quoteHistoryGraphModel.getMinClose() - 10, quoteHistoryGraphModel.getMaxClose() + 10)
                .setYLabels(AxisController.LabelPosition.NONE)
                .setLabelsColor(ContextCompat.getColor(holder.mContext, R.color.md_grey_200))
                .setXAxis(false)
                .setYAxis(false);

        holder.mLineChartViewStockPrices.show();
    }

    private SpannableStringBuilder getSelectedData(Context context) throws ParseException {
        List<QuoteHistoryModel> quoteHistoryModels = new ArrayList<>();
        for (int i = 3; i < mQuoteHistoryModels.size(); i++) {
            quoteHistoryModels.add(mQuoteHistoryModels.get(i));
        }

        QuoteHistoryGraphModel quoteHistoryGraphModel = DayUtils.getDaysWithQuotes(context, quoteHistoryModels);

        String high = context.getString(R.string.high) + ": " + String.format(Locale.getDefault(), "%.2f", quoteHistoryGraphModel.getHigh()[mSelectedDataPosition]);
        String low = context.getString(R.string.low) + ": " + String.format(Locale.getDefault(), "%.2f", quoteHistoryGraphModel.getLow()[mSelectedDataPosition]);
        String close = context.getString(R.string.close) + ": " + String.format(Locale.getDefault(), "%.2f", quoteHistoryGraphModel.getClose()[mSelectedDataPosition]);
        String day = String.format(Locale.getDefault(), "%s", quoteHistoryGraphModel.getDays()[mSelectedDataPosition]);
        String date = String.format(Locale.getDefault(), "%s", quoteHistoryGraphModel.getDates()[mSelectedDataPosition]);
        String finalDate = day + ", " + date;

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(high);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.md_blue_500)),
                0,
                high.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append(" " + context.getString(R.string.interpunct) + " ");
        spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD),
                high.length(),
                high.length() + 3,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append(low);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.md_red_500)),
                spannableStringBuilder.length() - low.length(),
                spannableStringBuilder.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append(" " + context.getString(R.string.interpunct) + " ");
        spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD),
                spannableStringBuilder.length() - 3,
                spannableStringBuilder.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append(close);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.md_green_500)),
                spannableStringBuilder.length() - close.length(),
                spannableStringBuilder.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append("\n");
        spannableStringBuilder.append(finalDate);
        spannableStringBuilder.setSpan(new AbsoluteSizeSpan(DimensionUtil.dpToPx(15)),
                spannableStringBuilder.length() - finalDate.length(),
                spannableStringBuilder.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableStringBuilder;
    }

    private void configureTitleViewHolder(ViewHolderListing holder, int position) {
        String date = holder.itemView.getContext().getString(R.string.date);
        String high = holder.itemView.getContext().getString(R.string.high);
        String low = holder.itemView.getContext().getString(R.string.low);
        String close = holder.itemView.getContext().getString(R.string.close);
        int textSize = 20;

        holder.mTxtDate.setText(date);
        holder.mTxtHigh.setText(high);
        holder.mTxtLow.setText(low);
        holder.mTxtClose.setText(close);

        holder.mTxtDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        holder.mTxtHigh.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        holder.mTxtLow.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        holder.mTxtClose.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);

        int gravity = Gravity.END;
        int gravityDate = Gravity.LEFT;
        if (TextUtils.equals(new SharedPrefUtil(holder.itemView.getContext()).getString(MyStocksActivity.ARG_LOCALE_TYPE, LocaleUtil.LOCALE_EN),
                LocaleUtil.LOCALE_AR)) {
            gravity = Gravity.LEFT;
            gravityDate = Gravity.LEFT;
        }

        holder.mTxtDate.setGravity(gravityDate);
        holder.mTxtHigh.setGravity(gravity);
        holder.mTxtLow.setGravity(gravity);
        holder.mTxtClose.setGravity(gravity);
    }

    private void configureListingViewHolder(ViewHolderListing holder, int position) {

        float highF = Float.parseFloat(mQuoteHistoryModels.get(position).getHigh());
        float lowF = Float.parseFloat(mQuoteHistoryModels.get(position).getLow());
        float closeF = Float.parseFloat(mQuoteHistoryModels.get(position).getClose());

        String high = String.format(Locale.getDefault(), "%.2f", highF);
        String low = String.format(Locale.getDefault(), "%.2f", lowF);
        String close = String.format(Locale.getDefault(), "%.2f", closeF);

        String date = mQuoteHistoryModels.get(position).getDate();

        holder.mTxtDate.setText(date);
        holder.mTxtHigh.setText(high);
        holder.mTxtLow.setText(low);
        holder.mTxtClose.setText(close);

        int gravity = Gravity.END;
        int gravityDate = Gravity.LEFT;
        if (TextUtils.equals(new SharedPrefUtil(holder.itemView.getContext()).getString(MyStocksActivity.ARG_LOCALE_TYPE, LocaleUtil.LOCALE_EN),
                LocaleUtil.LOCALE_AR)) {
            gravity = Gravity.LEFT;
            gravityDate = Gravity.LEFT;
        }

        holder.mTxtDate.setGravity(gravityDate);
        holder.mTxtHigh.setGravity(gravity);
        holder.mTxtLow.setGravity(gravity);
        holder.mTxtClose.setGravity(gravity);
    }


    @Override
    public int getItemCount() {
        return mQuoteHistoryModels.size();
    }

    public class ViewHolderData extends RecyclerView.ViewHolder {
        private Context mContext;
        private TextView mTxtStockPrice;

        public ViewHolderData(View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            mTxtStockPrice = (TextView) itemView.findViewById(R.id.text_view_stock_price);
        }
    }

    public class ViewHolderGraph extends RecyclerView.ViewHolder {
        private Context mContext;
        private LineChartView mLineChartViewStockPrices;

        public ViewHolderGraph(View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            mLineChartViewStockPrices = (LineChartView) itemView.findViewById(R.id.line_chart_stock_prices);
            mLineChartViewStockPrices.setOnEntryClickListener(new OnEntryClickListener() {
                @Override
                public void onClick(int setIndex, int entryIndex, Rect rect) {
                    mSelectedDataPosition = entryIndex;
                    notifyItemChanged(0);
                }
            });
        }
    }

    public class ViewHolderListing extends RecyclerView.ViewHolder {
        private TextView mTxtDate, mTxtHigh, mTxtLow, mTxtClose;

        public ViewHolderListing(View itemView) {
            super(itemView);

            mTxtDate = (TextView) itemView.findViewById(R.id.text_view_date);
            mTxtHigh = (TextView) itemView.findViewById(R.id.text_view_high);
            mTxtLow = (TextView) itemView.findViewById(R.id.text_view_low);
            mTxtClose = (TextView) itemView.findViewById(R.id.text_view_close);
        }
    }
}
