package com.almadev.znaniesila.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

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

    private final static int SPEED = 5;
    private ColorMatrix cm;

    public interface TimerCallback {
        void onTimer();
    }

    private float getDPFromPixels(Context context, float pixels) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wmanager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wmanager.getDefaultDisplay().getMetrics(metrics);
        switch (metrics.densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                pixels = pixels * 0.75f;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                //pixels = pixels * 1;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                pixels = pixels * 1.5f;
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                pixels = pixels * 2f;
        }
        return pixels;
    }

    public Timer(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        paint = new Paint();

        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wedgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fontPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        fontPaint.setTextSize(getDPFromPixels(context, 30));
        fontPaint.setTextSize(getResources().getDimension(R.dimen.question_number_text_size));
        fontPaint.setStyle(Paint.Style.FILL);
        fontPaint.setColor(Color.WHITE);

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

        PorterDuff.Mode xorMode = PorterDuff.Mode.SRC;
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setXfermode(new PorterDuffXfermode(xorMode));
        arcPaint.setStrokeWidth(6);
        arcPaint.setColor(Color.GREEN);
        cm = new ColorMatrix();

        wedgePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        wedgePaint.setStrokeWidth(0);
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

        float timeNormal = interpolate();
        drawArc(canvas, viewWidthHalf, viewHeightHalf, radius, timeNormal);

        String timeStr = "" + (time / 1000 < 10 ? "0" + time / 1000 : time / 1000);
        float stringWidth = fontPaint.measureText(timeStr);
        float stringHeight = fontPaint.descent() - fontPaint.ascent();

        canvas.drawText(timeStr, viewWidthHalf - stringWidth / 2.0f, viewHeightHalf + stringHeight / 3.0f, fontPaint);
    }

    private void drawArc(Canvas canvas, float centerX, float centerY, float radius, float timeNormal) {
        RectF oval = new RectF(centerX - radius,
                               centerY - radius,
                               centerX + radius,
                               centerY + radius
        );

        int arcLength = Math.round(360 * timeNormal);

        cm.setRotate(2, arcLength / 4);
        ColorFilter filter = new ColorMatrixColorFilter(cm);
        arcPaint.setColorFilter(filter);

        canvas.drawArc(oval, -90 + 360 * timeNormal * SPEED, arcLength, false, arcPaint);

        drawWedge(canvas, centerX, centerY, radius - 3, timeNormal);
    }

    private void drawWedge(Canvas canvas, float centerX, float centerY, float radius, float timeNormal) {
        wedgePaint.setShader(new RadialGradient(centerX, centerY, radius,
                                                new int[]{Color.argb(250, 255, 255, 255),Color.argb(30, 255, 255, 255)}, null,
                                                Shader.TileMode.MIRROR));

        RectF oval = new RectF(centerX - radius,
                               centerY - radius,
                               centerX + radius,
                               centerY + radius
        );

        int arcLength = Math.round(360 * timeNormal);

        canvas.drawArc(oval, -90 + 360 * timeNormal * SPEED, arcLength, true, wedgePaint);
    }

    public void start(int seconds, TimerCallback pCallback) {
        this.callback = pCallback;
        startAnimation(seconds);
    }

    public void pause() {
        isPaused = true;
    }

    public void run() {
        isPaused = false;
    }

    public void stop() {
        stopAnimation();
    }

    private boolean isPaused = false;
    private TimerCallback callback;
    private       int originalTime = 0;
    private       int time         = 30;
    private final int spf          = 20;

    Handler  mHandler = new Handler();
    Runnable mTick    = new Runnable() {
        public void run() {
            if (!isPaused) {
                time -= spf;
                invalidate();
            }
            if (time <= 0) {
                callback.onTimer();
            } else {
                mHandler.postDelayed(this, spf); // 20ms == 60fps
            }
        }
    };

    public int interpolatePoints(int points) {
        return Math.round((float) time / (float) originalTime * points);
    }

    private float interpolate() {
        return (float) (originalTime - time) / (float) originalTime;
    }

    void startAnimation(int seconds) {
//        mAnimStartTime = SystemClock.uptimeMillis();
        time = seconds * 1000;
        originalTime = seconds * 1000;
        mHandler.removeCallbacks(mTick);
        mHandler.post(mTick);
    }

    void stopAnimation() {
        mHandler.removeCallbacks(mTick);
    }
}
