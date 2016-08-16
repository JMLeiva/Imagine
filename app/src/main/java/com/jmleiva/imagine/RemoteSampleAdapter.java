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

import com.jmleiva.imaginelib.core.ImBitmapManager;
import com.jmleiva.imaginelib.core.ImRemoteBitmap;

public class RemoteSampleAdapter extends SampleAdapter
{
    public RemoteSampleAdapter(Context context, ImBitmapManager imBitmapManager) {
        super(context, imBitmapManager);
    }

    @Override
    protected void initializeBitmaps(Context context)
    {
        bitmaps.add(imBitmapManager.getRemoteBitmap("http://i.telegraph.co.uk/multimedia/archive/03589/Wellcome_Image_Awa_3589699k.jpg"));
        bitmaps.add(imBitmapManager.getRemoteBitmap("https://www.nasa.gov/sites/default/files/styles/image_card_4x3_ratio/public/images/115334main_image_feature_329_ys_full.jpg"));
        bitmaps.add(imBitmapManager.getRemoteBitmap("http://cdn.slidesharecdn.com/profile-photo-brighteyes-48x48.jpg?cb=1298696647"));
        bitmaps.add(imBitmapManager.getRemoteBitmap("https://khms1.googleapis.com/kh?v=98&hl=en&deg=0&&x=77201&y=108044&z=18"));
        bitmaps.add(imBitmapManager.getRemoteBitmap("http://homepages.inf.ed.ac.uk/rbf/HIPR2/images/txt2fil1.gif"));
        bitmaps.add(imBitmapManager.getRemoteBitmap("http://images.math.cnrs.fr/IMG/png/section8-image.png"));
        bitmaps.add(imBitmapManager.getRemoteBitmap("https://www.cs.cmu.edu/~chuck/lennapg/len_std.jpg"));
        bitmaps.add(imBitmapManager.getRemoteBitmap("http://www.cnmuqi.com/data/out/57/image-7617423.png"));
        bitmaps.add(imBitmapManager.getRemoteBitmap("http://static2.businessinsider.com/image/55d1eb352acae7c23f8be1c4-480/austin-texas.jpg"));
        bitmaps.add(imBitmapManager.getRemoteBitmap("http://i.forbesimg.com/media/lists/places/austin-tx_416x416.jpg"));
        bitmaps.add(imBitmapManager.getRemoteBitmap("http://theeconomiccollapseblog.com/wp-content/uploads/2011/05/What-Is-The-Best-Place-To-Live-In-The-United-States-To-Prepare-For-The-Coming-Economic-Collapse-250x166.jpg"));
        bitmaps.add(imBitmapManager.getRemoteBitmap("http://i.telegraph.co.uk/multimedia/archive/01934/HMS-Belfast-WW2_1934138b.jpg"));
        bitmaps.add(imBitmapManager.getRemoteBitmap("http://www.magictricks.com/assets/images/library/bios/magic-hat-electric.jpg"));
        bitmaps.add(imBitmapManager.getRemoteBitmap("http://pngimg.com/upload/star_PNG1580.png"));
        bitmaps.add(imBitmapManager.getRemoteBitmap("http://www.leonardodavinci.net/images/leonardo-da-vinci.jpg"));
        bitmaps.add(imBitmapManager.getRemoteBitmap("http://cdn-img.health.com/sites/default/files/migration/images/slides/magic-hat-makeup-400x400.jpg"));
        bitmaps.add(imBitmapManager.getRemoteBitmap("https://i.kinja-img.com/gawker-media/image/upload/s--h73zmOEU--/c_scale,fl_progressive,q_80,w_800/mslg7z1kkbt7exoiab9e.jpg"));

        for(int i = 0; i < 50; i++)
        {
            String str = Integer.toString(i);
            ImRemoteBitmap imBitamp = imBitmapManager.getRemoteBitmap("http://lorempixel.com/400/200/rand-" + str);
            bitmaps.add(imBitamp);
        }
    }
}
