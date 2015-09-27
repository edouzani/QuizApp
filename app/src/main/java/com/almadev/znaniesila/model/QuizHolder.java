package com.almadev.znaniesila.model;

import android.content.Context;

import com.almadev.znaniesila.questions.QuestionsAdapter;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

/**
 * Created by Aleksey on 25.09.2015.
 */
public class QuizHolder {
    private static final String QUESTIONS_DIR        = "questions";
    private static final String CATEGORIES_LIST_FILE = "list.qz";

    private File questionsDir = null;

    private volatile static QuizHolder instance = null;

    private CategoriesList        mCategoriesList = null;
    private HashMap<String, Quiz> quizes          = new HashMap<>();
    private static String quizVersion = "0";

    private QuizHolder(Context context) {
        questionsDir = new File(context.getFilesDir().getAbsolutePath() + File.separatorChar + QUESTIONS_DIR);
    }

    public static String getQuizVersion() {
        return quizVersion;
    }

    public static void setQuizVersion(String version) {
        quizVersion = version;
    }

    public static QuizHolder getInstance(Context context) {
        QuizHolder result = instance;
        if (result == null) {
            synchronized (QuizHolder.class) {
                result = instance;
                if (result == null) {
                    instance = result = new QuizHolder(context);
                }
            }
        }
        return result;
    }

    public void saveQuiz(Quiz quiz) {
        final File quizFile = new File(questionsDir.getAbsolutePath(), quiz.getId() + ".qz");
        final Gson gson = new Gson();

        if (!questionsDir.exists()) {
            questionsDir.mkdirs();
        }
        try {
            quizFile.createNewFile();
            final Writer writer = new FileWriter(quizFile);
            gson.toJson(quiz, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        quizes.put(quiz.getId(), quiz);
    }

    public void deleteAllQuizes() {
        File quizFile = null;
        for (String id : quizes.keySet()) {
            quizFile = new File(questionsDir.getAbsolutePath(), id + ".qz");
            if (quizFile.exists()) {
                quizFile.delete();
            }
        }

        quizes.clear();
    }

    public Quiz getQuiz(String id) {
        if (quizes != null && quizes.containsKey(id)) {
            return quizes.get(id);
        }

        final File quizFile = new File(questionsDir.getAbsolutePath(), id + ".qz");
        if (!quizFile.exists()) {
            return null;
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(quizFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        final Gson gson = new Gson();
        return gson.fromJson(br, Quiz.class);
    }

    public void saveCategories(CategoriesList list) {
        final File categoriesListFile = new File(questionsDir.getAbsolutePath(), CATEGORIES_LIST_FILE);
        final Gson gson = new Gson();

        if (!questionsDir.exists()) {
            questionsDir.mkdirs();
        }
        try {
            categoriesListFile.createNewFile();
            final Writer writer = new FileWriter(categoriesListFile);
            gson.toJson(list, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCategoriesList = list;
        setQuizVersion(list.getVersion());
    }

    public CategoriesList getCategories() {
        if (mCategoriesList != null) {
            setQuizVersion(mCategoriesList.getVersion());
            return mCategoriesList;
        }

        final File categoriesListFile = new File(questionsDir.getAbsolutePath(), CATEGORIES_LIST_FILE);
        if (!categoriesListFile.exists()) {
            return null;
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(categoriesListFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        final Gson gson = new Gson();
        CategoriesList list = gson.fromJson(br, CategoriesList.class);
        setQuizVersion(list.getVersion());
        return list;
    }
}
