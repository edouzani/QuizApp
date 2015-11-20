package com.almadev.znaniesila.model;

import android.content.Context;
import android.util.Log;

import com.almadev.znaniesila.BuildConfig;
import com.almadev.znaniesila.ZSApp;
import com.almadev.znaniesila.questions.QuestionsAdapter;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Aleksey on 25.09.2015.
 */
public class QuizHolder {
    private static final String QUESTIONS_DIR        = "questions";
    private static final String CATEGORIES_LIST_FILE = "list.qz";

    private File questionsDir = null;

    private volatile static QuizHolder instance = null;

    private        CategoriesList        mCategoriesList = null;
    private        HashMap<String, Quiz> quizes          = new HashMap<>();
    private static String                quizVersion     = "0";

    private QuizHolder(Context context) {
//        questionsDir = new File(context.getExternalFilesDir(null).getAbsolutePath() + File.separatorChar + QUESTIONS_DIR);
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
        for (int i = 0; i < quiz.getQuestions().size(); i++) {
            quiz.getQuestions().get(i).setLocal_id(i);
        }

        if (!questionsDir.exists()) {
            questionsDir.mkdirs();
        }

        Quiz local = getQuiz(quiz.getId());
        if (local != null) {
            if (ZSApp.DEBUG_ENV) {
                Log.i("QuizHolder", "merging quiz#" + quiz.getId());
            }
            quiz = mergeQuizes(local, quiz);
        }

        try {
            quizFile.createNewFile();

            OutputStream file = new FileOutputStream(quizFile);
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);

            output.writeObject(quiz);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        quizes.put(quiz.getId(), quiz);
    }

    private Quiz mergeQuizes(Quiz local, Quiz remote) {
        for (Question mQuestion : local.getQuestions()) {
            Question remoteQuestion = remote.getById(mQuestion.getStory_order_id());
            if (remoteQuestion != null) {
                remoteQuestion.setState(mQuestion.getState());
                remoteQuestion.setIsStoryViewed(mQuestion.isStoryViewed());
            }
        }
        return remote;
    }


    public void clear() {
        deleteAllQuizes();
        final File categoriesListFile = new File(questionsDir.getAbsolutePath(), CATEGORIES_LIST_FILE);
        if (categoriesListFile.exists()) {
            categoriesListFile.delete();
        }
        mCategoriesList = null;
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

        Quiz quiz = null;
        try {
            InputStream file = new FileInputStream(quizFile);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);

            quiz = (Quiz) input.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        quizes.put(id, quiz);

        if (quiz == null) {
            quiz = new Quiz();
        }
        return quiz;
    }

    public void saveCategories(CategoriesList list) {
        final File categoriesListFile = new File(questionsDir.getAbsolutePath(), CATEGORIES_LIST_FILE);
        final Gson gson = new Gson();

        if (!questionsDir.exists()) {
            questionsDir.mkdirs();
        }
        try {
            categoriesListFile.createNewFile();

            OutputStream file = new FileOutputStream(categoriesListFile);
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);

            output.writeObject(list);

            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("QuizHolder", "Something wrong on saveCats");
        }

        mCategoriesList = list;
        setQuizVersion(list.getVersion());
    }

    public List<Category> getPassedCategories() {
        List<Category> res = new LinkedList<>();
        Quiz qQuiz;
        for (Category c : getCategories().getCategories()) {
            qQuiz = getQuiz(c.getCategory_id());
            if (qQuiz == null) {
                return new LinkedList<>();
            }
            if (qQuiz.getAnsweredQuestions() == qQuiz.getQuestions().size()) {
                res.add(c);
            }
        }
        return res;
    }

    public List<Category> getPurchasableCategories() {
        List<Category> res = new LinkedList<>();
        for (Category c : getCategories().getCategories()) {
            if (c.getPrice() != null && !c.getPrice().isEmpty() && !c.isPurchased()) {
                res.add(c);
            }
        }
        return res;
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

        CategoriesList list = null;
        try {
            InputStream file = new FileInputStream(categoriesListFile);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);

            list = (CategoriesList) input.readObject();
            if (list != null) {
                setQuizVersion(list.getVersion());
            } else {
                Log.e("QuizHolder", "deserealization failed");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        mCategoriesList = list;
        return list;
    }
}
