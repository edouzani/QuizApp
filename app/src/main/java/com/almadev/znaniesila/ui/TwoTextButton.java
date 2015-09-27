package com.almadev.znaniesila.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.Button;

public class TwoTextButton extends Button {

	private String mAlternateText;
	private Paint mPaint = new Paint();
	private float mLeftPadding;
	private float mTopPadding;

	public TwoTextButton(Context context) {
		super(context);
		init(context);
	}

	public TwoTextButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public TwoTextButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context) {
		mPaint.setStyle(Style.FILL);
		mPaint.setColor(Color.WHITE);
		mPaint.setTextAlign(Align.CENTER);
		mPaint.setLinearText(true);
		
		float size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16,
							context.getResources().getDisplayMetrics());

		mLeftPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20,
							context.getResources().getDisplayMetrics());

		mTopPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5,
							context.getResources().getDisplayMetrics());
		
		mPaint.setTextSize(size);
	}

	public void setAlternateText(String text) {
		mAlternateText = text;
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		/*Log.d(VIEW_LOG_TAG, "!drawing text: " + mAlternateText +
						", left: " + getLeft() + ", top: " + getTop());*/
		if(!TextUtils.isEmpty(mAlternateText)) {
			canvas.drawText(mAlternateText, mLeftPadding, (getHeight()/2) + mTopPadding, mPaint);
		}
	}
}