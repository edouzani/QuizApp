package com.almadev.znaniesila;

import com.almadev.znaniesila.R;
import com.almadev.znaniesila.utils.Constants;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

public class AboutScreen extends Activity{
	
	private TextView aboutText;
	private SharedPreferences mPrefsmanager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_layout);
		aboutText=(TextView) findViewById(R.id.about_text);
		mPrefsmanager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		aboutText.setText(mPrefsmanager.getString(Constants.ABOUT_TEXT,""));
	}
}
