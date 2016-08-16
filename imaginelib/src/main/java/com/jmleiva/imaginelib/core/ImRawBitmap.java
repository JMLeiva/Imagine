/*
This file is part of Imagine by Juan Martin Leiva

PagedRecyclerView is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Foobar is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.jmleiva.imaginelib.core;

import android.graphics.Bitmap;

/**
 * {@link ImBitmap} Subclass, representing an In-Memory bitmap wrapper.
 */

public class ImRawBitmap extends ImBitmap
{
    Bitmap originalBitmap;

    /**
     * ImRawBitmap Constructor
     * @param bitmap {@link Bitmap} source of this instance.
     * @param cacheBitmapId {@link String} identifier. Unique for each ImBitmap, and used to find them if they are cached.
     */
    public ImRawBitmap(Bitmap bitmap, String cacheBitmapId)
    {
        super(cacheBitmapId);
        originalBitmap = bitmap;

        originalWidth = originalBitmap.getWidth();
        originalHeight = originalBitmap.getHeight();

        if(originalBitmap == null)
        {
            malformed = true;
        }
    }

    @Override
    public Bitmap retrieveBitmap(int width, int height)
    {
        float scaleX = ((float)width) / originalWidth;
        float scaleY = ((float)height) / originalHeight;

        float fixedScale = Math.max(scaleX, scaleY);
        int resultX = (int) Math.ceil(originalWidth * fixedScale);
        int resultY = (int) Math.ceil(originalHeight * fixedScale);

        Bitmap resultBitmap = Bitmap.createScaledBitmap(originalBitmap, resultX, resultY, true);

        return resultBitmap;
    }
}

