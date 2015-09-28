package com.almadev.znaniesila;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.drawable.LevelListDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.almadev.znaniesila.model.Category;
import com.almadev.znaniesila.model.Question;
import com.almadev.znaniesila.model.Quiz;
import com.almadev.znaniesila.model.QuizHolder;
import com.almadev.znaniesila.ui.Timer;
import com.almadev.znaniesila.ui.TwoTextButton;
import com.almadev.znaniesila.utils.Constants;
import com.almadev.znaniesila.utils.QuestionsParser;
import com.almadev.znaniesila.videoplayer.MovieView;
import com.chartboost.sdk.Chartboost;

public class HAQuizScreen extends Activity implements OnClickListener, Callback,
		OnBufferingUpdateListener, OnCompletionListener{

	private static final String TAG           = "QuizActivity";
	private static final int    RETRIEVE_DATA = 0;
	private static final int    SET_NEXT_DATA = 1;
	public static final  String ANIM_TYPE     = "anim_type";
	private String         mCategoryId;
	private Quiz           mQuiz;
	private List<Question> mQuestions;
	private Category       mCategory;
	private ImageView      mSmallImage;
	private TextView       mQuestion;
	private TextView       mQuestionNumber;
	private TextView       mCurrentPoints;
	private TwoTextButton  mOption0;
	private TwoTextButton  mOption1;
	private TwoTextButton  mOption2;
	private TwoTextButton  mOption3;
	private Button         mLeftBtn;
	private Button         mRightBtn;
	private int            mCurrentQuestion;
	private int            maxQuestions;
	private int            mScore;
	private MediaPlayer    mMediaPlayer;
	private QuizHolder     mQuizHolder;
	private String         mVideoPath;
	private int            mVideoWidth;
	private int            mVideoHeight;
	private boolean mIsVideoSizeKnown       = false;
	private boolean mIsVideoReadyToBePlayed = false;
	private DisplayMetrics    mMetrics;
	private HandlerThread     mThread;
	private SharedPreferences mPrefsManager;
	private boolean           isStarting;
	private TextView          mNextQuest;
	private Chartboost        cb;
	private Boolean           adSupportEnabled;
	private Boolean           adsDisabledAfterPurchase;
    private Timer mTimer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isStarting = true;
		setContentView(R.layout.quiz_activity1);
		mCategoryId = getIntent().getStringExtra(Constants.CATEGORY_ID);
		mMetrics = getResources().getDisplayMetrics();
		mPrefsManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		mQuizHolder = QuizHolder.getInstance(this);
		mCategory = mQuizHolder.getCategories().getCategoryById(mCategoryId);
		//readQuestions();
		mQuiz = mQuizHolder.getQuiz(mCategoryId);
		mQuestions = mQuiz.getQuestions();
		mCurrentQuestion = 0;
		maxQuestions = mQuestions.size() < mCategory.getCategory_question_max_limit() ?
				mQuestions.size() : mCategory.getCategory_question_max_limit();

        mTimer = (Timer) findViewById(R.id.timer);
		mThread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
		mThread.start();
//		mServiceHandler = new ServiceHandler(mThread.getLooper());
//		mServiceHandler.obtainMessage(RETRIEVE_DATA).sendToTarget();

		setupViews();
		adSupportEnabled = mPrefsManager.getBoolean(Constants.AD_SUPPORT_NEEDED, false);
		adsDisabledAfterPurchase = mPrefsManager.getBoolean(Constants.ADS_DISABLED_AFTER_PURCHASE, false);
		if (adSupportEnabled && !adsDisabledAfterPurchase) {
			String appId = mPrefsManager.getString(Constants.CHARTBOOST_APPID, "");
			String appSecret = mPrefsManager.getString(Constants.CHARTBOOST_APPSECRET, "");
			if (appId.trim().equals("") || appSecret.trim().equals("")) {
				Toast.makeText(this, getResources().getString(R.string.chartboost_error_msg), Toast.LENGTH_SHORT).show();
			} else {
				this.cb = Chartboost.sharedChartboost();
				this.cb.onCreate(this, appId, appSecret, null);
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (isStarting) {
			setupData();
			isStarting = false;
		}
		if (adSupportEnabled && this.cb != null && !adsDisabledAfterPurchase) {
			this.cb.onStart(this);
			this.cb.startSession();
			this.cb.showInterstitial();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (adSupportEnabled && this.cb != null && !adsDisabledAfterPurchase) {
			this.cb.onStop(this);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		releaseMediaPlayer();
		doCleanUp();
		if (mThread != null) {
			mThread.quit();
		}
		if (adSupportEnabled && this.cb != null && !adsDisabledAfterPurchase) {
			this.cb.onStop(this);
		}
	}

	@Override
	public void onBackPressed() {
		if (this.cb != null && this.cb.onBackPressed())
			return;
		else
			super.onBackPressed();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "!inside onActivityResult, request code: " + requestCode + ", result code: " +  resultCode);
	}
	
	private void setupViews() {
		mSmallImage = (ImageView)findViewById(R.id.small_image);

		mQuestion = (TextView)findViewById(R.id.question);
		mQuestionNumber = (TextView)findViewById(R.id.question_number);
		mCurrentPoints = (TextView)findViewById(R.id.current_points);

		mLeftBtn = (Button) findViewById(R.id.left_btn);
		mRightBtn = (Button) findViewById(R.id.right_btn);

		mOption0 = (TwoTextButton)findViewById(R.id.option1);
		mOption0.setAlternateText("1");
		mOption0.setOnClickListener(this);
		
		mOption1 = (TwoTextButton)findViewById(R.id.option2);
		mOption1.setAlternateText("2");
		mOption1.setOnClickListener(this);
		
		mOption2 = (TwoTextButton)findViewById(R.id.option3);
		mOption2.setAlternateText("3");
		mOption2.setOnClickListener(this);
		
		mOption3 = (TwoTextButton)findViewById(R.id.option4);
		mOption3.setAlternateText("4");
		mOption3.setOnClickListener(this);
		
		findViewById(R.id.home).setOnClickListener(this);
		mNextQuest = (TextView)findViewById(R.id.next_question);
		mNextQuest.setOnClickListener(this);

        TextView catname = (TextView) findViewById(R.id.cat_name);
        catname.setText(mCategory.getCategory_name());
	}
	
	private void setupData() {
		Intent intent = new Intent(this, MovieView.class);
		intent.putExtra("finish", true);
		startActivity(intent);
		int animType = mPrefsManager.getInt(Constants.OPTIONS_ANIMATION, 1);

		mNextQuest.setEnabled(false);
		
		mOption0.setBackgroundResource(R.drawable.options_button);
		mOption1.setBackgroundResource(R.drawable.options_button);
		mOption2.setBackgroundResource(R.drawable.options_button);
		mOption3.setBackgroundResource(R.drawable.options_button);
		
		mOption0.setVisibility(View.GONE);
		mOption1.setVisibility(View.GONE);
		mOption2.setVisibility(View.GONE);
		mOption3.setVisibility(View.GONE);

		mOption0.setAlternateText("1");
		mOption1.setAlternateText("2");
		mOption2.setAlternateText("3");
		mOption3.setAlternateText("4");
		
		mOption0.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
		mOption1.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
		mOption2.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
		mOption3.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
		
		mOption0.setTextColor(Color.WHITE);
		mOption1.setTextColor(Color.WHITE);
		mOption2.setTextColor(Color.WHITE);
		mOption3.setTextColor(Color.WHITE);
		
		((LevelListDrawable)mOption0.getBackground()).setLevel(2);
		((LevelListDrawable)mOption1.getBackground()).setLevel(2);
		((LevelListDrawable)mOption2.getBackground()).setLevel(2);
		((LevelListDrawable)mOption3.getBackground()).setLevel(2);
		
		int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, mMetrics);
		mOption0.setPadding(padding, 0, 0, 0);
		mOption1.setPadding(padding, 0, 0, 0);
		mOption2.setPadding(padding, 0, 0, 0);
		mOption3.setPadding(padding, 0, 0, 0);

//		if(mTimer != null) {
//			mTimer.cancel();
//		}
		if(mCurrentQuestion >= mQuestions.size()) {
    		return;
    	}
		final Question question = mQuestions.get(mCurrentQuestion);
		
		if(question.getQuestion_type() == 4) {
//			mOption0.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
//			mOption1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
//
//			float margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, mMetrics);
//
//			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mOption0.getLayoutParams();
//			lp.topMargin = (int) margin;
//			lp.bottomMargin = (int) margin;
//
//			lp = (LinearLayout.LayoutParams) mOption1.getLayoutParams();
//			lp.bottomMargin = (int) margin;
		} else {
			mOption0.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
			mOption1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
			mOption2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
			mOption3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
			
			float margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mMetrics);
			
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mOption0.getLayoutParams();
			lp.topMargin = (int) margin;
			lp.bottomMargin = (int) margin;
			
			lp = (LinearLayout.LayoutParams) mOption1.getLayoutParams();
			lp.bottomMargin = (int) margin;
			
			lp = (LinearLayout.LayoutParams) mOption2.getLayoutParams();
			lp.bottomMargin = (int) margin;
			
			lp = (LinearLayout.LayoutParams) mOption3.getLayoutParams();
			lp.bottomMargin = (int) margin;
		}
		
//		mTimeout.setMax((question.duration + 4)*1000);
		
		mQuestion.setText(question.getQuestion());
		mQuestionNumber.setText((mCurrentQuestion + 1) + "/" + maxQuestions);
		mCurrentPoints.setText(question.getPoints() + "");
		
		String text = mScore + "";
		if(mScore > 0) {
			text = "+" + mScore;
		}
		
		if(mSmallImage.getDrawable() != null) {
			mSmallImage.getDrawable().setCallback(null);
		}
//		if(question.questionType == 1) {
////			mSmallLayout.setVisibility(View.GONE);
////			mLargeVideo.setVisibility(View.GONE);
//		} else if(question.questionType == 2) {
//			mSmallLayout.setVisibility(View.VISIBLE);
//			mTapText.setText(R.string.tap_to_zoom);
//			try {
//				mSmallImage.setImageDrawable(Drawable.createFromStream(getAssets().open(
//										"Quiz Data/Pictures_Or_Videos/Quiz_Category_" +
//										mCategoryId + "/" + question.mediaName), null));
//			} catch(Exception e) {
//				e.printStackTrace();
//			}
//			if(mSmallImage.getDrawable() == null) {
//				mSmallLayout.setVisibility(View.GONE);
//			}
//		} else if(question.questionType == 3) {
//			mSmallLayout.setVisibility(View.VISIBLE);
//			mTapText.setText(R.string.tap_to_play);
//			File file = new File(getExternalCacheDir() + "/delete" + mCurrentQuestion);
//			if(!file.exists()) {
//				mSmallImage.setImageURI(Uri.fromFile(file));
//			} else {
//				mSmallImage.setImageResource(R.drawable.videooverlay);
//			}
//		} else
            if(question.getQuestion_type() == 4) {
//			mSmallLayout.setVisibility(View.GONE);
//			mLargeVideo.setVisibility(View.GONE);
			mOption0.setBackgroundResource(R.drawable.boolean_options_normal);
			mOption0.setText("True");
			
			if(animType == 1) {
//				mOption0.setVisibility(View.VISIBLE);
				OptionsSlideAnimation anim = new OptionsSlideAnimation(mMetrics.widthPixels, 0, 0, 0, 1);
				anim.setDuration(400);
				mLeftBtn.startAnimation(anim);

//				mOption1.setVisibility(View.VISIBLE);
				anim = new OptionsSlideAnimation(mMetrics.widthPixels, 0, 0, 0, 2);
				anim.setDuration(600);
				mRightBtn.startAnimation(anim);

                mTimer.startAnim();
			}
			
			mOption0.setAlternateText("");
			mOption0.setGravity(Gravity.CENTER);
			mOption0.setPadding(0, 0, 0, 0);
			
			mOption1.setText("False");
			mOption1.setBackgroundResource(R.drawable.boolean_options_normal);
			mOption1.setAlternateText("");
			mOption1.setGravity(Gravity.CENTER);
			mOption1.setPadding(0, 0, 0, 0);
		}

		if(animType == 2) {
			startOptionsFadeAnimation();
		}
	}
	
	private Handler mUiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what) {
			case SET_NEXT_DATA :
				mCurrentQuestion++;
				if(mCurrentQuestion < maxQuestions) {
					setupData();
				} else {
					showFinalScreen();
				}
				break;
			}
		}
	};

	private void showFinalScreen() {
		Intent intent = new Intent(this, MovieView.class);
		intent.putExtra("finish", true);
		startActivity(intent);
		
		intent = new Intent(this, HAFinalScreen.class);
		intent.putExtra(Constants.CATGEORY, mCategory.getCategory_name());
		intent.putExtra(Constants.LEADERBOARD_ID, mCategory.getLeaderboard_id());
		intent.putExtra(Constants.POINTS, mScore);
		startActivity(intent);
		finish();
	}
	
	private void answerSelected(int option) {
		if(mCurrentQuestion >= mQuestions.size()) {
    		return;
    	}
        mTimer.stopAnim();
		Question question = mQuestions.get(mCurrentQuestion);
		if(question.getQuestion_type() != 4) {
			if(question.getAnswer() == option) {
				mScore += question.getPoints();
				playSoundForAnswer(true);
			} else {
				mScore -= question.getNegative_points();
				playSoundForAnswer(false);
			}
		} else {
			if(question.getAnswer() == 1 && option == 0) {
				mScore += question.getPoints();
				playSoundForAnswer(true);
			}else if(question.getAnswer() == 0 && option == 1){
				mScore += question.getPoints();
				playSoundForAnswer(true);
			}else{
				mScore -= question.getNegative_points();
				playSoundForAnswer(false);
			}
		}
		
		mOption0.setEnabled(false);
		mOption1.setEnabled(false);
		mOption2.setEnabled(false);
		mOption3.setEnabled(false);

		boolean changeState = mPrefsManager.getBoolean(Constants.HIGHLIGHT_OPTIONS, false);
		if(changeState) {
			changeDrawableState(option);
		} else {
			mCurrentQuestion++;
			if(mCurrentQuestion < maxQuestions) {
				setupData();
			} else {
				showFinalScreen();
			}
		}
	}
	
	private void changeDrawableState(int selectedOption) {
		if(mCurrentQuestion >= mQuestions.size()) {
    		return;
    	}
		Question question = mQuestions.get(mCurrentQuestion);
		if(question.getQuestion_type() != 4) {
			try {
				TwoTextButton clicked = (TwoTextButton) HAQuizScreen.class.getDeclaredField(
											"mOption" + selectedOption).get(this);
				TwoTextButton correct = (TwoTextButton) HAQuizScreen.class.getDeclaredField(
											"mOption" + question.getAnswer()).get(this);
				
				((LevelListDrawable)correct.getBackground()).setLevel(1);
				if(clicked != correct) {
					((LevelListDrawable)clicked.getBackground()).setLevel(0);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			if(question.getAnswer() == 1 && selectedOption == 0) { //true, true
				mOption0.setTextColor(Color.GREEN);
			} else if(question.getAnswer() == 1 && selectedOption == 1) { //true, false
				mOption0.setTextColor(Color.GREEN);
				mOption1.setTextColor(Color.RED);
			} else if(question.getAnswer() == 0 && selectedOption == 0) { //false, true
				mOption0.setTextColor(Color.RED);
				mOption1.setTextColor(Color.GREEN);
			} else if(question.getAnswer() == 0 && selectedOption == 1) { //false, false
				mOption1.setTextColor(Color.GREEN);
			}
		}
		mUiHandler.sendEmptyMessageDelayed(SET_NEXT_DATA, 777);
	}
	
	private void playSoundForAnswer(boolean isCorrect) //send YES if answer is correct No if wrong. Also call this method for True false questions too.
	{
		boolean playSound = mPrefsManager.getBoolean(Constants.PLAY_SOUND_ON_ANSWERING, false);

		if(playSound)
		{
			MediaPlayer player;

			if(isCorrect)
			{
				player= MediaPlayer.create(this, R.raw.right);
			}
			else
			{
				player= MediaPlayer.create(this, R.raw.wrong);
			}
			player.start();

		}
		
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
            case R.id.next_question:
                mCurrentQuestion++;
                mTimer.stopAnim();
                if (mCurrentQuestion < maxQuestions) {
                    setupData();
                } else {
                    showFinalScreen();
                }
                break;

            case R.id.home:
                startActivity(new Intent(this, HAStartScreen.class));
                finish();
                break;

            case R.id.option1:
                answerSelected(0);
                break;

            case R.id.option2:
                answerSelected(1);
                break;

            case R.id.option3:
                answerSelected(2);
                break;

            case R.id.option4:
                answerSelected(3);
                break;
        }

	}

	public void onBufferingUpdate(MediaPlayer arg0, int percent) {
        Log.d(TAG, "onBufferingUpdate percent:" + percent);

    }

    public void onCompletion(MediaPlayer arg0) {
        Log.d(TAG, "onCompletion called");
    }

    public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
        Log.d(TAG, "surfaceChanged called");

    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
        Log.d(TAG, "surfaceDestroyed called");
    }


    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
        doCleanUp();
    }

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void doCleanUp() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        mIsVideoReadyToBePlayed = false;
        mIsVideoSizeKnown = false;
    }

    private class OptionsSlideAnimation extends TranslateAnimation {

		private int mOption;

		public OptionsSlideAnimation(float fromXDelta, float toXDelta,
				float fromYDelta, float toYDelta, int option) {
			super(fromXDelta, toXDelta, fromYDelta, toYDelta);
			mOption = option;
//			setDuration(222);
	    	setInterpolator(getBaseContext(), android.R.anim.accelerate_decelerate_interpolator);
		}
    	
		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
			super.applyTransformation(interpolatedTime, t);
			if(mCurrentQuestion >= mQuestions.size()) {
				return;
			}
			Question question = mQuestions.get(mCurrentQuestion);
			
			if((question.getQuestion_type() == 4 && mOption == 2 && interpolatedTime > 0.95) ||
					(question.getQuestion_type() != 4 && mOption == 4 && interpolatedTime > 0.95)) {
				mOption0.setEnabled(true);
				mOption1.setEnabled(true);
				mOption2.setEnabled(true);
				mOption3.setEnabled(true);
				
				mNextQuest.setEnabled(true);
			}
		}
    }
    
    private void startOptionsFadeAnimation() {
    	AlphaAnimation anim = new AlphaAnimation(0, 1);
    	anim.setDuration(777);
    	mOption0.setVisibility(View.VISIBLE);
    	mOption0.startAnimation(anim);
    	
    	anim = new AlphaAnimation(0, 1);
    	anim.setDuration(777);
    	mOption1.setVisibility(View.VISIBLE);
    	mOption1.startAnimation(anim);

    	if(mCurrentQuestion >= mQuestions.size()) {
    		return;
    	}
		Question question = mQuestions.get(mCurrentQuestion);
		if(question.getQuestion_type()!= 4) {
	    	anim = new AlphaAnimation(0, 1);
	    	anim.setDuration(777);
	    	mOption2.setVisibility(View.VISIBLE);
	    	mOption2.startAnimation(anim);
	    	
	    	anim = new AlphaAnimation(0, 1);
	    	anim.setDuration(777);
	    	mOption3.setVisibility(View.VISIBLE);
	    	mOption3.startAnimation(anim);
		}

		mOption0.setEnabled(true);
		mOption1.setEnabled(true);
		mOption2.setEnabled(true);
		mOption3.setEnabled(true);

		mNextQuest.setEnabled(true);
    }
}