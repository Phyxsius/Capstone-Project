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

package us.phyxsi.gameshelf.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import us.phyxsi.gameshelf.data.api.bgg.model.CollectionItems;
import us.phyxsi.gameshelf.data.api.bgg.model.User;

public class BGGPrefs {

    private static final String BGG_PREF = "BGG_PREF";
    private static final String KEY_USER_NAME = "KEY_USER_NAME";

    private static volatile BGGPrefs singleton;
    private final SharedPreferences prefs;

    private boolean isLoggedIn = false;
    private String username;
    private List<BGGLoginStatusListener> loginStatusListeners;

    public static BGGPrefs get(Context context) {
        if (singleton == null) {
            synchronized (BGGPrefs.class) {
                singleton = new BGGPrefs(context);
            }
        }
        return singleton;
    }

    private BGGPrefs(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(BGG_PREF, Context.MODE_PRIVATE);

        username = prefs.getString(KEY_USER_NAME, null);
        isLoggedIn = !TextUtils.isEmpty(username);
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedInUser(User user, CollectionItems collectionItems) {
        if (user != null) {
            username = user.username;

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_USER_NAME, username);
            editor.apply();

            isLoggedIn = true;
            dispatchLoginEvent(collectionItems);
        }
    }

    public String getUsername() {
        return username;
    }

    public void logout() {
        isLoggedIn = false;
        username = null;

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_NAME, null);
        editor.apply();
        dispatchLogoutEvent();
    }

    public void addLoginStatusListener(BGGLoginStatusListener listener) {
        if (loginStatusListeners == null) {
            loginStatusListeners = new ArrayList<>();
        }
        loginStatusListeners.add(listener);
    }

    public void removeLoginStatusListener(BGGLoginStatusListener listener) {
        if (loginStatusListeners != null) {
            loginStatusListeners.remove(listener);
        }
    }

    private void dispatchLoginEvent(CollectionItems collection) {
        if (loginStatusListeners != null && loginStatusListeners.size() > 0) {
            for (BGGLoginStatusListener listener : loginStatusListeners) {
                listener.onBGGLogin(collection);
            }
        }
    }

    private void dispatchLogoutEvent() {
        if (loginStatusListeners != null && loginStatusListeners.size() > 0) {
            for (BGGLoginStatusListener listener : loginStatusListeners) {
                listener.onBGGLogout();
            }
        }
    }

    public interface BGGLoginStatusListener {
        void onBGGLogin(CollectionItems collection);
        void onBGGLogout();
    }

}
