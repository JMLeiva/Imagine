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

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.jmleiva.imaginelib.core.ImBitmapManager;

public class FileSampleActivity extends Activity
{
    RecyclerView recyclerView;
    ImBitmapManager imBitmapManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remote_sample_activity);

        imBitmapManager = new ImBitmapManager(this);

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setAdapter(new FileSampleAdapter(this, imBitmapManager));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
