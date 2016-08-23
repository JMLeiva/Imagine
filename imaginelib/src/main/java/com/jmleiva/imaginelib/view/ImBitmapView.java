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


package com.jmleiva.imaginelib.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.widget.ImageView;

import com.jmleiva.imaginelib.core.ImBitmap;
import com.jmleiva.imaginelib.core.ImBitmapElement;

/**
 *
 * Used to display a {@link ImBitmap} since they're not loaded instantly
 * and it's impossible to know the measures of the bitmap,
 * this class holds a space to show it and resize it's own measures to
 * fit the bitmap once is's loaded.
 *
 */
public class ImBitmapView extends ImageView {

    public interface OnImBitmapViewRendered {
        void onRendered();
    }

    ImBitmap cacheBitmap;
    ImBitmapElement cacheBitmapElement;
    OnImBitmapViewRendered renderedListener;
    int currentBitmapHash;
    AsyncTask currenLoadingAsyncTask;

    public ImBitmapView(Context context) {
        super(context);
    }

    public ImBitmapView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // TODO ADD ROUNDED CORNERS SUPPORT
		/*
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.RoundedCacheImageView, 0, 0);

		try {
			hasRoundedCorners = a.getBoolean(R.styleable.RoundedCacheImageView_rounded_corners, false);
			cornerRadius = a.getInteger(R.styleable.RoundedCacheImageView_corners_radius, 0);
		} finally {
			a.recycle();
		}
		*/
    }

    public ImBitmapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnRenderedListener(OnImBitmapViewRendered renderedListener) {
        this.renderedListener = renderedListener;
    }

    public ImBitmap getImBitmap() {
        return cacheBitmap;
    }

    public void setImBitmap(ImBitmap cacheBitmap) {
        setImBitmap(cacheBitmap, null);
    }

    /**
     * Used to update the {@link ImBitmap},
     * @param cacheBitmap Bitmap to update.
     * @param defaultBitmap Bitmap to be displayed if this the loading of the
     * cache bitmap fails for some reason.
     */
    public void setImBitmap(ImBitmap cacheBitmap, Bitmap defaultBitmap) {
        if (this.cacheBitmap == cacheBitmap) {

            if(cacheBitmapElement != null)
            {
                this.invalidate();
                if (renderedListener != null)
                {
                    renderedListener.onRendered();
                }
                return;
            }
        }

        if(currenLoadingAsyncTask != null && currenLoadingAsyncTask.getStatus() != AsyncTask.Status.FINISHED)
        {
            currenLoadingAsyncTask.cancel(true);
        }

        if (this.cacheBitmapElement != null) {
            this.cacheBitmapElement.release(this);
            this.cacheBitmapElement = null;
        }



        if (cacheBitmap == null) {
            if (defaultBitmap != null) {
                setImageBitmap(defaultBitmap);
            }
            this.cacheBitmap = null;
            return;
        }

        this.cacheBitmap = cacheBitmap;

        if (getWidth() != 0 && getHeight() != 0) {
            if (defaultBitmap != null && !cacheBitmap.isReady(getWidth(), getHeight())) {
                setImageBitmap(defaultBitmap);
            }

            onSizeChanged(getWidth(), getHeight(), 0, 0);
        }
    }

    /**
     * Used to update the {@link ImBitmap},
     * @param cacheBitmap Bitmap to update.
     * @param defaultDrawableId drawable to be displayed if this the loading of the
     * cache bitmap fails for some reason.
     */
    public void setImBitmap(ImBitmap cacheBitmap, int defaultDrawableId) {
        if (this.cacheBitmap == cacheBitmap) {

            if(cacheBitmapElement != null)
            {
                this.invalidate();
                if (renderedListener != null)
                {
                    renderedListener.onRendered();
                }
                return;
            }
        }

        if(currenLoadingAsyncTask != null && currenLoadingAsyncTask.getStatus() != AsyncTask.Status.FINISHED)
        {
            currenLoadingAsyncTask.cancel(true);
        }

        if (this.cacheBitmapElement != null) {
            this.cacheBitmapElement.release(this);
            this.cacheBitmapElement = null;
        }

        if (cacheBitmap == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                setImageDrawable(getResources().getDrawable(defaultDrawableId, null));
            }
            else
            {
                setImageDrawable(getResources().getDrawable(defaultDrawableId));
            }

            this.cacheBitmap = null;
            return;
        }

        this.cacheBitmap = cacheBitmap;

        if (getWidth() != 0 && getHeight() != 0) {
            if (!cacheBitmap.isReady(getWidth(), getHeight())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    setImageDrawable(getResources().getDrawable(defaultDrawableId, null));
                }
                else
                {
                    setImageDrawable(getResources().getDrawable(defaultDrawableId));
                }
            }

            onSizeChanged(getWidth(), getHeight(), 0, 0);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ViewParent parent = this.getParent();
        LayoutParams layoutParams = this.getLayoutParams();

        if (parent != null && layoutParams.width == LayoutParams.WRAP_CONTENT && layoutParams.height == LayoutParams.WRAP_CONTENT) {
            final int widthSpecMode = MeasureSpec.getSize(widthMeasureSpec);
            final int heightSpecMode = MeasureSpec.getSize(heightMeasureSpec);

            if (widthSpecMode != 0 && heightSpecMode != 0) {
                super.setMeasuredDimension(widthSpecMode, heightSpecMode);
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (cacheBitmap == null) {
            return;
        }

        if (w == 0 || h == 0) {
            return;
        } else {
            if (cacheBitmapElement != null && !cacheBitmapElement.isDisposed()) {
                // Here it should check that the sizes of the alreaddy loaded
                // ImBitmapElement an the "view size" are coherent
                setImageBitmap(cacheBitmapElement.getBitmap());
                return;
            }

            currenLoadingAsyncTask = cacheBitmap.getBitmapAsync(getWidth(), getHeight(), new ImBitmap.OnGetBitmapListener()
            {
                @Override
                public void onComplete(ImBitmapElement bitmapElement)
                {
                    if(bitmapElement.getParent() != cacheBitmap)
                    {
                        return;
                    }

                    cacheBitmapElement = bitmapElement;

                    setImageBitmap(bitmapElement.getBitmap());
                    bitmapElement.retain(ImBitmapView.this);

                    if(renderedListener != null)
                    {
                        renderedListener.onRendered();
                    }
                }

                @Override
                public void onError(String message)
                {

                }
            });
        }
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            currentBitmapHash = 0;
        } else {
            currentBitmapHash = bitmap.hashCode();
        }

        super.setImageBitmap(bitmap);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (cacheBitmapElement != null) {
            if (cacheBitmapElement.getBitmap() != null) {
                if (cacheBitmapElement.getBitmap().hashCode() != currentBitmapHash) {
                    setImageBitmap(cacheBitmapElement.getBitmap());

                    if (renderedListener != null) {
                        renderedListener.onRendered();
                    }
                }
            }
        }

        super.onDraw(canvas);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (cacheBitmapElement != null) {
            cacheBitmapElement.release(this);
        }
    }

    public void removeImBitmap() {
        if (cacheBitmapElement != null) {
            cacheBitmapElement.release(this);
            cacheBitmapElement = null;
        }

        setImageBitmap(null);

        if (cacheBitmap != null) {
            cacheBitmap = null;
        }
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
}