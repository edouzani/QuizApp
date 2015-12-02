package com.almadev.znaniesila;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.almadev.znaniesila.utils.Constants;
import com.yandex.metrica.YandexMetrica;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences mPrefsmanager;
    private boolean           isSoundOn;
    private boolean           isMusicOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        findViewById(R.id.home).setOnClickListener(this);

//        YandexMetrica.reportEvent("TEST_EVENT");

        mPrefsmanager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        isSoundOn = mPrefsmanager.getBoolean(Constants.PREF_SOUND_ON, true);

        SwitchCompat mSoundSwitch = (SwitchCompat) findViewById(R.id.soundSwitch);
        mSoundSwitch.setChecked(isSoundOn);
        mSoundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton pCompoundButton, final boolean b) {
                SharedPreferences.Editor e = mPrefsmanager.edit();
                e.putBoolean(Constants.PREF_SOUND_ON, b);
                e.commit();
            }
        });

        isMusicOn = mPrefsmanager.getBoolean(Constants.PREF_MUSIC_ON, true);

        SwitchCompat mMusicSwitch = (SwitchCompat) findViewById(R.id.musicSwitch);
        mMusicSwitch.setChecked(isMusicOn);
        mMusicSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton pCompoundButton, final boolean b) {
                SharedPreferences.Editor e = mPrefsmanager.edit();
                e.putBoolean(Constants.PREF_MUSIC_ON, b);
                e.commit();
            }
        });



        String versionText = "v" + BuildConfig.VERSION_CODE + "/" + BuildConfig.VERSION_NAME;
        if (ZSApp.DEBUG_ENV) {
            versionText += "-test";
        }
        ((TextView) findViewById(R.id.versionText)).setText(versionText);

    }

    @Override
    public void onClick(final View pView) {
        switch (pView.getId()) {
            case R.id.home: finish();
                break;
        }
    }
}
