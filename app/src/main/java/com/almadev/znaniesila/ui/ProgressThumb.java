package com.almadev.znaniesila.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.TypedValue;

public class ProgressThumb extends RotateDrawable {

	private int mProgress;
	private Paint mPaint = new Paint();
	private int mLeft;
	private int mTop;
	private int mAngle;
	private int mResId;
	private Resources mResources;
	
	public ProgressThumb(Context context, Bitmap bitmap) {
		super(context.getResources(), bitmap);
		mResources = context.getResources();
		mPaint.setStyle(Style.FILL);
		mPaint.setColor(Color.BLACK);
		
		float size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20,
							mResources.getDisplayMetrics());
		mPaint.setTextSize(size);
		mPaint.setTextAlign(Align.CENTER);
	}
	
	@Override
	public void draw(Canvas canvas) {
//		Log.d(mText, "!inside draw, width: " + canvas.getWidth() + ", height: " + canvas.getHeight());
//		canvas.rotate(33, mLeft, mTop);
		mAngle += 10;
		super.draw(canvas);
		if(mProgress != 0) {
			canvas.drawText(mProgress + "", getRect().centerX(), mTop + 10, mPaint);
		}
	}
	
	public void setProgress(int progress, int left, int top) {
		mProgress = progress;
		mLeft = left;
		mTop = top;
		invalidateSelf();
	}
	
	@Override
	public int getOpacity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAlpha(int alpha) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		// TODO Auto-generated method stub
		
	}
	
	public void setThumbResource(int resId) {
		if(mResId != resId) {
			mResId = resId;
			setBitmap(BitmapFactory.decodeResource(mResources, resId));
		}
	}
}