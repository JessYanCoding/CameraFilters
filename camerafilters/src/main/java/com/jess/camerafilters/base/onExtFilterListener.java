package com.jess.camerafilters.base;

import android.content.Context;

import com.jess.camerafilters.filter.IFilter;

import java.util.List;

/**
 * Created by jess on 8/17/16 17:17
 * Contact with jess.yan.effort@gmail.com
 */
public interface onExtFilterListener {
    void onCreateExtFilter(Context context, List<IFilter> extFilters);
}
