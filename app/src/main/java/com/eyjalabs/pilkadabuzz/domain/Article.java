package com.eyjalabs.pilkadabuzz.domain;

/**
 * Created by eckyputrady on 11/1/15.
 */
public class Article {
    public final String id;
    public final String title;
    public final String body;
    public final String imgURL;

    public Article(String id, String title, String body, String imgURL) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.imgURL = imgURL;
    }
}
