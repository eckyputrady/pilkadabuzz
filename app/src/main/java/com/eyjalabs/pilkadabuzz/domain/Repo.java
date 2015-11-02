package com.eyjalabs.pilkadabuzz.domain;

import java.util.List;

import rx.Observable;

/**
 * Created by eckyputrady on 11/2/15.
 */
public interface Repo {
    Observable<List<BuzzAt>> getBuzzFor(String id);
    Observable<List<ArticleStats>> getArticlesFor(String id);
}
