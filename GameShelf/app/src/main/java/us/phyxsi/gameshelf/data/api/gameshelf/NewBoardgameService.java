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

package us.phyxsi.gameshelf.data.api.gameshelf;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

/**
 * An intent service which adds a new boardgame to the database. Invokers can listen for results by
 * setting the {@link #EXTRA_BOARDGAME_RESULT} flag as {@code true} in the launching intent and
 * then using a {@link LocalBroadcastManager} to listen for {@link #BROADCAST_ACTION_SUCCESS} and
 * {@link #BROADCAST_ACTION_FAILURE} broadcasts;
 */

public class NewBoardgameService extends IntentService {

    public static final String ACTION_ADD_NEW_BOARDGAME = "ACTION_ADD_NEW_BOARDGAME";
    public static final String EXTRA_BOARDGAME_TITLE = "EXTRA_BOARDGAME_TITLE";
    public static final String EXTRA_BOARDGAME_RESULT = "EXTRA_BOARDGAME_RESULT";
    public static final String EXTRA_NEW_BOARDGAME = "EXTRA_NEW_BOARDGAME";
    public static final String BROADCAST_ACTION_SUCCESS = "BROADCAST_ACTION_SUCCESS";
    public static final String BROADCAST_ACTION_FAILURE = "BROADCAST_ACTION_FAILURE";

    public NewBoardgameService() { super("NewBoardgameService"); }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) return;

        if (ACTION_ADD_NEW_BOARDGAME.equals(intent.getAction())) {
            final boolean broadcastResult = intent.getBooleanExtra(EXTRA_BOARDGAME_RESULT, false);

            String title = intent.getStringExtra(EXTRA_BOARDGAME_TITLE);

            if (TextUtils.isEmpty(title)) return;

        }
    }
}
