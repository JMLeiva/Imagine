/*
This file is part of PagedRecyclerView

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
import android.os.AsyncTask;
import android.util.Log;

import com.jmleiva.imaginelib.view.ImBitmapView;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a single image reference, with the ability to load the actual  {@link Bitmap} data when needed,
 * and free it when the app runs out of memory, without losing the reference. It means it can be loaded and freed several times.
 * <p>
 * Although each {@link ImBitmap} represent a single image, it can have many  {@link ImBitmapElement} instances representing the
 * same image in different sizes.
 */
public abstract class ImBitmap
{
    final static int SIZE_TOLERANCE = 48;

    protected String cacheBitmapId;
    ObtainBitmapTask obtainBitmapTask;

    int originalWidth;
    int originalHeight;
    protected boolean malformed;

    List<ImBitmapElement> imBitmapElements;
    ImBitmapManager imBitmapManager;

    /**
     * Listener used when a version (specific size) of the image is requested
     * <p>
     * As many subclasses of {@link ImBitmap} (like {@link ImRemoteBitmap} ) must be loaded synchronously, this onItemClickListener
     * is used to notify once the {@link ImBitmapElement} has been loaded, or if an error has occurred.
     */
    public interface OnGetBitmapListener
    {
        void onComplete(ImBitmapElement bitmapElement);
        void onError(String message);
    }

    /**
     * AsyncTask responsible of loading a single {@link ImBitmapElement} on background, and dispatch the corresponding event
     * of the onItemClickListener
     */
    class ObtainBitmapTask extends AsyncTask<Void, Void, ImBitmapElement>
    {
        OnGetBitmapListener callback;
        int width;
        int height;

        public ObtainBitmapTask(int width, int height, OnGetBitmapListener callback)
        {
            this.width = width;
            this.height = height;
            this.callback = callback;
        }

        @Override
        protected ImBitmapElement doInBackground(Void... arg0)
        {
            return getBitmapElement(width, height);
        }

        @Override
        protected void onPostExecute(ImBitmapElement bitmapElement)
        {
            if(bitmapElement == null)
            {
                callback.onError("Error Retrieving bitmap");
                return;
            }

            callback.onComplete(bitmapElement);
        }
    }

    /**
     * {@link ImBitmap}Constructor
     * @param cacheBitmapId {@link String} identifier. Unique for each CacheBitmap, and used to find them if they are cached.
     */
    public ImBitmap(String cacheBitmapId)
    {
        this(cacheBitmapId, null);
    }

    public ImBitmap(String cacheBitmapId, ImBitmapManager imBitmapManager)
    {
        this.cacheBitmapId = cacheBitmapId;


        originalWidth = 0;
        originalHeight = 0;

        malformed = false;

        imBitmapElements = new ArrayList<>();
        this.imBitmapManager = imBitmapManager;
    }

    /**
     * @return the CacheBitmap identifier of this instance
     */
    public String getCacheBitmapId()
    {
        return cacheBitmapId;
    }

    protected void setOriginalSize(int width, int height)
    {
        originalWidth = width;
        originalHeight = height;
    }

    protected ImBitmapElement getImBitmapElementBySize(int width, int height)
    {
        int factor = getResizeFactor(originalWidth, originalHeight, width, height);

        for(ImBitmapElement cacheBitmapElement : imBitmapElements)
        {
            if(cacheBitmapElement.sizeFactor == factor)
            {
                return cacheBitmapElement;
            }
        }

        return null;
    }

    /**
     * "Links" a {@link ImBitmapElement} instance with a {@link ImBitmapView} widget.
     * This is called each time a {@link ImBitmapElement} is assigned to be shown in a {@link ImBitmapView} to
     * ensure that it's delete from it in the case the {@link ImBitmapManager} decides the {@link ImBitmapElement}
     * must be destroyed, avoiding a "trying to use a recycled bitmap" RuntimeException
     *  <p>
     * Note that a single {@link ImBitmapElement} can be Retained with multiple {@link ImBitmapView}
     *
     * @param width used to identify the correct {@link ImBitmapElement}
     * @param height used to identify the correct {@link ImBitmapElement}
     * @param cacheBitmapView {@link ImBitmapView} that will show the {@link ImBitmapElement}
     */
    public void retain(int width, int height, ImBitmapView cacheBitmapView)
    {
        ImBitmapElement imBitmapElement = getImBitmapElementBySize(width, height);

        if(imBitmapElement != null)
        {
            imBitmapElement.retain(cacheBitmapView);
            Log.i("CacheBitmapManager", String.format("%s zoom: %d retainCount: %d", getCacheBitmapId(), imBitmapElement.sizeFactor, imBitmapElement.boundedImBitmapViews.size()));
        }
    }

    /**
     * "Unlinks" a {@link ImBitmapElement} instance with a {@link ImBitmapView} widget.
     * This is called each time a {@link ImBitmapElement} previously being shown in a {@link ImBitmapView} is not being shown by it anymore.
     *
     * @param width used to identify the correct {@link ImBitmapElement}
     * @param height used to identify the correct {@link ImBitmapElement}
     * @param cacheBitmapView {@link ImBitmapView} that will no longer show the {@link ImBitmapElement}
     */
    public void release(int width, int height, ImBitmapView cacheBitmapView)
    {
        ImBitmapElement imBitmapElement = getImBitmapElementBySize(width, height);

        if(imBitmapElement != null)
        {
            imBitmapElement.release(cacheBitmapView);
            Log.i("CacheBitmapManager", String.format("%s zoom: %d retainCount: %d", getCacheBitmapId(), imBitmapElement.sizeFactor, imBitmapElement.boundedImBitmapViews.size()));

            if(imBitmapElement.isSafeToDispose())
            {
                invalidateGetBitmapOnUI();
                Log.i("CacheBitmapManager", "SafeToDispose");
            }

        }
    }

    /**
     * Checks if the {@link ImBitmapElement} with the width height requested, is ready to be used in the UI (already loaded)
     *
     * @param width used to identify the correct {@link ImBitmapElement}
     * @param height used to identify the correct {@link ImBitmapElement}
     * @return true the {@link ImBitmapElement} with the width height requested, is ready to be used in the UI (already loaded), false otherwise
     */
    public boolean isReady(int width, int height)
    {
        ImBitmapElement cacheBitmapElement = getImBitmapElementBySize(width, height);

        return (cacheBitmapElement != null && !cacheBitmapElement.isDisposed());
    }

    /**
     * @return ture if this {@link ImBitmap} is malformed, flase otherwise.
     *
     */
    public boolean isMalformed()
    {
        return malformed;
    }

    /**
     * Abstract method to getAll the actual {@link Bitmap} for a corresponding size.
     * Implemented by each concrete subclass.
     */
    public abstract Bitmap retrieveBitmap(int width, int height);

    /**
     * @return the {@link Bitmap} in its original size.
     */
    public Bitmap retrieveBitmapFull()
    {
        return retrieveBitmap(originalWidth, originalHeight);
    }

    /**
     * Retrieves the {@link ImBitmapElement} in a specific size
     * @param width desired width of the {@link ImBitmapElement}
     * @param height desired height of the {@link ImBitmapElement}
     * @return the {@link ImBitmapElement} that is most near to the desired size
     */
    public ImBitmapElement getBitmapElement(int width, int height)
    {
        if(isMalformed())
        {
            return null;
        }

        ImBitmapElement cacheBitmapElement = getImBitmapElementBySize(width, height);

        if(cacheBitmapElement != null)
        {
            if(cacheBitmapElement.isDisposed())
            {
                imBitmapElements.remove(cacheBitmapElement);
                cacheBitmapElement = null;
            }
            else
            {
                return cacheBitmapElement;
            }
        }


        Bitmap bitmap = retrieveBitmap(width, height);

        if(bitmap != null)
        {
            if(imBitmapManager != null)
            {
                imBitmapManager.onMemoryIncreased(bitmap.getRowBytes() * bitmap.getHeight());
            }


            int factor = getResizeFactor(originalWidth, originalHeight, width, height);

            cacheBitmapElement = new ImBitmapElement(this, bitmap, factor, imBitmapManager);
            imBitmapElements.add(cacheBitmapElement);
        }

        return cacheBitmapElement;
    }

    /**
     *Like {@link ImBitmap#getBitmapAsync(int, int, OnGetBitmapListener)} but for full size
     */
    public void getBitmapFullAsync(OnGetBitmapListener callback)
    {
        getBitmapAsync(originalWidth, originalHeight, callback);
    }

    /**
     * This method uses {@link ObtainBitmapTask} and {@link ImBitmap#getBitmapElement(int, int)} to retrieve a
     * {@link ImBitmapElement} asynchronously
     * @param width desired width of the {@link ImBitmapElement}
     * @param height desired height of the {@link ImBitmapElement}
     * @param callback {@link OnGetBitmapListener} to be called once the operation has finished
     */
    public void getBitmapAsync(int width, int height, OnGetBitmapListener callback)
    {
        if(obtainBitmapTask != null)
        {
            if(obtainBitmapTask.getStatus() != AsyncTask.Status.FINISHED && !obtainBitmapTask.isCancelled())
            {
                obtainBitmapTask.cancel(true);
            }

            obtainBitmapTask = null;
        }

        ImBitmapElement cacheBitmapElement = getImBitmapElementBySize(width, height);

        if(cacheBitmapElement != null && !cacheBitmapElement.isDisposed())
        {
            if(cacheBitmapElement.isDisposed())
            {
                imBitmapElements.remove(cacheBitmapElement);
                obtainBitmapTask = new ObtainBitmapTask(width, height, callback);
                obtainBitmapTask.execute();
            }
            else
            {
                callback.onComplete(cacheBitmapElement);
            }
        }
        else
        {
            obtainBitmapTask = new ObtainBitmapTask(width, height, callback);
            obtainBitmapTask.execute();
        }
    }

    /**
     * Calls {@link ImBitmap#preload(int, int)} in a separated thread
     * @param width desired width of the {@link ImBitmapElement} to be preloaded
     * @param height desired height of the {@link ImBitmapElement} to be preloaded
     */
    public void preloadAsync(final int width, final int height)
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                preload(width, height);
            }
        });
        thread.start();
    }

    /**
     * Similar to {@link ImBitmap#getBitmapElement(int, int)}. Used to preload a {@link ImBitmapElement},
     * so it can be ready to use in the future
     * @param width desired width of the {@link ImBitmapElement} to be preloaded
     * @param height desired height of the {@link ImBitmapElement} to be preloaded
     */
    public void preload(int width, int height)
    {
        getBitmapElement(width, height);
    }

    /**
     * Used internally to cancel a {@link ObtainBitmapTask} currently in process
     * @return true if the {@link ObtainBitmapTask} was successfully cancelled, false otherwise.
     */
    private boolean invalidateGetBitmapOnUI()
    {
        boolean result = false;

        if(obtainBitmapTask != null)
        {
            if(obtainBitmapTask.getStatus() != AsyncTask.Status.FINISHED && !obtainBitmapTask.isCancelled())
            {
                obtainBitmapTask.cancel(true);
                result = true;
            }

            obtainBitmapTask = null;
        }

        return result;
    }

    /**
     * @return a {@link ImBitmapElement} with the desired size if exists. It will return null if it wasn't previously created.
     * Use this only in special cases
     */
    public Bitmap getRawBitmap(int width, int height)
    {
        ImBitmapElement cacheBitmapElement = getImBitmapElementBySize(width, height);
        return cacheBitmapElement.bitmap;
    }

    /**
     * Used by some implementations to getAll a Path related to the CacheBitmap
     * @return this base implementation returns always {@code null}
     */
    public String getPath()
    {
        return null;
    }

    /**
     * Helper function to calculate the Resize factor (power of 2) taking into account the original size of the image, and the desired size.
     */
    protected int getResizeFactor(int inputWidth, int inputHeight, int outputWidth, int outputHeight)
    {
        //TEMP
        if(outputWidth == 0 || outputHeight == 0)
        {
            return 1;
        }

        int factor = 1;

        if (inputHeight > outputHeight || inputWidth > outputWidth)
        {
            final int halfHeight = inputHeight / 2;
            final int halfWidth = inputWidth / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / factor) > outputHeight && (halfWidth / factor) > outputWidth)
            {
                factor *= 2;
            }
        }

        return factor;
    }
}