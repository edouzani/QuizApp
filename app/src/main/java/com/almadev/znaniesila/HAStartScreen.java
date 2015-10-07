package com.almadev.znaniesila;

import com.almadev.znaniesila.model.Category;
import com.almadev.znaniesila.network.SecurityChecker;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.almadev.znaniesila.events.NeedUpdateQuizesEvent;
import com.almadev.znaniesila.events.QuizDownloadedEvent;
import com.almadev.znaniesila.events.QuizesUpdateFailedEvent;
import com.almadev.znaniesila.events.QuizesUpdateFinishedEvent;
import com.almadev.znaniesila.model.CategoriesList;
import com.almadev.znaniesila.model.Question;
import com.almadev.znaniesila.model.Quiz;
import com.almadev.znaniesila.model.QuizHolder;
import com.almadev.znaniesila.network.CategoriesDownloader;
import com.almadev.znaniesila.questions.QuestionsAdapter;
import com.chartboost.sdk.Chartboost;
import com.google.android.gms.games.GamesClient;
import com.almadev.znaniesila.billing.utils.IabHelper;
import com.almadev.znaniesila.billing.utils.IabResult;
import com.almadev.znaniesila.billing.utils.Inventory;
import com.almadev.znaniesila.billing.utils.Purchase;
import com.almadev.znaniesila.utils.ConfigParser;
import com.almadev.znaniesila.utils.Constants;
import com.almadev.znaniesila.utils.RunningState;

import de.greenrobot.event.EventBus;

public class HAStartScreen extends BaseGameActivity implements OnClickListener {
    /**
     * Called when the activity is first created.
     */

    private static final String TAG = "HAStartScreen";
    private Button            play_quiz;
    private ImageView         about;
    private Chartboost        cb;
    private Boolean           adSupportEnabled;
    private Boolean           adsDisabledAfterPurchase;
    private Boolean           gameServicesEnabled;
    private Button            worldScoreButton;
    private SharedPreferences mPrefsmanager;
    private GamesClient       mGamesClient;
    private QuestionsAdapter  mQuestionsAdapter;
    private TextView          progressText;

    private Object lock             = new Object();
    private int    downloadedQuizes = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.main);
        mPrefsmanager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        play_quiz = (Button) findViewById(R.id.play_quiz);
        play_quiz.setOnClickListener(this);
        about = (ImageView) findViewById(R.id.about);
        about.setOnClickListener(this);
        worldScoreButton = (Button) findViewById(R.id.world_score);
        worldScoreButton.setOnClickListener(this);
        findViewById(R.id.wiki).setOnClickListener(this);

        parseConfig();

        gameServicesEnabled = mPrefsmanager.getBoolean(Constants.GAME_SERVICES_ENABLED, false);
        adSupportEnabled = mPrefsmanager.getBoolean(Constants.AD_SUPPORT_NEEDED, false);
        adsDisabledAfterPurchase = mPrefsmanager.getBoolean(Constants.ADS_DISABLED_AFTER_PURCHASE, false);

        if (!gameServicesEnabled) {
            worldScoreButton.setVisibility(View.GONE);
        } else {
            worldScoreButton.setVisibility(View.VISIBLE);
        }

        if (adSupportEnabled && !adsDisabledAfterPurchase) {
            String appId = mPrefsmanager.getString(Constants.CHARTBOOST_APPID, "");
            String appSecret = mPrefsmanager.getString(Constants.CHARTBOOST_APPSECRET, "");
            if (appId.trim().equals("") || appSecret.trim().equals("")) {
                Toast.makeText(this, getResources().getString(R.string.chartboost_error_msg), Toast.LENGTH_SHORT).show();
            } else {
                this.cb = Chartboost.sharedChartboost();
                this.cb.onCreate(this, appId, appSecret, null);
            }
        } else {
        }

        QuizHolder quizHolder = QuizHolder.getInstance(this);
        mQuestionsAdapter = new QuestionsAdapter(this);
        try {
            mQuestionsAdapter.getCategories(new QuestionsAdapter.CategoriesGetCallback() {
                @Override
                public void getCategories(final CategoriesList list) {
                    //close splash
                }
            }, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SecurityChecker.get.start();
    }

    public void onEventMainThread(QuizDownloadedEvent e) {
        synchronized (lock) {
            downloadedQuizes++;
            progressText.setText("Идет обновление викторин:" + downloadedQuizes + "/" + QuizHolder.getInstance(this).getCategories().getCategories().size());
        }
    }

    public void onEventMainThread(NeedUpdateQuizesEvent e) {
        //open splash
        Log.e("Update", "fetching started");
        progressText = (TextView) findViewById(R.id.progressText);
        progressText.setVisibility(View.VISIBLE);
        progressText.setText("Идет обновление викторин: 0/" + QuizHolder.getInstance(this).getCategories().getCategories().size());

        mQuestionsAdapter.fetchQuizes(this);
    }

    public void onEventMainThread(QuizesUpdateFinishedEvent e) {
        findViewById(R.id.splash).setVisibility(View.GONE);
        findViewById(R.id.progressText).setVisibility(View.GONE);
        Log.e("Update", "update finished");
    }


    public void onEventMainThread(QuizesUpdateFailedEvent e) {
//        findViewById(R.id.splash).setVisibility(View.GONE);
//        findViewById(R.id.progressText).setVisibility(View.GONE);
        Log.e("Update", "update failed");
        Toast.makeText(this, "Ошибка загрузки. Попробуйте открыть приложение позднее", Toast.LENGTH_SHORT).show();

        Handler mHandler = new Handler(this.getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 3000);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adSupportEnabled && this.cb != null && !adsDisabledAfterPurchase) {
            this.cb.onStart(this);
            this.cb.startSession();
            this.cb.showInterstitial();
        }
//        EventBus.getDefault().register(this);
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adSupportEnabled && this.cb != null && !adsDisabledAfterPurchase) {
            this.cb.onStop(this);
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    public void onMoreButtonClick(View view) {
        if (adSupportEnabled && this.cb != null && !adsDisabledAfterPurchase)
            this.cb.showMoreApps();
    }

    private void parseConfig() {
        try {
            InputStream assetFileDescriptor = getResources().getAssets().open(
                    "Quiz Data/JSON_Format/Config.json");
            String data = RunningState.readAssetFile(getApplicationContext(), assetFileDescriptor);
            ConfigParser parser = new ConfigParser(getApplicationContext());
            parser.parse(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.play_quiz:
                startActivity(new Intent(this, HACategoriesScreen.class));
                break;

            case R.id.about:
                startActivity(new Intent(this, AboutScreen.class));
                break;

            case R.id.wiki:
                Intent wikiIntent = new Intent(this, HACategoriesScreen.class);
                wikiIntent.putExtra(Constants.CATEGORY_FOR_KNOWLEDGE, true);
                startActivity(wikiIntent);
                break;
            case R.id.world_score:
                beginUserInitiatedSignIn();
                mGamesClient = this.getGamesClient();
                Log.d("Google Play Game Services", "mGamesClient=" + mGamesClient);
                if (mGamesClient != null) {
                    try {
                        startActivityForResult(mGamesClient.getAllLeaderboardsIntent(), 1);
                    } catch (Exception e) {
                        Log.d("Google Play Game Services", "Problem connecting to playservices");
                        e.printStackTrace();
                    }
                } else {
                    Log.d("Google Play Game Services", "Still not connected");
                }
                break;

        }
    }

    private void disableAds() {
        Editor edit = mPrefsmanager.edit();
        edit.putBoolean(Constants.ADS_DISABLED_AFTER_PURCHASE, true);
        edit.commit();
        this.cb = null;
    }


    @Override
    public void onSignInFailed() {
        // TODO Auto-generated method stub
        Log.d("Google Play Game Services", "Sign In Failed");

    }

    @Override
    public void onSignInSucceeded() {
        // TODO Auto-generated method stub
        Log.d("Google Play Game Services", "Sign In Successful. Submitting scores");
        if (mGamesClient != null) {
            try {
//				 startActivityForResult(mGamesClient.getAllLeaderboardsIntent(), 1);
            } catch (Exception e) {
                Log.d("Google Play Game Services", "Problem connecting to playservices");
                e.printStackTrace();
            }
        }
    }
}