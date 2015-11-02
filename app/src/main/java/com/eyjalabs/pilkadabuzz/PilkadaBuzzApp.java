package com.eyjalabs.pilkadabuzz;

import android.app.Application;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import net.danlew.android.joda.JodaTimeAndroid;

import io.fabric.sdk.android.Fabric;

/**
 * Created by eckyputrady on 11/1/15.
 */
public class PilkadaBuzzApp extends Application {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "9ZTEBEKFIjX9rpZqDQj7Xol5Q";
    private static final String TWITTER_SECRET = "5Qku5Pc8QRbrw5S5Bl2w13qLZs3ZTeYql7tbU4IQtoupn9xLBH";

    @Override
    public void onCreate() {
        super.onCreate();
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        JodaTimeAndroid.init(this);
    }
}
