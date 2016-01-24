/*
 * Copyright (C) 2011 The Android Open Source Project
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

package us.phyxsi.gameshelf.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import us.phyxsi.gameshelf.R;
import us.phyxsi.gameshelf.data.api.bgg.model.Boardgame;
import us.phyxsi.gameshelf.data.db.helper.BoardgameDbHelper;
import us.phyxsi.gameshelf.ui.BoardgameDetails;

public class StackWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final Map<Integer, AppWidgetTarget> TARGETS = new HashMap<>();
    private List<WidgetItem> mWidgetItems = new ArrayList<WidgetItem>();
    private Context mContext;
    private int mAppWidgetId;
    private Bitmap mBitmap;
    Handler handler = new Handler(Looper.getMainLooper());

    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    public void onCreate() {

        BoardgameDbHelper bgHelper = new BoardgameDbHelper(mContext);
        Cursor cursor = bgHelper.getAll("10");

        List<Boardgame> boardgameList = new ArrayList<Boardgame>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Boardgame bg = new Boardgame(cursor, mContext);
            boardgameList.add(bg);
        }

        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
        for (Boardgame boardgame : boardgameList) {
            mWidgetItems.add(new WidgetItem(boardgame));
        }

        // We sleep for 3 seconds here to show how the empty view appears in the interim.
        // The empty view is set in the StackWidgetProvider and should be a sibling of the
        // collection view.
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        // In onDestroy() you should tear down anything that was setup for your data source,
        // eg. cursors, connections, etc.
        mWidgetItems.clear();
    }

    public int getCount() {
        return mWidgetItems.size();
    }

    public RemoteViews getViewAt(int position) {
        // position will always range from 0 to getCount() - 1.

        // We construct a remote views item based on our widget item xml file
        final RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
//        rv.setTextViewText(R.id.widget_item, mWidgetItems.get(position).image);
        final int mPosition = position;
        handler.post(new Runnable() {
                         @Override
                         public void run() {

                             final int[] imageSize = {400, 400};
                             AppWidgetTarget target = TARGETS.get(mAppWidgetId);
                             if (target == null) {
                                 target = new AppWidgetTarget(mContext, rv, R.id.widget_item,
                                         imageSize[0], imageSize[1], new int[] {mAppWidgetId});
                                 TARGETS.put(mAppWidgetId, target);
                             }

                             Glide.with(mContext)
                                     .load("http:" + mWidgetItems.get(mPosition).boardgame.image)
                                     .asBitmap()
                                     .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                     .listener(new RequestListener<String, Bitmap>() {
                                         @Override
                                         public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                                             TARGETS.remove(mAppWidgetId);
                                             return false;
                                         }

                                         @Override
                                         public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                             TARGETS.remove(mAppWidgetId);
                                             mBitmap = resource;
                                             rv.setImageViewBitmap(R.id.widget_item, mBitmap);
                                             AppWidgetManager.getInstance(mContext).notifyAppWidgetViewDataChanged(mAppWidgetId, R.id.widget_item);
                                             return false;
                                         }
                                     })
                                      .into(imageSize[0], imageSize[1]);
                         }
                     });

        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in StackWidgetProvider.
        Bundle extras = new Bundle();
        extras.putInt(StackWidgetProvider.EXTRA_ITEM, position);
        extras.putLong(BoardgameDetails.EXTRA_BOARDGAME, mWidgetItems.get(position).boardgame.id);
        Intent fillInIntent = new Intent();
        fillInIntent.setClass(mContext, BoardgameDetails.class);
        fillInIntent.putExtra(BoardgameDetails.EXTRA_BOARDGAME, mWidgetItems.get(position).boardgame);
        fillInIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent);

        // You can do heaving lifting in here, synchronously. For example, if you need to
        // process an image, fetch something from the network, etc., it is ok to do it here,
        // synchronously. A loading view will show up in lieu of the actual contents in the
        // interim.
        try {
            System.out.println("Loading view " + position);
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mBitmap != null) {
            rv.setImageViewBitmap(R.id.widget_item, mBitmap);
        }
        mBitmap = null;

        // Return the remote views object.
        return rv;
    }

    public RemoteViews getLoadingView() {
        // You can create a custom loading view (for instance when getViewAt() is slow.) If you
        // return null here, you will get the default loading view.
        return null;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        // This is triggered when you call AppWidgetManager notifyAppWidgetViewDataChanged
        // on the collection view corresponding to this factory. You can do heaving lifting in
        // here, synchronously. For example, if you need to process an image, fetch something
        // from the network, etc., it is ok to do it here, synchronously. The widget will remain
        // in its current state while work is being done here, so you don't need to worry about
        // locking up the widget.
    }
}