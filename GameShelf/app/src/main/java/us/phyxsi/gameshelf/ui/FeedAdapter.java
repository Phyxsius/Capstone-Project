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
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import us.phyxsi.gameshelf.R;
import us.phyxsi.gameshelf.data.DataLoadingSubject;
import us.phyxsi.gameshelf.data.GameShelfItem;
import us.phyxsi.gameshelf.data.GameShelfItemComparator;
import us.phyxsi.gameshelf.data.api.bgg.model.Boardgame;
import us.phyxsi.gameshelf.ui.widget.BadgedFourFourImageView;
import us.phyxsi.gameshelf.util.ObservableColorMatrix;
import us.phyxsi.gameshelf.util.glide.BoardgameTarget;

/**
 * Adapter for the main screen grid of items
 */
public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_BOARDGAME = 0;
    private static final int TYPE_LOADING_MORE = -1;
    public static final float DUPE_WEIGHT_BOOST = 0.4f;

    // we need to hold on to an activity ref for the shared element transitions :/
    private final Activity host;
    private final LayoutInflater layoutInflater;
    private final GameShelfItemComparator comparator;
    private @Nullable DataLoadingSubject dataLoading;
    private final int columns;
    private final ColorDrawable[] shotLoadingPlaceholders;
    private int shotWidth = 0;

    private List<GameShelfItem> items;

    public FeedAdapter(Activity hostActivity,
                       DataLoadingSubject dataLoading,
                       int columns) {
        this.host = hostActivity;
        this.dataLoading = dataLoading;
        this.columns = columns;
        layoutInflater = LayoutInflater.from(host);
        comparator = new GameShelfItemComparator();
        items = new ArrayList<>();
        setHasStableIds(true);
        TypedArray placeholderColors = hostActivity.getResources()
                .obtainTypedArray(R.array.loading_placeholders);
        shotLoadingPlaceholders = new ColorDrawable[placeholderColors.length()];
        for (int i = 0; i < placeholderColors.length(); i++) {
            shotLoadingPlaceholders[i] = new ColorDrawable(
                    placeholderColors.getColor(i, Color.DKGRAY));
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_BOARDGAME:
                return new BoardgameHolder(
                        layoutInflater.inflate(R.layout.boardgame_item, parent, false));
            case TYPE_LOADING_MORE:
                return new LoadingMoreHolder(
                        layoutInflater.inflate(R.layout.infinite_loading, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position < getDataItemCount()
                && getDataItemCount() > 0) {
            GameShelfItem item = getItem(position);
            if (item instanceof Boardgame) {
                bindBoardgame((Boardgame) getItem(position), (BoardgameHolder) holder);
            }
        } else {
            bindLoadingViewHolder((LoadingMoreHolder) holder, position);
        }
    }

    private void bindBoardgame(final Boardgame game, final BoardgameHolder holder) {
        final BadgedFourFourImageView iv = (BadgedFourFourImageView) holder.itemView;
        Glide.with(host)
                .load("http:" + game.image)
                .listener(new RequestListener<String, GlideDrawable>() {

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model,
                                                   Target<GlideDrawable> target, boolean
                                                           isFromMemoryCache, boolean
                                                           isFirstResource) {
                        if (!game.hasFadedIn) {
                            iv.setHasTransientState(true);
                            final ObservableColorMatrix cm = new ObservableColorMatrix();
                            ObjectAnimator saturation = ObjectAnimator.ofFloat(cm,
                                    ObservableColorMatrix.SATURATION, 0f, 1f);
                            saturation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener
                                    () {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    // just animating the color matrix does not invalidate the
                                    // drawable so need this update listener.  Also have to create a
                                    // new CMCF as the matrix is immutable :(
                                    if (iv.getDrawable() != null) {
                                        iv.getDrawable().setColorFilter(new
                                                ColorMatrixColorFilter(cm));
                                    }
                                }
                            });
                            saturation.setDuration(2000);
                            saturation.setInterpolator(AnimationUtils.loadInterpolator(host,
                                    android.R.interpolator.fast_out_slow_in));
                            saturation.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    iv.setHasTransientState(false);
                                }
                            });
                            saturation.start();
                            game.hasFadedIn = true;
                        }
                        return false;
                    }

                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable>
                            target, boolean isFirstResource) {
                        return false;
                    }
                })
                // needed to prevent seeing through view as it fades in
                .placeholder(R.color.background_dark)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new BoardgameTarget(iv, false));

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iv.setTransitionName(iv.getResources().getString(R.string.transition_game));
                iv.setBackgroundColor(
                        ContextCompat.getColor(host, R.color.background_light));
                Intent intent = new Intent();
                intent.setClass(host, BoardgameActivity.class);
                intent.putExtra(BoardgameActivity.EXTRA_BOARDGAME, game);
                ActivityOptions options =
                        ActivityOptions.makeSceneTransitionAnimation(host,
                                Pair.create(view, host.getString(R.string.transition_game)),
                                Pair.create(view, host.getString(R.string.transition_game_background)));
                host.startActivity(intent, options.toBundle());
            }
        });
    }

    private void bindLoadingViewHolder(LoadingMoreHolder holder, int position) {
        // only show the infinite load progress spinner if there are already items in the
        // grid i.e. it's not the first item & data is being loaded
        holder.progress.setVisibility(position > 0 && dataLoading.isDataLoading() ?
                View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public int getItemViewType(int position) {
        if (position < getDataItemCount()
                && getDataItemCount() > 0) {
            GameShelfItem item = getItem(position);
            if (item instanceof Boardgame) {
                return TYPE_BOARDGAME;
            }
        }
        return TYPE_LOADING_MORE;
    }

    private GameShelfItem getItem(int position) {
        return items.get(position);
    }

    public int getItemColumnSpan(int position) {
        switch (getItemViewType(position)) {
            case TYPE_LOADING_MORE:
                return columns;
            default:
                return getItem(position).colspan;
        }
    }

    private void add(GameShelfItem item) {
        items.add(item);
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public void addAndResort(Collection<? extends GameShelfItem> newItems) {
        // de-dupe results as the same item can be returned by multiple feeds
        boolean add = true;
        for (GameShelfItem newItem : newItems) {
            int count = getDataItemCount();
            for (int i = 0; i < count; i++) {
                GameShelfItem existingItem = getItem(i);
                if (existingItem.equals(newItem)) {
                    // if we find a dupe mark the weight boost field on the first-in, but don't add
                    // the dupe. We use the fact that an item comes from multiple sources to indicate it
                    // is more important and sort it higher
                    existingItem.weightBoost = DUPE_WEIGHT_BOOST;
                    add = false;
                    break;
                }
            }
            if (add) {
                add(newItem);
                add = true;
            }
        }
        sort();
        expandPopularItems();
    }

    private void expandPopularItems() {
        // for now just expand the first dribbble image per page which should be
        // the most popular according to #sort.
        // TODO make this smarter & handle other item types
        List<Integer> expandedPositions = new ArrayList<>();
        int page = -1;
        final int count = items.size();
        for (int i = 0; i < count; i++) {
            GameShelfItem item = getItem(i);
//            if (item instanceof Shot && item.page > page) {
//                item.colspan = columns;
//                page = item.page;
//                expandedPositions.add(i);
//            } else {
                item.colspan = 1;
//            }
        }

        // make sure that any expanded items are at the start of a row
        // so that we don't leave any gaps in the grid
//        for (int expandedPos = 0; expandedPos < expandedPositions.size(); expandedPos++) {
//            int pos = expandedPositions.get(expandedPos);
//            int extraSpannedSpaces = expandedPos * (columns - 1);
//            int rowPosition = (pos + extraSpannedSpaces) % columns;
//            if (rowPosition != 0) {
//                int swapWith = pos + (columns - rowPosition);
//                if (swapWith < items.size()) {
//                    Collections.swap(items, pos, swapWith);
//                }
//            }
//        }
    }

    protected void sort() {
//        // calculate the 'weight' for each data type and then sort by that. Each data type has a
//        // different metric for weighing it e.g. Dribbble uses likes etc. Weights are 'scoped' to
//        // the page they belong to and lower weights are sorted higher in the grid.
//        int count = getDataItemCount();
//        int maxDesignNewsVotes = 0;
//        int maxDesignNewsComments = 0;
//        long maxDribbleLikes = 0;
//        int maxProductHuntVotes = 0;
//        int maxProductHuntComments = 0;
//
//        // work out some maximum values to weigh individual items against
//        for (int i = 0; i < count; i++) {
//            GameShelfItem item = getItem(i);
//            if (item instanceof Shot) {
//                maxDribbleLikes = Math.max(((Shot) item).likes_count, maxDribbleLikes);
//            }
//        }
//
//        // now go through and set the weight of each item
//        for (int i = 0; i < count; i++) {
//            GameShelfItem item = getItem(i);
//            if (item instanceof Story) {
//                ((Story) item).weigh(maxDesignNewsComments, maxDesignNewsVotes);
//            } else if (item instanceof Shot) {
//                ((Shot) item).weigh(maxDribbleLikes);
//            } else if (item instanceof Post) {
//                ((Post) item).weigh(maxProductHuntComments, maxProductHuntVotes);
//            }
//            // scope it to the page it came from
//            item.weight += item.page;
//        }
//
//        // sort by weight
//        Collections.sort(items, comparator);
//        notifyDataSetChanged(); // TODO call the more specific RV variants
    }

    public void removeDataSource(String dataSource) {
        int i = items.size() - 1;
        while (i >= 0) {
            GameShelfItem item = items.get(i);
            if (dataSource.equals(item.dataSource)) {
                items.remove(i);
            }
            i--;
        }
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        if (getItemViewType(position) == TYPE_LOADING_MORE) {
            return -1L;
        }
        return getItem(position).id;
    }

    @Override
    public int getItemCount() {
        // include loading footer
        return getDataItemCount() + 1;
    }

    public int getDataItemCount() {
        return items.size();
    }

    /* protected */ class BoardgameHolder extends RecyclerView.ViewHolder {

        public BoardgameHolder(View itemView) {
            super(itemView);
        }

    }

    /* protected */ class LoadingMoreHolder extends RecyclerView.ViewHolder {

        ProgressBar progress;

        public LoadingMoreHolder(View itemView) {
            super(itemView);
            progress = (ProgressBar) itemView;
        }

    }

}