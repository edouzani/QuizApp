package com.almadev.znaniesila;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import com.almadev.znaniesila.utils.Constants;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences mPrefsmanager;
    private boolean           isSoundOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        findViewById(R.id.home).setOnClickListener(this);

        mPrefsmanager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        isSoundOn = mPrefsmanager.getBoolean(Constants.SOUND_ON, true);

        SwitchCompat mSoundSwitch = (SwitchCompat) findViewById(R.id.soundSwitch);
        mSoundSwitch.setChecked(isSoundOn);
        mSoundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton pCompoundButton, final boolean b) {
                SharedPreferences.Editor e = mPrefsmanager.edit();
                e.putBoolean(Constants.SOUND_ON, b);
                e.commit();
            }
        });
    }

    @Override
    public void onClick(final View pView) {
        switch (pView.getId()) {
            case R.id.home: finish();
                break;
        }
    }
}
