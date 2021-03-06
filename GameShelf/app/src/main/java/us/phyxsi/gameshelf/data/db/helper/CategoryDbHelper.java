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
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import us.phyxsi.gameshelf.data.api.bgg.model.Boardgame;
import us.phyxsi.gameshelf.data.api.bgg.model.Category;
import us.phyxsi.gameshelf.data.db.GameShelfContract;

/**
 * Database helper methods for the Category table
 */
public class CategoryDbHelper {
    private static GameShelfDbHelper mDbHelper;

    public CategoryDbHelper(Context context) {
        mDbHelper = new GameShelfDbHelper(context);
    }

    public long insert(Category category) {
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(GameShelfContract.CategoryEntry.COLUMN_NAME_CATEGORY_ID, category.id);
        values.put(GameShelfContract.CategoryEntry.COLUMN_NAME_NAME, category.name);

        // Insert the new row, returning the primary key value of the new row
        long newRowId  = db.insert(
                GameShelfContract.CategoryEntry.TABLE_NAME,
                null,
                values);

        return newRowId;
    }

    public Cursor getAll() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                GameShelfContract.CategoryEntry.COLUMN_NAME_CATEGORY_ID,
                GameShelfContract.CategoryEntry.COLUMN_NAME_NAME
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                GameShelfContract.CategoryEntry._ID + " DESC";

        Cursor c = db.query(
                GameShelfContract.CategoryEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        return c;
    }

    public Cursor getByBoardgame(Boardgame boardgame) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                GameShelfContract.BoardgamesCategoriesEntry.COLUMN_NAME_CATEGORY_ID
        };

        String selection = GameShelfContract.BoardgamesCategoriesEntry.COLUMN_NAME_BOARDGAME_ID + " = " + boardgame.id;

        Cursor c = db.query(
                GameShelfContract.BoardgamesCategoriesEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // The sort order
        );

        List<Long> categoryIds = new ArrayList<Long>();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            categoryIds.add(c.getLong(c.getColumnIndexOrThrow(GameShelfContract.BoardgamesCategoriesEntry.COLUMN_NAME_CATEGORY_ID)));
        }

        c = db.rawQuery("SELECT * FROM " + GameShelfContract.CategoryEntry.TABLE_NAME + " WHERE _id IN (" +
                        TextUtils.join(", ", categoryIds) + ")", null);

        return c;
    }

    public Cursor getByTitle(String title) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                GameShelfContract.CategoryEntry._ID,
                GameShelfContract.CategoryEntry.COLUMN_NAME_CATEGORY_ID,
                GameShelfContract.CategoryEntry.COLUMN_NAME_NAME,
        };

        String selection = GameShelfContract.CategoryEntry.COLUMN_NAME_NAME + " = ?";
        String[] selectionArgs = { title };


        // How you want the results sorted in the resulting Cursor
        String sortOrder = null;

        Cursor c = db.query(
                GameShelfContract.CategoryEntry.TABLE_NAME,  // The table to query
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
