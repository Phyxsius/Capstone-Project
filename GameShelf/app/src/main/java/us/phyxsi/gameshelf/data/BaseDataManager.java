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

import java.util.List;

import retrofit.RestAdapter;
import us.phyxsi.gameshelf.data.api.bgg.BGGService;
import us.phyxsi.gameshelf.data.prefs.BGGPrefs;

/**
 * Base class for loading data.
 */
public abstract class BaseDataManager implements
        BGGPrefs.BGGLoginStatusListener {

    private BGGPrefs bggPrefs;
    private BGGService bggApi;

    public BaseDataManager(Context context) {
        // setup the API access objects
        bggPrefs = BGGPrefs.get(context);
        createBGGApi();
    }

    public abstract void onDataLoaded(List<? extends GameShelfItem> data);

    protected static void setPage(List<? extends GameShelfItem> items, int page) {
        for (GameShelfItem item : items) {
            item.page = page;
        }
    }

    protected static void setDataSource(List<? extends GameShelfItem> items, String dataSource) {
        for (GameShelfItem item : items) {
            item.dataSource = dataSource;
        }
    }

    private void createBGGApi() {
        bggApi = new RestAdapter.Builder()
                .setEndpoint(BGGService.ENDPOINT)
                .build()
                .create(BGGService.class);
    }

    public BGGService getBggApi() {
        return bggApi;
    }

    public BGGPrefs getBggPrefs() {
        return bggPrefs;
    }

    @Override
    public void onBGGLogin() {
        createBGGApi();
    }

    @Override
    public void onBGGLogout() {
        createBGGApi();
    }

}
