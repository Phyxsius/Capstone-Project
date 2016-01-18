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

package us.phyxsi.gameshelf.data.api.bgg;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import us.phyxsi.gameshelf.data.api.bgg.model.BoardgamesResponse;
import us.phyxsi.gameshelf.data.api.bgg.model.CollectionItems;

/**
 * Models the BGG API.
 *
 * v1 docs: http://www.boardgamegeek.com/xmlapi
 * v2 docs: http://www.boardgamegeek.com/xmlapi2
 */
public interface BGGService {

    String ENDPOINT = "http://www.boardgamegeek.com/";

    @GET("/xmlapi/search")
    void search(@Query("search") String query,
                Callback<BoardgamesResponse> callback);

    @GET("/xmlapi/boardgame/{id}")
    void getBoardgame(@Path("id") long boardgameId,
                      Callback<BoardgamesResponse> callback);

    @GET("/xmlapi/collection/{username}")
    void getCollection(@Path("username") String username,
                       Callback<CollectionItems> callback);
}
