package com.jess.camerafilters.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;

import com.jess.camerafilters.entity.FilterInfo;
import com.jess.camerafilters.filter.IFilter;
import com.jess.camerafilters.util.GlUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jerikc on 16/2/23.
 */
public class FilterManager {
    private final String TAG = this.getClass().getSimpleName();
    private int mSurfaceWidth;

    private int mSurfaceHeight;
    // Used for off-screen rendering.
    private int mOffscreenTexture;

    private int mFramebuffer;
    private FullFrameRect mFullScreen;


    private boolean mEnable;
    private FilterInfo mDefaultFilter;
    private FilterInfo mCurrentFilter;//现在使用的滤镜
    private FilterInfo mNewFilter;//新的滤镜
    private Context mContext;
    private onExtFilterListener mExtFilterListener;
    private List<IFilter> mExtFilters;//扩展的fitter,因为创建Filter必须在4个回调方法中.

    private FilterManager(Builder builder) {
        this.mEnable = builder.isEnable;
        this.mContext = builder.mContext;
        this.mDefaultFilter = builder.mDefaultFilter;
        this.mExtFilterListener = builder.mExtFilterListener;
    }

    public static Builder builder() {
        return new Builder();
    }


    public void updateSurfaceSize(int width, int height) {
        if (!mEnable) {
            return;
        }
        mSurfaceWidth = width;
        mSurfaceHeight = height;
    }

    public void initialize() {
        if (!mEnable) {
            return;
        }

        if (mFullScreen != null) {
            mFullScreen.release(false);
        }

        if (mExtFilterListener != null) {
            mExtFilters = new ArrayList<>();//扩展的fitter
            mExtFilterListener.onCreateExtFilter(mContext, mExtFilters);
        }
        if (mDefaultFilter == null) {//如果用户没有自定义默认的滤镜,则使用没有任何效果的滤镜
            mDefaultFilter = new FilterInfo(false, 0);
        }
        mFullScreen = new FullFrameRect(getFilter(mDefaultFilter));//获得滤镜,并设置给FullFrameRect

        mCurrentFilter = mNewFilter = mDefaultFilter;//初始化三个参数指向同一个滤镜

        mOffscreenTexture = 0;
    }


    /**
     * 判断使用内置滤镜或者扩展的滤镜
     */
    private IFilter getFilter(FilterInfo info) {
        if (info.isExt) {
            if (mExtFilterListener == null)//说明没有添加创建额外滤镜的监听,所以没有生成额外滤镜的列表
                throw new IllegalStateException("ExtFilterListener not setup");
            if (info.index > mExtFilters.size() - 1 || info.index < 0)
                throw new IllegalArgumentException("extFilters not have this index.");

            return mExtFilters.get(info.index);
        } else {
            return FilterFactory.getCameraFilter(mContext, info.index);
        }
    }

    /**
     * @param filter
     * @author: jess
     * @date 8/18/16 10:44 AM
     * @description: 改变过滤器
     */
    public void changeFilter(FilterInfo filter) {
        this.mNewFilter = filter;
    }

    /**
     * Creates a texture object suitable for use with drawFrame().
     */
    public int createTexture() {
        if (mFullScreen == null)
            throw new IllegalStateException("FullScreen is null,please invoke initialize");

        return mFullScreen.createTexture();
    }

    public int createTexture(Bitmap bitmap) {
        if (mFullScreen == null)
            throw new IllegalStateException("FullScreen is null,please invoke initialize");

        return mFullScreen.createTexture(bitmap);
    }

    public void scaleMVPMatrix(float x, float y) {
        if (mFullScreen != null)
            mFullScreen.scaleMVPMatrix(x, y);
    }


    public void release() {
        if (!mEnable || mFullScreen == null) {
            return;
        }
        mFullScreen.release(true);
    }

    /**
     * Prepares the off-screen framebuffer.
     */
    private void prepareFramebuffer(int width, int height) {
        GlUtil.checkGlError("start");
        int[] values = new int[1];

        // Create a texture object and bind it.  This will be the color buffer.
        GLES20.glGenTextures(1, values, 0);
        GlUtil.checkGlError("glGenTextures");
        mOffscreenTexture = values[0];   // expected > 0
        Log.i(TAG, "prepareFramebuffer mOffscreenTexture:" + mOffscreenTexture);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mOffscreenTexture);
        GlUtil.checkGlError("glBindTexture");

        // Create texture storage.
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GlUtil.checkGlError("glTexParameter");

        // Create framebuffer object and bind it.
        GLES20.glGenFramebuffers(1, values, 0);
        GlUtil.checkGlError("glGenFramebuffers");
        mFramebuffer = values[0];    // expected > 0

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer);
        GlUtil.checkGlError("glBindFramebuffer " + mFramebuffer);

        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mOffscreenTexture, 0);

        // See if GLES is happy with all this.
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer not complete, status=" + status);
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        // Switch back to the default framebuffer.
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GlUtil.checkGlError("glBindFramebuffer");
    }

    public int drawFrame(int texId, float[] texMatrix, int texWidth, int texHeight) {
        if (!mEnable || mFullScreen == null) {
            return texId;
        }

        if (mCurrentFilter != mNewFilter) {
            mFullScreen.changeProgram(getFilter(mNewFilter));
            mCurrentFilter = mNewFilter;
        }
        if (texMatrix == null) {//兼容七牛云的drawFrame方式
            GLES20.glViewport(0, 0, texWidth, texHeight);
            if (mOffscreenTexture == 0) {
                prepareFramebuffer(texWidth, texHeight);
            }
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer);

            mFullScreen.getFilter().setTextureSize(texWidth, texHeight);
            mFullScreen.drawFrame(texId, texMatrix);

            // Blit to display.
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
            return mOffscreenTexture;
        } else {//Camear的预览方式
            mFullScreen.getFilter().setTextureSize(texWidth, texHeight);
            mFullScreen.drawFrame(texId, texMatrix);
            return texId;
        }
    }

    public static final class Builder {
        private boolean isEnable = true;
        private FilterInfo mDefaultFilter;
        private Context mContext;
        private onExtFilterListener mExtFilterListener;

        private Builder() {
        }

        public Builder context(Context context) {
            this.mContext = context;
            return this;
        }

        public Builder isEnable(boolean isEnable) {
            this.isEnable = isEnable;
            return this;
        }

        public Builder defaultFilter(FilterInfo info) {
            this.mDefaultFilter = info;
            return this;
        }


        public Builder addExtFilterListener(onExtFilterListener listener) {
            this.mExtFilterListener = listener;
            return this;
        }


        public FilterManager build() {
            if (mContext == null) {
                throw new IllegalStateException("context is required");
            }
//            if (mDefaultFilter == null) {
//                throw new IllegalStateException("defaultFilter is required");
//            }
            return new FilterManager(this);
        }
    }
}
