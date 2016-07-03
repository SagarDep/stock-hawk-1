package com.sam_chordas.android.stockhawk.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Kartik_ch on 5/22/2016.
 */
public class QuoteHistoryModel implements Parcelable {
    // Creator
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public QuoteHistoryModel createFromParcel(Parcel in) {
            return new QuoteHistoryModel(in);
        }

        public QuoteHistoryModel[] newArray(int size) {
            return new QuoteHistoryModel[size];
        }
    };
    private String symbol;
    private String date;
    private String open;
    private String high;
    private String low;
    private String close;
    private String volume;
    private String adjClose;
    private String type;

    // "De-parcel object
    public QuoteHistoryModel(Parcel in) {
        symbol = in.readString();
        date = in.readString();
        open = in.readString();
        high = in.readString();
        low = in.readString();
        close = in.readString();
        volume = in.readString();
        adjClose = in.readString();
        type = in.readString();
    }

    public QuoteHistoryModel() {

    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public String getClose() {
        return close;
    }

    public void setClose(String close) {
        this.close = close;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getAdjClose() {
        return adjClose;
    }

    public void setAdjClose(String adjClose) {
        this.adjClose = adjClose;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(symbol);
        parcel.writeString(date);
        parcel.writeString(open);
        parcel.writeString(high);
        parcel.writeString(low);
        parcel.writeString(close);
        parcel.writeString(volume);
        parcel.writeString(adjClose);
        parcel.writeString(type);
    }
}
