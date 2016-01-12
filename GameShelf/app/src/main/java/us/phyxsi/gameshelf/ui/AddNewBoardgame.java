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
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.SimpleXMLConverter;
import us.phyxsi.gameshelf.R;
import us.phyxsi.gameshelf.data.api.bgg.BGGService;
import us.phyxsi.gameshelf.data.api.bgg.model.BoardgamesResponse;
import us.phyxsi.gameshelf.ui.transitions.FabDialogMorphSetup;
import us.phyxsi.gameshelf.ui.widget.BottomSheet;
import us.phyxsi.gameshelf.ui.widget.ObservableScrollView;

/**
 * Created by Andy on 1/11/2016.
 */
public class AddNewBoardgame extends Activity {

    public static final int RESULT_DRAG_DISMISSED = 3;

    @Bind(R.id.bottom_sheet) BottomSheet bottomSheet;
    @Bind(R.id.bottom_sheet_content) ViewGroup bottomSheetContent;
    @Bind(R.id.title) TextView sheetTitle;
    @Bind(R.id.scroll_container) ObservableScrollView scrollContainer;
    @Bind(R.id.add_new_boardgame_title) EditText title;
    @Bind(R.id.add_new_boardgame_collect) Button collect;
    @BindDimen(R.dimen.z_app_bar) float appBarElevation;

    private BGGService bggApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_new_boardgame);
        ButterKnife.bind(this);
        FabDialogMorphSetup.setupSharedEelementTransitions(this, bottomSheetContent, 0);

        createBGGApi();

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
        bggApi.search(title.getText().toString(), new Callback<BoardgamesResponse>() {
            @Override
            public void success(BoardgamesResponse boardgamesResponse, Response response) {
                Log.d("SUCCESS", response.toString());
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("FAILURE", error.toString());
            }
        });

    }

    private void createBGGApi() {
        bggApi = new RestAdapter.Builder()
                .setEndpoint(BGGService.ENDPOINT)
                .setConverter(new SimpleXMLConverter())
                .build()
                .create(BGGService.class);
    }

    private boolean isShareIntent() {
        return getIntent() != null && Intent.ACTION_SEND.equals(getIntent().getAction());
    }

    private void setPostButtonState() {
        collect.setEnabled(!TextUtils.isEmpty(title.getText()));
    }
}
