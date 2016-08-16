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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button remoteButton = (Button)findViewById(R.id.button_remote);
        remoteButton.setOnClickListener(this);

        Button resourceButton = (Button)findViewById(R.id.button_resource);
        resourceButton.setOnClickListener(this);

        Button fileButton = (Button)findViewById(R.id.button_file);
        fileButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.button_remote:
                onRemoteSample();
                break;
            case R.id.button_resource:
                onResourceSample();
                break;
            case R.id.button_file:
                onFileSample();
                break;
        }
    }

    private void onRemoteSample()
    {
        Intent intent = new Intent(this, RemoteSampleActivity.class);
        startActivity(intent);
    }

    private void onResourceSample()
    {
        Intent intent = new Intent(this, ResourceSampleActivity.class);
        startActivity(intent);
    }

    private void onFileSample()
    {
        Intent intent = new Intent(this, FileSampleActivity.class);
        startActivity(intent);
    }
}
