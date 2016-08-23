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

import com.jmleiva.imaginelib.core.cache.CacheImBitmapManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * {@link ImBitmap} Subclass, representing the picture hosted in a remote server.
 */
public class ImRemoteBitmap  extends ImBitmap
{
    protected String urlPath;
    protected URL url;

    /**
     * CacheRemoteBitmap Constructor
     * @param cacheBitmapId {@link String} identifier. Unique for each CacheBitmap, and used to find them if they are cached.
     * @param urlPath URL where the actual image file is stored.
     */
    public ImRemoteBitmap(String cacheBitmapId, String urlPath)
    {
       this(cacheBitmapId, urlPath, null);
    }

    ImRemoteBitmap(String cacheBitmapId, String urlPath, ImBitmapManager imBitmapManager)
    {
        super(cacheBitmapId, imBitmapManager);
        this.urlPath = urlPath;
    }


    @Override
    public boolean isMalformed()
    {
        if(urlPath == null) return true;
        return super.isMalformed();
    }

    /**
     * @return url of the file used as picture
     */
    @Override
    public String getPath()
    {
        return urlPath;
    }

	/*@Override
	public Bitmap GetThumbnailBitmap()
	{
		return GetBitmap();
	}*/

    @Override
    public Bitmap retrieveBitmap(int width, int height)
    {
        Bitmap mBitmap = null;

        if(url == null)
        {
            try
            {
                url = new URL(urlPath);
            }
            catch (MalformedURLException e)
            {
                malformed = true;
                return null;
            }
        }


        if(url == null)
        {
            return null;
        }

        try
        {
            BitmapFactory.Options options = new BitmapFactory.Options();
            InputStream is;

            if(originalWidth == 0 && originalHeight == 0)// && ConnectionManager.sharedManager().isInternetConnected())
            {
                options.inJustDecodeBounds = true;

                is = url.openConnection().getInputStream();
                BitmapFactory.decodeStream(is, null, options);

                setOriginalSize(options.outWidth, options.outHeight);
            }

            int factor = 1;

            if(width != 0 && height != 0)
            {
                factor = this.getResizeFactor(options.outHeight, options.outWidth, width, height);
            }



            if(factor < 1)
            {
                factor = 1;
            }

            //Try loading a Disc Cached Version
            CacheImBitmapManager.CacheImBitmap cacheImBitmap = null;/*CacheImBitmapManager.sharedManager().getCachedImBitmap(cacheBitmapId);

            if(cacheImBitmap != null && cacheImBitmap.sizeFactor >= factor)
            {
                //mBitmap = GetFromFile(diskCacheBitmap.localPath, width, height);
                mBitmap = getFromDatabase(cacheImBitmap, width, height);
            }*/

            if(mBitmap == null)
            {
                // Decode bitmap with inSampleSize update
                options.inJustDecodeBounds = false;
                options.inSampleSize = factor;
                options.inPreferredConfig = ImBitmapManager.COLOR_CONFIG;

                //Re-Create inputStream again
                is = url.openConnection().getInputStream();
                mBitmap = BitmapFactory.decodeStream(is, null, options);



                //CacheImBitmapManager.sharedManager().insertDiskCacheBitmapAsync(mBitmap, getCacheBitmapId(), factor);
            }

            if(mBitmap == null)
            {
                malformed = true;
            }
        }
        catch(IOException e)
        {
            malformed = true;
            e.printStackTrace();
        }

        return mBitmap;
    }

    /**
     * @param urlPath to be update as the source of the CacheRemoteBitmap
     * <p>
     * If it has previously loaded CacheBitmapElements using a different url, they are disposed.
     */
    public void setUrlPath(String urlPath)
    {
        if(this.urlPath != null)
        {
            if(this.urlPath.equals(urlPath))
            {
                return;
            }
        }

        this.urlPath = urlPath;
        malformed = false;
        url = null;

        for(ImBitmapElement cacheBitmapElement : imBitmapElements)
        {
            cacheBitmapElement.dispose();
        }

        imBitmapElements.clear();
    }

    /**
     * As retrieving pictures from the web is expensive and slow, {@link ImRemoteBitmap} images are cached locally.
     * This method is used to check if a local copy of the {@link ImRemoteBitmap} exists before actually getting it from the web.
     *
     * @param diskCacheBitmap
     * @param width
     * @param height
     * @return
     */
    /*
    private Bitmap getFromDatabase(DiskCacheBitmap diskCacheBitmap, int width, int height)
    {
        Bitmap mBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inPreferredConfig = CacheBitmapManager.COLOR_CONFIG;
        options.inJustDecodeBounds = true;

        //BitmapFactory.decodeFile(localPath, options);

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

        //mBitmap = BitmapFactory.decodeFile(localPath, options);
        byte[] diskCacheBitmapData = DiskCacheBitmapManager.SharedManager().getDiskCachedBitmapData(diskCacheBitmap.dataId);

        if(diskCacheBitmapData != null)
        {
            mBitmap = BitmapFactory.decodeByteArray(diskCacheBitmapData, 0, diskCacheBitmapData.length);

            if(mBitmap == null)
            {
                malformed = true;
            }
            else
            {
                Log.i(CacheBitmapManager.TAG, "Successfully loaded RemoteBitmap from DiskCache copy " + getCacheBitmapId());
            }
        }
        return mBitmap;
    }*/
}

