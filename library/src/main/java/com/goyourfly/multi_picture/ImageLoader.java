package com.goyourfly.multi_picture;

import android.net.Uri;
import android.widget.ImageView;

/**
 * @author 诸葛不亮
 * @version 1.0
 * @description
 */

public interface ImageLoader {
    void loadImage(ImageView image, Uri uri);
}
