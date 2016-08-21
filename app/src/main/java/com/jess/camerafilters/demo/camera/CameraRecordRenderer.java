package com.jess.camerafilters.demo.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.jess.camerafilters.base.FilterManager;
import com.jess.camerafilters.base.onExtFilterListener;
import com.jess.camerafilters.demo.R;
import com.jess.camerafilters.demo.filter.CameraFilterBlend;
import com.jess.camerafilters.demo.widget.CameraSurfaceView;
import com.jess.camerafilters.entity.FilterInfo;
import com.jess.camerafilters.filter.CameraFilter;
import com.jess.camerafilters.filter.IFilter;
import com.jess.camerafilters.util.GlUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraRecordRenderer implements GLSurfaceView.Renderer {


    private final Context mApplicationContext;
    private final CameraSurfaceView.CameraHandler mCameraHandler;
    private int mTextureId = GlUtil.NO_TEXTURE;
    private SurfaceTexture mSurfaceTexture;
    private final float[] mSTMatrix = new float[16];


    private float mMvpScaleX = 1f, mMvpScaleY = 1f;
    private int mSurfaceWidth, mSurfaceHeight;
    private int mIncomingWidth, mIncomingHeight;
    private FilterManager mFilterManager;
    private int mInnerIndex = 1;

    public CameraRecordRenderer(Context applicationContext,
                                CameraSurfaceView.CameraHandler cameraHandler) {
        mApplicationContext = applicationContext;
        mCameraHandler = cameraHandler;
    }


    public void setCameraPreviewSize(int width, int height) {
        mIncomingWidth = width;
        mIncomingHeight = height;

        float scaleHeight = mSurfaceWidth / (width * 1f / height * 1f);
        float surfaceHeight = mSurfaceHeight;

        mMvpScaleX = 1f;
        mMvpScaleY = scaleHeight / surfaceHeight;
        mFilterManager.scaleMVPMatrix(mMvpScaleX, mMvpScaleY);
        Log.w("test", "setCameraPreviewSize ------>" + width + "----" + height);

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Matrix.setIdentityM(mSTMatrix, 0);
        if (mFilterManager == null) {
            initFilterManager();//初始化
        }

        mFilterManager.initialize();
        mTextureId = mFilterManager.createTexture();
        mSurfaceTexture = new SurfaceTexture(mTextureId);

    }

    /**
     * 初始化滤镜管理器
     */
    private void initFilterManager() {
        mFilterManager = FilterManager
                .builder()
                .context(mApplicationContext)
                .addExtFilterListener(new onExtFilterListener() {//添加扩展的滤镜,因为滤镜创建必须在render的回调中,所以统一在这里管理滤镜
                    @Override
                    public IFilter onCreateExtFilter(Context context, int index) {
                        switch (index) {
                            case 0://继承于cameraFitlter后可自定义filter,此Filter可任意添加一张图片到界面上
                                return new CameraFilterBlend(context, R.mipmap.
                                        pic_addpic);
                            default:
                                return new CameraFilter(context, false);
                        }
                    }
                })
                .defaultFilter(new FilterInfo(false, 0))//设置默认滤镜(透明滤镜)
                .build();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;


        Log.w("test", "onSurfaceChanged ------>" + width + "----" + height);
        if (gl != null) {
            gl.glViewport(0, 0, width, height);
        }
        mCameraHandler.sendMessage(
                mCameraHandler.obtainMessage(CameraSurfaceView.CameraHandler.SETUP_CAMERA, width,
                        height, mSurfaceTexture));


        mFilterManager.updateSurfaceSize(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mSurfaceTexture.updateTexImage();
        mSurfaceTexture.getTransformMatrix(mSTMatrix);
        mFilterManager.drawFrame(mTextureId, mSTMatrix, mIncomingWidth, mIncomingHeight);

    }


    public void notifyPausing() {
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
    }

    public void onDestroy(){
        if (mFilterManager != null) {
            mFilterManager.release();
            mFilterManager = null;
        }
    }


    public void changeNoneFilter() {
        mFilterManager.changeFilter(new FilterInfo(false, 0));
    }

    /**
     * 内部一共有14种滤镜(包括透明滤镜index为0)
     */
    public void changeInnerFilter() {
        if (mInnerIndex > 13) {
            mInnerIndex = 1;
        }
        mFilterManager.changeFilter(new FilterInfo(false, mInnerIndex));
        mInnerIndex++;
    }

    public void changeExtensionFilter() {
        mFilterManager.changeFilter(new FilterInfo(true, 0));
    }
}
