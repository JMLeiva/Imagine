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

package com.jmleiva.imaginelib.core.cache;

import android.graphics.Bitmap;
import android.util.Log;

import com.jmleiva.imaginelib.core.ImBitmap;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Singleton Class.
 * <p>
 * This class is responsible of managing the locally cached bitmaps inside the SQLite Database
 * <p>
 * It also keeps track of the amount of memory used only by {@link CacheImBitmap} instances, and makes sure
 * the MaxMemory (fixed 10 MB) is never exceeded (except in some cases),
 * reducing the memory to another fixed value (trimMemory, fixed to 4MB)
 */
public class CacheImBitmapManager
{
    final static String TAG = "CacheImBitmapManager";

    private static CacheImBitmapManager _instance;

    public static CacheImBitmapManager sharedManager()
    {
        if (_instance == null)
        {
            _instance = new CacheImBitmapManager();
        }

        return _instance;
    }


    /**
     * Data Class.
     * <p>
     *
     * Used to encapsulate a Bitmap cached locally
     */
    public static class CacheImBitmap
    {
        public String id;
        public long dataId;
        public long lastTimeUsed;
        public int sizeFactor;
    }


    final long MAX_MEMORY 	= 	1024 * 1024 * 10; 	// 10 	MB
    long TRIM_MEMORY		=	1024 * 1024 * 4;	// 4 	MB

    Map<String, CacheImBitmap> diskBitmapMap;

    long currentSize;

    CacheImBitmapSource cacheImBitmapSource;

    private CacheImBitmapManager()
    {
        diskBitmapMap = new HashMap<>();
        currentSize = 0;

        Log.i(TAG, String.format("Starting CacheImBitmapManager. MAX_MEMORY: %d kb, TRIM_MEMORY %d kb", MAX_MEMORY / 1024,
                TRIM_MEMORY / 1024));

        // Implement a dependency injection frp the discCacheBitmapSource?
        cacheImBitmapSource = null;//new DiskCacheBitmapSourceImplementation();

        throw new RuntimeException("Not Implemented");
    }

    public void setSource(CacheImBitmapSource cacheImBitmapSource)
    {
        this.cacheImBitmapSource = cacheImBitmapSource;
    }

    /**
     * Called when upper memory limit is reached.
     */
    void trimMemory()
    {
        if(cacheImBitmapSource == null)
        {
            throw new IllegalStateException("Disk Cache Bitmap Source not update");
        }

        Log.i(TAG, String.format("**** Starting TRIM due exceding MAX_MEMORY: %d kb ****", MAX_MEMORY / 1024));

        List<CacheImBitmap> disposableCacheBitmaps = new ArrayList<CacheImBitmap>();
        Iterator<Map.Entry<String, CacheImBitmap>> it = diskBitmapMap.entrySet().iterator();

        while (it.hasNext())
        {
            Map.Entry<String, CacheImBitmap> pairs = it.next();

            CacheImBitmap cacheImBitmap = pairs.getValue();



            boolean atEnd = true;

            for (int i = 0; i < disposableCacheBitmaps.size(); i++)
            {
                if (disposableCacheBitmaps.get(i).lastTimeUsed > cacheImBitmap.lastTimeUsed)
                {
                    atEnd = false;
                    disposableCacheBitmaps.add(i, cacheImBitmap);
                    break;
                }
            }

            if (atEnd)
            {
                disposableCacheBitmaps.add(cacheImBitmap);
            }

        }

        int disposed = 0;

        while (currentSize > TRIM_MEMORY && disposableCacheBitmaps.size() > 0)
        {
            CacheImBitmap cacheImBitmapToDispose = disposableCacheBitmaps.remove(0);

            cacheImBitmapSource.deleteCacheImBitmap(cacheImBitmapToDispose.dataId);
            disposed++;

            byte[] data = cacheImBitmapSource.getCacheImBitmapData(cacheImBitmapToDispose.dataId);

            onMemoryDecreased(data.length);
        }

        Log.i(TAG, String.format("%d Disk Bitmaps disposed", disposed));

        Log.i(TAG, String.format("**** TRIM ended. CurrentSize: %d kb, TRIM_MEMORY: %d kb ****", currentSize / 1024,
                TRIM_MEMORY / 1024));
    }

    /**
     * Called internally each time a {@link CacheImBitmap} allocates memory
     * @param bytes number of bytes allocated by the {@link CacheImBitmap}
     */
    void onMemoryIncreased(long bytes)
    {
        currentSize += bytes;
        Log.i(TAG, String.format("Memory increased by %d kb, current size is %d kb", bytes / 1024, currentSize / 1024));

        if (currentSize > MAX_MEMORY)
        {
            trimMemory();
        }
    }

    /**
     * Called internally each time a {@link CacheImBitmap} deallocates memory
     * @param bytes number of bytes deallocated by the {@link CacheImBitmap}
     */
    void onMemoryDecreased(long bytes)
    {
        currentSize -= bytes;
        Log.i(TAG, String.format("Memory decreased by %d kb, current size is %d kb", bytes / 1024, currentSize / 1024));
    }

    /**
     * Just a shortcut to call {@link CacheImBitmapManager#insertCacheImBitmap(Bitmap, String, int)} in a diferent thread.
     *
     * @param bitmap
     * @param identifier
     * @param sizeFactor
     */
    public void insertDiskCacheBitmapAsync(final Bitmap bitmap, final String identifier, final int sizeFactor)
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                insertCacheImBitmap(bitmap, identifier, sizeFactor);
            }
        });
        thread.start();
    }

    /**
     * Adds a new {@link Bitmap} to be cached locally
     * <p>
     * If there's already a cached bitmap with the same identifier:
     * <ul>
     * <li> If the size factor of the stored bitmap is equal or bigger than the new one, nothing happens
     * <li> If the size factor of the stored bitmap is smaller than the new one, this is replaced.
     * </ul>
     *
     * @param bitmap Bitmap to be cached locally in dick (in the SQLite Database)
     * @param identifier Identifier used to be retriebed by a {@link ImBitmap} later
     * @param sizeFactor Size Factor of the Bitmap in relation of the it's original size
     */
    public void insertCacheImBitmap(Bitmap bitmap, String identifier, int sizeFactor)
    {
        if(cacheImBitmapSource == null)
        {
            throw new IllegalStateException("CacheImBitmapSource not set");
        }

        if(bitmap == null)
        {
            return;
        }

        CacheImBitmap cacheImBitmap = getCachedImBitmap(identifier);

        if(cacheImBitmap != null)
        {
            //Only override if new one is bigger
            if(cacheImBitmap.sizeFactor >= sizeFactor)
            {
                return;
            }
        }

        cacheImBitmap = new CacheImBitmap();
        cacheImBitmap.id = identifier;
        cacheImBitmap.sizeFactor = sizeFactor;
        cacheImBitmap.lastTimeUsed = System.currentTimeMillis();

        FileOutputStream out = null;

        try
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            byte[] data = outputStream.toByteArray();

            onMemoryIncreased(data.length);
            diskBitmapMap.put(identifier, cacheImBitmap);

            cacheImBitmapSource.doAddDiskCacheBitmap(cacheImBitmap, data);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(out != null)
            {
                try
                {
                    out.close();
                }
                catch (Throwable ignore)
                {
                }
            }
        }
    }

    /**
     * @param id Identifier used to search for a stored Bitamp
     * @return a localy stored {@link CacheImBitmap} with a matchin identifier, or {@code null} is it doesn't exist.
     */
    public CacheImBitmap getCachedImBitmap(String id)
    {
        if(cacheImBitmapSource == null)
        {
            throw new IllegalStateException("CacheImBitmapSource not set");
        }

        CacheImBitmap cacheImBitmap = diskBitmapMap.get(id);

        if(cacheImBitmap == null)
        {
            //Lazy load from DB
            cacheImBitmap = cacheImBitmapSource.getCacheImBitmap(id);

            if(cacheImBitmap != null)
            {
                byte[] diskCacheBitmapData = cacheImBitmapSource.getCacheImBitmapData(cacheImBitmap.dataId);

                diskBitmapMap.put(id, cacheImBitmap);

                //Add size to currentSize
				/*
				File file = new File(diskCacheBitmap.localPath);

				if(file.exists())
				{
					currentSize += file.length();
				}
				*/
                currentSize += diskCacheBitmapData.length;
            }
        }

        return cacheImBitmap;
    }

    /**
     * {@link CacheImBitmap} must be lazy initialized, as decoding the real {@link Bitmap} at startup would be
     * a huge memory problem.
     * <p>
     * This methods actually obtains the data of the stored bitmap from an identifier
     *
     * @param id identifier of the  {@link CacheImBitmap} to load
     * @return a {@link byte} array with the data of the bitmap, or {@link null} if it doesn't exist.
     */
    public byte[] getCachedImBitmapData(long id)
    {
        if(cacheImBitmapSource == null)
        {
            throw new IllegalStateException("CacheImBitmapSource not set");
        }

        byte[] diskCacheBitmapData = cacheImBitmapSource.getCacheImBitmapData(id);

        if(diskCacheBitmapData != null)
        {
            currentSize += diskCacheBitmapData.length;
        }


        return diskCacheBitmapData;
    }

}

