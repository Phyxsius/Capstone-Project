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
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.text.Spanned;
import android.text.TextUtils;
import android.transition.Transition;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import butterknife.Bind;
import butterknife.ButterKnife;
import us.phyxsi.gameshelf.R;
import us.phyxsi.gameshelf.data.api.bgg.model.Boardgame;
import us.phyxsi.gameshelf.ui.widget.ElasticDragDismissFrameLayout;
import us.phyxsi.gameshelf.ui.widget.ParallaxScrimageView;
import us.phyxsi.gameshelf.util.AnimUtils;
import us.phyxsi.gameshelf.util.ColorUtils;
import us.phyxsi.gameshelf.util.HtmlUtils;
import us.phyxsi.gameshelf.util.ViewUtils;
import us.phyxsi.gameshelf.util.glide.GlideUtils;

public class BoardgameDetails extends Activity {

    protected final static String EXTRA_BOARDGAME = "boardgame";
    private static final float SCRIM_ADJUSTMENT = 0.075f;

    @Bind(R.id.draggable_frame)
    ElasticDragDismissFrameLayout draggableFrame;
    @Bind(R.id.back)
    ImageButton back;
    @Bind(R.id.boardgame)
    ParallaxScrimageView imageView;
    private View title;
    private View description;
    private ListView detailsList;

    private Boardgame boardgame;
    private ElasticDragDismissFrameLayout.SystemChromeFader chromeFader;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_boardgame_details);
        boardgame = getIntent().getParcelableExtra(EXTRA_BOARDGAME);

        getWindow().getSharedElementReturnTransition().addListener(shotReturnHomeListener);
        Resources res = getResources();

        ButterKnife.bind(this);
        View boardgameDescription = getLayoutInflater().inflate(R.layout.boardgame_description,
                detailsList, false);
        title = boardgameDescription.findViewById(R.id.boardgame_title);
        description = boardgameDescription.findViewById(R.id.boardgame_description);
        detailsList = (ListView) findViewById(R.id.game_details);
        detailsList.addHeaderView(boardgameDescription);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandImageAndFinish();
            }
        });
        chromeFader = new ElasticDragDismissFrameLayout.SystemChromeFader(getWindow()) {
            @Override
            public void onDragDismissed() {
                expandImageAndFinish();
            }
        };
        
        // load the main image
        final int[] imageSize = { 800, 800 };
        Glide.with(this)
                .load("http:" + boardgame.image)
                .listener(boardgameLoadListener)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .priority(Priority.IMMEDIATE)
                .centerCrop()
                .override(imageSize[0], imageSize[1])
                .into(imageView);

        postponeEnterTransition();
        imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver
                .OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                enterAnimation(savedInstanceState != null);
                startPostponedEnterTransition();
                return true;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ((TextView) title).setText(boardgame.title);
        } else {
            ((TextView) title).setText(boardgame.title);
        }
        if (!TextUtils.isEmpty(boardgame.description)) {
            final Spanned descText = boardgame.getParsedDescription(
                    ContextCompat.getColorStateList(this, R.color.primary),
                    ContextCompat.getColor(this, R.color.accent));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ((TextView) description).setText(descText);
            } else {
                HtmlUtils.setTextWithNiceLinks((TextView) description, descText);
            }
        } else {
            description.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        draggableFrame.addListener(chromeFader);
    }

    @Override
    protected void onPause() {
        draggableFrame.removeListener(chromeFader);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        expandImageAndFinish();
    }

    @Override
    public boolean onNavigateUp() {
        expandImageAndFinish();
        return true;
    }

    private RequestListener boardgameLoadListener = new RequestListener<String, GlideDrawable>() {
        @Override
        public boolean onResourceReady(GlideDrawable resource, String model,
                                       Target<GlideDrawable> target, boolean isFromMemoryCache,
                                       boolean isFirstResource) {
            final Bitmap bitmap = GlideUtils.getBitmap(resource);
            float imageScale = (float) imageView.getHeight() / (float) bitmap.getHeight();
            float twentyFourDip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24,
                    BoardgameDetails.this.getResources().getDisplayMetrics());
            Palette.from(bitmap)
                    .maximumColorCount(3)
                    .clearFilters()
                    .setRegion(0, 0, bitmap.getWidth() - 1, (int) (twentyFourDip / imageScale))
                    // - 1 to work around https://code.google.com/p/android/issues/detail?id=191013
                    .generate(new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(Palette palette) {
                            boolean isDark;
                            @ColorUtils.Lightness int lightness = ColorUtils.isDark(palette);
                            if (lightness == ColorUtils.LIGHTNESS_UNKNOWN) {
                                isDark = ColorUtils.isDark(bitmap, bitmap.getWidth() / 2, 0);
                            } else {
                                isDark = lightness == ColorUtils.IS_DARK;
                            }

                            if (!isDark) { // make back icon dark on light images
                                back.setColorFilter(ContextCompat.getColor(
                                        BoardgameDetails.this, R.color.dark_icon));
                            }

                            // color the status bar. Set a complementary dark color on L,
                            // light or dark color on M (with matching status bar icons)
                            int statusBarColor = getWindow().getStatusBarColor();
                            Palette.Swatch topColor = ColorUtils.getMostPopulousSwatch(palette);
                            if (topColor != null &&
                                    (isDark || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
                                statusBarColor = ColorUtils.scrimify(topColor.getRgb(),
                                        isDark, SCRIM_ADJUSTMENT);
                                // set a light status bar on M+
                                if (!isDark && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    ViewUtils.setLightStatusBar(imageView);
                                }
                            }

                            if (statusBarColor != getWindow().getStatusBarColor()) {
                                imageView.setScrimColor(statusBarColor);
                                ValueAnimator statusBarColorAnim = ValueAnimator.ofArgb(getWindow
                                        ().getStatusBarColor(), statusBarColor);
                                statusBarColorAnim.addUpdateListener(new ValueAnimator
                                        .AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {
                                        getWindow().setStatusBarColor((int) animation
                                                .getAnimatedValue());
                                    }
                                });
                                statusBarColorAnim.setDuration(1000);
                                statusBarColorAnim.setInterpolator(AnimationUtils
                                        .loadInterpolator(BoardgameDetails.this, android.R
                                                .interpolator.fast_out_slow_in));
                                statusBarColorAnim.start();
                            }
                        }
                    });

            Palette.from(bitmap)
                    .clearFilters() // by default palette ignore certain hues (e.g. pure
                    // black/white) but we don't want this.
                    .generate(new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(Palette palette) {
                            // color the ripple on the image spacer (default is grey)
//                            shotSpacer.setBackground(ViewUtils.createRipple(palette, 0.25f, 0.5f,
//                                    ContextCompat.getColor(BoardgameDetails.this, R.color.mid_grey),
//                                    true));
                            // slightly more opaque ripple on the pinned image to compensate
                            // for the scrim
                            imageView.setForeground(ViewUtils.createRipple(palette, 0.3f, 0.6f,
                                    ContextCompat.getColor(BoardgameDetails.this, R.color.mid_grey),
                                    true));
                        }
                    });

            // TODO should keep the background if the image contains transparency?!
            imageView.setBackground(null);
            return false;
        }

        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target,
                                   boolean isFirstResource) {
            return false;
        }
    };

    private Transition.TransitionListener shotReturnHomeListener = new AnimUtils
            .TransitionListenerAdapter() {
        @Override
        public void onTransitionStart(Transition transition) {
            super.onTransitionStart(transition);
            // fade out the "toolbar" & list as we don't want them to be visible during return
            // animation
            back.animate()
                    .alpha(0f)
                    .setDuration(100)
                    .setInterpolator(AnimationUtils.loadInterpolator(BoardgameDetails.this, android.R
                            .interpolator.linear_out_slow_in));
            imageView.setElevation(1f);
            back.setElevation(0f);
        }
    };

    private void expandImageAndFinish() {
        if (imageView.getOffset() != 0f) {
            Animator expandImage = ObjectAnimator.ofFloat(imageView, ParallaxScrimageView.OFFSET,
                    0f);
            expandImage.setDuration(80);
            expandImage.setInterpolator(AnimationUtils.loadInterpolator(this, android.R
                    .interpolator.fast_out_slow_in));
            expandImage.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    finishAfterTransition();
                }
            });
            expandImage.start();
        } else {
            finishAfterTransition();
        }
    }

    /**
     * Animate in the title, description and author – can't do this in a content transition as they
     * are within the ListView so do it manually.  Also handle the FAB tanslation here so that it
     * plays nicely with #calculateFabPosition
     */
    private void enterAnimation(boolean isOrientationChange) {
        Interpolator interp = AnimationUtils.loadInterpolator(this, android.R.interpolator
                .fast_out_slow_in);
        int offset = title.getHeight();
        viewEnterAnimation(title, offset, interp);
        if (description.getVisibility() == View.VISIBLE) {
            offset *= 1.5f;
            viewEnterAnimation(description, offset, interp);
        }
        offset *= 1.5f;
//        viewEnterAnimation(shotActions, offset, interp);
//        offset *= 1.5f;
//        viewEnterAnimation(playerName, offset, interp);
//        viewEnterAnimation(playerAvatar, offset, interp);
//        viewEnterAnimation(shotTimeAgo, offset, interp);
        back.animate()
                .alpha(1f)
                .setDuration(600)
                .setInterpolator(interp)
                .start();
    }

    private void viewEnterAnimation(View view, float offset, Interpolator interp) {
        view.setTranslationY(offset);
        view.setAlpha(0.8f);
        view.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(600)
                .setInterpolator(interp)
                .setListener(null)
                .start();
    }

}
