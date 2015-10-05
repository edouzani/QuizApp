package com.almadev.znaniesila.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.almadev.znaniesila.BuildConfig;

public class ConfigParser {
	
	private static RunningState mState;
	private static SharedPreferences mPrefsManager;

	public ConfigParser(Context context) {
		mState = RunningState.getInstance(context.getApplicationContext());
		mPrefsManager = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
	}
	
	public void parse(String data) {
		if (BuildConfig.DEBUG) {
			Log.d("Parser", data);
		}
		resetPreferences();
		try {
			JSONObject jObject = new JSONObject(data);
			JSONArray keys = jObject.names();
			for(int i=0; i<keys.length(); i++) {
				if(keys.getString(i).equalsIgnoreCase("config")) {
					ConfigKeys.parseConfig(jObject.getJSONObject(keys.getString(i)));
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private enum ConfigKeys {
		defaultkey,
		
		shufflequestions, shuffleansweroptions, highlightcorrectoptionwhenanswered,
		useanimationtypeforanswers, categoryscreentitle, mainscreentitle,
		categoryscreentitlefontsize, finalscreentitlefontsize, playcorrectwronsoundonanswering,
		adsupportneeded, chartboostappid, chartboostappsecret, appkey64bit, removeadssku, gameservicessupportneeded, abouttext;
		
		private static void parseConfig(JSONObject config) {
			JSONArray keys = config.names();
			Editor edit = mPrefsManager.edit();
			for(int i=0; i<keys.length(); i++) {
				try {
					String configKey = keys.getString(i).trim();
					String configValue = config.getString(configKey).trim();
					if(!TextUtils.isEmpty(configValue) && !TextUtils.isEmpty(configKey.trim())) {
						ConfigKeys configEnum = defaultkey;
						try {
							configEnum = ConfigKeys.valueOf(configKey.trim().toLowerCase());
						} catch(Exception e) {
							e.printStackTrace();
						}
						switch(configEnum) {
						case shufflequestions :
							boolean shuffleQ = Boolean.parseBoolean(configValue);
							edit.putBoolean(Constants.SHUFFLE_QUESTIONS, shuffleQ);
							break;
							
						case shuffleansweroptions :
							boolean shuffleA = Boolean.parseBoolean(configValue);
							edit.putBoolean(Constants.SHUFFLE_OPTIONS, shuffleA);
							break;
							
						case highlightcorrectoptionwhenanswered :
							boolean highlightO = Boolean.parseBoolean(configValue);
							edit.putBoolean(Constants.HIGHLIGHT_OPTIONS, highlightO);
							break;
							
						case useanimationtypeforanswers :
							int animType = Integer.parseInt(configValue);
							edit.putInt(Constants.OPTIONS_ANIMATION, animType);
							break;
							
						case categoryscreentitle :
							edit.putString(Constants.CATEGORY_TITLE, configValue);
							break;
							
						case mainscreentitle :
							edit.putString(Constants.MAIN_TITLE, configValue);
							break;
							
						case categoryscreentitlefontsize :
							int cFont = Integer.parseInt(configValue);
							edit.putInt(Constants.CATEGORY_TITLE_FONT_SIZE, cFont);
							break;
							
						case finalscreentitlefontsize :
							int fFont = Integer.parseInt(configValue);
							edit.putInt(Constants.FINAL_SCREEN_FONT_SIZE, fFont);
							break;
						
						case playcorrectwronsoundonanswering :
							boolean playSound = Boolean.parseBoolean(configValue);
							edit.putBoolean(Constants.PLAY_SOUND_ON_ANSWERING, playSound);
							break;
							
						case adsupportneeded :	
							boolean adSupport = Boolean.parseBoolean(configValue);
							edit.putBoolean(Constants.AD_SUPPORT_NEEDED, adSupport);
							break;
						
						case chartboostappid :	
							edit.putString(Constants.CHARTBOOST_APPID, configValue);
							break;	
						
						case chartboostappsecret :	
							edit.putString(Constants.CHARTBOOST_APPSECRET,configValue);
							break;		
							
						case appkey64bit :	
							edit.putString(Constants.APPKEY_64BIT,configValue);
							break;		
							
						case removeadssku:
							edit.putString(Constants.REMOVE_ADS_SKU,configValue);
							break;		
							
						case gameservicessupportneeded:
							boolean gameServices = Boolean.parseBoolean(configValue);
							edit.putBoolean(Constants.GAME_SERVICES_ENABLED,gameServices);
							break;		
							
						case abouttext :	
							edit.putString(Constants.ABOUT_TEXT,configValue);
							break;		
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			edit.commit();
		}
	}
	
	private void resetPreferences(){
		Editor edit = mPrefsManager.edit();
		edit.putBoolean(Constants.AD_SUPPORT_NEEDED, false);
		edit.putString(Constants.CHARTBOOST_APPID, "");
		edit.putString(Constants.CHARTBOOST_APPID, "");
		edit.commit();
	}

}