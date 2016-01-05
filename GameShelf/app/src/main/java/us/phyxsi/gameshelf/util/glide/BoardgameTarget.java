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

package us.phyxsi.gameshelf.util.glide;


import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import us.phyxsi.gameshelf.R;
import us.phyxsi.gameshelf.ui.widget.BadgedFourFourImageView;
import us.phyxsi.gameshelf.util.ViewUtils;

/**
 * A Glide {@see ViewTarget} for {@link BadgedFourFourImageView}s. It applies a badge for animated
 * images, can prevent GIFs from auto-playing & applies a palette generated ripple.
 */
public class BoardgameTarget extends GlideDrawableImageViewTarget implements
        Palette.PaletteAsyncListener {

    private final boolean autoplayGifs;

    public BoardgameTarget(BadgedFourFourImageView view, boolean autoplayGifs) {
        super(view);
        this.autoplayGifs = autoplayGifs;
    }

    @Override
    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable>
            animation) {
        super.onResourceReady(resource, animation);
        if (!autoplayGifs) {
            resource.stop();
        }

        BadgedFourFourImageView badgedImageView = (BadgedFourFourImageView) getView();
        if (resource instanceof GlideBitmapDrawable) {
            Palette.from(((GlideBitmapDrawable) resource).getBitmap())
                    .clearFilters()
                    .generate(this);
            badgedImageView.showBadge(false);
        }
    }

    @Override
    public void onStart() {
        if (autoplayGifs) {
            super.onStart();
        }
    }

    @Override
    public void onStop() {
        if (autoplayGifs) {
            super.onStop();
        }
    }

    @Override
    public void onGenerated(Palette palette) {
        ((BadgedFourFourImageView) getView()).setForeground(
                ViewUtils.createRipple(palette, 0.25f, 0.5f,
                        ContextCompat.getColor(getView().getContext(), R.color.mid_grey), true));
    }

}