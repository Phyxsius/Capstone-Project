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

package us.phyxsi.gameshelf.data;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import us.phyxsi.gameshelf.data.api.bgg.model.Boardgame;
import us.phyxsi.gameshelf.data.db.helper.BoardgameDbHelper;

/**
 * Responsible for loading data from the various sources. Instantiating classes are responsible for
 * providing the {code onDataLoaded} method to do something with the data.
 */
public abstract class DataManager extends BaseDataManager
        implements DataLoadingSubject {

//    private final FilterAdapter filterAdapter;
    private AtomicInteger loadingCount;
    private Map<String, Integer> pageIndexes;
    private Context context;

    public DataManager(Context context) {
        super(context);

        this.context = context;
        loadingCount = new AtomicInteger(0);
        setupPageIndexes();
    }

//    public void loadAllDataSources() {
//        for (Source filter : filterAdapter.getFilters()) {
//            loadSource(filter);
//        }
//    }

    public void loadFromDatabase() {
        loadingCount.incrementAndGet();

        BoardgameDbHelper bgHelper = new BoardgameDbHelper(context);
        Cursor bgCursor = bgHelper.getAll();
        List<Boardgame> boardgameList = new ArrayList<Boardgame>();

        for (bgCursor.moveToFirst(); !bgCursor.isAfterLast(); bgCursor.moveToNext()) {
            Boardgame bg = new Boardgame(bgCursor);
            boardgameList.add(bg);
        }

        onDataLoaded(boardgameList);
    }

    @Override
    public boolean isDataLoading() {
        return loadingCount.get() > 0;
    }

    private void loadSource() {
//        if (source.active) {
//            loadingCount.incrementAndGet();
//            int page = getNextPageIndex(source.key);
//            switch (source.key) {
//                case SourceManager.SOURCE_DESIGNER_NEWS_POPULAR:
//                    loadDesignerNewsTopStories(page);
//                    break;
//                case SourceManager.SOURCE_DESIGNER_NEWS_RECENT:
//                    loadDesignerNewsRecent(page);
//                    break;
//                case SourceManager.SOURCE_DRIBBBLE_POPULAR:
//                    loadDribbblePopular(page);
//                    break;
//                case SourceManager.SOURCE_DRIBBBLE_FOLLOWING:
//                    loadDribbbleFollowing(page);
//                    break;
//                case SourceManager.SOURCE_DRIBBBLE_USER_LIKES:
//                    loadDribbbleUserLikes(page);
//                    break;
//                case SourceManager.SOURCE_DRIBBBLE_USER_SHOTS:
//                    loadDribbbleUserShots(page);
//                    break;
//                case SourceManager.SOURCE_DRIBBBLE_RECENT:
//                    loadDribbbleRecent(page);
//                    break;
//                case SourceManager.SOURCE_DRIBBBLE_DEBUTS:
//                    loadDribbbleDebuts(page);
//                    break;
//                case SourceManager.SOURCE_DRIBBBLE_ANIMATED:
//                    loadDribbbleAnimated(page);
//                    break;
//                case SourceManager.SOURCE_PRODUCT_HUNT:
//                    loadProductHunt(page);
//                    break;
//                default:
//                    if (source instanceof Source.DribbbleSearchSource) {
//                        loadDribbbleSearch((Source.DribbbleSearchSource) source, page);
//                    } else if (source instanceof Source.DesignerNewsSearchSource) {
//                        loadDesignerNewsSearch((Source.DesignerNewsSearchSource) source, page);
//                    }
//                    break;
//            }
//        }
    }

    private void setupPageIndexes() {
//        List<Source> dateSources = filterAdapter.getFilters();
//        pageIndexes = new HashMap<>(dateSources.size());
//        for (Source source : dateSources) {
//            pageIndexes.put(source.key, 0);
//        }
    }

    private int getNextPageIndex(String dataSource) {
        int nextPage = 1; // default to one – i.e. for newly added sources
        if (pageIndexes.containsKey(dataSource)) {
            nextPage = pageIndexes.get(dataSource) + 1;
        }
        pageIndexes.put(dataSource, nextPage);
        return nextPage;
    }

    private boolean sourceIsEnabled(String key) {
        return pageIndexes.get(key) != 0;
    }

}
