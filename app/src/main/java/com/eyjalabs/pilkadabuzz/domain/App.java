package com.eyjalabs.pilkadabuzz.domain;

import android.util.Log;

import com.eyjalabs.pilkadabuzz.RepoRetrofit;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;

import java.util.Arrays;
import java.util.List;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

/**
 * Created by eckyputrady on 11/1/15.
 */
public class App {

    private static App _i;
    public static App i() {
        return _i = _i == null ? new App() : _i;
    }

    private final RepoRetrofit repo;
    public final Streams streams;

    private final BehaviorSubject<Integer> activeCandidateId$ = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> loadCandidateBuzzes$ = BehaviorSubject.create();

    public App() {
        OkHttpClient client = new OkHttpClient();
        client.interceptors().add(c -> {
            Log.e("HAHA", c.request().urlString());
            Response resp = c.proceed(c.request());
            Log.e("HAHA", resp.body().toString());
            return resp;
        });
        this.repo = new Retrofit.Builder()
                .baseUrl("http://128.199.207.150")
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client)
                .build()
                .create(RepoRetrofit.class);

        Observable<List<Candidate>> candidate$ = createCandidate$().replay(1).refCount();
        Observable<List<CandidateBuzz>> candidateBuzzes$ = createCandidateBuzz$(candidate$, loadCandidateBuzzes$);
        Observable<Candidate> activeCandidate$ = createActiveCandidate$(candidate$, activeCandidateId$).share();
        Observable<List<BuzzAt>> activeCandidateBuzz$ = createActiveCandidateBuzz$(activeCandidate$);
        Observable<List<ArticleStats>> activeCandidateTopArticles$ = createActiveCandidateTopArticles$(activeCandidate$);

        this.streams = new Streams(
                candidate$,
                candidateBuzzes$,
                activeCandidate$,
                activeCandidateBuzz$,
                activeCandidateTopArticles$
        );
    }

    private Observable<List<ArticleStats>> createActiveCandidateTopArticles$(Observable<Candidate> activeCandidate$) {
        return activeCandidate$.switchMap(candidate -> {
            return repo.getArticlesFor(candidate.id).subscribeOn(Schedulers.io());
        });
    }

    private Observable<List<BuzzAt>> createActiveCandidateBuzz$(Observable<Candidate> activeCandidate$) {
        return activeCandidate$.switchMap(candidate -> {
            return repo.getBuzzFor(candidate.id).subscribeOn(Schedulers.io());
        });
    }

    private Observable<Candidate> createActiveCandidate$(Observable<List<Candidate>> candidates$, Observable<Integer> activeCandidateId$) {
        return activeCandidateId$.withLatestFrom(candidates$, (pos, cs) -> {
            return cs.get(pos);
        });
    }

    private Observable<List<CandidateBuzz>> createCandidateBuzz$(Observable<List<Candidate>> candidates$, Observable<Boolean> loadCandidateBuzzes$) {
        Observable<List<Long>> buzzes$ = loadCandidateBuzzes$.switchMap(i -> Observable.just(Arrays.asList(1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L)));
        return Observable.combineLatest(candidates$, buzzes$, (xs, ys) -> {
            Observable<Candidate> candidate$ = Observable.from(xs);
            Observable<Long> buzz$ = Observable.from(ys);
            return Observable.zip(candidate$, buzz$, (a, b) -> new CandidateBuzz(a, b)).toList().toBlocking().first();
        });
    }

    private Observable<List<Candidate>> createCandidate$() {
        String raw =
            "30\t32\tJAWA BARAT\t28924\tKAB. BANDUNG\tKABUPATEN\t349\tK.H. Sofyan Yahya\t\t\t\t\thttp://i67.tinypic.com/2gtotn9.jpg\tsofyanyahya\n" +
            "31\t32\tJAWA BARAT\t28924\tKAB. BANDUNG\tKABUPATEN\t351\tH. Dadang M. Naser, SH., S.IP., M.I.Pol.\thttps://twitter.com/Kang_DN1\t\t\t\thttp://apipemilu-caleg.s3-website-ap-southeast-1.amazonaws.com/caleg/foto/DPR/3103.%20DKI%20III/03.%20PKS/02.%20Drs.%20H.%20Adang%20Daradjatun%203x4.jpg\tdadangnaser\n" +
            "32\t32\tJAWA BARAT\t28924\tKAB. BANDUNG\tKABUPATEN\t711\tH. Deki Fajar, SH.\t\t\thttp://inilahcitarum.com/tag/h-deki-fajar-sh/\t\thttp://inilahcitarum.com/wp-content/uploads/2015/07/DSC_00871-e1436459441535-620x330.jpg\tdekifajar\n" +
            "731\t32\tJAWA BARAT\t34032\tKOTA DEPOK\tKOTA\t364\tDimas Oky Nugroho\thttps://twitter.com/dimasonugroho\thttps://www.facebook.com/Dokynugroho/\thttps://id.linkedin.com/pub/dimas-oky-nugroho/42/b9b/52\t\thttps://pbs.twimg.com/profile_images/621503069587533824/_C7PUVMi_400x400.jpg\tdimasokynugroho\n" +
            "732\t32\tJAWA BARAT\t34032\tKOTA DEPOK\tKOTA\t438\tDr. Mohammad Idris, MA\thttps://twitter.com/idris_pradi\thttps://www.facebook.com/Kita-Adalah-IDRIS-PRADI-520426274774383/\thttp://idris-pradi.com/\t\thttp://idris-pradi.com/wp-content/uploads/2015/08/wakil-walikota-depok-idris-abdul-shomad-dok-pribadi.jpg\tmohammadidris\n" +
            "772\t33\tJAWA TENGAH\t43146\tKOTA SEMARANG\tKOTA\t703\tH. Hendrar Prihadi, S.E., M.M.\thttps://twitter.com/hendrarprihadi\thttp://twicsy.com/i/rHdtXd\t\t\thttp://pbs.twimg.com/media/BPeScgaCcAA3voV.jpg:large\thendrarprihadi\n" +
            "773\t33\tJAWA TENGAH\t43146\tKOTA SEMARANG\tKOTA\t263\tDrs. H. Soemarmo HS, M.Si.\thttps://twitter.com/SoemarmoHS\thttps://www.facebook.com/soemarmoHS20162021\t\t\thttps://pbs.twimg.com/profile_images/1282839352/soemarmo_400x400.JPG\tsoemarmohs\n" +
            "774\t33\tJAWA TENGAH\t43146\tKOTA SEMARANG\tKOTA\t332\tSigit Ibnugroho Sarasprono\thttps://twitter.com/SIBAGUSwalikota\thttps://www.facebook.com/sigit.sarasprono\t\t\thttp://www.antarajateng.com/image/2015/10/pub/20151019173928sigit.jpg\tsigitibnugroho\n" +
            "835\t35\tJAWA TIMUR\t52914\tKOTA SURABAYA\tKOTA\t253\tH. Sabirin Yahya\thttps://twitter.com/sabirinyahya\thttps://www.facebook.com/sabirinyahyaID\t\t\thttp://www.sinjaikab.go.id/v2/images/Bupati%20d%20Wakil.jpg\tsabirinyahya";

        return Observable.from(raw.split("\n")).map(x -> {
            String[] row = x.split("\t");
            return new Candidate(row[13], row[7], row[12], row[4]);
        }).toList();
    }

    public void loadCandidateBuzzes() {
        loadCandidateBuzzes$.onNext(true);
    }

    public void setActiveCandidate(int position) {
        activeCandidateId$.onNext(position);
    }

    public static class Streams {
        public final Observable<List<Candidate>> candidates$;
        public final Observable<List<CandidateBuzz>> candidateBuzzes$;
        public final Observable<Candidate> activeCandidate$;
        public final Observable<List<BuzzAt>> candidateBuzz$;
        public final Observable<List<ArticleStats>> candidateBuzzArticles$;

        public Streams(Observable<List<Candidate>> candidates$, Observable<List<CandidateBuzz>> candidateBuzzes$, Observable<Candidate> activeCandidate$, Observable<List<BuzzAt>> candidateBuzz$, Observable<List<ArticleStats>> candidateBuzzArticles$) {
            this.candidates$ = candidates$;
            this.candidateBuzzes$ = candidateBuzzes$;
            this.activeCandidate$ = activeCandidate$;
            this.candidateBuzz$ = candidateBuzz$;
            this.candidateBuzzArticles$ = candidateBuzzArticles$;
        }
    }
}
