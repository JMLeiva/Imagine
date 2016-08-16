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
import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * This class is resposible of managing all the {@link ImBitmap} objects used within the application.
 * <p>
 * It also keeps track of the ammount of memory used only by {@link ImBitmap} instances, and makes sure
 * the MaxMemory (fixed to an eighth of max memory asigned to the app by the JVM) is never exceeded (except in some cases),
 * reducing the memory to another fixed value (trimMemory, fixed to a half of the MaxMemory)
 */
public class ImBitmapManager
{
    final static String TAG = "ImBitmapManager";

    /**
     * Color configuration used to decode Bitmaps.
     * <p>
     * RGB 565 chosen by now, as it has a good balance between memory usage and quality
     * <p>
     * If needed, it can be changed in the future
     */
    public final static Bitmap.Config COLOR_CONFIG = Bitmap.Config.RGB_565;

    private final static String REMOTE_BITMAP_ID_PREFIX = "REM_BIT_";
    private final static String RESOURCE_BITMAP_ID_PREFIX = "RES_BIT_";
    private final static String FILE_BITMAP_ID_PREFIX = "FIL_BIT_";
    private final static String ALBUM_PHOTO_BITMAP_ID_PREFIX = "ALB_BIT";
    private final static String RAW_BITMAP_ID_PREFIX = "RAW_BIT";


    /**
     * Amount of memory in BYTES, that, when exceeded, the {@link ImBitmapManager#trimMemory()} operation is performed
     */
    private long MAX_MEMORY;

    /**
     * Amount of memory in BYTES, that the {@link ImBitmapManager#trimMemory()} operation is performed tries to achieve after performed.
     */
    private long TRIM_MEMORY;

    private  Map<String, ImBitmap> imBitmapMap;

    private long currentSize;

    private int maxImBitmapsAlive = 1;

    private Context context;

    public ImBitmapManager(Context context)
    {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory());
        // Use 1/8th of the available memory for this memory cache.
        MAX_MEMORY = maxMemory / 8;
        TRIM_MEMORY = MAX_MEMORY / 2;

        // t_bitmap = new RemoteBitmap("t_bitmap",
        // "http://img4.wikia.nocookie.net/__cb20140501150431/walkingdead/images/3/3d/Wat.jpeg");

        imBitmapMap = new HashMap<>();
        currentSize = 0;

        this.context = context;

        Log.i(TAG, String.format("Starting ImBitmapManager. MAX_MEMORY: %d kb, TRIM_MEMORY %d kb", MAX_MEMORY / 1024,
                TRIM_MEMORY / 1024));
    }

    /**
     * Called when upper memory limit is reached.
     * <p>
     * The only way to reduce memory is freeing {@link ImBitmap} instances.
     * <p>
     * There are two criteria to select which {@link ImBitmap} are removed first:
     * <ul>
     * <li> Disposable {@link ImBitmap}, i.e. {@link ImBitmap} that are no currently visible to the user, are removed FIRST.
     * <li> Last used {@link ImBitmap}, i.e. {@link ImBitmap} showed most recently, are removed LAST.
     * <li> Disposable {@link ImBitmap} are always removed before Non-Disposable {@link ImBitmap} not matter when they were
     * used last.
     * <li> maxImBitmapsAlive is respected only for Non-Disposable {@link ImBitmap}. Even if the MaxMemory limit was exceded.
     * This means that when this method is called, it's guaranteed that at least [maxImBitmapsAlive] number of Non-Disposable {@link ImBitmap}
     * will be left alive.
     */
    void trimMemory()
    {
        Log.i(TAG, String.format("**** Starting TRIM due exceding MAX_MEMORY: %d kb ****", MAX_MEMORY / 1024));

        // Two lists, first with Safe to dispose bitmaps, second with the others
        List<ImBitmapElement> disposableImBitmaps1 = new ArrayList<>();
        List<ImBitmapElement> disposableImBitmaps2 = new ArrayList<>();

        Iterator<Map.Entry<String, ImBitmap>> it = imBitmapMap.entrySet().iterator();

        while (it.hasNext())
        {
            Map.Entry<String, ImBitmap> pairs = it.next();

            ImBitmap imBitmap = pairs.getValue();

            for(ImBitmapElement imBitmapElement : imBitmap.imBitmapElements)
            {
                if (imBitmapElement.getBitmap() != null)
                {
                    boolean atEnd = true;

                    List<ImBitmapElement> disposableImBitmaps;

                    if (imBitmapElement.isSafeToDispose())
                    {
                        disposableImBitmaps = disposableImBitmaps1;
                    }
                    else
                    {
                        disposableImBitmaps = disposableImBitmaps2;
                    }

                    for (int i = 0; i < disposableImBitmaps.size(); i++)
                    {
                        if (disposableImBitmaps.get(i).lastUsedTimesatmp() > imBitmapElement.lastUsedTimesatmp())
                        {
                            atEnd = false;
                            disposableImBitmaps.add(i, imBitmapElement);
                            break;
                        }
                    }

                    if (atEnd)
                    {
                        disposableImBitmaps.add(imBitmapElement);
                    }
                }
            }
        }

        int safeDisposed = 0;
        int notSafeDisposed = 0;

        while (currentSize > TRIM_MEMORY && disposableImBitmaps1.size() > 0)
        {
            ImBitmapElement bitmapToDispose = disposableImBitmaps1.remove(0);
            bitmapToDispose.dispose();
            safeDisposed++;
        }

        // If safe to dispose bitmaps where NOT enought...
        // This one uses MAX_MEMORY instead of TRIM_MEMORY, and leaves at least
        // maxImBitmapsAlive ImBitmap alive. (This is experimental)
        while (currentSize > MAX_MEMORY && disposableImBitmaps2.size() > maxImBitmapsAlive)
        {
            ImBitmapElement bitmapToDispose = disposableImBitmaps2.remove(0);
            bitmapToDispose.dispose();
            notSafeDisposed++;
        }

        Log.i(TAG, String.format("%d Safe Bitmaps disposed. %d Not-Safe bitmaps disposed", safeDisposed, notSafeDisposed));

        Log.i(TAG, String.format("**** TRIM ended. CurrentSize: %d kb, TRIM_MEMORY: %d kb ****", currentSize / 1024,
                TRIM_MEMORY / 1024));
    }

    /**
     * maxImBitmapsAlive setter.
     * maxImBitmapsAlive is respected only for Non-Disposable {@link ImBitmap}.
     * Even if the MaxMemory limit was exceded.
     * <p>
     * This means that when trimMemory method is called, it's guaranteed that at least [maxImBitmapsAlive] number of Non-Disposable {@link ImBitmap}
     * will be left alive.
     *
     *  @param n value to update to maxImBitmapsAlive
     */
    public void setMaxImBitmapsAlive(int n)
    {
        maxImBitmapsAlive = n;
    }

    /**
     *
     * @return current max cache bitmaps alive allowed
     */
    public int getMaxImBitmapsAlive()
    {
        return maxImBitmapsAlive;
    }

    /**
     * Called internally each time a {@link ImBitmap} allocates memory
     * @param bytes number of bytes allocated by the {@link ImBitmap}
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
     * Called internally each time a {@link ImBitmap} deallocates memory
     * @param bytes number of bytes deallocated by the {@link ImBitmap}
     */
    void onMemoryDecreased(long bytes)
    {
        currentSize -= bytes;
        Log.i(TAG, String.format("Memory decreased by %d kb, current size is %d kb", bytes / 1024, currentSize / 1024));
    }

    /**
     * Internal helper to avoid path errors when a path used to create {@link ImBitmap} contains invalid characters for a path.
     * @param path the path used to create a new {@link ImBitmap}
     * @return a safe version of the path parameter
     */
    private String safePath(String path)
    {
        String filtered = path.replace('\\', '-').
                replace('/', '-').
                replace('.', '-').
                replace(':', '-');

        return filtered;
    }

    /**
     * Searches for an existent {@link ImRemoteBitmap} with the urlPath specified, or creates a new one otherwise
     * @param urlPath of the picture to be assigned to the {@link ImRemoteBitmap}
     * @return a {@link ImRemoteBitmap} from the urlPath parameter
     */
    public ImRemoteBitmap getRemoteBitmap(String urlPath)
    {
        String key = REMOTE_BITMAP_ID_PREFIX + safePath(urlPath);

        if (imBitmapMap.get(key) == null)
        {
            ImRemoteBitmap newRemoteBitmap = new ImRemoteBitmap(key, urlPath);

            imBitmapMap.put(key, newRemoteBitmap);

            return newRemoteBitmap;
        }

        return (ImRemoteBitmap) imBitmapMap.get(key);
        // return t_bitmap;
    }

    /**
     * Searches for an existent {@link ImResourceBitmap} with the resourceId specified, or creates a new one otherwise
     * @param resourceId of the picture to be assigned to the {@link ImResourceBitmap}
     * @return a {@link ImResourceBitmap} from the resourceId parameter
     */
    public ImResourceBitmap getResourceBitmap(int resourceId)
    {
        String key = RESOURCE_BITMAP_ID_PREFIX + safePath(Integer.toString(resourceId));

        if (imBitmapMap.get(key) == null)
        {
            ImResourceBitmap newResourceBitmap = new ImResourceBitmap(key, resourceId, context);

            imBitmapMap.put(key, newResourceBitmap);

            return newResourceBitmap;
        }
        return (ImResourceBitmap) imBitmapMap.get(key);
    }

    /**
     * Searches for an existent {@link ImFileBitmap} with the path specified, or creates a new one otherwise
     * @param path of the picture to be assigned to the {@link ImFileBitmap}
     * @return a {@link ImFileBitmap} from the path parameter
     */
    public ImFileBitmap getFileBitmap(String path)
    {
        String key = FILE_BITMAP_ID_PREFIX + safePath(path);

        if (imBitmapMap.get(key) == null)
        {
            ImFileBitmap newFileBitmap = new ImFileBitmap(key, path);

            imBitmapMap.put(key, newFileBitmap);

            return newFileBitmap;
        }

        return (ImFileBitmap) imBitmapMap.get(key);
    }

    /**
     * Searches for an existent {@link ImRawBitmap} with the bitmap (HashCode) specified, or creates a new one otherwise
     * @param bitmap of the picture to be assigned to the {@link ImRawBitmap}
     * @return a {@link ImRawBitmap} from the bitmap (HashCode) parameter
     */
    public ImRawBitmap getRawBitmap(Bitmap bitmap)
    {
        String key = RAW_BITMAP_ID_PREFIX + safePath(Integer.toString(bitmap.hashCode()));

        if (imBitmapMap.get(key) == null)
        {
            ImRawBitmap cacheRawBitmap = new ImRawBitmap(bitmap, key);

            imBitmapMap.put(key, cacheRawBitmap);

            return cacheRawBitmap;
        }

        return (ImRawBitmap) imBitmapMap.get(key);
    }

    /**
     * Searchs a {@link ImBitmap}
     * @param key identifier of the {@link ImBitmap} to search
     * @return {@link ImBitmap} with an identifiar matching the key parameter, or null otherwise
     */
    public ImBitmap findImBitmapByKey(String key)
    {
        return imBitmapMap.get(key);
    }
}
