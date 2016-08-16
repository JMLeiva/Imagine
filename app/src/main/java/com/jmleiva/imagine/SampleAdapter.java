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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jmleiva.imaginelib.core.ImBitmap;
import com.jmleiva.imaginelib.core.ImBitmapManager;

import java.util.ArrayList;
import java.util.List;

public abstract  class SampleAdapter extends RecyclerView.Adapter<ImBitmapHolder>
{
    LayoutInflater layoutInflater;
    protected List<ImBitmap> bitmaps;
    protected ImBitmapManager imBitmapManager;

    public SampleAdapter(Context context, ImBitmapManager imBitmapManager)
    {
        layoutInflater = LayoutInflater.from(context);
        bitmaps = new ArrayList<>();
        this.imBitmapManager = imBitmapManager;

        initializeBitmaps(context);
    }

    protected abstract void initializeBitmaps(Context context);

    @Override
    public ImBitmapHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = layoutInflater.inflate(R.layout.im_bitmap_row, parent, false);
        ImBitmapHolder viewHolder = new ImBitmapHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ImBitmapHolder holder, int position)
    {
       holder.setup(bitmaps.get(position));
    }

    @Override
    public int getItemCount()
    {
        return bitmaps.size();
    }
}
