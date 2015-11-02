package com.eyjalabs.pilkadabuzz.domain;

/**
 * Created by eckyputrady on 11/1/15.
 */
public class CandidateBuzz {
    public final Candidate candidate;
    public final long buzz;

    public CandidateBuzz(Candidate candidate, long buzz) {
        this.candidate = candidate;
        this.buzz = buzz;
    }
}
