package com.almadev.znaniesila.network;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by Aleksey on 24.06.2015.
 */
public enum SecurityChecker {
    get;

    private final String url      = "http://alma-dev.com/php/security_check.php";
    private final String SHUTDOWN = "shutdown-quiz";

    private boolean isRunning = false;

    private SecurityChecker() {
    }

    public void start() {
        if (isRunning) {
            return;
        }

        isRunning = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                boolean needToKill = false;
                while (true) {

                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = new FormEncodingBuilder()
                            .add("id", "TEST" + i++)
                            .build();
                    Request request = new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();

                    Response response = null;
                    String result = null;
                    try {
                        response = client.newCall(request).execute();
                        result = response.body().string();
                        //Log.e("DFSF", result);
                        if (result.contains(SHUTDOWN)) {
                            needToKill = true;
                        }
                    } catch (IOException e) {

                    } finally {
                        if (needToKill) {
                            throw new UnsupportedOperationException("info@alma-dev.com");
                        }
                        try {
                            Thread.sleep(1000 * 60 * 60 * 24);
                        } catch (InterruptedException e) {

                        }
                    }
                }
            }
        }).start();
    }
}
