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

import us.phyxsi.gameshelf.data.api.bgg.model.Boardgame;

public class WidgetItem {
    public Boardgame boardgame;

    public WidgetItem(Boardgame boardgame) {
        this.boardgame = boardgame;
    }
}