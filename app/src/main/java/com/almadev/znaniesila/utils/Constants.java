package com.almadev.znaniesila.utils;

import com.almadev.znaniesila.BuildConfig;
import com.almadev.znaniesila.ZSApp;

public class Constants {
    public static final String HOST      = "http://app.znanie.tv/api/";
    public static final String TEST_HOST = "http://app.znanie.tv/test/api/";

    public static String API_CATEGORIES_LIST;

    static {
        if (ZSApp.DEBUG_ENV) {
            API_CATEGORIES_LIST = TEST_HOST + "v1/categories.json";
        } else {
            API_CATEGORIES_LIST = HOST + "v1/categories.json";
        }
    }

    public static final String API_CATEGORY   = HOST + "v1/category/";
    public static final String API_PAGE_ABOUT = HOST + "v1/page/about";

    public static final String CATEGORY_ID    = "category_id";
    public static final String CATEGORY       = "category";
    public static final String POINTS         = "points";
    public static final String MAX_POINTS     = "max_points";
    public static final String LEADERBOARD_ID = "leaderboard_id";

    public static final String SHUFFLE_QUESTIONS           = "shuffle_questions";
    public static final String SHUFFLE_OPTIONS             = "shuffle_options";
    public static final String HIGHLIGHT_OPTIONS           = "highlight_options";
    public static final String OPTIONS_ANIMATION           = "options_animation";
    public static final String CATEGORY_FOR_KNOWLEDGE      = "category_knowledge";
    public static final String CATEGORY_TITLE              = "category_title";
    public static final String MAIN_TITLE                  = "main_title";
    public static final String CATEGORY_TITLE_FONT_SIZE    = "category_title_font_size";
    public static final String FINAL_SCREEN_FONT_SIZE      = "final_screen_font_size";
    public static final String PLAY_SOUND_ON_ANSWERING     = "play_sound";
    public static final String AD_SUPPORT_NEEDED           = "ad_support";
    public static final String CHARTBOOST_APPID            = "charboost_appid";
    public static final String CHARTBOOST_APPSECRET        = "chartboost_appkey";
    public static final String ADS_DISABLED_AFTER_PURCHASE = "ads_disabled_after_purchase";
    public static final String APPKEY_64BIT                = "64bit_appkey";
    public static final String REMOVE_ADS_SKU              = "removeads_sku";
    public static final String GAME_SERVICES_ENABLED       = "gameservices_enabled";
    public static final String PREF_SOUND_ON               = "sound_on";
    public static final String PREF_MUSIC_ON               = "music_on";
    public static final String ABOUT_TEXT                  = "about_text";
    public static final String PREF_FIRS_RUN               = "first_run";

}