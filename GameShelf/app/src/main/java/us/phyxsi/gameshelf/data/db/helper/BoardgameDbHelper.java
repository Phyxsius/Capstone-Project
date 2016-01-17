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

import java.text.SimpleDateFormat;
import java.util.Date;

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

    public long insert(Boardgame boardgame) {
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_GAME_ID, boardgame.id);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_TITLE, boardgame.getTitle());
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_DESCRIPTION, boardgame.description);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_IMAGE, boardgame.image);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_MAX_PLAYERS, boardgame.maxPlayers);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_MAX_PLAYTIME, boardgame.maxPlaytime);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_MIN_AGE, boardgame.minAge);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_MIN_PLAYERS, boardgame.minPlayers);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_MIN_PLAYTIME, boardgame.minPlaytime);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_SUGGESTED_NUMPLAYERS, boardgame.getSuggestedNumplayers());
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_PUBLISHER, boardgame.getPublisher());
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_YEAR_PUBLISHED, boardgame.yearPublished);
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_CREATED_AT, dateFormat.format(date));

        // Insert the new row, returning the primary key value of the new row
        long newRowId  = db.insert(
                GameShelfContract.BoardgameEntry.TABLE_NAME,
                null,
                values);

        db.close();

        return newRowId;
    }

    public long delete(Boardgame boardgame) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String selection = GameShelfContract.BoardgameEntry.COLUMN_NAME_GAME_ID + " = ?";
        String[] selectionArgs = { String.valueOf(boardgame.id) };

        long rowId = db.delete(
                GameShelfContract.BoardgameEntry.TABLE_NAME,
                selection,
                selectionArgs
        );

        db.close();

        return rowId;
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
                GameShelfContract.BoardgameEntry.COLUMN_NAME_CREATED_AT,
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

    public Cursor getByTitle(String title) {
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
                GameShelfContract.BoardgameEntry.COLUMN_NAME_CREATED_AT,
        };

        String selection = GameShelfContract.BoardgameEntry.TABLE_NAME + " MATCH ?";
        String[] selectionArgs = {
                GameShelfContract.BoardgameEntry.COLUMN_NAME_DESCRIPTION + ":" + title
        };


        // How you want the results sorted in the resulting Cursor
        String sortOrder = null;

        Cursor c = db.query(
                GameShelfContract.BoardgameEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        return c;
    }

}
