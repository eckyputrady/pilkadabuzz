package com.eyjalabs.pilkadabuzz;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.eyjalabs.pilkadabuzz.domain.App;
import com.eyjalabs.pilkadabuzz.domain.CandidateBuzz;
import com.squareup.picasso.Picasso;
import com.trello.rxlifecycle.FragmentEvent;
import com.trello.rxlifecycle.RxLifecycle;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;

/**
 * A list fragment representing a list of Candidates. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link CandidateDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class CandidateListFragment extends ListFragment {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    private BehaviorSubject<FragmentEvent> lifecycle$ = BehaviorSubject.create();
    private BehaviorSubject<Integer> selectedItemPos$ = BehaviorSubject.create();

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String pos);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CandidateListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Observable<List<CandidateBuzz>> candidateBuzzes$ = App.i().streams.candidateBuzzes$;
        CandidateBuzzAdapter adapter = new CandidateBuzzAdapter(getActivity(), R.layout.candidate_list_item);
        setListAdapter(adapter);
        syncListAdapter(candidateBuzzes$, adapter);

        lifecycle$.onNext(FragmentEvent.CREATE);
    }

    private void syncListAdapter(Observable<List<CandidateBuzz>> candidateBuzzes$, final CandidateBuzzAdapter adapter) {
        Observable<List<CandidateBuzz>> composed = candidateBuzzes$.observeOn(AndroidSchedulers.mainThread()).compose(RxLifecycle.bindFragment(lifecycle$));
        composed.subscribe(candidateBuzzes -> {
            adapter.clear();
            adapter.addAll(candidateBuzzes);
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }

        lifecycle$.onNext(FragmentEvent.CREATE_VIEW);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;

        lifecycle$.onNext(FragmentEvent.ATTACH);
    }

    @Override
    public void onResume() {
        super.onResume();
        getListView().setDivider(null);
        App.i().loadCandidateBuzzes();

        lifecycle$.onNext(FragmentEvent.RESUME);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;

        lifecycle$.onNext(FragmentEvent.DETACH);
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

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        mCallbacks.onItemSelected(((CandidateBuzz)getListAdapter().getItem(position)).candidate.id);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    private static class CandidateBuzzAdapter extends ArrayAdapter<CandidateBuzz> {
        private final int layoutId;

        public CandidateBuzzAdapter(Context ctx, int layoutId) {
            super(ctx, layoutId);
            this.layoutId = layoutId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(this.layoutId, parent, false);

            CandidateBuzz item = getItem(position);
            ((TextView)convertView.findViewById(R.id.candidate_name)).setText(item.candidate.name);
            ((TextView)convertView.findViewById(R.id.candidate_location)).setText(item.candidate.province);
            ((TextView)convertView.findViewById(R.id.candidate_buzz)).setText(item.buzz + " buzz");
            Picasso .with(getContext())
                    .load(item.candidate.profpic)
                    .fit()
                    .centerCrop()
                    .into((ImageView) convertView.findViewById(R.id.candidate_img));

            return convertView;
        }
    }
}
