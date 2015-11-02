package com.eyjalabs.pilkadabuzz.domain;

import java.util.Date;

/**
 * Created by eckyputrady on 11/1/15.
 */
public class BuzzAt {
    public final Date at;
    public final long value;

    public BuzzAt(Date at, long value) {
        this.at = at;
        this.value = value;
    }
}
