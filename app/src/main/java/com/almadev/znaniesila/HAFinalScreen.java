package com.almadev.znaniesila;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.almadev.znaniesila.model.Category;
import com.almadev.znaniesila.utils.Constants;
import com.google.android.gms.games.GamesClient;

public class HAFinalScreen extends BaseGameActivity implements OnClickListener {

    private Category          mCategory;
    private int               mPoints;
    private SharedPreferences mPrefsmanager;
    public static final String HIGH_SCORES = "high_scores";
    private Boolean     adSupportEnabled;
    private Boolean     adsDisabledAfterPurchase;
    private Boolean     gameServicesEnabled;
    private View        rating;
    private GamesClient mGamesClient;
    private String      leaderBoard_Id;
    private TextView    record_text;
    private int         mMaxPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.final_screen);
        mCategory = (Category) getIntent().getSerializableExtra(Constants.CATEGORY);
        mPoints = getIntent().getIntExtra(Constants.POINTS, 0);
        mMaxPoints = getIntent().getIntExtra(Constants.MAX_POINTS, 0);
        mPrefsmanager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        leaderBoard_Id = getIntent().getStringExtra(Constants.LEADERBOARD_ID);
        gameServicesEnabled = mPrefsmanager.getBoolean(Constants.GAME_SERVICES_ENABLED, false);
        rating = findViewById(R.id.leaderboard);

        SharedPreferences preferences = getSharedPreferences(HIGH_SCORES, MODE_PRIVATE);
        int totalScore = preferences.getInt(mCategory.getCategory_id(), 0);
        if (totalScore < mPoints) {
            Editor edit = preferences.edit();
            edit.putInt(mCategory.getCategory_id(), mPoints);
            edit.commit();
        }

        if (!gameServicesEnabled) {
//			rating.setVisibility(View.GONE);
        } else {
            beginUserInitiatedSignIn();
            mGamesClient = this.getGamesClient();
            Log.d("GplayServices", "mGamesClient=" + mGamesClient);
            rating.setVisibility(View.VISIBLE);
        }


        record_text = (TextView) findViewById(R.id.record);
        HashMap<String, Integer> highScores = (HashMap<String, Integer>) preferences.getAll();
        int record = 0;
//		if (highScores != null && highScores.size() > 0) {
//			for (Integer score : highScores.values()) {
//                if (score > max) {
//                    max = score;
//                }
//            }
//		}
        record = highScores.get(mCategory.getCategory_id());
        record_text.setText(record + " БАЛЛОВ");

        TextView title = (TextView) findViewById(R.id.catname);
        title.setText(mCategory.getCategory_name());

        TextView result = (TextView) findViewById(R.id.points);
        result.setText("" + mPoints);

        TextView maxPoints = (TextView) findViewById(R.id.total_points);
        maxPoints.setText(String.format(getResources().getString(R.string.points_record), mMaxPoints));

        int size = mPrefsmanager.getInt(Constants.FINAL_SCREEN_FONT_SIZE, 18);
        title.setTextSize(size);

        findViewById(R.id.home).setOnClickListener(this);
        findViewById(R.id.share_fb).setOnClickListener(this);
        findViewById(R.id.share_ok).setOnClickListener(this);
        findViewById(R.id.share_vk).setOnClickListener(this);

        adSupportEnabled = mPrefsmanager.getBoolean(Constants.AD_SUPPORT_NEEDED, false);
        adsDisabledAfterPurchase = mPrefsmanager.getBoolean(Constants.ADS_DISABLED_AFTER_PURCHASE, false);

        findViewById(R.id.restart).setOnClickListener(this);
        findViewById(R.id.leaderboard).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
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
    }

    public void onLeaderBoardButtonClick(View view) {
        if (mGamesClient != null) {
            try {
                startActivityForResult(mGamesClient.getLeaderboardIntent(leaderBoard_Id), 1);
//                startActivityForResult(mGamesClient.getAllLeaderboardsIntent(), 1);
            } catch (Exception e) {
                Log.d("GplayServices", "Problem connecting to playservices");
                e.printStackTrace();
            }
        } else {
            Log.d("GplayServices", "Still not connected");
        }
    }

    private static class HSAdapter extends BaseAdapter {

        private WeakReference<Context>   wContext;
        private HashMap<String, Integer> mData;
        private LayoutInflater           sInflater;

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
            if (convertView == null) {
                convertView = sInflater.inflate(R.layout.high_score_item, null);
            }
            TextView category = (TextView) convertView.findViewById(R.id.category);
            TextView score = (TextView) convertView.findViewById(R.id.score);

            Object[] keys = mData.keySet().toArray();
            category.setText((CharSequence) keys[position]);
            score.setText(mData.get(keys[position]).toString());
            return convertView;
        }
    }

    @Override
    public void onClick(View v) {
        String comments = "У меня " + mPoints + " баллов из " + mMaxPoints + " в категории \"" +
                mCategory.getCategory_name() + "\". А сколько сможешь набрать ты?";
        switch (v.getId()) {
            case R.id.home:
                startActivity(new Intent(this, HAStartScreen.class));
                finish();
                break;

            case R.id.share_fb:
                Intent shareIntentFb = new Intent(Intent.ACTION_VIEW,
                                                  Uri.parse("https://www.facebook.com/sharer/sharer.php?u=http://www.znanie.tv"));
                startActivity(shareIntentFb);
                break;
            case R.id.share_vk:
                Intent shareIntentVk = new Intent(Intent.ACTION_VIEW,
                                                Uri.parse("http://vk.com/share.php?url=http://www.znanie.tv/&title=Знание-сила!" +
                                                                  "&description=" + comments + "&image=http://www.znanie.tv/zshare.jpg&noparse=true"));
                startActivity(shareIntentVk);
                break;
            case R.id.share_ok:
                Intent shareIntentOk = new Intent(Intent.ACTION_VIEW,
                                                Uri.parse("http://www.odnoklassniki.ru/dk?st.cmd=addShare&st.s=1&"
                                                                  + "st.comments=" + comments + "&st._surl=http://www.znanie.tv/"));
                startActivity(shareIntentOk);
                break;
            case R.id.restart:
                Intent restartIntent = new Intent(this, HAQuizScreen.class);
                restartIntent.putExtra(Constants.CATEGORY_ID, mCategory.getCategory_id());
                startActivity(restartIntent);
                break;
            case R.id.leaderboard:
                onLeaderBoardButtonClick(v);
                break;
        }
    }

    @Override
    public void onSignInFailed() {
        // TODO Auto-generated method stub
        Log.d("GplayServices", "Sign In Failed");

    }

    @Override
    public void onSignInSucceeded() {
        // TODO Auto-generated method stub
        Log.d("GplayServices", "Sign In Successful. Submitting scores");
        if (mPoints <= 0)
            mPoints = 0;
        mGamesClient.submitScore(leaderBoard_Id, mPoints);
    }
}