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

import android.content.res.ColorStateList;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.text.Spanned;
import android.text.TextUtils;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

import us.phyxsi.gameshelf.data.db.GameShelfContract;
import us.phyxsi.gameshelf.util.HtmlUtils;

/**
 * Models a Board Game Geek boardgame
 */
@Root(name = "boardgame", strict = false)
public class Boardgame implements Parcelable {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Boardgame> CREATOR = new Parcelable.Creator<Boardgame>() {
        @Override
        public Boardgame createFromParcel(Parcel in) { return new Boardgame(in); }

        @Override
        public Boardgame[] newArray(int size) { return new Boardgame[size]; }
    };

    @Attribute(name = "objectid")
    public long id;
    public String title;
    @ElementList(name = "name", inline = true, required = false)
    public List<BoardgameName> names;
    @Element(name = "description", required = false)
    public String description;
    @Element(name = "image", required = false)
    public String image;
    @Element(name = "maxplayers", required = false)
    public int maxPlayers;
    @Element(name = "maxplaytime", required = false)
    public int maxPlaytime;
    @Element(name = "age", required = false)
    public int minAge;
    @Element(name = "minplayers", required = false)
    public int minPlayers;
    @Element(name = "minplaytime", required = false)
    public int minPlaytime;
    public int suggestedNumplayers;
//    @ElementList(name = "boardgamepublisher", required = false)
    public String publisher;
    @Element(name = "yearpublished", required = false)
    public String yearPublished;
    public List<Category> categories;
    // todo move this into a decorator
    public boolean hasFadedIn = false;
    public Spanned parsedDescription;
    public int colspan;

    public Boardgame() {}

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
                     String publisher,
                     String yearPublished,
                     List<Category> categories) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.image = image;
        this.maxPlayers = maxPlayers;
        this.maxPlaytime = maxPlaytime;
        this.minAge = minAge;
        this.minPlayers = minPlayers;
        this.minPlaytime = minPlaytime;
        this.suggestedNumplayers = suggestedNumplayers;
        this.publisher = publisher;
        this.yearPublished = yearPublished;
        this.categories = categories;
    }

    public Boardgame(Cursor cursor) {
        this.id = cursor.getLong(cursor.getColumnIndexOrThrow(GameShelfContract.BoardgameEntry.COLUMN_NAME_GAME_ID));
        this.title = cursor.getString(cursor.getColumnIndexOrThrow(GameShelfContract.BoardgameEntry.COLUMN_NAME_TITLE));
        this.description = cursor.getString(cursor.getColumnIndexOrThrow(GameShelfContract.BoardgameEntry.COLUMN_NAME_DESCRIPTION));
        this.image = cursor.getString(cursor.getColumnIndexOrThrow(GameShelfContract.BoardgameEntry.COLUMN_NAME_IMAGE));
        this.maxPlayers = cursor.getInt(cursor.getColumnIndexOrThrow(GameShelfContract.BoardgameEntry.COLUMN_NAME_MAX_PLAYERS));
        this.maxPlaytime = cursor.getInt(cursor.getColumnIndexOrThrow(GameShelfContract.BoardgameEntry.COLUMN_NAME_MAX_PLAYTIME));
        this.minAge = cursor.getInt(cursor.getColumnIndexOrThrow(GameShelfContract.BoardgameEntry.COLUMN_NAME_MIN_AGE));
        this.minPlayers = cursor.getInt(cursor.getColumnIndexOrThrow(GameShelfContract.BoardgameEntry.COLUMN_NAME_MIN_PLAYERS));
        this.minPlaytime = cursor.getInt(cursor.getColumnIndexOrThrow(GameShelfContract.BoardgameEntry.COLUMN_NAME_MIN_PLAYTIME));
        this.suggestedNumplayers = cursor.getInt(cursor.getColumnIndexOrThrow(GameShelfContract.BoardgameEntry.COLUMN_NAME_SUGGESTED_NUMPLAYERS));
        this.publisher = cursor.getString(cursor.getColumnIndexOrThrow(GameShelfContract.BoardgameEntry.COLUMN_NAME_PUBLISHER));
        this.yearPublished = cursor.getString(cursor.getColumnIndexOrThrow(GameShelfContract.BoardgameEntry.COLUMN_NAME_YEAR_PUBLISHED));

        // TODO: Set categories from a cursor
        this.categories = null;
    }

    protected Boardgame(Parcel in) {
        id = in.readLong();
        title = in.readString();
        description = in.readString();
        image = in.readString();
        maxPlayers = in.readInt();
        maxPlaytime = in.readInt();
        minAge = in.readInt();
        minPlayers = in.readInt();
        minPlaytime = in.readInt();
        suggestedNumplayers = in.readInt();
        publisher = in.readString();
        yearPublished = in.readString();

        hasFadedIn = in.readByte() != 0x00;

        if (in.readByte() == 0x01) {
            categories = new ArrayList<Category>();
            in.readList(categories, Category.class.getClassLoader());
        } else {
            categories = null;
        }
    }

    public Spanned getParsedDescription(ColorStateList linkTextColor,
                                        @ColorInt int linkHighlightColor) {
        if (parsedDescription == null && !TextUtils.isEmpty(description)) {
            parsedDescription = HtmlUtils.parseHtml(description, linkTextColor, linkHighlightColor);
        }
        return parsedDescription;
    }

    public String getTitle() {
        if (names != null) {
            if (names.size() == 1) return names.get(0).getTitle();

            for (BoardgameName name : names) {
                if (name.isPrimary()) return name.getTitle();
            }
        }

        return "";
    }

    public String getPlayers() {
        if (minPlayers == 0) return null;

        String output = Integer.toString(minPlayers);

        if (minPlayers != maxPlayers) output += "-" + Integer.toString(maxPlayers);

        return output + " players";
    }

    public String getPlaytime() {
        if (minPlaytime == 0) return null;

        String output = Integer.toString(minPlaytime);

        if (minPlaytime != maxPlaytime) output += "-" + Integer.toString(maxPlaytime);

        return output + " minutes";
    }

    public String getByline() {
        String byline = "";
        if (!TextUtils.isEmpty(publisher)) {
            byline += "by " + publisher + ", ";
        }
        byline += yearPublished;

        return byline;
    }

    public void weigh() {
        // TODO: Weigh boardgame based on it's ranking in the hotness list?
//        weight = 1f;
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
        dest.writeString(publisher);
        dest.writeString(yearPublished);
        dest.writeByte((byte) (hasFadedIn ? 0x01 : 0x00));
        if (categories == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(categories);
        }

    }
}