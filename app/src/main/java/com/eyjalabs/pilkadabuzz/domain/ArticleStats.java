package com.eyjalabs.pilkadabuzz.domain;

/**
 * Created by eckyputrady on 11/2/15.
 */
public class ArticleStats {
    public final Article data;
    public final long value;

    public ArticleStats(Article data, long value) {
        this.data = data;
        this.value = value;
    }
}
