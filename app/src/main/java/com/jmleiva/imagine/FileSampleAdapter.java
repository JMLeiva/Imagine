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
import android.database.Cursor;
import android.provider.MediaStore;

import com.jmleiva.imaginelib.core.ImBitmap;
import com.jmleiva.imaginelib.core.ImBitmapManager;
import com.jmleiva.imaginelib.core.ImRemoteBitmap;

public class FileSampleAdapter extends SampleAdapter
{
    public FileSampleAdapter(Context context, ImBitmapManager imBitmapManager) {
        super(context, imBitmapManager);
    }

    @Override
    protected void initializeBitmaps(Context context)
    {
        String[] mProjection = {MediaStore.Images.Media._ID,  MediaStore.Images.Media.DATA};
        final String orderBy = MediaStore.Images.Media.DEFAULT_SORT_ORDER;
        final Cursor imageCursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                mProjection, null,  null, orderBy);

        for (int i = 0; i < Math.min(imageCursor.getCount(), 50); i++)
        {
            imageCursor.moveToPosition(i);
            //int id = imageCursor.getInt(imageColumnIndex);
            int dataColumnIndex = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);

            //thumbnails[i] = MediaStore.Images.Thumbnails.getThumbnail(getApplicationContext().getContentResolver(), id,
            //		MediaStore.Images.Thumbnails.MINI_KIND, null);

            String path = imageCursor.getString(dataColumnIndex);

            ImBitmap imBitmap = imBitmapManager.getFileBitmap(path);
            bitmaps.add(imBitmap);
        }

        imageCursor.close();
    }
}
