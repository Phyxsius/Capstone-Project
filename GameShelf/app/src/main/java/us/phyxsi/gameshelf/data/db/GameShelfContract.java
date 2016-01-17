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

import android.provider.BaseColumns;

/**
 * SQLite contract for the Boardgame table
 */
public final class GameShelfContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public GameShelfContract() {}

    /* Inner class that defines the table contents */
    public static abstract class BoardgameEntry implements BaseColumns {
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
    }

    public static abstract class CategoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "categories";

        public static final String COLUMN_NAME_CATEGORY_ID = "category_id";
        public static final String COLUMN_NAME_NAME = "name";
    }

    public static abstract class BoardgamesCategoriesEntry implements BaseColumns {
        public static final String TABLE_NAME = "boardgames_categories";

        public static final String COLUMN_NAME_BOARDGAME_ID = "boardgame_id";
        public static final String COLUMN_NAME_CATEGORY_ID = "category_id";
    }

}
