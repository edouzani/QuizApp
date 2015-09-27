package com.almadev.znaniesila;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.almadev.znaniesila.R;
import com.almadev.znaniesila.ui.ProgressThumb;
import com.almadev.znaniesila.ui.TwoTextButton;
import com.almadev.znaniesila.utils.Constants;
import com.almadev.znaniesila.utils.QuestionsParser;
import com.almadev.znaniesila.utils.RunningState;
import com.almadev.znaniesila.utils.RunningState.Quiz;
import com.almadev.znaniesila.utils.RunningState.QuizCategory;
import com.almadev.znaniesila.videoplayer.MovieView;
import com.chartboost.sdk.Chartboost;

public class HAQuizScreen extends Activity implements OnClickListener, Callback,
		OnBufferingUpdateListener, OnCompletionListener, OnPreparedListener, OnVideoSizeChangedListener {
	
	private static final String TAG = "QuizActivity";
	private static final int RETRIEVE_DATA = 0;
	private static final int SET_NEXT_DATA = 1;
	public static final String ANIM_TYPE = "anim_type";
	private RunningState mState;
	private String mCategoryId;
	private ArrayList<Quiz> mQuestions;
	private QuizCategory mCategory;
	private SeekBar mTimeout;
	private ImageView mSmallImage;
	private TextView mQuestion;
	private TextView mTotalPoints;
	private TextView mQuestionNumber;
	private TextView mCurrentPoints;
	private TwoTextButton mOption0;
	private TwoTextButton mOption1;
	private TwoTextButton mOption2;
	private TwoTextButton mOption3;
	private int mCurrentQuestion;
	private int maxQuestions;
	private int mScore;
	private CountDownTimer mTimer;
	private ProgressThumb mThumb;
	private float mLeftOffset1;
	private float mTopOffset;
	private float mLeftOffset2;
	private MediaPlayer vPlayer;
	private AssetFileDescriptor vFd;
	private MediaPlayer mMediaPlayer;
	private SurfaceView mLargeVideo;
	private SurfaceHolder mSmallHolder;
	private String mVideoPath;
    private int mVideoWidth;
    private int mVideoHeight;
    private boolean mIsVideoSizeKnown = false;
    private boolean mIsVideoReadyToBePlayed = false;
	private DisplayMetrics mMetrics;
	private HandlerThread mThread;
	private ServiceHandler mServiceHandler;
	private SharedPreferences mPrefsManager;
	private ImageView mLargeImage;
	private View mLargeMedia;
	private TextView mSecondaryProgress;
	private boolean isStarting;
	private ImageView mNextQuest;
	private View mSmallLayout;
	private TextView mTapText;
	private Chartboost cb;
	private Boolean adSupportEnabled;
	private Boolean adsDisabledAfterPurchase;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isStarting = true;
		setContentView(R.layout.quiz_activity1);
		mCategoryId = getIntent().getStringExtra(Constants.CATGEORY_ID);
		mState = RunningState.getInstance(getApplicationContext());
		mMetrics = getResources().getDisplayMetrics();
		mPrefsManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		mCategory = mState.getQuizCategory(mCategoryId);
		//readQuestions();
		mQuestions = new ArrayList<Quiz>(mState.getQuestions(mCategoryId));
		mCurrentQuestion = 0;
		maxQuestions = mQuestions.size() < mCategory.limit ? mQuestions.size() : mCategory.limit;
		
		mThread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mThread.start();
        mServiceHandler = new ServiceHandler(mThread.getLooper());
        mServiceHandler.obtainMessage(RETRIEVE_DATA).sendToTarget();
		
		mLeftOffset1 = getResources().getDimension(R.dimen.left_offset1);
		mLeftOffset2 = getResources().getDimension(R.dimen.left_offset2);
		
		mTopOffset = getResources().getDimension(R.dimen.top_offset);
        
		setupViews();
		adSupportEnabled = mPrefsManager.getBoolean(Constants.AD_SUPPORT_NEEDED,false);
		adsDisabledAfterPurchase = mPrefsManager.getBoolean(Constants.ADS_DISABLED_AFTER_PURCHASE,false);
		if(adSupportEnabled && !adsDisabledAfterPurchase){
			String appId = mPrefsManager.getString(Constants.CHARTBOOST_APPID,"");
			String appSecret = mPrefsManager.getString(Constants.CHARTBOOST_APPSECRET,"");
			if(appId.trim().equals("") || appSecret.trim().equals("")){
				Toast.makeText(this, getResources().getString(R.string.chartboost_error_msg), 1000).show();
			}else{
				this.cb = Chartboost.sharedChartboost();
				this.cb.onCreate(this, appId, appSecret, null);
			}
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if(isStarting) {
			setupData();
			isStarting = false;
		}
		if(adSupportEnabled && this.cb!=null && !adsDisabledAfterPurchase){
			this.cb.onStart(this);
		    this.cb.startSession();
		    this.cb.showInterstitial();
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(adSupportEnabled && this.cb!=null && !adsDisabledAfterPurchase){
			this.cb.onStop(this);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mTimer != null) {
			mTimer.cancel();
		}
        releaseMediaPlayer();
        doCleanUp();
        if(mThread != null) {
        	mThread.quit();
        }
        if(mThumb != null) {
        	mThumb.setCallback(null);
        }
        if(adSupportEnabled && this.cb!=null && !adsDisabledAfterPurchase){
			this.cb.onStop(this);
		}
	}
	
	@Override
	public void onBackPressed() {
		if (this.cb!=null && this.cb.onBackPressed())
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
		mTimeout = (SeekBar)findViewById(R.id.time_out);
		mTimeout.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		final BitmapDrawable d = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_seekbar1);
		d.setBounds(new Rect(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight()));
		mThumb = new ProgressThumb(this, d.getBitmap());
		mTimeout.setThumb(mThumb);
		d.setCallback(null);
		
		mLargeMedia = findViewById(R.id.large_media);
		mSecondaryProgress = (TextView)findViewById(R.id.secondary_progress);

		mLargeVideo = (SurfaceView) findViewById(R.id.large_video);
		mLargeVideo.setOnClickListener(this);
		/*mLargeVideo.setLayoutParams(new FrameLayout.LayoutParams(
								mMetrics.widthPixels, mMetrics.heightPixels/2));*/
        mSmallHolder = mLargeVideo.getHolder();
        mSmallHolder.addCallback(this);
        mSmallHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
		mLargeImage = (ImageView)findViewById(R.id.large_image);
		mLargeImage.setOnClickListener(this);
		mLargeImage.setLayoutParams(new FrameLayout.LayoutParams(
							mMetrics.widthPixels, mMetrics.heightPixels/2));
		
		mSmallLayout = findViewById(R.id.small_layout);
		mTapText = (TextView)findViewById(R.id.tap_text);
		mSmallImage = (ImageView)findViewById(R.id.small_image);
		mSmallImage.setOnClickListener(this);
		mSmallImage.post(new Runnable() {
			
			@Override
			public void run() {
				/*int[] location = new int[2];
				mSmallImage.getLocationOnScreen(location);
				mLargeMedia.setPadding(0, location[1], 0, (mMetrics.heightPixels/2)-location[1]);*/
			}
		});
		
		mQuestion = (TextView)findViewById(R.id.question);
		mTotalPoints = (TextView)findViewById(R.id.total_points);
		mQuestionNumber = (TextView)findViewById(R.id.question_number);
		mCurrentPoints = (TextView)findViewById(R.id.current_points);
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
		mNextQuest = (ImageView)findViewById(R.id.next_question);
		mNextQuest.setOnClickListener(this);
		
		View pointsLayout = findViewById(R.id.points_layout);
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) pointsLayout.getLayoutParams();
		int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, mMetrics);
		lp.topMargin = lp.topMargin - margin;
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
		
		mLargeMedia.setVisibility(View.GONE);
		
		if(mLargeVideo != null && mLargeVideo.getVisibility() == View.VISIBLE) {
			diminishVideo();
		}
		if(mTimer != null) {
			mTimer.cancel();
		}
		if(mCurrentQuestion >= mQuestions.size()) {
    		return;
    	}
		final Quiz question = mQuestions.get(mCurrentQuestion);
		
		if(question.questionType == 4) {
			mOption0.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
			mOption1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
			
			float margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, mMetrics);
			
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mOption0.getLayoutParams();
			lp.topMargin = (int) margin;
			lp.bottomMargin = (int) margin;
			
			lp = (LinearLayout.LayoutParams) mOption1.getLayoutParams();
			lp.bottomMargin = (int) margin;
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
		
		mTimeout.setMax((question.duration + 4)*1000);
		
		mQuestion.setText(question.question);
		mQuestionNumber.setText((mCurrentQuestion + 1) + "/" + maxQuestions);
		mCurrentPoints.setText(question.points + "");
		
		String text = mScore + "";
		if(mScore > 0) {
			text = "+" + mScore;
		}
		mTotalPoints.setText(text + " Points");
		
		if(mSmallImage.getDrawable() != null) {
			mSmallImage.getDrawable().setCallback(null);
		}
		if(question.questionType == 1) {
			mSmallLayout.setVisibility(View.GONE);
			mLargeVideo.setVisibility(View.GONE);
		} else if(question.questionType == 2) {
			mSmallLayout.setVisibility(View.VISIBLE);
			mTapText.setText(R.string.tap_to_zoom);
			try {
				mSmallImage.setImageDrawable(Drawable.createFromStream(getAssets().open(
										"Quiz Data/Pictures_Or_Videos/Quiz_Category_" +
										mCategoryId + "/" + question.mediaName), null));
			} catch(Exception e) {
				e.printStackTrace();
			}
			if(mSmallImage.getDrawable() == null) {
				mSmallLayout.setVisibility(View.GONE);
			}
		} else if(question.questionType == 3) {
			mSmallLayout.setVisibility(View.VISIBLE);
			mTapText.setText(R.string.tap_to_play);
			File file = new File(getExternalCacheDir() + "/delete" + mCurrentQuestion);
			if(!file.exists()) {
				mSmallImage.setImageURI(Uri.fromFile(file));
			} else {
				mSmallImage.setImageResource(R.drawable.videooverlay);
			}
		} else if(question.questionType == 4) {
			mSmallLayout.setVisibility(View.GONE);
			mLargeVideo.setVisibility(View.GONE);
			mOption0.setBackgroundResource(R.drawable.boolean_options_normal);
			mOption0.setText("True");
			
			if(animType == 1) {
				mOption0.setVisibility(View.VISIBLE);
				OptionsSlideAnimation anim = new OptionsSlideAnimation(mMetrics.widthPixels, 0, 0, 0, 1);
				anim.setDuration(400);
				mOption0.startAnimation(anim);

				mOption1.setVisibility(View.VISIBLE);
				anim = new OptionsSlideAnimation(mMetrics.widthPixels, 0, 0, 0, 2);
				anim.setDuration(600);
				mOption1.startAnimation(anim);
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
		
		for(int i=0; i<question.options.size(); i++) {
			switch(i) {
			case 0 :
				mOption0.setText(question.options.get(0));
				if(animType == 1) {
					OptionsSlideAnimation anim = new OptionsSlideAnimation(mMetrics.widthPixels, 0, 0, 0, 1);
					anim.setDuration(400);
					mOption0.setVisibility(View.VISIBLE);
					mOption0.startAnimation(anim);
					
					anim = new OptionsSlideAnimation(mMetrics.widthPixels, 0, 0, 0, 2);
					anim.setDuration(600);
					mOption1.setVisibility(View.VISIBLE);
					mOption1.startAnimation(anim);
					
					anim = new OptionsSlideAnimation(mMetrics.widthPixels, 0, 0, 0, 3);
					anim.setDuration(800);
					mOption2.setVisibility(View.VISIBLE);
					mOption2.startAnimation(anim);
					
					anim = new OptionsSlideAnimation(mMetrics.widthPixels, 0, 0, 0, 4);
					anim.setDuration(1000);
					mOption3.setVisibility(View.VISIBLE);
					mOption3.startAnimation(anim);
				}
				break;
				
			case 1 :
				mOption1.setText(question.options.get(1));
				break;
				
			case 2 :
				mOption2.setText(question.options.get(2));
				break;
				
			case 3 :
				mOption3.setText(question.options.get(3));
				break;
			}
		}
		
		mTimer = new CountDownTimer((question.duration)*1000, 100) {
			
			@Override
			public void onTick(long millisUntilFinished) {
				int progress = (int) (((question.duration + 2) * 1000) - millisUntilFinished);
				long text = (millisUntilFinished/1000) + 1;
				mTimeout.setProgress(progress);
				Rect rect = mThumb.getBounds();
				if(text < 10) {
					mThumb.setProgress((int) (text == 0 ? 1 : text) , (int) (rect.left + mLeftOffset1),
									(int) (mTimeout.getHeight()/2 - mTopOffset));
					mSecondaryProgress.setText(text == 0 ? "1" : text + "");
				} else {
					mThumb.setProgress((int) (text == 0 ? 1 : text) , (int) (rect.left + mLeftOffset2),
									(int) (mTimeout.getHeight()/2 - mTopOffset));
					mSecondaryProgress.setText(text == 0 ? "1" : text + "");
				}
				float progressAge = (float)progress/(float)mTimeout.getMax();
				if(progressAge > 0.66) {
					mThumb.setThumbResource(R.drawable.ic_seekbar3);
				} else if(progressAge > 0.33) {
					mThumb.setThumbResource(R.drawable.ic_seekbar2);
				} else {
					mThumb.setThumbResource(R.drawable.ic_seekbar1);
				}
//				Log.d("", "!thumb positions, height: " + mTimeout.getHeight());
			}
			
			@Override
			public void onFinish() {
				mCurrentQuestion++;
				if(mCurrentQuestion < maxQuestions) {
					setupData();
				} else {
					showFinalScreen();
				}
			}
		};
		
		mTimeout.post(new Runnable() {
			
			@Override
			public void run() {
				mTimer.start();
			}
		});
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
	
	private final class ServiceHandler extends Handler {

		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case RETRIEVE_DATA :
				for(int i=0; i<mQuestions.size(); i++) {
					Quiz question = mQuestions.get(i);
					if(question.questionType == 3) {
						try {
							String resName = "";
							if(question.mediaName.contains(".")) {
								String[] temp = question.mediaName.split("\\.");
								resName = temp[0];
							} else {
								resName = question.mediaName;
							}
				    		int resId = getResources().getIdentifier("raw/" + resName, "raw", getPackageName());
							Uri uri = Uri.parse("android.resource://com.quizapp.android/" + resId);
							InputStream iStream = getContentResolver().openInputStream(uri);
							File file = new File(getExternalCacheDir() + "/" + resId);
							FileOutputStream oStream = new FileOutputStream(file);
			                oStream.write(IOUtils.toByteArray(iStream));
			                oStream.flush();
			                oStream.close();
			                iStream.close();
			                
							Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(file.getPath(), Thumbnails.MICRO_KIND);
			                file = new File(getExternalCacheDir() + "/delete" + i);
							oStream = new FileOutputStream(file);
							bitmap.compress(CompressFormat.JPEG, 100, oStream);
							bitmap.recycle();
			                oStream.flush();
			                oStream.close();
			                iStream.close();
							Log.d(TAG, "!bitmap from resource video file: " + bitmap);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				break;
			}
		}
	}

	private void readQuestions() {
		try {
			InputStream assetFileDescriptor = getResources().getAssets().open(
					"Quiz Data/JSON_Format/Quiz_Category_" + mCategoryId + ".json");
			String data = RunningState.readAssetFile(getApplicationContext(), assetFileDescriptor);
			QuestionsParser parser = new QuestionsParser(getApplicationContext());
			parser.parse(data, mCategoryId);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void showFinalScreen() {
		Intent intent = new Intent(this, MovieView.class);
		intent.putExtra("finish", true);
		startActivity(intent);
		
		intent = new Intent(this, HAFinalScreen.class);
		intent.putExtra(Constants.CATGEORY, mCategory.name);
		intent.putExtra(Constants.LEADERBOARD_ID, mCategory.leaderBoardId);
		intent.putExtra(Constants.POINTS, mScore);
		startActivity(intent);
		finish();
	}
	
	private void answerSelected(int option) {
		if(mCurrentQuestion >= mQuestions.size()) {
    		return;
    	}
		Quiz question = mQuestions.get(mCurrentQuestion);
		if(question.questionType != 4) {
			if(question.answer == option) {
				mScore += question.points;
				playSoundForAnswer(true);
			} else {
				mScore -= question.negativePoints;
				playSoundForAnswer(false);
			}
		} else {
			if(question.answer == 1 && option == 0) {
				mScore += question.points;
				playSoundForAnswer(true);
			}else if(question.answer == 0 && option == 1){
				mScore += question.points;
				playSoundForAnswer(true);
			}else{
				mScore -= question.negativePoints;
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
		Quiz question = mQuestions.get(mCurrentQuestion);
		if(question.questionType != 4) {
			try {
				TwoTextButton clicked = (TwoTextButton) HAQuizScreen.class.getDeclaredField(
											"mOption" + selectedOption).get(this);
				TwoTextButton correct = (TwoTextButton) HAQuizScreen.class.getDeclaredField(
											"mOption" + question.answer).get(this);
				
				((LevelListDrawable)correct.getBackground()).setLevel(1);
				if(clicked != correct) {
					((LevelListDrawable)clicked.getBackground()).setLevel(0);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			if(question.answer == 1 && selectedOption == 0) { //true, true
				mOption0.setTextColor(Color.GREEN);
			} else if(question.answer == 1 && selectedOption == 1) { //true, false
				mOption0.setTextColor(Color.GREEN);
				mOption1.setTextColor(Color.RED);
			} else if(question.answer == 0 && selectedOption == 0) { //false, true
				mOption0.setTextColor(Color.RED);
				mOption1.setTextColor(Color.GREEN);
			} else if(question.answer == 0 && selectedOption == 1) { //false, false
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
		case R.id.next_question :
			mCurrentQuestion++;
			if(mCurrentQuestion < maxQuestions) {
				setupData();
			} else {
				showFinalScreen();
			}
			break;
			
		case R.id.home :
			startActivity(new Intent(this, HAStartScreen.class));
			finish();
			break;
			
		case R.id.option1 :
			answerSelected(0);
			break;

		case R.id.option2 :
			answerSelected(1);
			break;

		case R.id.option3 :
			answerSelected(2);
			break;

		case R.id.option4 :
			answerSelected(3);
			break;
			
		case R.id.small_image :
			if(mCurrentQuestion >= mQuestions.size()) {
	    		return;
	    	}
			Quiz question = mQuestions.get(mCurrentQuestion);
			if(question.questionType == 2 && mLargeMedia.getVisibility() != View.VISIBLE) {
				enlargeImage();
			} else if(question.questionType == 3 && mLargeMedia.getVisibility() != View.VISIBLE) {
				String resName = "";
				if(question.mediaName.contains(".")) {
					String[] temp = question.mediaName.split("\\.");
					resName = temp[0];
				} else {
					resName = question.mediaName;
				}
				int resId = getResources().getIdentifier("raw/" + resName, "raw", getPackageName());
				Toast.makeText(getApplicationContext(), "playing video, resource name: " +
								resName + ", resource Id: " + resId,  Toast.LENGTH_LONG).show();
				Uri uri = Uri.parse("android.resource://com.quizapp.android/" + resId);
				if(resId != 0) {
					Intent intent = new Intent(this, MovieView.class);
	                intent.setDataAndType(uri, "video/*");
	                startActivity(intent);
				}/* else {
					enlargeVideo();
				}*/
			}
			break;
			
		case R.id.large_image :
		case R.id.large_video :
			if(mCurrentQuestion >= mQuestions.size()) {
	    		return;
	    	}
			question = mQuestions.get(mCurrentQuestion);
			if(question.questionType == 2 && mLargeMedia.getVisibility() == View.VISIBLE) {
				diminishImage();
			} else if(question.questionType == 3 && mLargeMedia.getVisibility() == View.VISIBLE) {
				diminishVideo();
			}
			break;
		}
	}
	
	private void enlargeVideo() {
		mLargeMedia.setBackgroundColor(Color.BLACK);
		mLargeMedia.setVisibility(View.VISIBLE);
		mLargeVideo.setVisibility(View.VISIBLE);
		int[] location = new int[2];
		mSmallImage.getLocationOnScreen(location);
		Animation anim = new MediaLayoutAnimation(0, 1, 0, 1, location[0], location[1]);
		mLargeVideo.startAnimation(anim);
	}
	
	private void diminishVideo() {
        releaseMediaPlayer();
        doCleanUp();
		int[] location = new int[2];
		mSmallImage.getLocationOnScreen(location);
		Animation anim = new MediaLayoutAnimation(1, 0, 1, 0, location[0], location[1]);
		mLargeVideo.startAnimation(anim);
		mLargeMedia.setVisibility(View.GONE);
		mLargeVideo.setVisibility(View.GONE);
		mSecondaryProgress.setVisibility(View.GONE);
	}
	
	private void enlargeImage() {
		int[] location = new int[2];
		mSmallImage.getLocationOnScreen(location);
		mLargeMedia.setPadding(0, location[1], 0, (mMetrics.heightPixels/2)-location[1]);
		
		mLargeMedia.setVisibility(View.VISIBLE);
		mLargeMedia.setBackgroundColor(Color.BLACK);
		mLargeImage.setVisibility(View.VISIBLE);
		Animation anim = new MediaLayoutAnimation(0.2f, 1, 0.2f, 1, location[0], location[1]);
		mLargeMedia.startAnimation(anim);
		try {
			Quiz question = mQuestions.get(mCurrentQuestion);
			mLargeImage.setImageDrawable(Drawable.createFromStream(getAssets().open(
									"Quiz Data/Pictures_Or_Videos/Quiz_Category_" +
									mCategoryId + "/" + question.mediaName), null));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void diminishImage() {
		mLargeMedia.setPadding(0, 0, 0, 0);
		
		int[] location = new int[2];
		mSmallImage.getLocationOnScreen(location);
		Animation anim = new MediaLayoutAnimation(1, 0.2f, 1, 0.2f, location[0], location[1]);
		mLargeMedia.startAnimation(anim);
		mLargeMedia.setVisibility(View.GONE);
		mSecondaryProgress.setVisibility(View.GONE);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Quiz question;
		switch(keyCode) {
		case KeyEvent.KEYCODE_BACK :
			if(mCurrentQuestion >= mQuestions.size()) {
	    		return false;
	    	}
			question = mQuestions.get(mCurrentQuestion);
			if(question.questionType == 2 && mLargeMedia.getVisibility() == View.VISIBLE) {
				diminishImage();
				return true;
			} else if(question.questionType == 3 && mLargeMedia.getVisibility() == View.VISIBLE) {
				diminishVideo();
				return true;
			}
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void onBufferingUpdate(MediaPlayer arg0, int percent) {
        Log.d(TAG, "onBufferingUpdate percent:" + percent);

    }

    public void onCompletion(MediaPlayer arg0) {
        Log.d(TAG, "onCompletion called");
    }

    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        Log.v(TAG, "onVideoSizeChanged called");
        if (width == 0 || height == 0) {
            Log.e(TAG, "invalid video width(" + width + ") or height(" + height + ")");
            return;
        }
        mIsVideoSizeKnown = true;
        mVideoWidth = width;
        mVideoHeight = height;
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayback();
        }
    }

    public void onPrepared(MediaPlayer mediaplayer) {
        Log.d(TAG, "onPrepared called");
        mIsVideoReadyToBePlayed = true;
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayback();
        }
    }

    public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
        Log.d(TAG, "surfaceChanged called");

    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
        Log.d(TAG, "surfaceDestroyed called");
    }


    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated called");
        playVideo(mSmallHolder);
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

    private void startVideoPlayback() {
        Log.v(TAG, "!startVideoPlayback, width: " + mVideoWidth + ", height: " + mVideoHeight);
        mSmallHolder.setFixedSize(mVideoWidth, mVideoHeight);
        mMediaPlayer.start();
    }
    
    private void playVideo(final SurfaceHolder holder) {
    	doCleanUp();

    	/*
    	 * TODO: Set path variable to progressive streamable mp4 or
    	 * 3gpp format URL. Http protocol should be used.
    	 * Mediaplayer can only play "progressive streamable
    	 * contents" which basically means: 1. the movie atom has to
    	 * precede all the media data atoms. 2. The clip has to be
    	 * reasonably interleaved.
    	 * 
    	 */

    	try {
    		if(mCurrentQuestion >= mQuestions.size()) {
    			return;
    		}
    		Quiz question = mQuestions.get(mCurrentQuestion);
			String resName = "";
			if(question.mediaName.contains(".")) {
				String[] temp = question.mediaName.split("\\.");
				resName = temp[0];
			} else {
				resName = question.mediaName;
			}
    		int resId = getResources().getIdentifier("raw/" + resName, "raw", getPackageName());
    		mMediaPlayer = new MediaPlayer();
    		mMediaPlayer.setDataSource(this, Uri.parse("android.resource://com.quizapp.android/" + resId));
    		mMediaPlayer.setDisplay(holder);
    		mMediaPlayer.setOnBufferingUpdateListener(HAQuizScreen.this);
    		mMediaPlayer.setOnCompletionListener(HAQuizScreen.this);
    		mMediaPlayer.setOnPreparedListener(HAQuizScreen.this);
    		mMediaPlayer.setOnVideoSizeChangedListener(HAQuizScreen.this);
    		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    		mMediaPlayer.prepareAsync();
    	} catch (Exception e) {
    		Log.e(TAG, "error: " + e.getMessage(), e);
    	}
    }
    
    private class MediaLayoutAnimation extends ScaleAnimation {

		private float mToX;

		public MediaLayoutAnimation(float fromX, float toX, float fromY,
				float toY, float pivotX, float pivotY) {
			super(fromX, toX, fromY, toY, pivotX, pivotY);
			mToX = toX;
			setDuration(444);
		}
		
		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
			super.applyTransformation(interpolatedTime, t);
			if(mToX == 1 && interpolatedTime > 0.9) {
				mSecondaryProgress.setVisibility(View.VISIBLE);
			}
			if(mToX == 0.2f && interpolatedTime > 0.8) {
				mLargeMedia.setBackgroundColor(Color.TRANSPARENT);
			}
		}
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
			Quiz question = mQuestions.get(mCurrentQuestion);
			
			if((question.questionType == 4 && mOption == 2 && interpolatedTime > 0.95) ||
					(question.questionType != 4 && mOption == 4 && interpolatedTime > 0.95)) {
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
		Quiz question = mQuestions.get(mCurrentQuestion);
		if(question.questionType != 4) {
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