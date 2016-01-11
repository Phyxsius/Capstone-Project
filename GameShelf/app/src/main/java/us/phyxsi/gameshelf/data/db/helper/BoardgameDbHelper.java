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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import us.phyxsi.gameshelf.data.api.bgg.model.Boardgame;
import us.phyxsi.gameshelf.data.db.GameShelfContract;

/**
 * Helper methods for the Boardgames table
 */
public class BoardgameDbHelper {
    private static GameShelfDbHelper mDbHelper;

    public BoardgameDbHelper(Context context) {
        mDbHelper = new GameShelfDbHelper(context);
    }

    public long insert(Context context, Boardgame boardgame) {
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_GAME_ID, boardgame.id);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_TITLE, boardgame.title);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_DESCRIPTION, boardgame.description);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_IMAGE, boardgame.image);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_MAX_PLAYERS, boardgame.maxPlayers);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_MAX_PLAYTIME, boardgame.maxPlaytime);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_MIN_AGE, boardgame.minAge);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_MIN_PLAYERS, boardgame.minPlayers);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_MIN_PLAYTIME, boardgame.minPlaytime);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_SUGGESTED_NUMPLAYERS, boardgame.suggestedNumplayers);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_PUBLISHER, boardgame.publisher);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_YEAR_PUBLISHED, boardgame.yearPublished);

        // Insert the new row, returning the primary key value of the new row
        long newRowId  = db.insert(
                GameShelfContract.BoardgameEntry.TABLE_NAME,
                null,
                values);

        return newRowId;
    }

    public Cursor getAll() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                GameShelfContract.BoardgameEntry._ID,
                GameShelfContract.BoardgameEntry.COLUMN_NAME_GAME_ID,
                GameShelfContract.BoardgameEntry.COLUMN_NAME_TITLE,
                GameShelfContract.BoardgameEntry.COLUMN_NAME_DESCRIPTION,
                GameShelfContract.BoardgameEntry.COLUMN_NAME_IMAGE,
                GameShelfContract.BoardgameEntry.COLUMN_NAME_MAX_PLAYERS,
                GameShelfContract.BoardgameEntry.COLUMN_NAME_MAX_PLAYTIME,
                GameShelfContract.BoardgameEntry.COLUMN_NAME_MIN_AGE,
                GameShelfContract.BoardgameEntry.COLUMN_NAME_MIN_PLAYERS,
                GameShelfContract.BoardgameEntry.COLUMN_NAME_MIN_PLAYTIME,
                GameShelfContract.BoardgameEntry.COLUMN_NAME_SUGGESTED_NUMPLAYERS,
                GameShelfContract.BoardgameEntry.COLUMN_NAME_PUBLISHER,
                GameShelfContract.BoardgameEntry.COLUMN_NAME_YEAR_PUBLISHED,
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                GameShelfContract.BoardgameEntry._ID + " DESC";

        Cursor c = db.query(
                GameShelfContract.BoardgameEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        return c;
    }

}
