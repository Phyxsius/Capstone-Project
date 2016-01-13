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
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
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
import us.phyxsi.gameshelf.ui.transitions.FabDialogMorphSetup;
import us.phyxsi.gameshelf.ui.widget.BaselineGridTextView;
import us.phyxsi.gameshelf.ui.widget.BottomSheet;
import us.phyxsi.gameshelf.ui.widget.ObservableScrollView;

public class AddNewBoardgame extends Activity {

    public static final int RESULT_DRAG_DISMISSED = 3;

    @Bind(R.id.bottom_sheet) BottomSheet bottomSheet;
    @Bind(R.id.bottom_sheet_content) ViewGroup bottomSheetContent;
    @Bind(R.id.title) TextView sheetTitle;
    @Bind(R.id.scroll_container) ObservableScrollView scrollContainer;
    @Bind(R.id.results_container) ViewGroup resultsContainer;
    @Bind(R.id.add_new_boardgame_title) EditText title;
    @Bind(R.id.add_new_boardgame_collect) Button collect;
    @BindDimen(R.dimen.z_app_bar) float appBarElevation;

    @Bind(android.R.id.empty) ProgressBar progress;
    @Bind(R.id.search_results) RecyclerView results;
    private BaselineGridTextView noResults;
    private Transition auto;

    private SearchDataManager dataManager;
    private FeedAdapter adapter;

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
                if (data != null && data.size() > 0) {
                    if (results.getVisibility() != View.VISIBLE) {
                        TransitionManager.beginDelayedTransition(resultsContainer, auto);
                        progress.setVisibility(View.GONE);
                        results.setVisibility(View.VISIBLE);
                    }
                    adapter.addAndResort(data);
                } else {
                    TransitionManager.beginDelayedTransition(resultsContainer, auto);
                    progress.setVisibility(View.GONE);
                    setNoResultsVisibility(View.VISIBLE);
                }
            }
        };

        adapter = new FeedAdapter(this, dataManager, 1);
        results.setAdapter(adapter);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getItemColumnSpan(position);
            }
        });
        results.setLayoutManager(layoutManager);
        results.setHasFixedSize(true);

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

        setPostButtonState();
    }

    @OnClick(R.id.add_new_boardgame_collect)
    protected void collectBoardgame() {
        clearResults();
        progress.setVisibility(View.VISIBLE);
        dataManager.searchFor(title.getText().toString());
    }

    private void clearResults() {
        adapter.clear();
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
//                noResults.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        searchView.setQuery("", false);
//                        searchView.requestFocus();
//                        ImeUtils.showIme(searchView);
//                    }
//                });
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
}
