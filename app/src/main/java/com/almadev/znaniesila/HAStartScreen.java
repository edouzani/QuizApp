package com.almadev.znaniesila;

import com.almadev.znaniesila.network.SecurityChecker;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

import java.io.IOException;
import java.io.InputStream;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.almadev.znaniesila.events.NeedUpdateQuizesEvent;
import com.almadev.znaniesila.events.QuizDownloadedEvent;
import com.almadev.znaniesila.events.QuizesUpdateFailedEvent;
import com.almadev.znaniesila.events.QuizesUpdateFinishedEvent;
import com.almadev.znaniesila.model.CategoriesList;
import com.almadev.znaniesila.model.QuizHolder;
import com.almadev.znaniesila.questions.QuestionsAdapter;
import com.google.ads.conversiontracking.AdWordsConversionReporter;
import com.google.android.gms.games.GamesClient;
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
        findViewById(R.id.settings).setOnClickListener(this);

        parseConfig();

        gameServicesEnabled = mPrefsmanager.getBoolean(Constants.GAME_SERVICES_ENABLED, false);
        adSupportEnabled = mPrefsmanager.getBoolean(Constants.AD_SUPPORT_NEEDED, false);
        adsDisabledAfterPurchase = mPrefsmanager.getBoolean(Constants.ADS_DISABLED_AFTER_PURCHASE, false);

        if (!gameServicesEnabled) {
            worldScoreButton.setVisibility(View.GONE);
        } else {
            worldScoreButton.setVisibility(View.VISIBLE);
        }

        QuizHolder quizHolder = QuizHolder.getInstance(this);
        mQuestionsAdapter = new QuestionsAdapter(this);

        String versionText = BuildConfig.VERSION_CODE + "/" + BuildConfig.VERSION_NAME;
        if (BuildConfig.DEBUG) {
            versionText += "-test";
        }
        ((TextView)findViewById(R.id.versionText)).setText(versionText);

        SecurityChecker.get.start();

        //init first run

        mPrefsmanager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean isFirstRun = mPrefsmanager.getBoolean(Constants.PREF_FIRS_RUN, true);

        if (isFirstRun) {
            AdWordsConversionReporter.reportWithConversionId(this.getApplicationContext(),
                                                             "956867547", "yqeQCLfzmGEQ28eiyAM", "3.00", true);

            SharedPreferences.Editor e = mPrefsmanager.edit();
            e.putBoolean(Constants.PREF_FIRS_RUN, false);
            e.commit();
        }
    }

    public void onEventMainThread(QuizDownloadedEvent e) {
        synchronized (lock) {
            downloadedQuizes++;
            progressText.setText("Идет обновление викторин:" + downloadedQuizes + "/" + QuizHolder.getInstance(this).getCategories().getCategories().size());
        }
    }

    public void onEventMainThread(NeedUpdateQuizesEvent e) {
        Log.e("Update", "fetching started");
        Log.e("Update", "updating to version " + e.getVersion());
        progressText = (TextView) findViewById(R.id.progressText);
        progressText.setVisibility(View.VISIBLE);
        progressText.setText("Идет обновление викторин: 0/" + QuizHolder.getInstance(this).getCategories().getCategories().size());

        mQuestionsAdapter.fetchQuizes(this);
    }

    public void onEventMainThread(QuizesUpdateFinishedEvent e) {
        findViewById(R.id.splash).setVisibility(View.GONE);
        if (!BuildConfig.DEBUG) {
            findViewById(R.id.versionText).setVisibility(View.GONE);
        }
        findViewById(R.id.progressText).setVisibility(View.GONE);
        Log.e("Update", "update finished");
    }


    public void onEventMainThread(QuizesUpdateFailedEvent e) {
        Log.e("Update", "update failed");
        QuizHolder.getInstance(this).clear();

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
//        EventBus.getDefault().register(this);
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            findViewById(R.id.splash).setVisibility(View.VISIBLE);
            findViewById(R.id.versionText).setVisibility(View.VISIBLE);
            mQuestionsAdapter.getCategories(new QuestionsAdapter.CategoriesGetCallback() {
                @Override
                public void getCategories(final CategoriesList list) {
                    //close splash
                }
            }, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void onMoreButtonClick(View view) {
        //
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
                Log.d("GPLay services", "mGamesClient=" + mGamesClient);
                if (mGamesClient != null) {
                    try {
                        startActivityForResult(mGamesClient.getAllLeaderboardsIntent(), 1);
                    } catch (Exception e) {
                        Log.d("GPLay services", "Problem connecting to playservices");
                        e.printStackTrace();
                    }
                } else {
                    Log.d("GPLay services", "Still not connected");
                }
                break;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
    }

    private void disableAds() {
        Editor edit = mPrefsmanager.edit();
        edit.putBoolean(Constants.ADS_DISABLED_AFTER_PURCHASE, true);
        edit.commit();
    }


    @Override
    public void onSignInFailed() {
        // TODO Auto-generated method stub
        Log.d("GPLay services", "Sign In Failed");

    }

    @Override
    public void onSignInSucceeded() {
        // TODO Auto-generated method stub
        Log.d("GPLay services", "Sign In Successful. Submitting scores");
        if (mGamesClient != null) {
            try {
//				 startActivityForResult(mGamesClient.getAllLeaderboardsIntent(), 1);
            } catch (Exception e) {
                Log.d("GPLay services", "Problem connecting to playservices");
                e.printStackTrace();
            }
        }
    }
}