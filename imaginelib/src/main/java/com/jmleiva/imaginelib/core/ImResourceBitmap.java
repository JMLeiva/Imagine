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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * {@link ImBitmap} Subclass, representing the resource picture, belonging in the Wifiesta Project
 */
public class ImResourceBitmap extends ImBitmap
{
    public final int INTERNAL_RESOURCE = -2;

    private int resourceId = -1;
    private Context context;
    /**
     * ImResourceBitmap Constructor
     * @param imBitmapId {@link String} identifier. Unique for each CacheBitmap, and used to find them if they are cached.
     */
    public ImResourceBitmap(String imBitmapId, Context context)
    {
        super(imBitmapId);
        this.context = context;
    }

    public ImResourceBitmap(String imBitmapId, Context context, ImBitmapManager imBitmapManager)
    {
        super(imBitmapId, imBitmapManager);
        this.context = context;
    }

    /**
     * ImResourceBitmap Contructor
     * @param imBitmapId {@link String} identifier. Unique for each CacheBitmap, and used to find them if they are cached.
     * @param resourceId resource ID used to load the Bitmap
     */
    public ImResourceBitmap(String imBitmapId, int resourceId, Context context)
    {
        this(imBitmapId, context);
        this.resourceId = resourceId;
    }

    ImResourceBitmap(String imBitmapId, int resourceId, Context context, ImBitmapManager imBitmapManager)
    {
        this(imBitmapId, context);
        this.resourceId = resourceId;
    }

    @Override
    public boolean isMalformed()
    {
        if(resourceId == -1) return true;
        return super.isMalformed();
    }

    /**
     * @return the resource id used to load this instance's bitmap
     */
    public int getResourceId()
    {
        return resourceId;
    }

    @Override
    public Bitmap retrieveBitmap(int width, int height)
    {
        Bitmap mBitmap = null;

        Resources res = context.getResources();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(res, resourceId, options);

        int factor = this.getResizeFactor(options.outHeight, options.outWidth, width, height);

        setOriginalSize(options.outWidth, options.outHeight);

        if(factor < 1)
        {
            factor = 1;
        }

        // Decode bitmap with inSampleSize update
        options.inJustDecodeBounds = false;
        options.inSampleSize = factor;

        mBitmap = BitmapFactory.decodeResource(res, resourceId, options);

        if(mBitmap == null)
        {
            malformed = true;
        }

        return mBitmap;
    }

    public void setResourceId(int resourceId)
    {
        if(this.resourceId == resourceId)
        {
            return;
        }

        this.resourceId = resourceId;
        malformed = false;

        for(ImBitmapElement cacheBitmapElement : imBitmapElements)
        {
            cacheBitmapElement.dispose();
        }

        imBitmapElements.clear();
    }
}

