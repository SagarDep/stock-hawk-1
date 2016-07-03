package com.sam_chordas.android.stockhawk.data;

/**
 * Created by Kartik_ch on 5/29/2016.
 */
public class QuoteHistoryGraphModel {
    private String[] days;
    private String[] dates;
    private float[] open;
    private float[] low;
    private float[] high;
    private float[] close;
    private float[] adjClose;
    private float[] volume;
    private int maxClose;
    private int minClose;

    public String[] getDays() {
        return days;
    }

    public void setDays(String[] days) {
        this.days = days;
    }

    public String[] getDates() {
        return dates;
    }

    public void setDates(String[] dates) {
        this.dates = dates;
    }

    public float[] getOpen() {
        return open;
    }

    public void setOpen(float[] open) {
        this.open = open;
    }

    public float[] getLow() {
        return low;
    }

    public void setLow(float[] low) {
        this.low = low;
    }

    public float[] getHigh() {
        return high;
    }

    public void setHigh(float[] high) {
        this.high = high;
    }

    public float[] getClose() {
        return close;
    }

    public void setClose(float[] close) {
        this.close = close;
    }

    public float[] getAdjClose() {
        return adjClose;
    }

    public void setAdjClose(float[] adjClose) {
        this.adjClose = adjClose;
    }

    public float[] getVolume() {
        return volume;
    }

    public void setVolume(float[] volume) {
        this.volume = volume;
    }

    public int getMaxClose() {
        return maxClose;
    }

    public void setMaxClose(int maxClose) {
        this.maxClose = maxClose;
    }

    public int getMinClose() {
        return minClose;
    }

    public void setMinClose(int minClose) {
        this.minClose = minClose;
    }
}
