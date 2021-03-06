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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import us.phyxsi.gameshelf.data.db.GameShelfContract.BoardgameEntry;
import us.phyxsi.gameshelf.data.db.GameShelfContract.BoardgamesCategoriesEntry;
import us.phyxsi.gameshelf.data.db.GameShelfContract.CategoryEntry;

/**
 * Database helper methods for the Game Shelf database
 */
public class GameShelfDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "GameShelf.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INT";
    private static final String COMMA_SEP = ",";

    static final String SQL_CREATE_BOARDGAMES =
            "CREATE VIRTUAL TABLE " + BoardgameEntry.TABLE_NAME + " USING fts3 ( " +
                    BoardgameEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    BoardgameEntry.COLUMN_NAME_GAME_ID + TEXT_TYPE + " UNIQUE " + COMMA_SEP +
                    BoardgameEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    BoardgameEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    BoardgameEntry.COLUMN_NAME_IMAGE + TEXT_TYPE + COMMA_SEP +
                    BoardgameEntry.COLUMN_NAME_MAX_PLAYERS + INT_TYPE + COMMA_SEP +
                    BoardgameEntry.COLUMN_NAME_MAX_PLAYTIME + INT_TYPE + COMMA_SEP +
                    BoardgameEntry.COLUMN_NAME_MIN_AGE + INT_TYPE + COMMA_SEP +
                    BoardgameEntry.COLUMN_NAME_MIN_PLAYERS + INT_TYPE + COMMA_SEP +
                    BoardgameEntry.COLUMN_NAME_MIN_PLAYTIME + INT_TYPE + COMMA_SEP +
                    BoardgameEntry.COLUMN_NAME_SUGGESTED_NUMPLAYERS + INT_TYPE + COMMA_SEP +
                    BoardgameEntry.COLUMN_NAME_PUBLISHER + TEXT_TYPE + COMMA_SEP +
                    BoardgameEntry.COLUMN_NAME_YEAR_PUBLISHED + TEXT_TYPE + COMMA_SEP +
                    BoardgameEntry.COLUMN_NAME_CREATED_AT + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_BOARDGAMES =
            "DROP TABLE IF EXISTS " + BoardgameEntry.TABLE_NAME;

    static final String SQL_CREATE_CATEGORIES =
            "CREATE TABLE " + CategoryEntry.TABLE_NAME + " ( " +
                    CategoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    CategoryEntry.COLUMN_NAME_CATEGORY_ID + TEXT_TYPE + " UNIQUE " + COMMA_SEP +
                    CategoryEntry.COLUMN_NAME_NAME + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_CATEGORIES =
            "DROP TABLE IF EXISTS " + CategoryEntry.TABLE_NAME;

    static final String SQL_CREATE_BOARDGAMES_CATEGORIES =
            "CREATE TABLE " + BoardgamesCategoriesEntry.TABLE_NAME + " ( " +
                    BoardgamesCategoriesEntry.COLUMN_NAME_BOARDGAME_ID + INT_TYPE +
                    " REFERENCES " + BoardgameEntry.TABLE_NAME + " (" + BoardgameEntry._ID + ") " +
                    COMMA_SEP +
                    BoardgamesCategoriesEntry.COLUMN_NAME_CATEGORY_ID + INT_TYPE +
                    " REFERENCES " + CategoryEntry.TABLE_NAME + " (" + CategoryEntry._ID + ") " +
                    COMMA_SEP +
                    " PRIMARY KEY (" + BoardgamesCategoriesEntry.COLUMN_NAME_BOARDGAME_ID + COMMA_SEP +
                    BoardgamesCategoriesEntry.COLUMN_NAME_CATEGORY_ID + ")" +
                    " )";

    private static final String SQL_DELETE_BOARDGAMES_CATEGORIES =
            "DROP TABLE IF EXISTS " + BoardgamesCategoriesEntry.TABLE_NAME;

    public GameShelfDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_BOARDGAMES);
        db.execSQL(SQL_CREATE_CATEGORIES);
        db.execSQL(SQL_CREATE_BOARDGAMES_CATEGORIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_BOARDGAMES);
        db.execSQL(SQL_DELETE_CATEGORIES);
        db.execSQL(SQL_DELETE_BOARDGAMES_CATEGORIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
