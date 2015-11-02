package com.eyjalabs.pilkadabuzz;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eyjalabs.pilkadabuzz.domain.App;
import com.eyjalabs.pilkadabuzz.domain.ArticleStats;
import com.eyjalabs.pilkadabuzz.domain.BuzzAt;
import com.eyjalabs.pilkadabuzz.domain.Candidate;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.squareup.picasso.Picasso;
import com.trello.rxlifecycle.FragmentEvent;
import com.trello.rxlifecycle.RxLifecycle;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;

/**
 * A fragment representing a single Candidate detail screen.
 * This fragment is either contained in a {@link CandidateListActivity}
 * in two-pane mode (on tablets) or a {@link CandidateDetailActivity}
 * on handsets.
 */
public class CandidateDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    private BehaviorSubject<FragmentEvent> lifecycle$ = BehaviorSubject.create();
    private static final DateFormat justHour = new SimpleDateFormat("HH:mm");

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CandidateDetailFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        lifecycle$.onNext(FragmentEvent.ATTACH);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lifecycle$.onNext(FragmentEvent.CREATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_candidate_detail, container, false);

        App.i().streams.activeCandidate$
                .observeOn(AndroidSchedulers.mainThread())
                .compose(RxLifecycle.bindFragment(lifecycle$))
                .subscribe(candidate -> setupCandidateDetail(rootView, candidate), e -> Log.e("HAHA", "Fail", e));
        App.i().streams.candidateBuzz$
                .observeOn(AndroidSchedulers.mainThread())
                .compose(RxLifecycle.bindFragment(lifecycle$))
                .subscribe(buzz -> setupChart(rootView, buzz));
        App.i().streams.candidateBuzzArticles$
                .observeOn(AndroidSchedulers.mainThread())
                .compose(RxLifecycle.bindFragment(lifecycle$))
                .subscribe(articles -> setupTopArticles(rootView, articles), e -> Log.e("HAHA", "Fail articles", e));

        App.i().setActiveCandidate(getCandidateIdFromArgument());

        lifecycle$.onNext(FragmentEvent.CREATE_VIEW);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        lifecycle$.onNext(FragmentEvent.RESUME);
    }

    @Override
    public void onPause() {
        super.onPause();
        lifecycle$.onNext(FragmentEvent.PAUSE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        lifecycle$.onNext(FragmentEvent.DESTROY_VIEW);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lifecycle$.onNext(FragmentEvent.DESTROY);
    }



    private void setupCandidateDetail(View rootView, Candidate candidate) {
        ((TextView)rootView.findViewById(R.id.candidate_name)).setText(candidate.name);
        ((TextView)rootView.findViewById(R.id.candidate_location)).setText(candidate.province);
        Picasso.with(getActivity()).load(candidate.profpic).fit().centerCrop().into((ImageView) rootView.findViewById(R.id.candidate_img));
    }

    private void setupChart(View rootView, List<BuzzAt> buzz) {
        List<Entry> entries = Observable.zip(Observable.range(0, buzz.size()), Observable.from(buzz), (a,b) -> new Entry(b.value, a)).toList().toBlocking().first();
        List<String> xVals = Observable.from(buzz).map(x -> justHour.format(x.at)).toList().toBlocking().first();

        // get
        LineChart lc = (LineChart)rootView.findViewById(R.id.line_chart);
        lc.setDrawBorders(false);
        lc.setDrawGridBackground(false);
        lc.getAxisRight().setEnabled(false);
        lc.setDescription("");
        LineDataSet dataSet = new LineDataSet(
                entries,
                "Tweets about the candidate"
        );
        dataSet.setDrawCubic(true);
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(255);
        dataSet.setFillColor(getResources().getColor(R.color.primary));
        dataSet.setColor(getResources().getColor(R.color.primaryDark));
        dataSet.setCircleColor(getResources().getColor(R.color.primaryDark));
        dataSet.setCircleColorHole(getResources().getColor(R.color.primaryDark));
        dataSet.setDrawHighlightIndicators(false);
        dataSet.setDrawValues(false);
        LineData data = new LineData(
                xVals,
                Arrays.asList(
                    dataSet
                )
        );
        lc.setData(data);
        lc.invalidate();
    }

    private void setupTopArticles(View rootView, List<ArticleStats> articles) {
        // get the list
        LinearLayout lv = (LinearLayout)rootView.findViewById(R.id.top_articles_container);
        lv.removeAllViews();

        // setup adapter
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        for (ArticleStats article : articles) {
            lv.addView(setupArticle(inflater.inflate(R.layout.article_item, lv, false), article));
        }
    }

    private View setupArticle(View inflated, ArticleStats article) {
        ((TextView)inflated.findViewById(R.id.article_title)).setText(article.data.title);
        ((TextView)inflated.findViewById(R.id.article_share)).setText(article.value + " shares");
        ImageView img = (ImageView)inflated.findViewById(R.id.article_img);
        Picasso .with(getActivity())
                .load(article.data.imgURL)
                .fit().centerCrop()
                .into(img);

        inflated.setOnClickListener(i -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(article.data.id)));
        });

        return inflated;
    }

    public String getCandidateIdFromArgument() {
        String i = getArguments().getString(ARG_ITEM_ID);
        return i;
    }
}
