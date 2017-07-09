package com.galadar.example.stockxchange;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Galadar on 4/1/2017.
 *
 * This is a Surface view specifically created for printing financial data as a Line or Candles Chart, with the added functionality of
 * saving what appears on it as an image file.
 */

public class ChartSurface extends SurfaceView {

    public static final int TickSize = 7;

    private Point p1;
    private Point p2;

    PriceControl pc;    //PriceControl Object for handling prices
    boolean lineChart;
    int FirstPrintedCandle;
    String name;
    Candle[] candles;           //table to use for drawing Candlesticks
    int[] linePoints;           //table to use for drawing line
    int[] Dates;
    //boolean screenShot;         //Take sviewshot or not
    //Canvas ScreenShotCanvas;    //Canvas Object to use for Viewshots
    //Bitmap bmp;                 //Bitmap for the Viewshot Canvas to draw on
    SurfaceHolder h;            //Variable for holding the holder Object

    //Constructors
    public ChartSurface(Context context) {
        super(context);
    }

    public ChartSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChartSurface(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    //The "create" method of the view, also sets up all data
    public void generateCandle(Context context, String n, Candle[] ca, int[] da) {

        candles = ca;
        Dates = da;
        name = n;

        int AxisX_Bottom = Dates[0];
        int AxisX_Top = Dates[Dates.length - 1];
        int AxisY_Bottom = candles[0].getLow();
        int AxisY_Top = candles[0].getHigh();
        for (int i = 0; i < candles.length; i++) {
            if (AxisY_Top < candles[i].getHigh()) {
                AxisY_Top = candles[i].getHigh();
            }
            if (AxisY_Bottom > candles[i].getLow()) {
                AxisY_Bottom = candles[i].getLow();
            }
        }

        AxisY_Top = Math.round(AxisY_Top * 1.2f);
        AxisY_Bottom = Math.round(AxisY_Bottom * 0.8f);
        AxisX_Top = Math.round(AxisX_Top * 1.2f);
        AxisX_Bottom = Math.round(AxisX_Bottom * 0.8f);

        pc = new PriceControl(context);
        pc.setObj(AxisX_Top, AxisX_Bottom, AxisY_Top, AxisY_Bottom, ca.length);

        //bmp = Bitmap.createBitmap(pc.getWidth(), pc.getHeight(), Bitmap.Config.ARGB_8888);
        //ScreenShotCanvas = new Canvas(bmp);
        //screenShot = false;

        FirstPrintedCandle = candles.length - getCurrentCandleAmount();

    }


    public void generateLine(Context context, String n, int[] ca, int[] da) {

        linePoints = ca;
        Dates = da;
        name = n;

        int AxisX_Bottom = Dates[0];
        int AxisX_Top = Dates[Dates.length - 1];
        int AxisY_Bottom = linePoints[0];
        int AxisY_Top = linePoints[0];
        for (int i = 0; i < linePoints.length; i++) {
            if (AxisY_Top < linePoints[i]) {
                AxisY_Top = linePoints[i];
            }
            if (AxisY_Bottom > linePoints[i]) {
                AxisY_Bottom = linePoints[i];
            }
        }

        AxisY_Top = Math.round(AxisY_Top * 1.2f);
        AxisY_Bottom = Math.round(AxisY_Bottom * 0.8f);
        AxisX_Top = Math.round(AxisX_Top * 1.2f);
        AxisX_Bottom = Math.round(AxisX_Bottom * 0.8f);

        pc = new PriceControl(context);
        pc.setObj(AxisX_Top, AxisX_Bottom, AxisY_Top, AxisY_Bottom, ca.length);

        //bmp = Bitmap.createBitmap(pc.getWidth(), pc.getHeight(), Bitmap.Config.ARGB_8888);
        //ScreenShotCanvas = new Canvas(bmp);
        //screenShot = false;

    }

    //set the holder on the surface, to handle draw requests and "viewshots" here
    public void finalizeSurface(SurfaceHolder holder) {
        h = holder;
    }

    //Function for creating a Candles Chart (also handles viewshots)
    public void createCandleChart() { //Price data HOCL, in that column order per lineChart
        lineChart = false;
        Canvas c = h.lockCanvas();

        c.drawColor(Color.WHITE);
        c = DrawAxis(c);
        Paint p = new Paint();
        p.setARGB(100, 100, 100, 100);
        p.setStyle(Paint.Style.FILL);
        p.setTextSize(300.0f);
        p.setTextAlign(Paint.Align.CENTER);
        c.drawText(name, pc.getWidth() * 0.5f, pc.getHeight() * 0.5f, p);
        c = drawCandleChart(c);
        h.unlockCanvasAndPost(c);

        //Top right of screen to Top price (AxisY_Top) and first date / data point (
    }

    //Function for creating a Line Chart (also handles viewshots)
    public void createLineChart() { //Price data per tick
        lineChart = true;
        Canvas c = h.lockCanvas();

        c.drawColor(Color.WHITE);
        c = DrawAxis(c);
        Paint p = new Paint();
        p.setARGB(100, 100, 100, 100);
        p.setStyle(Paint.Style.FILL);
        p.setTextSize(300.0f);
        p.setTextAlign(Paint.Align.CENTER);
        c.drawText(name, pc.getWidth() * 0.5f, pc.getHeight() * 0.5f, p);

        c = drawLineChart(c);

        h.unlockCanvasAndPost(c);
    }

    //Draw the Axii
    private Canvas DrawAxis(Canvas c) {
        Paint p = new Paint();
        p.setStrokeWidth(2.0f);
        p.setColor(Color.BLACK);
        int XPos = pc.getHORIZONTAL_BORDER() / 2;
        int YPos = pc.getHeight() - pc.getVERTICAL_BORDER() / 2;
        c.drawLine(-pc.getWidth(), YPos, 2 * pc.getWidth(), YPos, p);     //X Axis for dates, with set Y position across all width
        c.drawLine(XPos, -pc.getHeight(), XPos, 2 * pc.getHeight(), p);    //Y Axis for prices, with set X position, across all height

        int tickValue;
        float tickPos;
        Paint tickPaint = new Paint();
        tickPaint.setColor(Color.BLACK);
        tickPaint.setStrokeWidth(3.0f);
        tickPaint.setTextSize(16.0f);
        tickPaint.setTextAlign(Paint.Align.LEFT);

        int max = (int) Math.ceil(pc.getY_Top() / 100);
        int min = (int) Math.floor(pc.getY_Bottom() / 100);
        int diff = max - min;

        if (diff > 400) diff = 50;
        else if (diff > 150) diff = 25;
        else if (diff > 50) diff = 5;
        else diff = 1;

        tickValue = 0;

        while (tickValue < min) {
            tickValue += diff;
        }

        while (tickValue <= max) {     //Y Axis Ticks (Prices)
            tickPos = pc.PriceToYCoord(tickValue * 100);
            c.drawLine(XPos - TickSize, tickPos, XPos + TickSize, tickPos, tickPaint);
            c.drawText(("$" + Integer.toString(tickValue) + " "), XPos - (pc.getHORIZONTAL_BORDER() / 2), tickPos, tickPaint);
            tickValue += diff;
        }

        //AXIS X Ticks (Dates)

        tickPaint.setTextAlign(Paint.Align.CENTER);

        if (lineChart) {
            diff = Dates.length / 5;
            int count = 0;
            while (count < Dates.length) {
                tickPos = pc.getHORIZONTAL_BORDER() + ((count * pc.getAvailWidth()) / Dates.length);
                c.drawLine(tickPos, YPos - TickSize, tickPos, YPos + TickSize, tickPaint);
                c.drawText(Integer.toString(Dates[count]), tickPos, YPos + 2 * TickSize, tickPaint);
                count += diff;
                count--;
            }
        } else {
            min = Dates[FirstPrintedCandle];
            max = Dates[FirstPrintedCandle + getCurrentCandleAmount() - 1];
            diff = 3;
            tickValue = min;
            int count = FirstPrintedCandle;
            while (tickValue <= max) {
                tickPos = pc.CandlePosToXAxisCoord(count, FirstPrintedCandle);
                c.drawLine(tickPos, YPos - TickSize, tickPos, YPos + TickSize, tickPaint);
                c.drawText(Integer.toString(Dates[count]), tickPos, YPos + 2 * TickSize, tickPaint);
                count += diff;
                if (count >= Dates.length) {
                    tickValue = max + 1;
                    continue;
                }
                tickValue = Dates[count];
            }
        }

        return c;
    }

    //Actual Candles Chart drawing function, getting Canvas from calling function and returning it, to also handle Viewshots
    private Canvas drawCandleChart(Canvas c) {
        for (int i = 0; i < getCurrentCandleAmount(); i++) {
            c.drawBitmap(drawCandle(candles[FirstPrintedCandle + i]), i * pc.getRectWidth() + pc.getHORIZONTAL_BORDER(), pc.getVERTICAL_BORDER(), null);
        }
        return c;
    }

    //Function for preparing to draw the chart dragged to the left/right
    public float dragChart(float dx) {
        Canvas c = h.lockCanvas();
        c.drawColor(Color.WHITE);
        if (dx > 0) {
            if (FirstPrintedCandle > 0) {
                while (dx > pc.getRectWidth()) {
                    dx -= pc.getRectWidth();
                    FirstPrintedCandle--;
                }
            } else dx = pc.getRectWidth() / 2;
        } else {
            if (FirstPrintedCandle < (candles.length - getCurrentCandleAmount())) {
                while (-dx > pc.getRectWidth()) {
                    dx += pc.getRectWidth();
                    FirstPrintedCandle++;
                }
            } else dx = pc.getRectWidth() / 2;
        }
        if (FirstPrintedCandle < 0) FirstPrintedCandle = 0;
        else if (FirstPrintedCandle >= (candles.length - getCurrentCandleAmount()))
            FirstPrintedCandle = candles.length - getCurrentCandleAmount() - 1;
        c = DrawAxis(c);
        c = drawCandleChart(c, dx);

        h.unlockCanvasAndPost(c);
        return dx;
    }

    //Function for repainting the Candles Chart dragged by dx points to the right (negative dx => to the left)
    private Canvas drawCandleChart(Canvas c, float dx) {
        for (int i = 0; i < getCurrentCandleAmount(); i++) {
            c.drawBitmap(drawCandle(candles[FirstPrintedCandle + i]), i * pc.getRectWidth() + pc.getHORIZONTAL_BORDER() + dx, pc.getVERTICAL_BORDER(), null);
        }
        return c;
    }

    //Actual Line Chart drawing function, getting Canvas from calling function and returning it
    private Canvas drawLineChart(Canvas c) {

        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(3.0f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        float lastX, lastY, pri, loc;

        if(linePoints.length<2) return(c);

        lastX = pc.getHORIZONTAL_BORDER() - 1;
        lastY = pc.PriceToYCoord(linePoints[0]);
        for (int i = 0; i < linePoints.length; i++) {
            pri = pc.PriceToYCoord(linePoints[i]);
            loc = pc.LinePosToXCoord(i);
            c.drawLine(lastX, lastY, loc, pri, paint);
            lastX = loc;
            lastY = pri;
        }

        return c;
    }

    //Helper function for preparing the Candles Chart, returning the amount of Candles to draw
    private int getCurrentCandleAmount() {
        return (int) Math.round(Math.floor(pc.getAvailWidth() / pc.getRectWidth()));
    }

    //Helper function for getting the matrix index of the price datapoint on the value selected by user, on a Line Chart
    public int getLineDataPos(float x) {
        return pc.LineXCoordToPos(x);
    }

    //Helper function for getting the matrix index of the price datapoint on the value selected by user, on a Candles Chart
    public int getCandleDataPos(float x) {
        return pc.CandleXCoordToPos(x, FirstPrintedCandle);
    }

    //Helper function for drawing an individual Candle
    private Bitmap drawCandle(Candle candle) {
        Canvas c = new Canvas();
        int RTop = Math.round(pc.PriceToYCoord(Math.max(candle.getOpen(), candle.getClose()))); //pc.getAvailHeight()*(Math.max(candle.getOpen(),candle.getClose())-pc.getY_Bottom())/(pc.getY_Top()-pc.getY_Bottom());
        int RBottom = Math.round(pc.PriceToYCoord(Math.min(candle.getOpen(), candle.getClose()))); //pc.getAvailHeight()*(Math.min(candle.getOpen(),candle.getClose())-pc.getY_Bottom())/(pc.getY_Top()-pc.getY_Bottom());

        Bitmap CandleBmp = Bitmap.createBitmap(1 + Math.round(pc.getRectWidth()), pc.getHeight(), Bitmap.Config.ALPHA_8);
        c.setBitmap(CandleBmp);

        c.drawARGB(0, 0, 0, 0);

        Rect body = new Rect(Math.round(pc.getRectWidth() * 0.15f), RTop, Math.round(pc.getRectWidth() * 0.85f), RBottom);

        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(3.0f);
        int ht = Math.round(pc.PriceToYCoord(candle.getHigh())); //pc.getAvailHeight()*(candle.getHigh()-pc.getY_Bottom())/(pc.getY_Top()-pc.getY_Bottom());
        c.drawLine(pc.getRectWidth() * 0.5f, RTop, pc.getRectWidth() * 0.5f, ht, paint);

        Paint fillPaint = new Paint();
        fillPaint.setColor(Color.BLACK);
        fillPaint.setStrokeWidth(2.0f);
        if (candle.getOpen() >= candle.getClose()) fillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        else fillPaint.setStyle(Paint.Style.STROKE);
        c.drawRect(body, fillPaint);

        ht = Math.round(pc.PriceToYCoord(candle.getLow())); //pc.getAvailHeight()*(candle.getLow()-pc.getY_Bottom())/(pc.getY_Top()-pc.getY_Bottom());
        c.drawLine(pc.getRectWidth() * 0.5f, RBottom, pc.getRectWidth() * 0.5f, ht, paint);

        return CandleBmp;
    }

}