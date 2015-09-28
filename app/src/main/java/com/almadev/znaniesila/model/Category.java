package com.almadev.znaniesila.model;

import java.io.Serializable;

/**
 * Created by Aleksey on 24.09.2015.
 */
public class Category implements Serializable {
    private static final long serialVersionUID = 1L;

    private String category_color;
    private boolean timer_required;
    private String category_id;
    private String category_name;
    private String leaderboard_id;
    private String category_description;
    private String category_image_path;
    private int category_questions_max_limit;
    private String productIdentifier;

    private int record;

    public String getCategory_color() {
        return category_color;
    }

    public boolean isTimer_required() {
        return timer_required;
    }

    public String getCategory_id() {
        return category_id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public String getLeaderboard_id() {
        return leaderboard_id;
    }

    public String getCategory_description() {
        return category_description;
    }

    public String getCategory_image_path() {
        return category_image_path;
    }

    public int getCategory_question_max_limit() {
        return category_questions_max_limit;
    }

    public String getProductIdentifier() {
        return productIdentifier;
    }
}
