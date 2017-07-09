package com.galadar.example.stockxchange;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Galadar on 22/1/2017.
 *
 * This is a class with helper functions for converting price data to screen points/locations for drawing the chart.
 *
 * Some of these functions will be used for dragging and/or otherwise controlling the chart.
 */

public class PriceControl {

    public static final float MinRectWidth = 50.0f;

    float RectWidth, lineDiff;
    private int HORIZONTAL_BORDER, VERTICAL_BORDER;

    private SharedPreferences prefs;
    private int height, width, length;
    private int X_Bottom, X_Top, Y_Bottom, Y_Top;
    private boolean set;

    public PriceControl(Context context){
        prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        height = prefs.getInt("height", 720);
        width = prefs.getInt("width", 720);
        HORIZONTAL_BORDER = Math.round(width*0.05f);
        VERTICAL_BORDER = Math.round(height*0.1f);
        set = false;
    }

    public int getHORIZONTAL_BORDER() {
        return HORIZONTAL_BORDER;
    }

    public int getVERTICAL_BORDER() {
        return VERTICAL_BORDER;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void setObj(int maxX, int minX, int maxY, int minY, int len){
        X_Bottom = minX;
        X_Top = maxX;
        Y_Bottom = minY;
        Y_Top = maxY;
        length = len;
        RectWidth = getAvailWidth()/length;
        if(RectWidth<MinRectWidth) RectWidth=MinRectWidth;
        lineDiff = (float)getAvailWidth()/(float)length;
        set=true;
    }

    public int getX_Bottom() {
        if(set) return X_Bottom;
        else return 1;
    }

    public int getX_Top() {
        if(set) return X_Top;
        else return 1;
    }

    public int getY_Bottom() {
        if(set) return Y_Bottom;
        else return 1;
    }

    public int getY_Top() {
        if(set) return Y_Top;
        else return 1;
    }

    public int getAvailHeight() {
        return height-(2*VERTICAL_BORDER);
    }

    public int getAvailWidth(){
        return width-HORIZONTAL_BORDER;
    }

    public float PriceToYCoord(int price){
        if(set) {
            //float p = (float)price/100.0f;
            float k = height - Math.round( VERTICAL_BORDER + getAvailHeight() * (float)(price - Y_Bottom)/(float)(Y_Top - Y_Bottom) );
            //System.out.println("PriceCoords: "+k);
            return k;
        } else return 1;
    }

    public int LineXCoordToPos(float x){
        if(set) {
            x=x-HORIZONTAL_BORDER;
            int total = Math.round(x/lineDiff);
            if(total<0)total=0;
            else if(total>=length)total=length-1;
            return total;
        } else return 1;
    }

    public int CandleXCoordToPos(float x, int CurrFirst){
        if(set) {
            int k = 0;
            k+= Math.round((x-HORIZONTAL_BORDER-0.5*RectWidth)/RectWidth);
            return k+CurrFirst;
        } else return 1;
    }

    public float LinePosToXCoord(int pos) {
        if(set) return HORIZONTAL_BORDER + pos*lineDiff;
        else return 0;
    }

    public float CandlePosToXAxisCoord(int pos, int firstPrintedCandle) {
        return getHORIZONTAL_BORDER() + (pos-firstPrintedCandle+0.5f)*RectWidth;
    }

    public float CandlePostoXCoord(int pos, int firstPrintedCandle){
        return getHORIZONTAL_BORDER() + (pos-firstPrintedCandle)*RectWidth;
    }

    public float getRectWidth(){
        return RectWidth;
    }

    public float getLineDiff() {
        return lineDiff;
    }
}
