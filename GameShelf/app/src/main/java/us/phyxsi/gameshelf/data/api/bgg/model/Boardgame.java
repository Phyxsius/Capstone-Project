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

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import us.phyxsi.gameshelf.data.GameShelfItem;

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
    public final int maxplayers;
    public final int maxplaytime;
    public final int minage;
    public final int minplayers;
    public final int minplaytime;
    public final int suggested_numplayers;
    public final long yearpublished;
    public final List<Category> categories;

    public Boardgame(long id,
                     String title,
                     String description,
                     String image,
                     int maxplayers,
                     int maxplaytime,
                     int minage,
                     int minplayers,
                     int minplaytime,
                     int suggested_numplayers,
                     long yearpublished,
                     List<Category> categories) {
        super(id, title);
        this.description = description;
        this.image = image;
        this.maxplayers = maxplayers;
        this.maxplaytime = maxplaytime;
        this.minage = minage;
        this.minplayers = minplayers;
        this.minplaytime = minplaytime;
        this.suggested_numplayers = suggested_numplayers;
        this.yearpublished = yearpublished;
        this.categories = categories;
    }

    protected Boardgame(Parcel in) {
        super(in.readLong(), in.readString());

        description = in.readString();
        image = in.readString();
        maxplayers = in.readInt();
        maxplaytime = in.readInt();
        minage = in.readInt();
        minplayers = in.readInt();
        minplaytime = in.readInt();
        suggested_numplayers = in.readInt();
        yearpublished = in.readLong();
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
        dest.writeInt(maxplayers);
        dest.writeInt(maxplaytime);
        dest.writeInt(minage);
        dest.writeInt(minplayers);
        dest.writeInt(minplaytime);
        dest.writeInt(suggested_numplayers);
        dest.writeLong(yearpublished);
        if (categories == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(categories);
        }
    }
}
