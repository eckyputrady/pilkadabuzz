package com.eyjalabs.pilkadabuzz.domain;

/**
 * Created by eckyputrady on 11/1/15.
 */
public class Candidate {
    public final String id;
    public final String name;
    public final String profpic;
    public final String province;

    public Candidate(String id, String name, String profpic, String province) {
        this.id = id;
        this.name = name;
        this.profpic = profpic;
        this.province = province;
    }
}
