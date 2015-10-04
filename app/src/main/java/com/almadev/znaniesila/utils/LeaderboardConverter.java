package com.almadev.znaniesila.utils;

import android.content.Context;
import android.content.res.Resources;

import com.almadev.znaniesila.R;

/**
 * Created by Aleksey on 04.10.2015.
 */
public class LeaderboardConverter {
    public static String getLeaderboard(Context context, String leaderboardId) {
        Resources r = context.getResources();
        switch (leaderboardId) {
            case "kosmos_leaderboard" : return r.getString(R.string.leaderboard_kosmos);
            case "kakizobreli_leaderboard  " : return r.getString(R.string.leaderboard_izobretenie);
            case "mifyotele_leaderboard  " : return r.getString(R.string.leaderboard_body);
            case "sovetmult_leaderboard  " : return r.getString(R.string.leaderboard_su_mults);
            case "rosestrada_leaderboard  " : return r.getString(R.string.leaderboard_rus_show);
            case "otechkino" : return r.getString(R.string.leaderboard_rus_movie);
            case "izvbrendy" : return r.getString(R.string.leaderboard_brands);
        }
        return "";
    }
}
