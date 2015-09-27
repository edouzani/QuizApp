package com.almadev.znaniesila.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Aleksey on 24.09.2015.
 */
public class CategoriesList implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Category> Categories;

    private String version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<Category> getCategories() {
        return Categories;
    }
}
