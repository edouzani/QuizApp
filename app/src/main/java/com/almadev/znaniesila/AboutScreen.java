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
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class AboutScreen extends Activity{
	
	private TextView aboutText;
	private SharedPreferences mPrefsmanager;
    private WebView webView;
    private ProgressBar mProgressBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_layout);

        findViewById(R.id.home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View pView) {
                finish();
            }
        });
        mProgressBar = (ProgressBar) findViewById(R.id.about_progress);

        webView = (WebView) findViewById(R.id.webView);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT <= 16) {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
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
                    } else {

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
	}

    private void callback(final String html) {
        this.runOnUiThread( new Runnable() {
            @Override
            public void run() {
                if (html == null) {
                    mProgressBar.setVisibility(View.GONE);
                    Toast.makeText(AboutScreen.this, "Ошибка соединения с сетью, попробуйте позднее", Toast.LENGTH_SHORT).show();
                    return ;
                }
                int imgIdxStart = html.indexOf("http://s3.scena.tv");
                int imgIdxEnd = html.indexOf(".JPG") + 4;
                String htmlWithImg;
//                if (imgIdxStart != -1 && imgIdxEnd != -1) {
                    htmlWithImg = html.substring(0, imgIdxStart) + "<img src=\"" + html.substring(imgIdxStart, imgIdxEnd) +
                            "\" width=\"220\" height=\"220\"/>" +
                            html.substring(imgIdxEnd);
//                } else {
//                    htmlWithImg = html;
//                }
                String text = "<html><head>"
                        + "<style type=\"text/css\">body{color: #fff;}"
                        + "</style></head>"
                        + "<body>"
                        + htmlWithImg
                        + "</body></html>";
                webView.loadData(text, "text/html; charset=UTF-8", null);
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }
}
