package com.jess.camerafilters.base;

import android.content.Context;

import com.jess.camerafilters.filter.IFilter;

/**
 * Created by jess on 8/17/16 17:17
 * Contact with jess.yan.effort@gmail.com
 */
public interface onExtFilterListener {
    IFilter onCreateExtFilter(Context context, int index);
}
