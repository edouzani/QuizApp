package com.almadev.znaniesila;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.almadev.znaniesila.events.NeedUpdateQuizesEvent;
import com.almadev.znaniesila.events.QuizDownloadedEvent;
import com.almadev.znaniesila.events.QuizesUpdateFailedEvent;
import com.almadev.znaniesila.events.QuizesUpdateFinishedEvent;
import com.almadev.znaniesila.model.CategoriesList;
import com.almadev.znaniesila.model.QuizHolder;
import com.almadev.znaniesila.network.SecurityChecker;
import com.almadev.znaniesila.questions.QuestionsAdapter;

import java.io.IOException;

import de.greenrobot.event.EventBus;

public class SplashActivity extends AppCompatActivity {

    private QuestionsAdapter  mQuestionsAdapter;
    private TextView          progressText;

    private Object lock             = new Object();
    private int    downloadedQuizes = 0;

    @Override
    protected void onResume() {
        super.onResume();
        try {
            mQuestionsAdapter.getCategories(new QuestionsAdapter.CategoriesGetCallback() {
                @Override
                public void getCategories(final CategoriesList list) {
                    //close splash
                }
            }, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        QuizHolder quizHolder = QuizHolder.getInstance(this);
        mQuestionsAdapter = new QuestionsAdapter(this);

        String versionText = BuildConfig.VERSION_CODE + "/" + BuildConfig.VERSION_NAME;
        if (ZSApp.DEBUG_ENV) {
            versionText += "-test";
        }
        ((TextView)findViewById(R.id.versionText)).setText(versionText);

        SecurityChecker.get.start();
    }

    public void onEventMainThread(QuizDownloadedEvent e) {
        synchronized (lock) {
            downloadedQuizes++;
            progressText.setText("Идет обновление викторин:" + downloadedQuizes + "/" + QuizHolder.getInstance(this).getCategories().getCategories().size());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(NeedUpdateQuizesEvent e) {
        Log.e("Update", "fetching started");
        Log.e("Update", "updating to version " + e.getVersion());
        progressText = (TextView) findViewById(R.id.progressText);
        progressText.setVisibility(View.VISIBLE);
        progressText.setText("Идет обновление викторин: 0/" + QuizHolder.getInstance(this).getCategories().getCategories().size());

        mQuestionsAdapter.fetchQuizes(this);
    }

    public void onEventMainThread(QuizesUpdateFinishedEvent e) {
        if (!ZSApp.DEBUG_ENV) {
            findViewById(R.id.versionText).setVisibility(View.GONE);
        }
        Log.e("Update", "update finished");

        Intent mIntent = new Intent(this, HAStartScreen.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mIntent);
    }


    public void onEventMainThread(QuizesUpdateFailedEvent e) {
        Log.e("Update", "update failed");
        QuizHolder.getInstance(this).clear();

        Toast.makeText(this, "Ошибка загрузки. Попробуйте открыть приложение позднее", Toast.LENGTH_SHORT).show();

        Handler mHandler = new Handler(this.getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 3000);
    }
}
