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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit.RestAdapter;
import retrofit.converter.SimpleXMLConverter;
import us.phyxsi.gameshelf.data.api.bgg.BGGService;
import us.phyxsi.gameshelf.data.api.bgg.model.Boardgame;
import us.phyxsi.gameshelf.data.prefs.BGGPrefs;

/**
 * Base class for loading data.
 */
public abstract class BaseDataManager implements
        DataLoadingSubject,
        BGGPrefs.BGGLoginStatusListener {

    private AtomicInteger loadingCount;
    private BGGPrefs bggPrefs;
    private BGGService bggApi;
    private List<DataLoadingSubject.DataLoadingCallbacks> loadingCallbacks;

    public BaseDataManager(Context context) {
        // setup the API access objects
        bggPrefs = BGGPrefs.get(context);
        createBGGApi();
        loadingCount = new AtomicInteger(0);
    }

    @Override
    public boolean isDataLoading() {
        return loadingCount.get() > 0;
    }

    protected void loadStarted() {
        if (0 == loadingCount.getAndIncrement()) {
            notifyCallbacksLoadingStarted();
        }
    }

    protected void resetLoadingCount() {
        loadingCount.set(0);
    }

    protected void loadFinished() {
        if (0 == loadingCount.decrementAndGet()) {
            notifyCallbacksLoadingFinished();
        }
    }

    @Override
    public void addCallbacks(DataLoadingSubject.DataLoadingCallbacks callbacks) {
        if (loadingCallbacks == null) {
            loadingCallbacks = new ArrayList<>(1);
        }
        loadingCallbacks.add(callbacks);
    }

    @Override
    public void removeCallbacks(DataLoadingSubject.DataLoadingCallbacks callbacks) {
        if (loadingCallbacks.contains(callbacks)) {
            loadingCallbacks.remove(callbacks);
        }
    }

    protected void notifyCallbacksLoadingStarted() {
        if (loadingCallbacks == null) return;
        for (DataLoadingCallbacks loadingCallback : loadingCallbacks) {
            loadingCallback.dataStartedLoading();
        }
    }

    protected void notifyCallbacksLoadingFinished() {
        if (loadingCallbacks == null) return;
        for (DataLoadingCallbacks loadingCallback : loadingCallbacks) {
            loadingCallback.dataFinishedLoading();
        }
    }

    public abstract void onDataLoaded(List<? extends Boardgame> data);

    private void createBGGApi() {
        bggApi = new RestAdapter.Builder()
                .setEndpoint(BGGService.ENDPOINT)
                .setConverter(new SimpleXMLConverter())
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
