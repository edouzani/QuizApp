package com.almadev.znaniesila.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;

public class RunningState {
	
	private static RunningState mState;

	public static RunningState getInstance(Context context) {
		if(mState == null) {
			mState = new RunningState(context.getApplicationContext());
		}
		return mState;
	}
	
	private RunningState(Context context) {
		
	}
	
	private ConcurrentHashMap<String, QuizCategory> mCategories = new ConcurrentHashMap<String, RunningState.QuizCategory>();
	private ConcurrentHashMap<String, ArrayList<Quiz>> mQuestions = new ConcurrentHashMap<String, ArrayList<Quiz>>();
	
	public class QuizCategory {
		public String id;
		public String name;
		public String image;
		public String description;
		public int limit;
		public String leaderBoardId;
	}
	
	public class Quiz {
		public String question;
		public int questionType;
		public int points;
		public int negativePoints;
		public int duration;
		public int answer;
		public String mediaName;
		public ArrayList<String> options = new ArrayList<String>();
	}
	
	public void addQuizCategory(QuizCategory category) {
		mCategories.put(category.id, category);
	}
	
	public ConcurrentHashMap<String, QuizCategory> getQuizCategories() {
		return mCategories;
	}
	
	public QuizCategory getQuizCategory(String categoryId) {
		return mCategories.get(categoryId);
	}
	
	public void addQuizQuestions(String categoryId, ArrayList<Quiz> questions) {
		mQuestions.put(categoryId, questions);
	}
	
	public ArrayList<Quiz> getQuestions(String categoryId) {
		return mQuestions.get(categoryId);
	}
	
	public static String readRawTextFile(Context ctx, int resId) {
		InputStream inputStream = ctx.getResources().openRawResource(resId);

		InputStreamReader inputreader = new InputStreamReader(inputStream);
		BufferedReader buffreader = new BufferedReader(inputreader);
		String line;
		StringBuilder text = new StringBuilder();

		try {
			while (( line = buffreader.readLine()) != null) {
				text.append(line);
				text.append('\n');
			}
		} catch (IOException e) {
			return null;
		}
		return text.toString();
	}
	
	public static String readAssetFile(Context ctx, InputStream fd) {
		StringBuilder text = new StringBuilder();
		try {
			InputStreamReader inputreader = new InputStreamReader(fd);
			BufferedReader buffreader = new BufferedReader(inputreader);
			String line;

			while (( line = buffreader.readLine()) != null) {
				text.append(line);
				text.append('\n');
			}
		} catch (IOException e) {
			return null;
		}
		return text.toString();
	}
}