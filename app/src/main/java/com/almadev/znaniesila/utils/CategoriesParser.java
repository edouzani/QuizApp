package com.almadev.znaniesila.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.almadev.znaniesila.utils.RunningState.QuizCategory;

public class CategoriesParser {
	
	private static RunningState mState;

	public CategoriesParser(Context context) {
		mState = RunningState.getInstance(context.getApplicationContext());
	}
	
	public void parse(String data) {
		try {
			CategoryKeys.parseCategories(new JSONArray(data));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private enum CategoryKeys {
		defaultkey,
		
		category_id, category_name, category_description,
		category_image_path, category_questions_max_limit,leaderboard_id;
		
		private static void parseCategories(JSONArray categories) {
			for(int i=0; i<categories.length(); i++) {
				try {
					QuizCategory qCategory = mState.new QuizCategory();
					JSONObject categoryObject = categories.getJSONObject(i);
					JSONArray categoryKeys = categoryObject.names();
					for(int j=0; j<categoryKeys.length(); j++) {
						String categoryKey = categoryKeys.getString(j).trim();
						String categoryValue = categoryObject.getString(categoryKey).trim();
						if(!TextUtils.isEmpty(categoryValue) && !TextUtils.isEmpty(categoryKey.trim())) {
							CategoryKeys catgeoryEnum = defaultkey;
							try {
								catgeoryEnum = CategoryKeys.valueOf(categoryKey.trim().toLowerCase());
							} catch(Exception e) {
								e.printStackTrace();
							}
							switch(catgeoryEnum) {
							case category_id :
								qCategory.id = categoryValue;
								break;

							case category_name :
								qCategory.name = categoryValue;
								break;
								
							case category_description :
								qCategory.description = categoryValue;
								break;
								
							case category_image_path :
								qCategory.image = categoryValue;
								break;
								
							case category_questions_max_limit :
								qCategory.limit = Integer.parseInt(categoryValue);
								break;
								
							case leaderboard_id :
								qCategory.leaderBoardId = categoryValue;
								break;
							}
						}
					}
					if(!TextUtils.isEmpty(qCategory.id)) {
						mState.addQuizCategory(qCategory);
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}		

}