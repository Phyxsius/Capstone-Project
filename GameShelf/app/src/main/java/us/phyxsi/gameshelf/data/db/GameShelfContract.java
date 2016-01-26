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

package us.phyxsi.gameshelf.data.db;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * SQLite contract for the Boardgame table
 */
public final class GameShelfContract {

    public static final String AUTHORITY = "us.phyxsi.gameshelf";
    public static final String URL = "content://" + AUTHORITY;
    public static final Uri CONTENT_URI = Uri.parse(URL);

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public GameShelfContract() {}

    /* Inner class that defines the table contents */
    public static final class BoardgameEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.parse(URL + "/boardgames");

        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE +
                        "/us.phyxsi.gameshelf_boardgames";
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE +
                        "/us.phyxsi.gameshelf_boardgames";

        public static final String TABLE_NAME = "boardgames";

        public static final String COLUMN_NAME_GAME_ID = "game_id";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_IMAGE = "image";
        public static final String COLUMN_NAME_MAX_PLAYERS = "max_players";
        public static final String COLUMN_NAME_MAX_PLAYTIME = "max_playtime";
        public static final String COLUMN_NAME_MIN_AGE = "min_age";
        public static final String COLUMN_NAME_MIN_PLAYERS = "min_players";
        public static final String COLUMN_NAME_MIN_PLAYTIME = "min_playtime";
        public static final String COLUMN_NAME_SUGGESTED_NUMPLAYERS = "suggested_numplayers";
        public static final String COLUMN_NAME_PUBLISHER = "publisher";
        public static final String COLUMN_NAME_YEAR_PUBLISHED = "year_published";
        public static final String COLUMN_NAME_CREATED_AT = "created_at";


        public static final String[] PROJECTION_ALL =
                {_ID, COLUMN_NAME_GAME_ID, COLUMN_NAME_TITLE, COLUMN_NAME_DESCRIPTION,
                COLUMN_NAME_IMAGE, COLUMN_NAME_MAX_PLAYERS, COLUMN_NAME_MAX_PLAYTIME,
                COLUMN_NAME_MIN_AGE, COLUMN_NAME_MIN_PLAYERS, COLUMN_NAME_MIN_PLAYTIME,
                COLUMN_NAME_SUGGESTED_NUMPLAYERS, COLUMN_NAME_PUBLISHER, COLUMN_NAME_YEAR_PUBLISHED,
                COLUMN_NAME_CREATED_AT};

        public static final String SORT_ORDER_DEFAULT = COLUMN_NAME_CREATED_AT + " DESC";
    }

    public static final class CategoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "categories";

        public static final String COLUMN_NAME_CATEGORY_ID = "category_id";
        public static final String COLUMN_NAME_NAME = "name";
    }

    public static final class BoardgamesCategoriesEntry implements BaseColumns {
        public static final String TABLE_NAME = "boardgames_categories";

        public static final String COLUMN_NAME_BOARDGAME_ID = "boardgame_id";
        public static final String COLUMN_NAME_CATEGORY_ID = "category_id";
    }

}
