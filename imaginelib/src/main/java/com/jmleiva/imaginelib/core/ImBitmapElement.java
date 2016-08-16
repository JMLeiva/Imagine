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
import android.os.Handler;
import android.os.Looper;

import com.jmleiva.imaginelib.view.ImBitmapView;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a single "version" (size) of a {@link Bitmap}
 */
public class ImBitmapElement
{

    private ImBitmap parent;
    protected Bitmap bitmap;
    protected long lastUsedTimestamp;
    public int sizeFactor;
    List<ImBitmapView> boundedImBitmapViews;
    ImBitmapManager imBitmapManager;

    /**
     * ImBitmapElement constructor
     * @param bitmap {@link Bitmap} used by this instance
     * @param sizeFactor Size factor (power of 2) relative to the size of the original Bitmap
     */
    public ImBitmapElement(ImBitmap parent, Bitmap bitmap, int sizeFactor, ImBitmapManager imBitmapManager)
    {
        if(bitmap == null)
        {
            throw new IllegalArgumentException();
        }

        lastUsedTimestamp = 0;
        this.sizeFactor = sizeFactor;
        this.bitmap = bitmap;
        this.parent = parent;

        boundedImBitmapViews = new ArrayList<>();
    }

    /**
     * @return the {@link ImBitmap} owner of this {@link ImBitmapElement}
     */
    public ImBitmap getParent()
    {
        return parent;
    }

        /**
     * Links this {@link ImBitmapElement} with a {@link ImBitmapView}
     * @see  {@link ImBitmap#retain(int, int, ImBitmapView)}
     */
    public void retain(ImBitmapView ImBitmapView)
    {
        lastUsedTimestamp = System.nanoTime();
        boundedImBitmapViews.add(ImBitmapView);
    }

    /**
     * Unlinks this {@link ImBitmapElement} from a {@link ImBitmapView}
     * @see  {@link ImBitmap#release(int width, int height, ImBitmapView ImBitmapView)}
     */
    public void release(ImBitmapView ImBitmapView)
    {
        boundedImBitmapViews.remove(ImBitmapView);
    }

    /**
     * @return true if this {@link ImBitmapElement} is not linked (retained) to any {@link ImBitmapView}, false otherwise
     */
    public boolean isSafeToDispose()
    {
        return boundedImBitmapViews.size() <= 0;
    }

    /**
     * @return timestamp in millis of the last time this picture was loaded inside a Layout.
     */
    public long lastUsedTimesatmp()
    {
        return lastUsedTimestamp;
    }

    /**
     * Destroys the ImBitmapElement.
     * <p>
     * If bound to any {@link ImBitmapView}, it's unset and unbounded. Then the Bitmap is recycled.
     */
    public void dispose()
    {
        Handler mHandler = new Handler(Looper.getMainLooper());

        mHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                List<ImBitmapView> tempBoundImBitmapViews = new ArrayList<ImBitmapView>(boundedImBitmapViews);
                for(ImBitmapView imBitmapView : tempBoundImBitmapViews)
                {
                    imBitmapView.removeImBitmap();
                }

                if(bitmap != null && !bitmap.isRecycled())
                {
                    if(imBitmapManager != null)
                    {
                        imBitmapManager.onMemoryDecreased(bitmap.getRowBytes() * bitmap.getHeight());
                    }

                    bitmap.recycle();
                }

                bitmap = null;
            }
        });
    }

    /**
     * @return the Raw {@link Bitmap} of this instance
     */
    public Bitmap getBitmap()
    {
        return bitmap;
    }

    /**
     * @return {@code true} if this instance has been disposed using {@link ImBitmapElement#dispose()}, {@code false} otherwise
     */
    public boolean isDisposed()
    {
        return bitmap == null;
    }
}