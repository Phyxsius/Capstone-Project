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

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

import us.phyxsi.gameshelf.data.db.GameShelfContract;

/**
 * Models a category on a Board Game Geek boardgame
 */

@Root(name = "boardgamecategory")
public class Category implements Parcelable {

    @Attribute(name = "objectid", required = false)
    public long id;

    @Text
    public String name;

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) { return new Category(in); }

        @Override
        public Category[] newArray(int size) { return new Category[size]; }
    };

    public Category() {}

    public Category(Parcel in) {
        id = in.readLong();
        name = in.readString();
    }

    public Category(Cursor cursor) {
        this.id = cursor.getLong(cursor.getColumnIndexOrThrow(GameShelfContract.CategoryEntry.COLUMN_NAME_CATEGORY_ID));
        this.name = cursor.getString(cursor.getColumnIndexOrThrow(GameShelfContract.CategoryEntry.COLUMN_NAME_NAME));
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
    }
}
