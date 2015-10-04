package com.almadev.znaniesila;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.almadev.znaniesila.model.CategoriesList;
import com.almadev.znaniesila.model.Category;
import com.almadev.znaniesila.model.Question;
import com.almadev.znaniesila.model.Quiz;
import com.almadev.znaniesila.model.QuizHolder;
import com.almadev.znaniesila.utils.Constants;
import com.almadev.znaniesila.utils.RunningState;
import com.almadev.znaniesila.utils.RunningState.QuizCategory;
import com.chartboost.sdk.Chartboost;

public class HACategoriesScreen extends ListActivity implements View.OnClickListener {

	private CategoryAdapter mAdapter;
	private SharedPreferences mPrefsmanager;
	private Chartboost cb;
	private Boolean adsDisabledAfterPurchase;
	private Boolean adSupportEnabled;
	private boolean isKnowledgeCats;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		isKnowledgeCats = intent.getBooleanExtra(Constants.CATEGORY_FOR_KNOWLEDGE, false);
		if (isKnowledgeCats) {
			///
		}
		setContentView(R.layout.quiz_categories_layout);
		mPrefsmanager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		List<Category> listItems = new LinkedList<>();
        for (Category c : QuizHolder.getInstance(this).getCategories().getCategories()) {
            if (c.getProductIdentifier() == null || c.getProductIdentifier().isEmpty()) {
                listItems.add(c);
            }
        }
		Collections.sort(listItems,new CategoryComparator());
		mAdapter = new CategoryAdapter(new WeakReference<Context>(this), listItems);
		setListAdapter(mAdapter);
		
		adSupportEnabled = mPrefsmanager.getBoolean(Constants.AD_SUPPORT_NEEDED,false);
		adsDisabledAfterPurchase = mPrefsmanager.getBoolean(Constants.ADS_DISABLED_AFTER_PURCHASE,false);

		findViewById(R.id.home).setOnClickListener(this);
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

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Category category = mAdapter.getItem(position);
        Intent intent;
        if (isKnowledgeCats ) {
            intent = new Intent(this, KnowledgeActivity.class);
            intent.putExtra(Constants.CATEGORY, category);
        } else {
            intent = new Intent(this, HAQuizScreen.class);
            intent.putExtra(Constants.CATEGORY_ID, category.getCategory_id());
        }

		startActivity(intent);
		finish();
	}

	@Override
	public void onClick(final View pView) {
		switch (pView.getId()) {
            case R.id.home:
                finish();
                break;
        }
	}

	private static class CategoryAdapter extends BaseAdapter {

		private List<Category> mData;
		private LayoutInflater sInflater;
		private WeakReference<Context> wContext;

		public CategoryAdapter(WeakReference<Context> context, List<Category> data) {
			wContext = context;
			mData = data;
			sInflater = (LayoutInflater)context.get().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			return mData != null ? mData.size() : 0;
		}

		@Override
		public Category getItem(int position) {
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
			View root = convertiView.findViewById(R.id.category_item_layout);

			TextView name = (TextView) convertiView.findViewById(R.id.name);
			TextView description = (TextView) convertiView.findViewById(R.id.description);
            TextView numberAnswered = (TextView) convertiView.findViewById(R.id.answeredQuestions);
			ImageView image = (ImageView) convertiView.findViewById(R.id.image);
			if(image.getDrawable() != null) {
				image.getDrawable().setCallback(null);
			}
			
			Category qCategory = getItem(position);
			Quiz qQuiz = QuizHolder.getInstance(convertiView.getContext()).getQuiz(qCategory.getCategory_id());
			root.setBackgroundColor(Color.parseColor("#" + qCategory.getCategory_color().trim()));
			name.setText(qCategory.getCategory_name());

			SharedPreferences preferences = convertiView.getContext().getSharedPreferences(HAFinalScreen.HIGH_SCORES, MODE_PRIVATE);
			int recordScore = preferences.getInt(qCategory.getCategory_id(), 0);
			TextView record_value = (TextView) convertiView.findViewById(R.id.record_value);
			record_value.setText("" + recordScore);

            numberAnswered.setText(qQuiz.getAnsweredQuestions() + "/" + qQuiz.getQuestions().size());
			description.setText(qCategory.getCategory_description());
			
			try {
                Drawable d = Drawable.createFromStream(wContext.get().getAssets().open(
                        qCategory.getCategory_image_path()), null);
				image.setImageDrawable(d);

			} catch (Exception e) {
				e.printStackTrace();
			}
			return convertiView;
		}
	}
	
	private class CategoryComparator implements Comparator<Category>{

		@Override
		public int compare(Category lhs, Category rhs) {
			if(Integer.valueOf(lhs.getCategory_id()) > Integer.valueOf(rhs.getCategory_id()))
				return 1;
			else
				return -1;
		}
		
	}
}