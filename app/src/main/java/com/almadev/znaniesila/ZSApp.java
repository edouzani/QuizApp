package com.almadev.znaniesila;

import android.app.Application;

import com.google.ads.conversiontracking.InstallReceiver;
import com.yandex.metrica.YandexMetrica;

/**
 * Created by Aleksey on 11.11.2015.
 */
public class ZSApp extends Application {
    private static final String API_KEY = "6f03830c-8106-4325-abf6-bb75d347d387";//"2c8dd60e-e769-4708-ab48-6e68b733ebd6";

    @Override
    public void onCreate() {
        super.onCreate();

        if (API_KEY != null) {
            YandexMetrica.activate(getApplicationContext(), API_KEY);
            // Отслеживание активности пользователей
            if (BuildConfig.DEBUG) {
                YandexMetrica.setLogEnabled();
            }

            YandexMetrica.enableActivityAutoTracking(this);
            YandexMetrica.setReportCrashesEnabled(false);
            YandexMetrica.setReportNativeCrashesEnabled(false);
            YandexMetrica.setTrackLocationEnabled(false);

            InstallReceiver mInstallReceiver = new InstallReceiver();
            YandexMetrica.registerReferrerBroadcastReceivers(mInstallReceiver);
        }
    }
}
