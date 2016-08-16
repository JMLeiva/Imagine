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

package com.jmleiva.imagine;


import android.content.Context;

import com.jmleiva.imaginelib.core.ImBitmapManager;
import com.jmleiva.imaginelib.core.ImRemoteBitmap;

public class ResourceSampleAdapter extends SampleAdapter
{
    public ResourceSampleAdapter(Context context, ImBitmapManager imBitmapManager) {
        super(context, imBitmapManager);
    }

    @Override
    protected void initializeBitmaps(Context context)
    {
        bitmaps.add(imBitmapManager.getResourceBitmap(R.drawable.i_001));
        bitmaps.add(imBitmapManager.getResourceBitmap(R.drawable.i_002));
        bitmaps.add(imBitmapManager.getResourceBitmap(R.drawable.i_003));
        bitmaps.add(imBitmapManager.getResourceBitmap(R.drawable.i_004));
        bitmaps.add(imBitmapManager.getResourceBitmap(R.drawable.i_005));
        bitmaps.add(imBitmapManager.getResourceBitmap(R.drawable.i_006));
        bitmaps.add(imBitmapManager.getResourceBitmap(R.drawable.i_007));
        bitmaps.add(imBitmapManager.getResourceBitmap(R.drawable.i_008));
        bitmaps.add(imBitmapManager.getResourceBitmap(R.drawable.i_009));
        bitmaps.add(imBitmapManager.getResourceBitmap(R.drawable.i_010));
        bitmaps.add(imBitmapManager.getResourceBitmap(R.drawable.i_011));
        bitmaps.add(imBitmapManager.getResourceBitmap(R.drawable.i_012));
        bitmaps.add(imBitmapManager.getResourceBitmap(R.drawable.i_013));
        bitmaps.add(imBitmapManager.getResourceBitmap(R.drawable.i_014));
        bitmaps.add(imBitmapManager.getResourceBitmap(R.drawable.i_015));

    }
}
