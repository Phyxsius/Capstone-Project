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

package us.phyxsi.gameshelf.data.api.bgg.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import us.phyxsi.gameshelf.data.GameShelfItem;
import us.phyxsi.gameshelf.data.db.GameShelfContract;

/**
 * Models a Board Game Geek boardgame
 */
public class Boardgame extends GameShelfItem implements Parcelable {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Boardgame> CREATOR = new Parcelable.Creator<Boardgame>() {
        @Override
        public Boardgame createFromParcel(Parcel in) { return new Boardgame(in); }

        @Override
        public Boardgame[] newArray(int size) { return new Boardgame[size]; }
    };

    public final String description;
    public final String image;
    public final int maxPlayers;
    public final int maxPlaytime;
    public final int minAge;
    public final int minPlayers;
    public final int minPlaytime;
    public final int suggestedNumplayers;
    public final long yearPublished;
    public final List<Category> categories;

    public Boardgame(long id,
                     String title,
                     String description,
                     String image,
                     int maxPlayers,
                     int maxPlaytime,
                     int minAge,
                     int minPlayers,
                     int minPlaytime,
                     int suggestedNumplayers,
                     long yearPublished,
                     List<Category> categories) {
        super(id, title);
        this.description = description;
        this.image = image;
        this.maxPlayers = maxPlayers;
        this.maxPlaytime = maxPlaytime;
        this.minAge = minAge;
        this.minPlayers = minPlayers;
        this.minPlaytime = minPlaytime;
        this.suggestedNumplayers = suggestedNumplayers;
        this.yearPublished = yearPublished;
        this.categories = categories;
    }

    public Boardgame(Cursor cursor) {
        super(cursor.getLong(cursor.getColumnIndexOrThrow(GameShelfContract.BoardgameEntry.COLUMN_NAME_GAME_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(GameShelfContract.BoardgameEntry.COLUMN_NAME_TITLE)));

        this.description = cursor.getString(cursor.getColumnIndexOrThrow(GameShelfContract.BoardgameEntry.COLUMN_NAME_DESCRIPTION));
        this.image = cursor.getString(cursor.getColumnIndexOrThrow(GameShelfContract.BoardgameEntry.COLUMN_NAME_IMAGE));
        this.maxPlayers = cursor.getInt(cursor.getColumnIndexOrThrow(GameShelfContract.BoardgameEntry.COLUMN_NAME_MAX_PLAYERS));
        this.maxPlaytime = cursor.getInt(cursor.getColumnIndexOrThrow(GameShelfContract.BoardgameEntry.COLUMN_NAME_MAX_PLAYTIME));
        this.minAge = cursor.getInt(cursor.getColumnIndexOrThrow(GameShelfContract.BoardgameEntry.COLUMN_NAME_MIN_AGE));
        this.minPlayers = cursor.getInt(cursor.getColumnIndexOrThrow(GameShelfContract.BoardgameEntry.COLUMN_NAME_MIN_PLAYERS));
        this.minPlaytime = cursor.getInt(cursor.getColumnIndexOrThrow(GameShelfContract.BoardgameEntry.COLUMN_NAME_MIN_PLAYTIME));
        this.suggestedNumplayers = cursor.getInt(cursor.getColumnIndexOrThrow(GameShelfContract.BoardgameEntry.COLUMN_NAME_SUGGESTED_NUMPLAYERS));
        this.yearPublished = cursor.getInt(cursor.getColumnIndexOrThrow(GameShelfContract.BoardgameEntry.COLUMN_NAME_YEAR_PUBLISHED));

        // TODO: Set categories from a cursor
        this.categories = null;
    }

    protected Boardgame(Parcel in) {
        super(in.readLong(), in.readString());

        description = in.readString();
        image = in.readString();
        maxPlayers = in.readInt();
        maxPlaytime = in.readInt();
        minAge = in.readInt();
        minPlayers = in.readInt();
        minPlaytime = in.readInt();
        suggestedNumplayers = in.readInt();
        yearPublished = in.readLong();
        if (in.readByte() == 0x01) {
            categories = new ArrayList<Category>();
            in.readList(categories, Category.class.getClassLoader());
        } else {
            categories = null;
        }
    }

    public void weigh() {
        // TODO: Weigh boardgame based on it's ranking in the hotness list?
        weight = 1f;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(image);
        dest.writeInt(maxPlayers);
        dest.writeInt(maxPlaytime);
        dest.writeInt(minAge);
        dest.writeInt(minPlayers);
        dest.writeInt(minPlaytime);
        dest.writeInt(suggestedNumplayers);
        dest.writeLong(yearPublished);
        if (categories == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(categories);
        }
    }
}
