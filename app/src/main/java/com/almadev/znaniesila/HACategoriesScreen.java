package com.almadev.znaniesila;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.almadev.znaniesila.R;
import com.almadev.znaniesila.model.Category;
import com.almadev.znaniesila.model.Question;
import com.almadev.znaniesila.model.Quiz;
import com.almadev.znaniesila.model.QuizHolder;
import com.almadev.znaniesila.utils.CategoriesParser;
import com.almadev.znaniesila.utils.Constants;
import com.almadev.znaniesila.utils.RunningState;
import com.almadev.znaniesila.utils.RunningState.QuizCategory;
import com.chartboost.sdk.Chartboost;

public class HACategoriesScreen extends ListActivity {
	
	private RunningState mState;
	private CategoryAdapter mAdapter;
	private TextView mTitle;
	private SharedPreferences mPrefsmanager;
	private Chartboost cb;
	private Boolean adsDisabledAfterPurchase;
	private Boolean adSupportEnabled;
	private Button moreAppsButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.quiz_categories_layout);
		mTitle = (TextView)findViewById(R.id.title);
		mPrefsmanager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String title = mPrefsmanager.getString(Constants.CATEGORY_TITLE, getString(R.string.categories));
		mTitle.setText(title);

		int size = mPrefsmanager.getInt(Constants.CATEGORY_TITLE_FONT_SIZE, 18);
		mTitle.setTextSize(size);
		
		mState = RunningState.getInstance(getApplicationContext());
		readCategories();
		ArrayList<QuizCategory> listItems = new ArrayList<QuizCategory>(mState.getQuizCategories().values());
		Collections.sort(listItems,new CategoryComparator());
		mAdapter = new CategoryAdapter(new WeakReference<Context>(this), listItems);
		setListAdapter(mAdapter);
		moreAppsButton = (Button) findViewById(R.id.more_apps);
		
		adSupportEnabled = mPrefsmanager.getBoolean(Constants.AD_SUPPORT_NEEDED,false);
		adsDisabledAfterPurchase = mPrefsmanager.getBoolean(Constants.ADS_DISABLED_AFTER_PURCHASE,false);
		if(adSupportEnabled && !adsDisabledAfterPurchase){
			/*String appId = "52f08e889ddc3557343c2e93";
			String appSignature = "65e28932944416d0f1e0a6652185362e3205bec5";*/
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
			this.cb.onDestroy(this);
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
	
	private void readCategories() {

		mState = RunningState.getInstance(this);
		for (Category c : QuizHolder.getInstance(this).getCategories().getCategories()) {
			QuizCategory qc = mState.new QuizCategory();
			qc.description = c.getCategory_description();
			qc.id = "" + c.getCategory_id();
			qc.image = c.getCategory_image_path();
			qc.leaderBoardId = c.getLeaderboard_id();
			qc.limit = c.getCategory_question_max_limit();
			qc.name = c.getCategory_name();
			mState.addQuizCategory(qc);

            ArrayList<RunningState.Quiz> questionsList = new ArrayList<RunningState.Quiz>();
            for (Question q : QuizHolder.getInstance(this).getQuiz(qc.id).getQuestions()) {
                RunningState.Quiz question = mState.new Quiz();
                question.answer = q.getAnswer();
                question.duration = q.getDuration_in_seconds();
                question.mediaName = null;
                question.negativePoints = q.getNegative_points();
                question.points = q.getPoints();
                question.question = q.getQuestion();
                question.questionType = q.getQuestion_type();

                questionsList.add(question);
            }

            Collections.shuffle(questionsList, new Random(System.currentTimeMillis()));

            mState.addQuizQuestions(qc.id, questionsList);
		}

//		try {
//			InputStream assetFileDescriptor = getResources().getAssets().open(
//							"Quiz Data/JSON_Format/Quiz_Categories.json");
//			String data = RunningState.readAssetFile(getApplicationContext(), assetFileDescriptor);
//			CategoriesParser parser = new CategoriesParser(getApplicationContext());
//			parser.parse(data);
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		QuizCategory category = mAdapter.getItem(position);
		Intent intent = new Intent(this, HAQuizScreen.class);
		intent.putExtra(Constants.CATGEORY_ID, category.id);
		startActivity(intent);
		finish();
	}
	
	private static class CategoryAdapter extends BaseAdapter {

		private ArrayList<QuizCategory> mData;
		private LayoutInflater sInflater;
		private WeakReference<Context> wContext;

		public CategoryAdapter(WeakReference<Context> context, ArrayList<QuizCategory> data) {
			wContext = context;
			mData = data;
			sInflater = (LayoutInflater)context.get().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			return mData != null ? mData.size() : 0;
		}

		@Override
		public QuizCategory getItem(int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertiView, ViewGroup parent) {
			if(convertiView == null) {
				convertiView = sInflater.inflate(R.layout.quiz_category_item, null);
			}
			TextView name = (TextView) convertiView.findViewById(R.id.name);
			TextView description = (TextView) convertiView.findViewById(R.id.description);
			ImageView image = (ImageView) convertiView.findViewById(R.id.image);
			if(image.getDrawable() != null) {
				image.getDrawable().setCallback(null);
			}
			
			QuizCategory qCategory = getItem(position);

			name.setText(qCategory.name);
			description.setText(qCategory.description);
			
			try {
				image.setImageDrawable(Drawable.createFromStream(wContext.get().getAssets().open(
									"Quiz Data/Pictures_Or_Videos/Quiz_Category_" +
									qCategory.id + "/" + qCategory.image), null));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return convertiView;
		}
	}
	
	private class CategoryComparator implements Comparator<QuizCategory>{

		@Override
		public int compare(QuizCategory lhs, QuizCategory rhs) {
			if(Integer.valueOf(lhs.id)>Integer.valueOf(rhs.id))
				return 1;
			else
				return -1;
		}
		
	}
}