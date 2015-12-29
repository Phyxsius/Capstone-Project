package us.phyxsi.gameshelf.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toolbar;

import butterknife.Bind;
import butterknife.BindInt;
import butterknife.ButterKnife;
import us.phyxsi.gameshelf.R;

public class HomeActivity extends Activity {

    @Bind(R.id.drawer) DrawerLayout drawer;
    @Bind(R.id.toolbar) Toolbar toolbar;
//    @Bind(R.id.games_grid) RecyclerView grid;
    @Bind(R.id.fab) ImageButton fab;
    @Bind(android.R.id.empty) ProgressBar loading;
    private TextView noFiltersEmptyText;
    private GridLayoutManager layoutManager;
    @BindInt(R.integer.num_columns) int columns;

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
//            animateToolbar();
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }
}
