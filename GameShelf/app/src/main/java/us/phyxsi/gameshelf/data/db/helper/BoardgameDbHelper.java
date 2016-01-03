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

package us.phyxsi.gameshelf.data.db.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import us.phyxsi.gameshelf.data.api.bgg.model.Boardgame;
import us.phyxsi.gameshelf.data.db.GameShelfContract;

/**
 * Helper methods for the Boardgames table
 */
public class BoardgameDbHelper {

    public long insert(Context context, Boardgame boardgame) {
        GameShelfDbHelper mDbHelper = new GameShelfDbHelper(context);
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_GAME_ID, boardgame.id);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_TITLE, boardgame.title);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_DESCRIPTION, boardgame.description);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_MAX_PLAYERS, boardgame.maxPlayers);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_MAX_PLAYTIME, boardgame.maxPlaytime);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_MIN_AGE, boardgame.minAge);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_MIN_PLAYERS, boardgame.minPlayers);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_MIN_PLAYTIME, boardgame.minPlaytime);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_SUGGESTED_NUMPLAYERS, boardgame.suggestedNumplayers);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_YEAR_PUBLISHED, boardgame.yearPublished);

        // Insert the new row, returning the primary key value of the new row
        long newRowId  = db.insert(
                GameShelfContract.BoardgameEntry.TABLE_NAME,
                null,
                values);

        return newRowId;
    }

}
