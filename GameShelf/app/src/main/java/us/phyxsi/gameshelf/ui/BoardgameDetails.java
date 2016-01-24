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
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.text.Spanned;
import android.text.TextUtils;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import us.phyxsi.gameshelf.R;
import us.phyxsi.gameshelf.data.api.bgg.model.Boardgame;
import us.phyxsi.gameshelf.data.api.bgg.model.Category;
import us.phyxsi.gameshelf.ui.widget.ElasticDragDismissFrameLayout;
import us.phyxsi.gameshelf.ui.widget.FabOverlapTextView;
import us.phyxsi.gameshelf.ui.widget.ParallaxScrimageView;
import us.phyxsi.gameshelf.util.AnimUtils;
import us.phyxsi.gameshelf.util.ColorUtils;
import us.phyxsi.gameshelf.util.HtmlUtils;
import us.phyxsi.gameshelf.util.ViewUtils;
import us.phyxsi.gameshelf.util.glide.GlideUtils;

public class BoardgameDetails extends Activity {

    public final static String EXTRA_BOARDGAME = "boardgame";
    private static final float SCRIM_ADJUSTMENT = 0.075f;

    @Bind(R.id.draggable_frame) ElasticDragDismissFrameLayout draggableFrame;
    @Bind(R.id.back) ImageButton back;
    @Bind(R.id.boardgame) ParallaxScrimageView imageView;
    private View spacer;
    private View title;
    private View byline;
    private View description;
    private LinearLayout boardgameSummary;
    private LinearLayout boardgameSummaryLine1;
    private LinearLayout boardgameSummaryLine2;
    private TextView numOfPlayers;
    private ImageView numOfPlayersIcon;
    private TextView playTime;
    private ImageView playTimeIcon;
    private TextView bestWith;
    private ImageView bestWithIcon;
    private TextView ages;
    private ImageView agesIcon;
    private ListView detailsList;
    private CategoriesAdapter categoriesAdapter;

    private Boardgame boardgame;
    private ElasticDragDismissFrameLayout.SystemChromeFader chromeFader;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_boardgame_details);
        boardgame = getIntent().getParcelableExtra(EXTRA_BOARDGAME);

        getWindow().getSharedElementReturnTransition().addListener(boardgameReturnHomeListener);

        ButterKnife.bind(this);

        View boardgameDescription = getLayoutInflater().inflate(R.layout.boardgame_description,
                detailsList, false);
        spacer = boardgameDescription.findViewById(R.id.description_spacer);
        title = boardgameDescription.findViewById(R.id.boardgame_title);
        byline = boardgameDescription.findViewById(R.id.boardgame_byline);
        description = boardgameDescription.findViewById(R.id.boardgame_description);
        boardgameSummary = (LinearLayout) boardgameDescription.findViewById(R.id.boardgame_summary);
        boardgameSummaryLine1 = (LinearLayout) boardgameDescription.findViewById(R.id.boardgame_summary_line_one);
        boardgameSummaryLine2 = (LinearLayout) boardgameDescription.findViewById(R.id.boardgame_summary_line_two);
        numOfPlayers = (TextView) boardgameDescription.findViewById(R.id.num_of_players_text);
        numOfPlayersIcon = (ImageView) boardgameDescription.findViewById(R.id.num_of_players_icon);
        playTime = (TextView) boardgameDescription.findViewById(R.id.play_time_text);
        playTimeIcon = (ImageView) boardgameDescription.findViewById(R.id.play_time_icon);
        bestWith = (TextView) boardgameDescription.findViewById(R.id.best_with_text);
        bestWithIcon = (ImageView) boardgameDescription.findViewById(R.id.best_with_icon);
        ages = (TextView) boardgameDescription.findViewById(R.id.ages_text);
        agesIcon = (ImageView) boardgameDescription.findViewById(R.id.ages_icon);
        detailsList = (ListView) findViewById(R.id.game_details);
        detailsList.addHeaderView(boardgameDescription);
        detailsList.setOnScrollListener(scrollListener);

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

        final int[] imageSize = { 800, 800 };
        Glide.with(this)
                .load("http:" + boardgame.image)
                .listener(boardgameImageLoadListener)
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
            ((FabOverlapTextView) title).setText(boardgame.title);
            ((FabOverlapTextView) byline).setText(boardgame.getByline());
        } else {
            ((TextView) title).setText(boardgame.title);
            ((TextView) byline).setText(boardgame.getByline());
        }
        if (!TextUtils.isEmpty(boardgame.description)) {
            final Spanned descText = boardgame.getParsedDescription(
                    ContextCompat.getColorStateList(this, R.color.primary),
                    ContextCompat.getColor(this, R.color.accent));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ((FabOverlapTextView) description).setText(descText);
            } else {
                HtmlUtils.setTextWithNiceLinks((TextView) description, descText);
            }
        } else {
            description.setVisibility(View.GONE);
        }

        numOfPlayers.setText(boardgame.getPlayers());
        playTime.setText(boardgame.getPlaytime());
        bestWith.setText("Best with " + boardgame.suggestedNumplayers + " players");
        ages.setText(boardgame.minAge + " and up");

        if (boardgame.categories.size() > 0) {
            categoriesAdapter = new CategoriesAdapter(BoardgameDetails.this, R.layout
                    .category_button, boardgame.categories);
            detailsList.setAdapter(categoriesAdapter);
        } else {
            detailsList.setAdapter(getNoCategoriesAdapter());
        }
    }

    private ListAdapter getNoCategoriesAdapter() {
        String[] noCategories = { "No categories" };
        return new ArrayAdapter<>(this, R.layout.details_no_categories, noCategories);
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

    private RequestListener boardgameImageLoadListener = new RequestListener<String, GlideDrawable>() {
        @Override
        public boolean onResourceReady(GlideDrawable resource, String model,
                                       Target<GlideDrawable> target, boolean isFromMemoryCache,
                                       boolean isFirstResource) {
            final Bitmap bitmap = GlideUtils.getBitmap(resource);
            float imageScale = 0;
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
                            if (topColor != null) {
                                int scrimmed = ColorUtils.scrimify(topColor.getRgb(),
                                        isDark, SCRIM_ADJUSTMENT);

                                title.setBackgroundColor(ColorUtils.getSlightlyDarkerColor(scrimmed));
                                byline.setBackgroundColor(ColorUtils.getSlightlyDarkerColor(scrimmed));

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    ((FabOverlapTextView) title).setTextColor(
                                            ColorUtils.getTitleTextColor(isDark, BoardgameDetails.this));
                                    ((FabOverlapTextView) byline).setTextColor(
                                                ColorUtils.getTextColor(isDark, BoardgameDetails.this));
                                } else {
                                    ((TextView) title).setTextColor(
                                            ColorUtils.getTitleTextColor(isDark, BoardgameDetails.this));
                                    ((TextView) byline).setTextColor(
                                            ColorUtils.getTextColor(isDark, BoardgameDetails.this));
                                }

                                // Summary views
                                boardgameSummary.setBackgroundColor(scrimmed);
                                numOfPlayers.setTextColor(ColorUtils.getTextColor(isDark, BoardgameDetails.this));
                                numOfPlayersIcon.setColorFilter(ColorUtils.getIconColor(isDark, BoardgameDetails.this));
                                bestWith.setTextColor(ColorUtils.getTextColor(isDark, BoardgameDetails.this));
                                bestWithIcon.setColorFilter(ColorUtils.getIconColor(isDark, BoardgameDetails.this));
                                playTime.setTextColor(ColorUtils.getTextColor(isDark, BoardgameDetails.this));
                                playTimeIcon.setColorFilter(ColorUtils.getIconColor(isDark, BoardgameDetails.this));
                                ages.setTextColor(ColorUtils.getTextColor(isDark, BoardgameDetails.this));
                                agesIcon.setColorFilter(ColorUtils.getIconColor(isDark, BoardgameDetails.this));

                                if ((isDark || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
                                    statusBarColor = ColorUtils.scrimify(topColor.getRgb(),
                                            isDark, SCRIM_ADJUSTMENT);
                                    // set a light status bar on M+
                                    if (!isDark && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        ViewUtils.setLightStatusBar(imageView);
                                    }
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
                            spacer.setBackground(ViewUtils.createRipple(palette, 0.25f, 0.5f,
                                    ContextCompat.getColor(BoardgameDetails.this, R.color.mid_grey),
                                    true));
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

    private AbsListView.OnScrollListener scrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScroll(AbsListView view, int firstVisibleItemPosition, int
                visibleItemCount, int totalItemCount) {
            if (detailsList.getMaxScrollAmount() > 0
                    && firstVisibleItemPosition == 0
                    && detailsList.getChildAt(0) != null) {
                int listScroll = detailsList.getChildAt(0).getTop();
                imageView.setOffset(listScroll);
            }
        }

        public void onScrollStateChanged(AbsListView view, int scrollState) {
            // as we animate the main image's elevation change when it 'pins' at it's min height
            // a fling can cause the title to go over the image before the animation has a chance to
            // run. In this case we short circuit the animation and just jump to state.
            imageView.setImmediatePin(scrollState == AbsListView.OnScrollListener
                    .SCROLL_STATE_FLING);
        }
    };

    private Transition.TransitionListener boardgameReturnHomeListener = new AnimUtils
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
            detailsList.animate()
                    .alpha(0f)
                    .setDuration(50)
                    .setInterpolator(AnimationUtils.loadInterpolator(BoardgameDetails.this, android.R
                            .interpolator.linear_out_slow_in));
        }
    };

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
        viewEnterAnimation(byline, offset, interp);
        if (description.getVisibility() == View.VISIBLE) {
            offset *= 1.5f;
            viewEnterAnimation(description, offset, interp);
        }
        offset *= 1.5f;
        viewEnterAnimation(boardgameSummary, offset, interp);
        offset *= 1.5f;
        viewEnterAnimation(boardgameSummaryLine1, offset, interp);
        offset *= 1.5f;
        viewEnterAnimation(boardgameSummaryLine2, offset, interp);
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

    protected class CategoriesAdapter extends ArrayAdapter<Category> {

        private final LayoutInflater inflater;
        private final Transition change;
        private int expandedCommentPosition = ListView.INVALID_POSITION;

        public CategoriesAdapter(Context context, int resource, List<Category> comments) {
            super(context, resource, comments);
            inflater = LayoutInflater.from(context);
            change = new AutoTransition();
            change.setDuration(200L);
            change.setInterpolator(AnimationUtils.loadInterpolator(context,
                    android.R.interpolator.fast_out_slow_in));
        }

        @Override
        public View getView(int position, View view, ViewGroup container) {
            if (view == null) {
                view = newCategoryView(position, container);
            }
            bindCategory(getItem(position), position, view);
            return view;
        }

        private View newCategoryView(int position, ViewGroup parent) {
            View view = inflater.inflate(R.layout.category_button, parent, false);
            view.setTag(R.id.category_button_text, view.findViewById(R.id.category_button_text));
            return view;
        }

        private void bindCategory(final Category category, final int position, final View view) {
            final TextView title = (TextView) view.getTag(R.id.category_button_text);

            title.setText(category.name);
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
