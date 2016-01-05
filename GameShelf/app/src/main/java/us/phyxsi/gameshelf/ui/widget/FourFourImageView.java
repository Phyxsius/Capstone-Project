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

package us.phyxsi.gameshelf.ui.widget;

import android.content.Context;
import android.util.AttributeSet;

/**
 * A extension of ForegroundImageView that is always 4:4 aspect ratio.
 */
public class FourFourImageView extends ForegroundImageView {

    public FourFourImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int fourFourHeight = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthSpec),
                MeasureSpec.EXACTLY);
        super.onMeasure(widthSpec, fourFourHeight);
    }
}