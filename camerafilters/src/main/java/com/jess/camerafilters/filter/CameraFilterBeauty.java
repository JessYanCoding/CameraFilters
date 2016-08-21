package com.jess.camerafilters.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.jess.camerafilters.R;
import com.jess.camerafilters.util.GlUtil;

import java.nio.FloatBuffer;

/**
 * Created by shengwenhui on 16/3/3.
 */
public class CameraFilterBeauty extends CameraFilter {
    private int singleStepOffset;

    private static final float offset_array[] = {
            2, 2,
    };

    public CameraFilterBeauty(Context context,boolean isUseQiniu) {
        super(context, isUseQiniu);
        offset_array[0] = offset_array[0] / 90;
        offset_array[1] = offset_array[1] / 160;
    }

    @Override
    protected int createProgram(Context applicationContext, boolean isUseQiniu) {
        return GlUtil.createProgram(applicationContext, isUseQiniu ? R.raw.vertex_shader_qiniu : R.raw.vertex_shader,
                R.raw.fragment_shader_beauty);
    }

    @Override
    protected void getGLSLValues() {
        super.getGLSLValues();

        singleStepOffset = GLES20.glGetUniformLocation(mProgramHandle, "singleStepOffset");
    }

    @Override
    protected void bindGLSLValues(float[] mvpMatrix, FloatBuffer vertexBuffer, int coordsPerVertex,
                                  int vertexStride, float[] texMatrix, FloatBuffer texBuffer, int texStride) {
        super.bindGLSLValues(mvpMatrix, vertexBuffer, coordsPerVertex, vertexStride, texMatrix,
                texBuffer, texStride);

        GLES20.glUniform2fv(singleStepOffset, 1, offset_array, 0);
    }
}

