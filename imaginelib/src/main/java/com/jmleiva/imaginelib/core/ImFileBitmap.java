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
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.IOException;

/**
 * {@link ImBitmap} Subclass, representing the picture in the file system.
 */
public class ImFileBitmap extends ImBitmap
{
    String localPath;

    /**
     * ImFileBitmap Constructor
     * @param imBitmapId {@link String} identifier. Unique for each CacheBitmap, and used to find them if they are cached.
     * @param localPath path where the actual image file is stored.
     */
    public ImFileBitmap(String imBitmapId, String localPath)
    {
        super(imBitmapId);
        this.localPath = localPath;
    }

    @Override
    public boolean isMalformed()
    {
        if(localPath == null) return true;
        return super.isMalformed();
    }


    /**
     * @return local path of the file used as picture
     */
    @Override
    public String getPath()
    {
        return localPath;
    }

    @Override
    public Bitmap retrieveBitmap(int width, int height)
    {
        Bitmap mBitmap = null;

        int orientation = 0;

        try
        {
            ExifInterface exif = new ExifInterface(localPath);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            switch (exifOrientation)
            {
                case ExifInterface.ORIENTATION_NORMAL:
                    orientation = 0;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    orientation = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    orientation = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    orientation = -90;
                    break;
            }
        }
        catch (IOException e)
        {
            // DO nothing, it simply doesn't have EXIF support
        }



        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inPreferredConfig = ImBitmapManager.COLOR_CONFIG;
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(localPath, options);

        int factor = 1;

        if(width != 0 && height != 0)
        {
            factor = this.getResizeFactor(options.outHeight, options.outWidth, width, height);
        }

        setOriginalSize(options.outWidth, options.outHeight);

        if(factor < 1)
        {
            factor = 1;
        }

        // Decode bitmap with inSampleSize update
        options.inJustDecodeBounds = false;
        options.inSampleSize = factor;

        mBitmap = BitmapFactory.decodeFile(localPath, options);

        if(mBitmap == null)
        {
            malformed = true;
        }
        else if(orientation != 0)
        {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);
            Bitmap rotatedBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
            mBitmap.recycle();
            mBitmap = rotatedBitmap;
        }

        return mBitmap;
    }
}

