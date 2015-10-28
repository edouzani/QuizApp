package com.almadev.znaniesila.questions;

import android.content.Context;
import android.util.Log;

import com.almadev.znaniesila.events.QuizDownloadedEvent;
import com.almadev.znaniesila.events.QuizesUpdateFailedEvent;
import com.almadev.znaniesila.events.QuizesUpdateFinishedEvent;
import com.almadev.znaniesila.model.CategoriesList;
import com.almadev.znaniesila.model.Category;
import com.almadev.znaniesila.model.Quiz;
import com.almadev.znaniesila.model.QuizHolder;
import com.almadev.znaniesila.network.CategoriesDownloader;

import java.io.IOException;

import de.greenrobot.event.EventBus;

/**
 * Created by Aleksey on 24.09.2015.
 */
public class QuestionsAdapter {
    private final Context mContext;

    public interface QuizGetCallback {
        void callback(Quiz quiz);
    }

    public interface CategoriesGetCallback {
        void getCategories(CategoriesList list);
    }

    public QuestionsAdapter(Context context) {
        mContext = context;
    }

    public void getCategories(final CategoriesGetCallback callback, boolean checkUpdate) throws IOException {
        CategoriesList list = QuizHolder.getInstance(mContext).getCategories();
        if (list != null && !checkUpdate) {
            if (callback != null) {
                callback.getCategories(list);
            }
        } else {
            CategoriesDownloader.downloadCategoriesList(list, QuizHolder.getInstance(mContext), new CategoriesDownloader.CategoriesDownloadCallback() {
                @Override
                public void downloadFinished(final CategoriesList list, int code) {
                    if (list == null && code != 304) {
                        Log.e("QUIZ_DOWNLOADER", "download failed");
                        EventBus.getDefault().postSticky(new QuizesUpdateFinishedEvent());
                        return;
                    }
                    if (code == 304) {
                        Log.e("QUIZ_DOWNLOADER", "bases are up-to-date");
                        EventBus.getDefault().postSticky(new QuizesUpdateFinishedEvent());
                    }
                    if (callback != null) {
                        callback.getCategories(list);
                    }
                }
            });
        }
    }

    public void fetchQuizes(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                CategoriesList list = QuizHolder.getInstance(context).getCategories();
                try {
                    for (Category c : list.getCategories()) {
                        if (getQuiz("" + c.getCategory_id(), true) == null) {
                            EventBus.getDefault().post(new QuizesUpdateFailedEvent());
                            return;
                        }
                        EventBus.getDefault().post(new QuizDownloadedEvent("" + c.getCategory_id()));
                    }

                    EventBus.getDefault().post(new QuizesUpdateFinishedEvent());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void getQuiz(final String id, final boolean forceDownload, final QuizGetCallback callback) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    callback.callback(getQuiz(id, forceDownload));
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.callback(null);
                }
            }
        }).start();
    }

    public Quiz getQuiz(final String id, final boolean forceDownload) throws IOException {
        Quiz quiz = null;
        if (!forceDownload) {
            quiz = QuizHolder.getInstance(mContext).getQuiz(id);
        }

        if (quiz != null) {
            return quiz;
        } else {
            quiz = CategoriesDownloader.downloadQuiz(id);
            if (quiz == null) {
                Log.e("QUIZ_DOWNLOADER", "download failed");
                return null;
            }
            QuizHolder.getInstance(mContext).saveQuiz(quiz);
            return quiz;
        }
    }


}
