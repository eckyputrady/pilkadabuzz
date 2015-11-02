package com.eyjalabs.pilkadabuzz;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * Created by eckyputrady on 11/1/15.
 */
public class PilkadaBuzzApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
    }
}
