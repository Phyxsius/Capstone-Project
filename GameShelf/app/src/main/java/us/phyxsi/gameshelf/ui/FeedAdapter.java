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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import us.phyxsi.gameshelf.R;
import us.phyxsi.gameshelf.data.BoardgameComparator;
import us.phyxsi.gameshelf.data.DataLoadingSubject;
import us.phyxsi.gameshelf.data.api.bgg.model.Boardgame;
import us.phyxsi.gameshelf.data.db.helper.BoardgameDbHelper;
import us.phyxsi.gameshelf.ui.widget.BadgedFourFourImageView;
import us.phyxsi.gameshelf.util.AnimUtils;
import us.phyxsi.gameshelf.util.ObservableColorMatrix;
import us.phyxsi.gameshelf.util.ViewUtils;
import us.phyxsi.gameshelf.util.glide.BoardgameTarget;

/**
 * Adapter for the main screen grid of items
 */
public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements DataLoadingSubject.DataLoadingCallbacks {

    private static final int TYPE_BOARDGAME = 0;
    private static final int TYPE_SEARCH_RESULT = 1;
    private static final int TYPE_LOADING_MORE = -1;
    public static final float DUPE_WEIGHT_BOOST = 0.4f;

    // we need to hold on to an activity ref for the shared element transitions :/
    private final Activity host;
    private final Context context;
    private final LayoutInflater layoutInflater;
    private final BoardgameComparator comparator;
    private @Nullable DataLoadingSubject dataLoading;
    private final int columns;
    private final ColorDrawable[] shotLoadingPlaceholders;

    private List<Boardgame> items;

    public FeedAdapter(Activity hostActivity,
                       Context context,
                       DataLoadingSubject dataLoading,
                       int columns) {
        this.host = hostActivity;
        this.context = context;
        this.dataLoading = dataLoading;
        dataLoading.addCallbacks(this);
        this.columns = columns;
        layoutInflater = LayoutInflater.from(host);
        comparator = new BoardgameComparator();
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
                return createBoardgameHolder(parent);
            case TYPE_SEARCH_RESULT:
                return createSearchResultHolder(parent);
            case TYPE_LOADING_MORE:
                return new LoadingMoreHolder(
                        layoutInflater.inflate(R.layout.infinite_loading, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_BOARDGAME:
                bindBoardgameHolder((Boardgame) getItem(position), (BoardgameHolder) holder);
                break;
            case TYPE_SEARCH_RESULT:
                bindSearchResultHolder((Boardgame) getItem(position), (SearchResultHolder) holder);
                break;
            case TYPE_LOADING_MORE:
                bindLoadingViewHolder((LoadingMoreHolder) holder);
                break;
        }
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        if (holder instanceof BoardgameHolder) {
            // reset the badge & ripple which are dynamically determined
            BoardgameHolder bgHolder = (BoardgameHolder) holder;
            bgHolder.image.showBadge(false);
            bgHolder.image.setForeground(
                    ContextCompat.getDrawable(host, R.drawable.mid_grey_ripple));
        }
    }

    @NonNull
    private BoardgameHolder createBoardgameHolder(ViewGroup parent) {
        final BoardgameHolder holder = new BoardgameHolder(
                layoutInflater.inflate(R.layout.boardgame_item, parent, false));
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.itemView.setTransitionName(holder.itemView.getResources().getString(R
                        .string.transition_game));
                holder.itemView.setBackgroundColor(
                        ContextCompat.getColor(host, R.color.background_light));
                Intent intent = new Intent();
                intent.setClass(host, BoardgameDetails.class);
                intent.putExtra(BoardgameDetails.EXTRA_BOARDGAME,
                        (Boardgame) getItem(holder.getAdapterPosition()));
                setGridItemContentTransitions(holder.itemView);
                ActivityOptions options =
                        ActivityOptions.makeSceneTransitionAnimation(host,
                                Pair.create(view, host.getString(R.string.transition_game)),
                                Pair.create(view, host.getString(R.string
                                        .transition_game_background)));
                host.startActivity(intent, options.toBundle());
            }
        });

        // show deletion confirmation
        holder.image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle(host.getString(R.string.remove_game_title))
                        .setMessage(host.getString(R.string.remove_game_message))
                        .setPositiveButton(host.getString(R.string.dialog_remove),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        BoardgameDbHelper bgHelper = new BoardgameDbHelper(context);
                                        bgHelper.delete((Boardgame) getItem(holder.getAdapterPosition()));

                                        items.remove(getItem(holder.getAdapterPosition()));

                                        notifyDataSetChanged();
                                    }
                        })
                        .setNegativeButton(host.getString(R.string.dialog_cancel), null)
                        .show();

                return false;
            }
        });

        return holder;
    }

    private void bindBoardgameHolder(final Boardgame game, final BoardgameHolder holder) {
        final int[] imageSize = {400, 400};
        Glide.with(host)
                .load("http:" + game.image)
                .listener(new RequestListener<String, GlideDrawable>() {

                    @Override
                    public boolean onResourceReady(GlideDrawable resource,
                                                   String model,
                                                   Target<GlideDrawable> target,
                                                   boolean isFromMemoryCache,
                                                   boolean isFirstResource) {
                        if (!game.hasFadedIn) {
                            holder.image.setHasTransientState(true);
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
                                    if (holder.image.getDrawable() != null) {
                                        holder.image.getDrawable().setColorFilter(
                                                new ColorMatrixColorFilter(cm));
                                    }
                                }
                            });
                            saturation.setDuration(1000);
                            saturation.setInterpolator(AnimationUtils.loadInterpolator(host,
                                    android.R.interpolator.fast_out_slow_in));
                            saturation.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    holder.image.setHasTransientState(false);
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
                .placeholder(shotLoadingPlaceholders[holder.getAdapterPosition() %
                        shotLoadingPlaceholders.length])
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .centerCrop()
                .override(imageSize[0], imageSize[1])
                .into(new BoardgameTarget(holder.image, false));

    }

    @NonNull
    private SearchResultHolder createSearchResultHolder(ViewGroup parent) {
        final SearchResultHolder holder = new SearchResultHolder(
                layoutInflater.inflate(R.layout.search_result_item, parent, false));

        // TODO: Handle onClick
        return holder;
    }

    private void bindSearchResultHolder(final Boardgame game, final SearchResultHolder holder) {
        holder.title.setText(game.title);
    }

    private void bindLoadingViewHolder(LoadingMoreHolder holder) {
        // only show the infinite load progress spinner if there are already items in the
        // grid i.e. it's not the first item & data is being loaded
        holder.progress.setVisibility((holder.getAdapterPosition() > 0
                && dataLoading.isDataLoading()) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public int getItemViewType(int position) {
        if (position < getDataItemCount()
                && getDataItemCount() > 0) {
            Boardgame item = getItem(position);
            if (item instanceof Boardgame) {
                return item.image != null ? TYPE_BOARDGAME : TYPE_SEARCH_RESULT;
            }
        }
        return TYPE_LOADING_MORE;
    }

    private Boardgame getItem(int position) {
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

    private void add(Boardgame item) {
        items.add(item);
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public void addAndResort(Collection<? extends Boardgame> newItems) {
        // de-dupe results
        for (Boardgame newItem : newItems) {
            boolean add = true;
            int count = getDataItemCount();
            for (int i = 0; i < count; i++) {
                Boardgame existingItem = getItem(i);
                if (existingItem.equals(newItem)) {
                    // if we find a dupe mark the weight boost field on the first-in, but don't add
                    // the dupe. We use the fact that an item comes from multiple sources to indicate it
                    // is more important and sort it higher
//                    existingItem.weightBoost = DUPE_WEIGHT_BOOST;
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
            Boardgame item = getItem(i);
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
//            Boardgame item = getItem(i);
//            if (item instanceof Shot) {
//                maxDribbleLikes = Math.max(((Shot) item).likes_count, maxDribbleLikes);
//            }
//        }
//
//        // now go through and set the weight of each item
//        for (int i = 0; i < count; i++) {
//            Boardgame item = getItem(i);
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
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(BoardgameComparator.SORT_PREF, Context.MODE_PRIVATE);
        comparator.setSortMode(prefs.getInt(BoardgameComparator.KEY_SORT_ORDER, 0));

        Collections.sort(items, comparator);
        notifyDataSetChanged(); // TODO call the more specific RV variants
    }

//    public void removeDataSource(String dataSource) {
//        int i = items.size() - 1;
//        while (i >= 0) {
//            Boardgame item = items.get(i);
//            if (dataSource.equals(item.dataSource)) {
//                items.remove(i);
//            }
//            i--;
//        }
//        notifyDataSetChanged();
//    }

    @Override
    public long getItemId(int position) {
        if (getItemViewType(position) == TYPE_LOADING_MORE) {
            return -1L;
        }
        return getItem(position).id;
    }

    @Override
    public int getItemCount() {
        return getDataItemCount() + 1; // include loading footer
    }

    /**
     * The shared element transition to dribbble shots & dn stories can intersect with the FAB.
     * This can cause a strange layers-passing-through-each-other effect, especially on return.
     * In this situation, hide the FAB on exit and re-show it on return.
     */
    private void setGridItemContentTransitions(View gridItem) {
        if (host.findViewById(R.id.fab) == null) return;
        if (!ViewUtils.viewsIntersect(gridItem, host.findViewById(R.id.fab))) return;

        final TransitionInflater ti = TransitionInflater.from(host);
        host.getWindow().setExitTransition(
                ti.inflateTransition(R.transition.home_content_item_exit));
        final Transition reenter = ti.inflateTransition(R.transition.home_content_item_reenter);
        // we only want this content transition in certain cases so clear it out after it's done.
        reenter.addListener(new AnimUtils.TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                host.getWindow().setExitTransition(null);
                host.getWindow().setReenterTransition(null);
            }
        });
        host.getWindow().setReenterTransition(reenter);
    }

    public int getDataItemCount() {
        return items.size();
    }

    @Override
    public void dataStartedLoading() {
        notifyItemChanged(getItemCount());
    }

    @Override
    public void dataFinishedLoading() {
        notifyItemChanged(getItemCount());
    }

    /* package */ class BoardgameHolder extends RecyclerView.ViewHolder {

        BadgedFourFourImageView image;

        public BoardgameHolder(View itemView) {
            super(itemView);
            image = (BadgedFourFourImageView) itemView;
        }

    }

    /* package */ class SearchResultHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.search_result) TextView title;

        public SearchResultHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    /* package */ class LoadingMoreHolder extends RecyclerView.ViewHolder {

        ProgressBar progress;

        public LoadingMoreHolder(View itemView) {
            super(itemView);
            progress = (ProgressBar) itemView;
        }

    }

}