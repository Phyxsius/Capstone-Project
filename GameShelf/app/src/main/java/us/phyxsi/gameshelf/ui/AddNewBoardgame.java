/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.phyxsi.gameshelf.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import us.phyxsi.gameshelf.R;
import us.phyxsi.gameshelf.data.SearchDataManager;
import us.phyxsi.gameshelf.data.api.bgg.model.Boardgame;
import us.phyxsi.gameshelf.data.db.helper.BoardgameDbHelper;
import us.phyxsi.gameshelf.ui.transitions.FabDialogMorphSetup;
import us.phyxsi.gameshelf.ui.widget.BaselineGridTextView;
import us.phyxsi.gameshelf.ui.widget.BottomSheet;
import us.phyxsi.gameshelf.ui.widget.ObservableScrollView;

public class AddNewBoardgame extends Activity {

    public static final int RESULT_DRAG_DISMISSED = 3;
    public static final int RESULT_BOARDGAME_ADDED = 4;

    @Bind(R.id.bottom_sheet) BottomSheet bottomSheet;
    @Bind(R.id.bottom_sheet_content) ViewGroup bottomSheetContent;
    @Bind(R.id.title) TextView sheetTitle;
    @Bind(R.id.scroll_container) ObservableScrollView scrollContainer;
    @Bind(R.id.results_container) ViewGroup resultsContainer;
    @Bind(R.id.add_new_boardgame_title) EditText title;
    @Bind(R.id.add_new_boardgame_collect) Button collect;
    @BindDimen(R.dimen.z_app_bar) float appBarElevation;

    @Bind(android.R.id.empty) ProgressBar progress;
    @Bind(R.id.search_results) ListView results;
    private BaselineGridTextView noResults;
    private Transition auto;

    private SearchDataManager dataManager;
    private AddNewBoardgameAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_new_boardgame);
        ButterKnife.bind(this);
        FabDialogMorphSetup.setupSharedEelementTransitions(this, bottomSheetContent, 0);

        auto = TransitionInflater.from(this).inflateTransition(R.transition.auto);
        dataManager = new SearchDataManager(this) {
            @Override
            public void onDataLoaded(List<? extends Boardgame> data) {
                if (data != null) {
                    if (data.size() == 1) {
                        // We got a single hit from the search so we need to get all the details of
                        // the game and then we can add it to the database
                        if (data.get(0).image == null) {
                            addToCollection(data.get(0));
                            return;
                        }

                        // Save to DB
                        BoardgameDbHelper bgHelper = new BoardgameDbHelper(AddNewBoardgame.this);
                        bgHelper.insert(data.get(0));
                        setResult(RESULT_BOARDGAME_ADDED);

                        finishAfterTransition();
                    } else if (data.size() > 1) {
                        if (results.getVisibility() == View.VISIBLE) {
                            TransitionManager.beginDelayedTransition(resultsContainer, auto);
                            progress.setVisibility(View.INVISIBLE);
                            results.setVisibility(View.VISIBLE);
                        }
//                    adapter.addAndResort(data);
                        adapter = new AddNewBoardgameAdapter(AddNewBoardgame.this, R.layout.search_result_item, (List<Boardgame>) data);
                        results.setAdapter(adapter);
                        results.setDivider(getDrawable(R.drawable.list_divider));
                        results.setDividerHeight(getResources().getDimensionPixelSize(R.dimen
                                .divider_height));
                    }
                } else {
                    TransitionManager.beginDelayedTransition(resultsContainer, auto);
                    progress.setVisibility(View.GONE);
                    setNoResultsVisibility(View.VISIBLE);
                }
            }
        };

//        adapter = new FeedAdapter(this, dataManager, 1);
        results.setAdapter(adapter);
//        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
//        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
//            @Override
//            public int getSpanSize(int position) {
//                return adapter.getItemColumnSpan(position);
//            }
//        });
//        results.setLayoutManager(layoutManager);
//        results.setHasFixedSize(true);
        results.setOnScrollListener(scrollListener);

        bottomSheet.addListener(new BottomSheet.Listener() {
            @Override
            public void onDragDismissed() {
                // After a drag dismiss, finish without the shared element return transition as
                // it no longer makes sense.  Let the launching window know it's a drag dismiss so
                // that it can restore any UI used as an entering shared element
                setResult(RESULT_DRAG_DISMISSED);
                finish();
            }

            @Override
            public void onDrag(int top) { /* no-op */ }
        });

        scrollContainer.setListener(new ObservableScrollView.OnScrollListener() {
            @Override
            public void onScrolled(int scrollY) {
                if (scrollY != 0
                        && sheetTitle.getTranslationZ() != appBarElevation) {
                    sheetTitle.animate()
                            .translationZ(appBarElevation)
                            .setStartDelay(0L)
                            .setDuration(80L)
                            .setInterpolator(AnimationUtils.loadInterpolator
                                    (AddNewBoardgame.this, android.R.interpolator
                                            .fast_out_slow_in))
                            .start();
                } else if (scrollY == 0 && sheetTitle.getTranslationZ() == appBarElevation) {
                    sheetTitle.animate()
                            .translationZ(0f)
                            .setStartDelay(0L)
                            .setDuration(80L)
                            .setInterpolator(AnimationUtils.loadInterpolator
                                    (AddNewBoardgame.this,
                                            android.R.interpolator.fast_out_slow_in))
                            .start();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        // customize window animations
        overridePendingTransition(0, R.anim.fade_out_rapidly);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (isShareIntent()) {
            bottomSheetContent.animate()
                    .translationY(bottomSheetContent.getHeight())
                    .setDuration(160L)
                    .setInterpolator(AnimationUtils.loadInterpolator(
                            AddNewBoardgame.this,
                            android.R.interpolator.fast_out_linear_in))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            finishAfterTransition();
                        }
                    });
        } else {
            super.onBackPressed();
        }
    }

    @OnClick(R.id.bottom_sheet)
    protected void dismiss() {
        finishAfterTransition();
    }

    @OnTextChanged(R.id.add_new_boardgame_title)
    protected void titleTextChanged(CharSequence text) {
        if (TextUtils.isEmpty(title.getText().toString())) return;

        setPostButtonState();
    }

    @OnClick(R.id.add_new_boardgame_collect)
    protected void collectBoardgame() {
        searchFor(title.getText().toString());
    }

    private void searchFor(String query) {
        clearResults();
        progress.setVisibility(View.VISIBLE);
        results.setVisibility(View.VISIBLE);
        dataManager.searchFor(query);
    }

    private void clearResults() {
//        adapter.clear();
        dataManager.clear();
        TransitionManager.beginDelayedTransition(resultsContainer, auto);
        results.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        setNoResultsVisibility(View.GONE);
    }

    private void setNoResultsVisibility(int visibility) {
        if (visibility == View.VISIBLE) {
            if (noResults == null) {
                noResults = (BaselineGridTextView) ((ViewStub)
                        findViewById(R.id.stub_no_search_results)).inflate();
                noResults.setCompoundDrawablesWithIntrinsicBounds(null, getDrawable(R.drawable.ic_search_dark_24dp), null, null);
                if (Build.VERSION.SDK_INT < 23) {
                    noResults.setTextAppearance(AddNewBoardgame.this, R.style.Widget_GameShelf_EmptyText_Dark);
                } else {
                    noResults.setTextAppearance(R.style.Widget_GameShelf_EmptyText_Dark);
                }
            }
            String message = String.format(getString(R
                    .string.no_search_results), title.getText().toString());
            SpannableStringBuilder ssb = new SpannableStringBuilder(message);
            ssb.setSpan(new StyleSpan(Typeface.ITALIC),
                    message.indexOf('“') + 1,
                    message.length() - 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            noResults.setText(ssb);
        }
        if (noResults != null) {
            noResults.setVisibility(visibility);
        }
    }

    private boolean isShareIntent() {
        return getIntent() != null && Intent.ACTION_SEND.equals(getIntent().getAction());
    }

    private void setPostButtonState() {
        collect.setEnabled(!TextUtils.isEmpty(title.getText()));
    }

    private AbsListView.OnScrollListener scrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScroll(AbsListView view, int firstVisibleItemPosition, int
                visibleItemCount, int totalItemCount) {
            if (results.getMaxScrollAmount() > 0
                    && firstVisibleItemPosition == 0
                    && results.getChildAt(0) != null) {
                int listScroll = results.getChildAt(0).getTop();
            }
        }

        public void onScrollStateChanged(AbsListView view, int scrollState) {
            // as we animate the main image's elevation change when it 'pins' at it's min height
            // a fling can cause the title to go over the image before the animation has a chance to
            // run. In this case we short circuit the animation and just jump to state.
//            imageView.setImmediatePin(scrollState == AbsListView.OnScrollListener
//                    .SCROLL_STATE_FLING);
        }
    };

    private void addToCollection(Boardgame boardgame) {
        // Get the full boardgame details
        dataManager.getById(boardgame.id);
    }

    protected class AddNewBoardgameAdapter extends ArrayAdapter<Boardgame> {

        private final LayoutInflater inflater;
        private final Transition change;
        private int expandedCommentPosition = ListView.INVALID_POSITION;

        public AddNewBoardgameAdapter(Context context, int resource, List<Boardgame> boardgames) {
            super(context, resource, boardgames);
            inflater = LayoutInflater.from(context);
            change = new AutoTransition();
            change.setDuration(200L);
            change.setInterpolator(AnimationUtils.loadInterpolator(context,
                    android.R.interpolator.fast_out_slow_in));
        }

        @Override
        public View getView(int position, View view, ViewGroup container) {
            if (view == null) {
                view = newSearchResultView(position, container);
            }
            bindSearchResult(getItem(position), position, view);
            return view;
        }

        private View newSearchResultView(int position, ViewGroup parent) {
            View view = inflater.inflate(R.layout.search_result_item, parent, false);
            view.setTag(R.id.search_result_title, view.findViewById(R.id.search_result_title));
            view.setTag(R.id.search_result_date, view.findViewById(R.id.search_result_date));
            return view;
        }

        private void bindSearchResult(final Boardgame boardgame, final int position, final View view) {
            final TextView title = (TextView) view.getTag(R.id.search_result_title);
            final TextView date = (TextView) view.getTag(R.id.search_result_date);

            title.setText(boardgame.getTitle());
            date.setText(boardgame.yearPublished);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addToCollection(boardgame);

                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).id;
        }

    }
}
