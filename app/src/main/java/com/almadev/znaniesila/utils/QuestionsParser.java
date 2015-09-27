package com.almadev.znaniesila.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.almadev.znaniesila.utils.RunningState.Quiz;

public class QuestionsParser {
	
	private static RunningState mState;
	private static SharedPreferences mPrefsManager;

	public QuestionsParser(Context context) {
		mState = RunningState.getInstance(context.getApplicationContext());
		mPrefsManager = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
	}
	
	public void parse(String data, String categoryId) {
		try {
			QuestionKeys.parseQuestions(new JSONArray(data), categoryId);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private enum QuestionKeys {
		
		defaultkey,
		
		question, question_type, points, negative_points,
		duration_in_seconds, answer, picture_or_video_name, options;
		
		private static void parseQuestions(JSONArray questions, String categoryId) {
			ArrayList<Quiz> questionsList = new ArrayList<Quiz>();
			for(int i=0; i<questions.length(); i++) {
				try {
					Quiz question = mState.new Quiz();
					JSONObject questionObject = questions.getJSONObject(i);
					JSONArray questionKeys = questionObject.names();
					for(int j=0; j<questionKeys.length(); j++) {
						String questionKey = questionKeys.getString(j).trim();
						String questionValue = questionObject.getString(questionKey).trim();
						if(!TextUtils.isEmpty(questionValue) && !TextUtils.isEmpty(questionKey.trim())) {
							QuestionKeys questionEnum = defaultkey;
							try {
								questionEnum = QuestionKeys.valueOf(questionKey.trim().toLowerCase());
							} catch(Exception e) {
								e.printStackTrace();
							}
							switch(questionEnum) {
							case question :
								question.question = questionValue;
								break;

							case question_type :
								question.questionType = Integer.parseInt(questionValue);
								break;
								
							case points :
								question.points = Integer.parseInt(questionValue);
								break;
								
							case negative_points :
								question.negativePoints = Integer.parseInt(questionValue);
								break;
								
							case duration_in_seconds :
								question.duration = Integer.parseInt(questionValue);
								break;
								
							case answer :
								question.answer = Integer.parseInt(questionValue);
								break;
								
							case picture_or_video_name :
								question.mediaName = questionValue;
								break;

							case options :
								JSONArray temp = new JSONArray(questionValue);
								for(int k=0; k<temp.length(); k++) {
									question.options.add(temp.getString(k));
								}
								break;
							}
						}
					}
					boolean shuffleO = mPrefsManager.getBoolean(Constants.SHUFFLE_OPTIONS, false);
					if(shuffleO && question.questionType != 4) {
						String temp = question.options.get(question.answer);
						Collections.shuffle(question.options, new Random(System.currentTimeMillis()));
						for(int k=0; k<question.options.size(); k++) {
							if(temp.equals(question.options.get(k))) {
								question.answer = k;
								break;
							}
						}
					}
					questionsList.add(question);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			boolean shuffleQ = mPrefsManager.getBoolean(Constants.SHUFFLE_QUESTIONS, false);
			if(shuffleQ) {
				Collections.shuffle(questionsList, new Random(System.currentTimeMillis()));
			}
			mState.addQuizQuestions(categoryId, questionsList);
		}
	}
}