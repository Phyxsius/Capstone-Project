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

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import us.phyxsi.gameshelf.data.api.bgg.model.Boardgame;
import us.phyxsi.gameshelf.data.api.bgg.model.BoardgamesResponse;
import us.phyxsi.gameshelf.data.db.helper.BoardgameDbHelper;

/**
 * Responsible for loading search results from Board Game Geek. Instantiating classes are
 * responsible for providing the {code onDataLoaded} method to do something with the data.
 */
public abstract class SearchDataManager extends BaseDataManager {

    // state
    private String query = "";
    private boolean loadingResults = false;

    public SearchDataManager(Context context) { super(context); }

    @Override
    public boolean isDataLoading() {
        return loadingResults;
    }

    public void searchFor(String query) {
        loadingResults = true;
        getBggApi().search(query, new Callback<BoardgamesResponse>() {
            @Override
            public void success(BoardgamesResponse boardgamesResponse, Response response) {
                if (boardgamesResponse != null) {
                    onDataLoaded(boardgamesResponse.boardgames);
                }
                loadingResults = false;
            }

            @Override
            public void failure(RetrofitError error) {
                loadingResults = false;
            }
        });

    }

    public void getByTitle(String title, Context context) {
        BoardgameDbHelper bgHelper = new BoardgameDbHelper(context);
        Cursor bgCursor = bgHelper.getByTitle(title);
        List<Boardgame> boardgameList = new ArrayList<Boardgame>();

        loadStarted();
        for (bgCursor.moveToFirst(); !bgCursor.isAfterLast(); bgCursor.moveToNext()) {
            Boardgame bg = new Boardgame(bgCursor);
            boardgameList.add(bg);
        }
        loadFinished();

        onDataLoaded(boardgameList);
    }

    public void getById(Long id) {
        loadingResults = true;
        getBggApi().getBoardgame(id, new Callback<BoardgamesResponse>() {
            @Override
            public void success(BoardgamesResponse boardgamesResponse, Response response) {
                if (boardgamesResponse != null) {
                    onDataLoaded(boardgamesResponse.boardgames);
                }
                loadingResults = false;
            }

            @Override
            public void failure(RetrofitError error) {
                loadingResults = false;
            }
        });
    }

    public void clear() {
        query = "";
        loadingResults = false;
    }

    public String getQuery() {
        return query;
    }
}
