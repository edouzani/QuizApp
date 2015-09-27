package com.almadev.znaniesila;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.almadev.znaniesila.R;
import com.almadev.znaniesila.utils.Constants;
import com.chartboost.sdk.Chartboost;
import com.google.android.gms.games.GamesClient;

public class HAFinalScreen extends BaseGameActivity implements OnClickListener {
	
	private String mCategory;
	private int mPoints;
	private SharedPreferences mPrefsmanager;
	public static final String HIGH_SCORES = "high_scores";
	private Chartboost cb;
	private Boolean adSupportEnabled;
	private Boolean adsDisabledAfterPurchase;
	private Boolean gameServicesEnabled;
	private Button moreAppsButton;
	private Button worldScoreButton;
	private ListView listView;
	private GamesClient mGamesClient;
	private String leaderBoard_Id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.final_screen);
		mCategory = getIntent().getStringExtra(Constants.CATGEORY);
		mPoints = getIntent().getIntExtra(Constants.POINTS, 0);
		mPrefsmanager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		leaderBoard_Id = getIntent().getStringExtra(Constants.LEADERBOARD_ID);
		gameServicesEnabled = mPrefsmanager.getBoolean(Constants.GAME_SERVICES_ENABLED,false);
		worldScoreButton=(Button) findViewById(R.id.world_score);
		
		SharedPreferences preferences = getSharedPreferences(HIGH_SCORES, MODE_PRIVATE);
		int totalScore = preferences.getInt(mCategory, 0);
		if(totalScore < mPoints) {
			Editor edit = preferences.edit();
			edit.putInt(mCategory, mPoints);
			edit.commit();
		}
		
		if(!gameServicesEnabled){
			worldScoreButton.setVisibility(View.GONE);
		}else{
			beginUserInitiatedSignIn();
			mGamesClient=this.getGamesClient();
			Log.d("Google Play Game Services", "mGamesClient="+mGamesClient);
			worldScoreButton.setVisibility(View.VISIBLE);
		}

		
		listView=(ListView) findViewById(android.R.id.list);
		HashMap<String, Integer> highScores = (HashMap<String, Integer>) preferences.getAll();
		if(highScores != null && highScores.size() > 0) {
			listView.setAdapter(new HSAdapter(new WeakReference<Context>(this), highScores));
		} else {
			listView.setVisibility(View.GONE);
		}
		
		TextView title = (TextView)findViewById(R.id.title);
		title.setText(mCategory);

		int size = mPrefsmanager.getInt(Constants.FINAL_SCREEN_FONT_SIZE, 18);
		title.setTextSize(size);
		
		Button score = (Button)findViewById(R.id.score);
		score.setText(getString(R.string.final_score, mPoints > 0 ? mPoints : 0));
		
		findViewById(R.id.home).setOnClickListener(this);
		findViewById(R.id.share).setOnClickListener(this);
		
		moreAppsButton = (Button) findViewById(R.id.more_apps);
		
		adSupportEnabled = mPrefsmanager.getBoolean(Constants.AD_SUPPORT_NEEDED,false);
		adsDisabledAfterPurchase = mPrefsmanager.getBoolean(Constants.ADS_DISABLED_AFTER_PURCHASE,false);
		if(adSupportEnabled && !adsDisabledAfterPurchase){
			String appId = mPrefsmanager.getString(Constants.CHARTBOOST_APPID,"");
			String appSecret = mPrefsmanager.getString(Constants.CHARTBOOST_APPSECRET,"");
			if(appId.trim().equals("") || appSecret.trim().equals("")){
				Toast.makeText(this, getResources().getString(R.string.chartboost_error_msg), 1000).show();
				moreAppsButton.setVisibility(View.GONE);
			}else{
				this.cb = Chartboost.sharedChartboost();
				moreAppsButton.setVisibility(View.VISIBLE);
				this.cb.onCreate(this, appId, appSecret, null);
			}
		}else{
			moreAppsButton.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if(adSupportEnabled && this.cb!=null && !adsDisabledAfterPurchase){
			this.cb.onStart(this);
		    this.cb.startSession();
		    this.cb.showInterstitial();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
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
	
	 public void onMoreButtonClick(View view) {
		 if(adSupportEnabled && this.cb!=null && !adsDisabledAfterPurchase)
			 this.cb.showMoreApps();
	 }
	 
	 public void onLeaderBoardButtonClick(View view) {
		 if(mGamesClient!=null){
			 try{
				//startActivityForResult(mGamesClient.getLeaderboardIntent(leaderBoard_Id), 1);
				 startActivityForResult(mGamesClient.getAllLeaderboardsIntent(), 1);
			 }catch(Exception e){
				 Log.d("Google Play Game Services", "Problem connecting to playservices");
				 e.printStackTrace();
			 }
		 }
		 else{
			 Log.d("Google Play Game Services", "Still not connected");
		 }
	 }

	private static class HSAdapter extends BaseAdapter {

		private WeakReference<Context> wContext;
		private HashMap<String, Integer> mData;
		private LayoutInflater sInflater;

		public HSAdapter(WeakReference<Context> context, HashMap<String, Integer> data) {
			wContext = context;
			mData = data;
			sInflater = (LayoutInflater) wContext.get().getSystemService(LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			return mData != null ? mData.size() : 0;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null) {
				convertView = sInflater.inflate(R.layout.high_score_item, null);
			}
			TextView category = (TextView)convertView.findViewById(R.id.category);
			TextView score = (TextView)convertView.findViewById(R.id.score);
			
			Object[] keys = mData.keySet().toArray();
			category.setText((CharSequence) keys[position]);
			score.setText(mData.get(keys[position]).toString());
			return convertView;
		}
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.home :
			startActivity(new Intent(this, HAStartScreen.class));
			finish();
			break;

		case R.id.share :
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_points, mPoints));
			startActivity(intent);
			break;
		}
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
		if(mPoints<=0)
			mPoints=0;
		mGamesClient.submitScore(leaderBoard_Id,mPoints);
	}
}