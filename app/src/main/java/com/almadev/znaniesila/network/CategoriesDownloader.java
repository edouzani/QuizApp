package com.almadev.znaniesila.network;

import android.util.Log;

import com.almadev.znaniesila.BuildConfig;
import com.almadev.znaniesila.events.NeedUpdateQuizesEvent;
import com.almadev.znaniesila.model.CategoriesList;
import com.almadev.znaniesila.model.Question;
import com.almadev.znaniesila.model.Quiz;
import com.almadev.znaniesila.model.QuizHolder;
import com.almadev.znaniesila.utils.Constants;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import de.greenrobot.event.EventBus;

/**
 * Created by Aleksey on 24.09.2015.
 */
public class CategoriesDownloader {

    interface DownloaderCallback {
    }

    public interface QuizDownloadCallback {
        void downloadFinished(Quiz quiz);
    }

    public interface CategoriesDownloadCallback {
        void downloadFinished(CategoriesList list, int code);
    }

    private CategoriesDownloader() {
    }

    private static OkHttpClient httpClient = new OkHttpClient();

    private static final String QZ_VERSION_HEADER    = "QZ-VERSION";
    private static final String IF_NONE_MATCH_HEADER = "If-None-Match";
    private static final String ETAG_HEADER          = "ETAG";

    public static void downloadCategoriesList(final CategoriesList currentList, final QuizHolder quizHolder, final CategoriesDownloadCallback callback) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                String ver;
                if (currentList == null || currentList.getVersion() == null) {
                    ver = "0";
                } else {
                    ver = currentList.getVersion();
                }
                Request request = new Request.Builder().url(Constants.API_CATEGORIES_LIST)
                                                       .addHeader(IF_NONE_MATCH_HEADER, ver)
                                                       .addHeader("Content-Type", "application/json").build();

                Response response = null;
                try {
                    response = httpClient.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (response == null || (response.code() != 200 && response.code() != 304)) {
                    callback.downloadFinished(null, -1);
                    return;
                }

                if (response.code() == 304) {
                    callback.downloadFinished(null, 304);
                    return;
                }

                CategoriesList list = null;
                try {
                    String jsonStr = response.body().string();
                    list = new Gson().fromJson(jsonStr, CategoriesList.class);
                    list.setVersion(response.header(ETAG_HEADER));
//                    list.setVersion("39");
                    quizHolder.saveCategories(list);
                    EventBus.getDefault().post(new NeedUpdateQuizesEvent(list.getVersion()));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                callback.downloadFinished(list, 200);
                return;
            }
        }).start();
    }

    public static Quiz downloadQuiz(final String quizId) {
        Log.e("QUIZ_DOWNLOADER", "downloading quiz# " + quizId);

        Request request = new Request.Builder().url(Constants.API_CATEGORY + quizId + ".json")
                                               .addHeader(QZ_VERSION_HEADER, QuizHolder.getQuizVersion())
//                                               .addHeader(QZ_VERSION_HEADER, "39")
                                               .addHeader("Content-Type", "application/json").build();

        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (response == null || !response.isSuccessful()) {
            return null;
        }

        Quiz quiz = null;
        try {
            String jsonStr = response.body().string();
//            if (BuildConfig.DEBUG) {
//                Log.i("QUIZ_DOWNLOADER " + quizId, jsonStr);
//            }
            quiz = new Gson().fromJson(jsonStr, Quiz.class);
            quiz.setId(quizId);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return quiz;
    }
}
