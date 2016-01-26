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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import us.phyxsi.gameshelf.data.api.bgg.model.Boardgame;
import us.phyxsi.gameshelf.data.api.bgg.model.Category;
import us.phyxsi.gameshelf.data.db.GameShelfContract;

/**
 * Helper methods for the Boardgames table
 */
public class BoardgameDbHelper extends ContentProvider {

    private static GameShelfDbHelper mDbHelper;
    private static Context context;

    static final int BOARDGAMES = 1;
    static final int BOARDGAME_ID = 2;

    static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(GameShelfContract.AUTHORITY, "boardgames", BOARDGAMES);
        uriMatcher.addURI(GameShelfContract.AUTHORITY, "boardgames/#", BOARDGAME_ID);
    }

    public BoardgameDbHelper() {}

    public BoardgameDbHelper(Context context) {
        this.context = context;
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
                values
        );

        db.close();

        // Insert categories and tie to joining table
        CategoryDbHelper categoryDbHelper = new CategoryDbHelper(context);
        for (Category category : boardgame.categories) {
            long catId = categoryDbHelper.insert(category);

            if (catId == -1) {
                Cursor catCursor = categoryDbHelper.getByTitle(category.name);

                for (catCursor.moveToFirst(); !catCursor.isAfterLast(); catCursor.moveToNext()) {
                    catId = catCursor.getLong(catCursor.getColumnIndex(
                            GameShelfContract.CategoryEntry._ID));
                }
            }

            insertCategory(boardgame.id, catId);
        }

        return newRowId;
    }

    public long insertCategory(long boardgameId, long categoryId) {
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(GameShelfContract.BoardgamesCategoriesEntry.COLUMN_NAME_BOARDGAME_ID, boardgameId);
        values.put(GameShelfContract.BoardgamesCategoriesEntry.COLUMN_NAME_CATEGORY_ID, categoryId);

        long newRowId = db.insert(
                GameShelfContract.BoardgamesCategoriesEntry.TABLE_NAME,
                null,
                values
        );

        db.close();

        return newRowId;
    }

    public long delete(Boardgame boardgame) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String selection = GameShelfContract.BoardgameEntry.COLUMN_NAME_GAME_ID + " = " + boardgame.id;
        String[] selectionArgs = null;
//        String[] selectionArgs = { String.valueOf(boardgame.id) };

        long rowId = db.delete(
                GameShelfContract.BoardgameEntry.TABLE_NAME,
                selection,
                selectionArgs
        );

        return rowId;
    }

    public Cursor getAll(String limit) {
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
                GameShelfContract.BoardgameEntry.COLUMN_NAME_CREATED_AT + " DESC";

        Cursor c = db.query(
                GameShelfContract.BoardgameEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder,                                // The sort order
                limit                                     // The number to return
        );

        return c;
    }

    public Cursor get(String game_id) {
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

        String selection = GameShelfContract.BoardgameEntry.COLUMN_NAME_GAME_ID + " = " + game_id;
        String[] selectionArgs = null;


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

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            case BOARDGAMES:
                return GameShelfContract.BoardgameEntry.CONTENT_TYPE;
            case BOARDGAME_ID:
                return GameShelfContract.BoardgameEntry.CONTENT_ITEM_TYPE;
            default:
                return null;
        }
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new GameShelfDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(GameShelfContract.BoardgameEntry.TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case BOARDGAMES:
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = GameShelfContract.BoardgameEntry.SORT_ORDER_DEFAULT;
                }
                break;

            case BOARDGAME_ID:
                builder.appendWhere(GameShelfContract.BoardgameEntry._ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        values.put(GameShelfContract.BoardgameEntry.COLUMN_NAME_CREATED_AT, dateFormat.format(date));


        // Insert the new row, returning the primary key value of the new row
        long id  = db.insert(
                GameShelfContract.BoardgameEntry.TABLE_NAME,
                null,
                values
        );

        if (id > 0) {
            Uri itemUri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(itemUri, null);
            return itemUri;
        }

        throw new SQLException("Problem while inserting into uri: " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int deleteCount = 0;


        switch (uriMatcher.match(uri)) {
            case BOARDGAMES:
                deleteCount = db.delete(GameShelfContract.BoardgameEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case BOARDGAME_ID:
                String idStr = uri.getLastPathSegment();
                String where = GameShelfContract.BoardgameEntry._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }

                deleteCount = db.delete(GameShelfContract.BoardgameEntry.TABLE_NAME, where, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int updateCount = 0;


        switch (uriMatcher.match(uri)) {
            case BOARDGAMES:
                updateCount = db.update(GameShelfContract.BoardgameEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            case BOARDGAME_ID:
                String idStr = uri.getLastPathSegment();
                String where = GameShelfContract.BoardgameEntry._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }

                updateCount = db.update(GameShelfContract.BoardgameEntry.TABLE_NAME, values, where, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return updateCount;
    }
}
