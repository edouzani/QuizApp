package com.almadev.znaniesila;

import com.almadev.znaniesila.R;
import com.almadev.znaniesila.model.Page;
import com.almadev.znaniesila.utils.Constants;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.IOException;

public class AboutScreen extends Activity{
	
	private TextView aboutText;
	private SharedPreferences mPrefsmanager;
    private WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_layout);

        webView = (WebView) findViewById(R.id.webView);
        webView.setBackgroundColor(Color.TRANSPARENT);
//        webView.set
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient httpClient = new OkHttpClient();
                Request mRequest = new Request.Builder().url(Constants.API_PAGE_ABOUT).build();
                Response response = null;

                try {
                    response = httpClient.newCall(mRequest).execute();

                    if (response != null && response.isSuccessful()) {
                        String jsonStr = response.body().string();
                        Page page = new Gson().fromJson(jsonStr, Page.class);
                        callback(page.getHtml());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
	}

    private void callback(final String html) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.loadData(html, "text/html", "UTF-8");
            }
        });
    }
}
