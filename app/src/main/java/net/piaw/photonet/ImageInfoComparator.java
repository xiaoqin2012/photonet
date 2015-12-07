package net.piaw.photonet;

import java.util.Comparator;

/**
 * Created by xiaoqin on 11/13/2015.
 */
public class ImageInfoComparator implements Comparator<ImageInfo> {
    @Override
    public int compare(ImageInfo lhs, ImageInfo rhs) {
        if (lhs.date == null || rhs.date == null)
            return 0;
        if (lhs.date.getTime() - rhs.date.getTime() > 0)
            return 1;
        if (lhs.date.getTime() - rhs.date.getTime() < 0)
            return -1;

        return 0;
    }
}
