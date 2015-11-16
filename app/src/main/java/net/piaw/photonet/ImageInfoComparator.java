package net.piaw.photonet;

import java.util.Comparator;

/**
 * Created by xiaoqin on 11/13/2015.
 */
public class ImageInfoComparator implements Comparator<ImageInfo> {
    @Override
    public int compare(ImageInfo lhs, ImageInfo rhs) {
        return lhs.dateStr.compareTo(rhs.dateStr);
    }
}
