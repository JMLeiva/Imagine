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


import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.jmleiva.imaginelib.core.ImBitmap;
import com.jmleiva.imaginelib.view.ImBitmapView;

public class ImBitmapHolder extends RecyclerView.ViewHolder
{
    ImBitmapView imBitmapView;

    public ImBitmapHolder(View itemView)
    {
        super(itemView);
        setup(null);
    }

    public void setup(ImBitmap imBitmap)
    {
        imBitmapView = (ImBitmapView)itemView.findViewById(R.id.imBitmapView);

        imBitmapView.setImBitmap(imBitmap, R.drawable.placeholder);
    }
}
