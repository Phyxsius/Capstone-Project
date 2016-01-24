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
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import us.phyxsi.gameshelf.data.api.bgg.BGGService;
import us.phyxsi.gameshelf.data.api.bgg.model.Boardgame;
import us.phyxsi.gameshelf.data.api.bgg.model.BoardgamesResponse;
import us.phyxsi.gameshelf.data.api.bgg.model.CollectionItem;
import us.phyxsi.gameshelf.data.api.bgg.model.CollectionItems;
import us.phyxsi.gameshelf.data.db.helper.BoardgameDbHelper;

/**
 * Responsible for loading data from the various sources. Instantiating classes are responsible for
 * providing the {code onDataLoaded} method to do something with the data.
 */
public abstract class DataManager extends BaseDataManager {

//    private final FilterAdapter filterAdapter;
    private Context context;

    public DataManager(Context context) {
        super(context);

        this.context = context;
        setupPageIndexes();
    }

    public void loadFromDatabase() {
        BoardgameDbHelper bgHelper = new BoardgameDbHelper(context);
        Cursor bgCursor = bgHelper.getAll("");
        List<Boardgame> boardgameList = new ArrayList<Boardgame>();

        loadStarted();
        for (bgCursor.moveToFirst(); !bgCursor.isAfterLast(); bgCursor.moveToNext()) {
            Boardgame bg = new Boardgame(bgCursor, context);
            boardgameList.add(bg);
        }
        loadFinished();

        onDataLoaded(boardgameList);
    }

    public void loadCollectionFromBGG(CollectionItems collection) {
        final BoardgameDbHelper bgHelper = new BoardgameDbHelper(context);
        final BGGService bggApi = getBggApi();
        final List<Boardgame> boardgameList = new ArrayList<Boardgame>();

        loadStarted();
        for (final CollectionItem item : collection.getItemList()) {
            bggApi.getBoardgame(Long.parseLong(item.getObjectid()), new Callback<BoardgamesResponse>() {
                @Override
                public void success(BoardgamesResponse boardgamesResponse, Response response) {
                    if (boardgamesResponse != null) {
                        bgHelper.insert(boardgamesResponse.boardgames.get(0));
                        Cursor bgCursor = bgHelper.get(item.getObjectid());

                        for (bgCursor.moveToFirst(); !bgCursor.isAfterLast(); bgCursor.moveToNext()) {
                            Boardgame bg = new Boardgame(bgCursor, context);

                            boardgameList.add(bg);

                            onDataLoaded(boardgameList);
                        }
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e("error", error.getMessage());
                }
            });
        }
        loadFinished();
    }

    private void setupPageIndexes() {
//        List<Source> dateSources = filterAdapter.getFilters();
//        pageIndexes = new HashMap<>(dateSources.size());
//        for (Source source : dateSources) {
//            pageIndexes.put(source.key, 0);
//        }
    }

    @Override
    public void onBGGLogin(CollectionItems collection) {
        super.onBGGLogin(collection);

        loadCollectionFromBGG(collection);
    }

    @Override
    public void onBGGLogout() {
        super.onBGGLogout();
    }

    public interface DataUpdatedListener {

        public void onDataAdded();

        public void onDataRemoved();
    }
}
