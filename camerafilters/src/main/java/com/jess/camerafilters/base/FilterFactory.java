package com.jess.camerafilters.base;

import android.content.Context;

import com.jess.camerafilters.R;
import com.jess.camerafilters.filter.CameraFilter;
import com.jess.camerafilters.filter.CameraFilterBeauty;
import com.jess.camerafilters.filter.CameraFilterMosaic;
import com.jess.camerafilters.filter.CameraFilterToneCurve;
import com.jess.camerafilters.filter.IFilter;

/**
 * Created by jess on 8/17/16 15:50
 * Contact with jess.yan.effort@gmail.com
 */
public class FilterFactory {
    private static int[] mCurveArrays = new int[]{
            R.raw.cross_1, R.raw.cross_2, R.raw.cross_3, R.raw.cross_4, R.raw.cross_5,
            R.raw.cross_6, R.raw.cross_7, R.raw.cross_8, R.raw.cross_9, R.raw.cross_10,
            R.raw.cross_11,
    };

    private FilterFactory() {
    }

    /**
     * 内部一共有14种滤镜(包括透明滤镜index为0)
     * @param context
     * @param index
     * @return
     */
    public static IFilter getCameraFilter(Context context, int index) {
        if (index > 3 + mCurveArrays.length - 1 || index < 0) {
            throw new IllegalArgumentException("not have this index.");
        }
        switch (index) {
            case 0:
                return new CameraFilter(context);
            case 1:
                return new CameraFilterBeauty(context);
            case 2:
                return new CameraFilterMosaic(context);
            default:
                return new CameraFilterToneCurve(context,
                        context.getResources().openRawResource(mCurveArrays[index - 3]));
        }
    }
}

