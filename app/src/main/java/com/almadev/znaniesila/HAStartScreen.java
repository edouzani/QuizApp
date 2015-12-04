package com.almadev.znaniesila;

import java.io.InputStream;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.ads.conversiontracking.AdWordsConversionReporter;
import com.almadev.znaniesila.utils.ConfigParser;
import com.almadev.znaniesila.utils.Constants;
import com.almadev.znaniesila.utils.RunningState;

public class HAStartScreen extends BaseGameActivity implements OnClickListener {
    /**
     * Called when the activity is first created.
     */

    private static final String TAG = "HAStartScreen";
    private Button            play_quiz;
    private ImageView         about;
    private Boolean           gameServicesEnabled;
    private Button            worldScoreButton;
    private SharedPreferences mPrefsManager;
    private MediaPlayer       player;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mPrefsManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        play_quiz = (Button) findViewById(R.id.play_quiz);
        play_quiz.setOnClickListener(this);
        about = (ImageView) findViewById(R.id.about);
        about.setOnClickListener(this);
        worldScoreButton = (Button) findViewById(R.id.world_score);
        worldScoreButton.setOnClickListener(this);
        findViewById(R.id.wiki).setOnClickListener(this);
        findViewById(R.id.settings).setOnClickListener(this);

        parseConfig();

        gameServicesEnabled = mPrefsManager.getBoolean(Constants.GAME_SERVICES_ENABLED, false);

        if (!gameServicesEnabled) {
            worldScoreButton.setVisibility(View.GONE);
        } else {
            worldScoreButton.setVisibility(View.VISIBLE);
        }

        //init first run

        mPrefsManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean isFirstRun = mPrefsManager.getBoolean(Constants.PREF_FIRS_RUN, true);

        if (isFirstRun) {
            AdWordsConversionReporter.reportWithConversionId(this.getApplicationContext(),
                                                             "956867547", "yqeQCLfzmGEQ28eiyAM", "3.00", true);

            SharedPreferences.Editor e = mPrefsManager.edit();
            e.putBoolean(Constants.PREF_FIRS_RUN, false);
            e.commit();
        }

        initPlayer();
    }

    private void initPlayer() {
        boolean playSound = mPrefsManager.getBoolean(Constants.PREF_MUSIC_ON, true);

        if (playSound && player == null) {
            player = MediaPlayer.create(this, R.raw.main);
            if (player != null) {
                player.setLooping(false);
            }
        }
    }

    @Override
    public void onConnected(final Bundle pBundle) {
        super.onConnected(pBundle);
        getAllLeaderboards();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean playSound = mPrefsManager.getBoolean(Constants.PREF_MUSIC_ON, true);
        if (playSound) {
            initPlayer();
            player.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private static final int TIME_INTERVAL = 3000; // # milliseconds, desired time passed between two back presses.
    private long mBackPressed;

    @Override
    public void onBackPressed() {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        } else {
            Toast.makeText(getBaseContext(), "Нажмите еще раз для выхода из приложения", Toast.LENGTH_SHORT).show();
        }

        mBackPressed = System.currentTimeMillis();
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
                getAllLeaderboards();
                break;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
    }

    private void disableAds() {
        Editor edit = mPrefsManager.edit();
        edit.putBoolean(Constants.ADS_DISABLED_AFTER_PURCHASE, true);
        edit.commit();
    }
}