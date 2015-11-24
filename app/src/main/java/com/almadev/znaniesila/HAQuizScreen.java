package com.almadev.znaniesila;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.LevelListDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.almadev.znaniesila.model.Category;
import com.almadev.znaniesila.model.Question;
import com.almadev.znaniesila.model.QuestionState;
import com.almadev.znaniesila.model.Quiz;
import com.almadev.znaniesila.model.QuizHolder;
import com.almadev.znaniesila.ui.Timer;
import com.almadev.znaniesila.ui.TwoTextButton;
import com.almadev.znaniesila.utils.Constants;
import com.almadev.znaniesila.utils.LeaderboardConverter;
import com.squareup.picasso.Picasso;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

public class HAQuizScreen extends Activity implements OnClickListener, Callback,
                                                      OnBufferingUpdateListener, OnCompletionListener {

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
    private DisplayMetrics      mMetrics;
    private HandlerThread       mThread;
    private SharedPreferences   mPrefsManager;
    private boolean             isStarting;
    private View                mNextQuest;
    private Boolean             adSupportEnabled;
    private Boolean             adsDisabledAfterPurchase;
    private Timer               mTimer;
    private int                 maxPoints;
    private Timer.TimerCallback mTimerCallback;
    private ImageView           mBigImage;
    private View                mFullScreenImage;
    private String                   imgUrl                    = null;
    private boolean                  mDescriptionVisible       = false;
    private DescriptionClickListener mDescriptionClickListener = new DescriptionClickListener();
    private MediaPlayer bgMusicPlayer;

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

        Collections.sort(mQuestions, new Comparator<Question>() {
            @Override
            public int compare(final Question pQuestion, final Question pT1) {
                int lw = pQuestion.getState().getWeight() + pQuestion.getLocal_id();
                if (ZSApp.DEBUG_ENV) {
                    lw += (pQuestion.getImage_url() != null) ? -50000 : 0;
                }
                int rw = pT1.getState().getWeight() + pT1.getLocal_id();
                if (ZSApp.DEBUG_ENV) {
                    rw += (pT1.getImage_url() != null) ? -50000 : 0;
                }
                if (lw < rw) {
                    return -1;
                } else if (lw > rw) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });


        mCurrentQuestion = 0;
        maxQuestions = mQuestions.size() < mCategory.getCategory_question_max_limit() ?
                mQuestions.size() : mCategory.getCategory_question_max_limit();

        mQuestions = mQuestions.subList(0, maxQuestions);
        Collections.shuffle(mQuestions);

        mTimer = (Timer) findViewById(R.id.timer);
        mThread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mThread.start();
//		mServiceHandler = new ServiceHandler(mThread.getLooper());
//		mServiceHandler.obtainMessage(RETRIEVE_DATA).sendToTarget();

        setupViews();
        adSupportEnabled = mPrefsManager.getBoolean(Constants.AD_SUPPORT_NEEDED, false);
        adsDisabledAfterPurchase = mPrefsManager.getBoolean(Constants.ADS_DISABLED_AFTER_PURCHASE, false);
//        if (adSupportEnabled && !adsDisabledAfterPurchase) {
//            String appId = mPrefsManager.getString(Constants.CHARTBOOST_APPID, "");
//            String appSecret = mPrefsManager.getString(Constants.CHARTBOOST_APPSECRET, "");
//            if (appId.trim().equals("") || appSecret.trim().equals("")) {
//                Toast.makeText(this, getResources().getString(R.string.chartboost_error_msg), Toast.LENGTH_SHORT).show();
//            } else {
//                this.cb = Chartboost.sharedChartboost();
//                this.cb.onCreate(this, appId, appSecret, null);
//            }
//        }
        mTimerCallback = new TimerCallbackImpl();

        initBackgroundMusic();
    }

    private void initBackgroundMusic() {
        boolean playSound = mPrefsManager.getBoolean(Constants.PREF_MUSIC_ON, true);

        if (playSound) {
            bgMusicPlayer = MediaPlayer.create(this, R.raw.bg_music_game);
            bgMusicPlayer.setLooping(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isStarting) {
            setupData();
            isStarting = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTimer.run();
        if (bgMusicPlayer != null) {
            bgMusicPlayer.start();
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
        mTimer.stop();
    }

    private void backPress() {
        super.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (mDescriptionVisible) {
            mDescriptionClickListener.onClick(null);
        } else {
            AlertDialog.Builder ad;
            ad = new AlertDialog.Builder(this);
            ad.setTitle("Выход");  // заголовок
            ad.setMessage("Действительно ли Вы хотите покинуть викторину?"); // сообщение
            ad.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
//                    Toast.makeText(HAQuizScreen.this, "Вы сделали правильный выбор",
//                                   Toast.LENGTH_LONG).show();
                    backPress();
                }
            });
            ad.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
//                    Toast.makeText(HAQuizScreen.this, "Возможно вы правы", Toast.LENGTH_LONG)
//                         .show();
                }
            });
            ad.setCancelable(true);
            ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
//                    Toast.makeText(HAQuizScreen.this, "Вы ничего не выбрали",
//                                   Toast.LENGTH_LONG).show();
                }
            });
            ad.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // Пользователь успешно авторизовался
            }

            @Override
            public void onError(VKError error) {
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    class TimerCallbackImpl implements Timer.TimerCallback {

        @Override
        public void onTimer() {
            findViewById(R.id.next_question).callOnClick();
        }
    }

    private void setupViews() {
        mBigImage = (ImageView) findViewById(R.id.big_image);

        mFullScreenImage = findViewById(R.id.fullscreen_image);
        mFullScreenImage.setOnClickListener(this);

        mSmallImage = (ImageView) findViewById(R.id.small_image);
        mSmallImage.setOnClickListener(this);

        mQuestion = (TextView) findViewById(R.id.question);
        mQuestionNumber = (TextView) findViewById(R.id.question_number);
        mCurrentPoints = (TextView) findViewById(R.id.current_points);

        mLeftBtn = (Button) findViewById(R.id.left_btn);
        mLeftBtn.setOnClickListener(this);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);

        mOption0 = (TwoTextButton) findViewById(R.id.option1);
        mOption0.setAlternateText("1");
        mOption0.setOnClickListener(this);

        mOption1 = (TwoTextButton) findViewById(R.id.option2);
        mOption1.setAlternateText("2");
        mOption1.setOnClickListener(this);

        mOption2 = (TwoTextButton) findViewById(R.id.option3);
        mOption2.setAlternateText("3");
        mOption2.setOnClickListener(this);

        mOption3 = (TwoTextButton) findViewById(R.id.option4);
        mOption3.setAlternateText("4");
        mOption3.setOnClickListener(this);

        findViewById(R.id.home).setOnClickListener(this);
        mNextQuest = findViewById(R.id.next_question);
        mNextQuest.setOnClickListener(this);

        TextView catname = (TextView) findViewById(R.id.cat_name);
        catname.setText(mCategory.getCategory_name());
    }

    private void setupData() {
        mTimer.stop();
        int animType = mPrefsManager.getInt(Constants.OPTIONS_ANIMATION, 1);

        mLeftBtn.setBackgroundResource(R.drawable.btn_left);
        mRightBtn.setBackgroundResource(R.drawable.btn_right);
        mNextQuest.setEnabled(false);

        if (mCurrentQuestion >= mQuestions.size()) {
            return;
        }
        Question question = mQuestions.get(mCurrentQuestion);

        maxPoints += question.getPoints();
        if (question.getState() != QuestionState.CORRECT) {
            question.setState(QuestionState.VIEWED);
        }


//		mTimeout.setMax((question.duration + 4)*1000);

        mQuestion.setText(question.getQuestion());
        mQuestionNumber.setText((mCurrentQuestion + 1) + "/" + maxQuestions);
//        mCurrentPoints.setText(question.getPoints() + "");
        mCurrentPoints.setText(mScore + "");

        String text = mScore + "";
        if (mScore > 0) {
            text = "+" + mScore;
        }

        imgUrl = question.getImage_url();
        if (imgUrl != null) {
            Picasso.with(this).load(imgUrl).into(mSmallImage);
        } else {
            mSmallImage.setImageDrawable(null);
        }

//			mSmallLayout.setVisibility(View.GONE);
//			mLargeVideo.setVisibility(View.GONE);
        mOption0.setBackgroundResource(R.drawable.boolean_options_normal);
        mOption0.setText("True");

        if (animType == 1) {
//				mOption0.setVisibility(View.VISIBLE);
            OptionsSlideAnimation anim = new OptionsSlideAnimation(mMetrics.widthPixels, 0, 0, 0, 1);
            anim.setDuration(400);
            mLeftBtn.startAnimation(anim);

//				mOption1.setVisibility(View.VISIBLE);
            anim = new OptionsSlideAnimation(mMetrics.widthPixels, 0, 0, 0, 2);
            anim.setDuration(600);
            mRightBtn.startAnimation(anim);

            mTimer.start(question.getDuration_in_seconds(), mTimerCallback);
        }

        mOption0.setAlternateText("");
        mOption0.setGravity(Gravity.CENTER);
        mOption0.setPadding(0, 0, 0, 0);

        mOption1.setText("False");
        mOption1.setBackgroundResource(R.drawable.boolean_options_normal);
        mOption1.setAlternateText("");
        mOption1.setGravity(Gravity.CENTER);
        mOption1.setPadding(0, 0, 0, 0);

//        if (animType == 2) {
//            startOptionsFadeAnimation();
//        }
    }

    private Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SET_NEXT_DATA:
                    mCurrentQuestion++;
                    if (mCurrentQuestion < maxQuestions) {
                        setupData();
                    } else {
                        showFinalScreen();
                    }
                    break;
            }
        }
    };

    private void showFinalScreen() {
        mTimer.stop();
        mQuizHolder.saveQuiz(mQuiz);
        Intent intent = null;
        intent = new Intent(this, HAFinalScreen.class);
        intent.putExtra(Constants.CATEGORY, mCategory);
        intent.putExtra(Constants.LEADERBOARD_ID, LeaderboardConverter.getLeaderboard(this, mCategory.getLeaderboard_id()));
//        intent.putExtra(Constants.POINTS, mScore < 0 ? 0 : mScore);
        intent.putExtra(Constants.POINTS, mScore);
        intent.putExtra(Constants.MAX_POINTS, maxPoints);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void answerSelected(int option) {
        if (mCurrentQuestion >= mQuestions.size()) {
            return;
        }
        boolean isCorrect = false;

        mTimer.stop();
        Question question = mQuestions.get(mCurrentQuestion);

        if (question.getAnswer() == option) {
            if (option == 1) {
                mLeftBtn.setBackgroundResource(R.drawable.btn_left_true);
            } else if (option == 0) {
                mRightBtn.setBackgroundResource(R.drawable.btn_right_true);
            }
            question.setState(QuestionState.CORRECT);
            mScore += mTimer.interpolatePoints(question.getPoints());
            playSoundForAnswer(true);
            isCorrect = true;
        } else {
            if (option == 1) {
                mLeftBtn.setBackgroundResource(R.drawable.btn_left_false);
            } else if (option == 0) {
                mRightBtn.setBackgroundResource(R.drawable.btn_right_false);
            }

            if (question.getState() != QuestionState.CORRECT) {
                question.setState(QuestionState.WRONG);
            }
            mScore -= question.getNegative_points();
            playSoundForAnswer(false);
            isCorrect = false;
        }

        mCurrentPoints.setText(mScore + "");

        mOption0.setEnabled(false);
        mOption1.setEnabled(false);
        mOption2.setEnabled(false);
        mOption3.setEnabled(false);
        mLeftBtn.setEnabled(false);
        mRightBtn.setEnabled(false);

        final boolean finalIsCorrect = isCorrect;
        final Question finalQuestion = question;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showAnswerDescription(finalIsCorrect, finalQuestion);
            }
        }, 200);
    }

    private void showAnswerDescription(boolean correct, Question question) {
        mDescriptionVisible = true;
        if (correct) {
            findViewById(R.id.wrong_text).setVisibility(View.GONE);
            findViewById(R.id.correct_text).setVisibility(View.VISIBLE);
            findViewById(R.id.answer_description).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.answer_description)).setText(question.getCorrect_ans_explanation());
        } else {
            findViewById(R.id.correct_text).setVisibility(View.GONE);
            findViewById(R.id.wrong_text).setVisibility(View.VISIBLE);
            findViewById(R.id.answer_description).setVisibility(View.GONE);
//            ((TextView) findViewById(R.id.answer_description)).setText(question.getWrong_ans_explanation());
        }

        findViewById(R.id.answer_result_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.answer_result_layout).setOnClickListener(mDescriptionClickListener);
    }

    class DescriptionClickListener implements OnClickListener {
        @Override
        public void onClick(final View pView) {
            mDescriptionVisible = false;
            findViewById(R.id.answer_result_layout).setVisibility(View.GONE);

            mCurrentQuestion++;
            if (mCurrentQuestion < maxQuestions) {
                setupData();
            } else {
                showFinalScreen();
            }
        }
    }

    private void changeDrawableState(int selectedOption) {
        if (mCurrentQuestion >= mQuestions.size()) {
            return;
        }
        Question question = mQuestions.get(mCurrentQuestion);
        if (question.getQuestion_type() != 4) {
            try {
                TwoTextButton clicked = (TwoTextButton) HAQuizScreen.class.getDeclaredField(
                        "mOption" + selectedOption).get(this);
                TwoTextButton correct = (TwoTextButton) HAQuizScreen.class.getDeclaredField(
                        "mOption" + question.getAnswer()).get(this);

                ((LevelListDrawable) correct.getBackground()).setLevel(1);
                if (clicked != correct) {
                    ((LevelListDrawable) clicked.getBackground()).setLevel(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (question.getAnswer() == 1 && selectedOption == 0) { //true, true
                mOption0.setTextColor(Color.GREEN);
            } else if (question.getAnswer() == 1 && selectedOption == 1) { //true, false
                mOption0.setTextColor(Color.GREEN);
                mOption1.setTextColor(Color.RED);
            } else if (question.getAnswer() == 0 && selectedOption == 0) { //false, true
                mOption0.setTextColor(Color.RED);
                mOption1.setTextColor(Color.GREEN);
            } else if (question.getAnswer() == 0 && selectedOption == 1) { //false, false
                mOption1.setTextColor(Color.GREEN);
            }
        }
        mUiHandler.sendEmptyMessageDelayed(SET_NEXT_DATA, 777);
    }

    private void playSoundForAnswer(boolean isCorrect) //send YES if answer is correct No if wrong. Also call this method for True false questions too.
    {
//        boolean playSound = mPrefsManager.getBoolean(Constants.PLAY_SOUND_ON_ANSWERING, false);
        boolean playSound = mPrefsManager.getBoolean(Constants.PREF_SOUND_ON, true);

        if (playSound) {
            MediaPlayer player;

            if (isCorrect) {
                player = MediaPlayer.create(this, R.raw.yes);
            } else {
                player = MediaPlayer.create(this, R.raw.no);
            }
            if (player != null) {
                player.start();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next_question:
                mCurrentQuestion++;
                mTimer.stop();
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

            case R.id.right_btn:
            case R.id.option1:
                answerSelected(0);
                break;

            case R.id.left_btn:
            case R.id.option2:
                answerSelected(1);
                break;

            case R.id.option3:
                answerSelected(2);
                break;

            case R.id.option4:
                answerSelected(3);
                break;

            case R.id.small_image:
                if (imgUrl != null) {
                    Picasso.with(this).load(imgUrl).into(mBigImage);
                    findViewById(R.id.fullscreen_image).setVisibility(View.VISIBLE);
                }
                break;

            case R.id.fullscreen_image:
                findViewById(R.id.fullscreen_image).setVisibility(View.GONE);
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
        if (bgMusicPlayer != null) {
            bgMusicPlayer.pause();
        }
        releaseMediaPlayer();
        doCleanUp();
        mTimer.pause();
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
            setInterpolator(new DecelerateInterpolator());
        }

        @Override
        protected void applyTransformation(float interpolatedTime,
                                           Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            if (mCurrentQuestion >= mQuestions.size()) {
                return;
            }
            Question question = mQuestions.get(mCurrentQuestion);

            if (interpolatedTime > 0.95) {
                mOption0.setEnabled(true);
                mOption1.setEnabled(true);
                mOption2.setEnabled(true);
                mOption3.setEnabled(true);

                mLeftBtn.setEnabled(true);
                mRightBtn.setEnabled(true);
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

        if (mCurrentQuestion >= mQuestions.size()) {
            return;
        }
        Question question = mQuestions.get(mCurrentQuestion);
        if (question.getQuestion_type() != 4) {
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