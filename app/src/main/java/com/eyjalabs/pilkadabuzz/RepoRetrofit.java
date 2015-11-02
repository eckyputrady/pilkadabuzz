package com.eyjalabs.pilkadabuzz;

import com.eyjalabs.pilkadabuzz.domain.Article;
import com.eyjalabs.pilkadabuzz.domain.ArticleStats;
import com.eyjalabs.pilkadabuzz.domain.BuzzAt;
import com.eyjalabs.pilkadabuzz.domain.Repo;
import com.eyjalabs.pilkadabuzz.domain.SocioBuzzTweet;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

/**
 * Created by eckyputrady on 11/2/15.
 */
public interface RepoRetrofit{
    @GET("/api/projects/{projectId}/links")
    Observable<List<ArticleStats>> getArticlesFor(@Path("projectId") String id);

    @GET("/api/projects/{projectId}/buzz")
    Observable<List<BuzzAt>> getBuzzFor(@Path("projectId") String id);

    @GET("/api/projects/{projectId}/articles")
    Observable<List<SocioBuzzTweet>> getSocioBuzzTweetsFor(@Path("projectId") String id);
}
