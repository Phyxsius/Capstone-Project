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

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowInsets;
import android.view.animation.AnimationUtils;
import android.widget.ActionMenuView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.util.List;

import butterknife.Bind;
import butterknife.BindInt;
import butterknife.ButterKnife;
import butterknife.OnClick;
import us.phyxsi.gameshelf.R;
import us.phyxsi.gameshelf.data.BoardgameComparator;
import us.phyxsi.gameshelf.data.DataManager;
import us.phyxsi.gameshelf.data.api.bgg.model.Boardgame;
import us.phyxsi.gameshelf.data.api.gameshelf.NewBoardgameService;
import us.phyxsi.gameshelf.data.prefs.BGGPrefs;
import us.phyxsi.gameshelf.ui.transitions.FabDialogMorphSetup;
import us.phyxsi.gameshelf.util.ViewUtils;

public class HomeActivity extends AppCompatActivity {

    private static final int RC_SEARCH = 0;
    private static final int RC_ADD_NEW_BOARDGAME = 1;
    private static final int RC_IMPORT = 2;

    @Bind(R.id.drawer) DrawerLayout drawer;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.games_grid) RecyclerView grid;
    @Bind(R.id.fab) ImageButton fab;
    @Bind(android.R.id.empty) ProgressBar loading;
    private TextView noResultsEmptyText;
    private GridLayoutManager layoutManager;
    @BindInt(R.integer.num_columns) int columns;

    // data
    private DataManager dataManager;
    private FeedAdapter adapter;
    private BGGPrefs bggPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        drawer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        setActionBar(toolbar);
        if (savedInstanceState == null) {
            animateToolbar();
        }

        bggPrefs = BGGPrefs.get(this);
        dataManager = new DataManager(this) {
            @Override
            public void onDataLoaded(List<? extends Boardgame> data) {
                adapter.addAndResort(data);
                checkEmptyState();
            }
        };
        adapter = new FeedAdapter(this, HomeActivity.this, dataManager, columns);
        grid.setAdapter(adapter);
        layoutManager = new GridLayoutManager(this, columns);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getItemColumnSpan(position);
            }
        });
        grid.setLayoutManager(layoutManager);
        grid.addOnScrollListener(gridScroll);
//        grid.addOnScrollListener(new InfiniteScrollListener(layoutManager) {
//            @Override
//            public void onLoadMore() {
//                // TODO: Load more items
//            }
//        });
        grid.setHasFixedSize(true);

        // drawer layout treats fitsSystemWindows specially so we have to handle insets ourselves
        drawer.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                // inset the toolbar down by the status bar height
                ViewGroup.MarginLayoutParams lpToolbar = (ViewGroup.MarginLayoutParams) toolbar
                        .getLayoutParams();
                lpToolbar.topMargin += insets.getSystemWindowInsetTop();
                lpToolbar.rightMargin += insets.getSystemWindowInsetRight();
                toolbar.setLayoutParams(lpToolbar);

                // inset the grid top by statusbar+toolbar & the bottom by the navbar (don't clip)
                grid.setPadding(grid.getPaddingLeft(),
                        insets.getSystemWindowInsetTop() + ViewUtils.getActionBarSize
                                (HomeActivity.this),
                        grid.getPaddingRight() + insets.getSystemWindowInsetRight(), // landscape
                        grid.getPaddingBottom());

                // inset the fab for the navbar
                ViewGroup.MarginLayoutParams lpFab = (ViewGroup.MarginLayoutParams) fab
                        .getLayoutParams();
                lpFab.bottomMargin += insets.getSystemWindowInsetBottom(); // portrait
                lpFab.rightMargin += insets.getSystemWindowInsetRight(); // landscape
                fab.setLayoutParams(lpFab);

                // we place a background behind the status bar to combine with it's semi-transparent
                // color to get the desired appearance.  Set it's height to the status bar height
                View statusBarBackground = findViewById(R.id.status_bar_background);
                FrameLayout.LayoutParams lpStatus = (FrameLayout.LayoutParams)
                        statusBarBackground.getLayoutParams();
                lpStatus.height = insets.getSystemWindowInsetTop();
                statusBarBackground.setLayoutParams(lpStatus);

                // clear this listener so insets aren't re-applied
                drawer.setOnApplyWindowInsetsListener(null);

                return insets.consumeSystemWindowInsets();
            }
        });
        setupTaskDescription();

        dataManager.loadFromDatabase();

        checkEmptyState();
        checkConnectivity();
    }

    private int gridScrollY = 0;
    private RecyclerView.OnScrollListener gridScroll = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            gridScrollY += dy;
            if (gridScrollY > 0 && toolbar.getTranslationZ() != -1f) {
                toolbar.setTranslationZ(-1f);
            } else if (gridScrollY == 0 && toolbar.getTranslationZ() != 0) {
                toolbar.setTranslationZ(0f);
            }
        }
    };

    @OnClick(R.id.fab)
    protected void fabClick() {
        Intent intent = new Intent(this, AddNewBoardgame.class);
        intent.putExtra(FabDialogMorphSetup.EXTRA_SHARED_ELEMENT_START_COLOR,
                ContextCompat.getColor(this, R.color.accent));
        intent.putExtra(NewBoardgameService.EXTRA_BOARDGAME_RESULT, true);
//        registerPostStoryResultListener();
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, fab,
                getString(R.string.transition_add_new_boardgame));
        startActivityForResult(intent, RC_ADD_NEW_BOARDGAME, options.toBundle());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem bggLogin = menu.findItem(R.id.menu_import);
        if (bggLogin != null) {
            bggLogin.setTitle(bggPrefs.isLoggedIn() ? R.string
                    .bgg_log_out : R.string.bgg_login);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort:
                final SharedPreferences prefs = getApplicationContext()
                        .getSharedPreferences(BoardgameComparator.SORT_PREF, Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor = prefs.edit();


                new AlertDialog.Builder(HomeActivity.this)
                        .setTitle(getString(R.string.sort_title))
                        .setSingleChoiceItems(R.array.sort_options,
                                prefs.getInt(BoardgameComparator.KEY_SORT_ORDER, 0),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        editor.putInt(BoardgameComparator.KEY_SORT_ORDER, which);
                                        editor.commit();
                                    }
                                })
                        .setPositiveButton(getString(R.string.dialog_sort),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        adapter.sort();
                                    }
                                })
                        .setNegativeButton(getString(R.string.dialog_cancel), null)
                        .show();

                return true;
            case R.id.menu_search:
                // get the icon's location on screen to pass through to the search screen
                View searchMenuView = toolbar.findViewById(R.id.menu_search);
                int[] loc = new int[2];
                searchMenuView.getLocationOnScreen(loc);
                startActivityForResult(SearchActivity.createStartIntent(this, loc[0], loc[0] +
                        (searchMenuView.getWidth() / 2)), RC_SEARCH, ActivityOptions
                        .makeSceneTransitionAnimation(this).toBundle());
                searchMenuView.setAlpha(0f);
                return true;
            case R.id.menu_import:
                if (!bggPrefs.isLoggedIn()) {
                    startActivityForResult(new Intent(this, BGGLogin.class), RC_IMPORT);

                } else {
                    bggPrefs.logout();
                    // TODO something better than a toast!!
                    Toast.makeText(getApplicationContext(), R.string.bgg_logged_out,
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.menu_about:
                startActivity(new Intent(HomeActivity.this, AboutActivity.class),
                        ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_SEARCH:
                // reset the search icon which we hid
                View searchMenuView = toolbar.findViewById(R.id.menu_search);
                if (searchMenuView != null) {
                    searchMenuView.setAlpha(1f);
                }
                break;
            case RC_ADD_NEW_BOARDGAME:
                if (resultCode == AddNewBoardgame.RESULT_BOARDGAME_ADDED) {
                    dataManager.loadFromDatabase();
                }
                break;
            case RC_IMPORT:
                break;
        }
    }


    private void checkEmptyState() {
        if (adapter.getDataItemCount() == 0) {
            loading.setVisibility(View.GONE);
            setNoResultsEmptyTextVisibility(View.VISIBLE);

            // ensure grid scroll tracking/toolbar z-order is reset
            gridScrollY = 0;
            toolbar.setTranslationZ(0f);
        } else {
            loading.setVisibility(View.GONE);
            setNoResultsEmptyTextVisibility(View.GONE);
        }
    }

    private void setNoResultsEmptyTextVisibility(int visibility) {
        if (visibility == View.VISIBLE) {
            if (noResultsEmptyText == null) {
                // create the no results empty text
                ViewStub stub = (ViewStub) findViewById(R.id.stub_no_results);
                noResultsEmptyText = (TextView) stub.inflate();
                String emptyText = getString(R.string.no_results_found);
                int addPlaceholderStart = emptyText.indexOf('\u08B4');
                int altMethodStart = addPlaceholderStart + 3;
                SpannableStringBuilder ssb = new SpannableStringBuilder(emptyText);
                // show an image of the add icon
                ssb.setSpan(new ImageSpan(this, R.drawable.ic_add_small,
                                ImageSpan.ALIGN_BASELINE),
                        addPlaceholderStart,
                        addPlaceholderStart + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                // make the alt method (swipe from right) less prominent and italic
                ssb.setSpan(new ForegroundColorSpan(
                                ContextCompat.getColor(this, R.color.text_secondary_light)),
                        altMethodStart,
                        emptyText.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new StyleSpan(Typeface.ITALIC),
                        altMethodStart,
                        emptyText.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                noResultsEmptyText.setText(ssb);
            }

            noResultsEmptyText.setVisibility(visibility);
        } else if (noResultsEmptyText != null) {
            noResultsEmptyText.setVisibility(visibility);
        }
    }

    private void checkConnectivity() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean connected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        if (!connected) {
            loading.setVisibility(View.GONE);
            ViewStub stub = (ViewStub) findViewById(R.id.stub_no_connection);
            ImageView iv = (ImageView) stub.inflate();
            final AnimatedVectorDrawable avd =
                    (AnimatedVectorDrawable) getDrawable(R.drawable.avd_no_connection);
            iv.setImageDrawable(avd);
            avd.start();
        }
    }

    private void setupTaskDescription() {
        // set a silhouette icon in overview as the launcher icon is a bit busy
        // and looks bad on top of colorPrimary
        //Bitmap overviewIcon = ImageUtils.vectorToBitmap(this, R.drawable.ic_launcher_silhouette);
        // TODO replace launcher icon with a monochrome version from RN.
        Bitmap overviewIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name),
                overviewIcon,
                ContextCompat.getColor(this, R.color.primary)));
        overviewIcon.recycle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bggPrefs.addLoginStatusListener(dataManager);
    }

    @Override
    protected void onPause() {
        bggPrefs.removeLoginStatusListener(dataManager);
        super.onPause();
    }

    private void animateToolbar() {
        // this is gross but toolbar doesn't expose it's children to animate them :(
        View t = toolbar.getChildAt(0);
        if (t != null && t instanceof TextView) {
            TextView title = (TextView) t;

            // fade in and space out the title.  Animating the letterSpacing performs horribly so
            // fake it by setting the desired letterSpacing then animating the scaleX ¯\_(ツ)_/¯
            title.setAlpha(0f);
            title.setScaleX(0.8f);

            title.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .setStartDelay(300)
                    .setDuration(900)
                    .setInterpolator(AnimationUtils.loadInterpolator(this,
                            android.R.interpolator.fast_out_slow_in));
        }
        View amv = toolbar.getChildAt(1);
        if (amv != null & amv instanceof ActionMenuView) {
            ActionMenuView actions = (ActionMenuView) amv;
            popAnim(actions.getChildAt(0), 500, 200); // filter
            popAnim(actions.getChildAt(1), 700, 200); // overflow
        }
    }

    private void popAnim(View v, int startDelay, int duration) {
        if (v != null) {
            v.setAlpha(0f);
            v.setScaleX(0f);
            v.setScaleY(0f);

            v.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(startDelay)
                    .setDuration(duration)
                    .setInterpolator(AnimationUtils.loadInterpolator(this, android.R.interpolator
                            .overshoot));
        }
    }
}
