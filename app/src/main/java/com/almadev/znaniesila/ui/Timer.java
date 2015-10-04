package com.almadev.znaniesila.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import com.almadev.znaniesila.R;

/**
 * Created by Aleksey on 27.09.2015.
 */
public class Timer extends View {
    private Paint paint;
    private Paint arcPaint;
    private Paint wedgePaint;
    private Paint fontPaint;

    //circle and text colors
    private int circleCol, labelCol;

    public interface TimerCallback {
        void onTimer();
    }

    public Timer(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();

        arcPaint = new Paint();
        wedgePaint = new Paint();
        fontPaint = new Paint();
        fontPaint.setTextSize(60);
        fontPaint.setStyle(Paint.Style.FILL);
        fontPaint.setColor(Color.WHITE);

//        Shader gradient = new SweepGradient(0, getMeasuredHeight() / 2, Color.RED, Color.WHITE);
//        arcPaint.setShader(new LinearGradient(0, 0, 0, getHeight(), Color.GREEN, Color.BLACK, Shader.TileMode.MIRROR));
//        arcPaint.setShader(gradient);
//        arcPaint.setStyle(Paint.Style.STROKE);
//        arcPaint.setStrokeWidth();

        //get the attributes specified in attrs.xml using the name we included
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                                                                 R.styleable.Timer, 0, 0);


        try {
            //get the text and colors specified using the names in attrs.xml
            circleCol = a.getInteger(R.styleable.Timer_circleColor, 0x00ff00);//0 is default
            labelCol = a.getInteger(R.styleable.Timer_labelColor, 0);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
//        super.onDraw(canvas);
        //get half of the width and height as we are working with a circle
        int viewWidthHalf = this.getMeasuredWidth() / 2;
        int viewHeightHalf = this.getMeasuredHeight() / 2;

        //get the radius as half of the width or height, whichever is smaller
        //subtract ten so that it has some space around it
        int radius = 0;
        if (viewWidthHalf > viewHeightHalf)
            radius = viewHeightHalf - 10;
        else
            radius = viewWidthHalf - 10;

        paint.setStrokeWidth(6);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);

        //set the paint color using the circle color specified
        paint.setColor(circleCol);

        canvas.drawCircle(viewWidthHalf, viewHeightHalf, radius, paint);


//        canvas.drawArc(oval, -90, time / 30 * 360, true, wedgePaint);
//        canvas.drawArc(oval, -90, time / 30 * 360, false, arcPaint);

        String timeStr = "" + (time / 1000 < 10 ? "0" + time / 1000 : time / 1000);
        canvas.drawText( timeStr, viewWidthHalf - 30, viewHeightHalf + 20, fontPaint);
    }

    private void drawArc(Canvas canvas, float centerX, float centerY, float radius) {
        arcPaint.setShader(new RadialGradient(centerX, centerY, radius,
                                              new int[]{Color.BLACK, Color.GREEN}, null,
                                              Shader.TileMode.MIRROR));
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(6);

        RectF oval = new RectF(centerX - radius,
                               centerY - radius,
                               centerX + radius,
                               centerY + radius
        );

        canvas.drawArc(oval, -90, time / 30 * 360, false, arcPaint);
    }

    private void drawWedge(Canvas canvas, float centerX, float centerY, float radius) {
        wedgePaint.setShader(new RadialGradient(centerX, centerY, radius,
                                                new int[]{Color.GREEN, Color.BLACK}, null,
                                                Shader.TileMode.MIRROR));
        wedgePaint.setStyle(Paint.Style.FILL);
        wedgePaint.setStrokeWidth(0);

        RectF oval = new RectF(centerX - radius,
                               centerY - radius,
                               centerX + radius,
                               centerY + radius
        );

        canvas.drawArc(oval, -90, time / 30 * 360, false, wedgePaint);
    }

    public void start(int seconds, TimerCallback pCallback) {
        this.callback = pCallback;
        startAnimation(seconds);
    }

    public void stop() {
        stopAnimation();
    }

    private TimerCallback callback;
    private       int time = 30;
    private final int spf  = 20;

    Handler  mHandler = new Handler();
    Runnable mTick    = new Runnable() {
        public void run() {
            time -= spf;
            invalidate();
            if (time <= 0) {
                callback.onTimer();
            } else {
                mHandler.postDelayed(this, spf); // 20ms == 60fps
            }
        }
    };


    void startAnimation(int seconds) {
//        mAnimStartTime = SystemClock.uptimeMillis();
        time = seconds * 1000;
        mHandler.removeCallbacks(mTick);
        mHandler.post(mTick);
    }

    void stopAnimation() {
        mHandler.removeCallbacks(mTick);
    }
}
