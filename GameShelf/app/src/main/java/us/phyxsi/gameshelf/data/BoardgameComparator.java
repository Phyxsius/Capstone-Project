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

import java.util.Comparator;

import us.phyxsi.gameshelf.data.api.bgg.model.Boardgame;

/**
 * A comparator that compares {@link Boardgame}s based on their {@code created_at} attribute.
 */
public class BoardgameComparator implements Comparator<Boardgame> {

    public static final String SORT_PREF = "SORT_PREF";
    public static final String KEY_SORT_ORDER = "KEY_SORT_ORDER";

    private static final int SORT_ALPHABETICALLY = 0;
    private static final int SORT_DATE_ADDED = 1;

    private int sortMode = SORT_ALPHABETICALLY;

    public int getSortMode() {
        return sortMode;
    }

    public void setSortMode(int sortMode) {
        this.sortMode = sortMode;
    }

    @Override
    public int compare(Boardgame lbg, Boardgame rbg) {
        switch (sortMode) {
            case SORT_ALPHABETICALLY:
                return lbg.title.compareTo(rbg.title);
            case SORT_DATE_ADDED:
                return rbg.created_at.compareTo(lbg.created_at);
        }

        // Default to game ID if needed
        return Long.valueOf(lbg.getId()).compareTo(Long.valueOf(rbg.getId()));
    }
}