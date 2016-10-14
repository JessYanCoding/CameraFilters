# CameraFilters
##摄像头实时滤镜处理库,自带10多种滤镜,支持滤镜扩展,并且兼容七牛云直播滤镜处理

##Usage
###Declare permissions:
```
 <uses-permission android:name="android.permission.CAMERA"/>
```
###Declare feature:
```
 <uses-feature 
        android:glEsVersion="0x00020000"
        android:required="true"/>
```
###Step 1
``` 
 mFilterManager = FilterManager
                .builder()
                .context(mApplicationContext)
                .isUseQiniu(false)//是否在qiniu云直播上使用滤镜
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
                .defaultFilter(new FilterInfo(false, 0))//设置默认滤镜(false为使用内置滤镜,角标范围是0-13,0为透明滤镜)
                .build();
```
###Step 2
* 在``GLSurfaceView.Renderer``的三个回调方法中，将参数传给``FilterManager``对应的方法.
* onSurfaceCreated ---> FilterManager.initialize();
* onSurfaceChanged ---> FilterManager.updateSurfaceSize(width, height);
* onDrawFrame ---> mFilterManager.drawFrame(mTextureId, mSTMatrix, mIncomingWidth, mIncomingHeight);
* 以上为``GLSurfaceView.Renderer ``回调和``FilterManager``方法的对应关系（请参照Demo）
* 最后在结束时记得调用``mFilterManager.release()``释放资源 

###Step 3 ( if you use qiniu see here )
```
 mCameraStreamingManager.setSurfaceTextureCallback(new SurfaceTextureCallback() {
            @Override
            public void onSurfaceCreated() {
               mFilterManager.initialize();
            }

            @Override
            public void onSurfaceChanged(int i, int i1) {
               mFilterManager.updateSurfaceSize(i, i1);
            }

            @Override
            public void onSurfaceDestroyed() {
                mFilterManager.release();
            }

            @Override
            public int onDrawFrame(int i, int i1, int i2, float[] floats) {
               return mFilterManager.drawFrame(var1, null, var2, var3);
            }
        });

```
* 在七牛直播上使用滤镜也非常简单,和第二步相似，记得``FilterManager.isUseQiniu(true)`` & ``mFilterManager.drawFrame(var1, null, var2, var3);``第二个参数一定要传null,七牛视频编码必须为硬编码

## FilterInfo structure
使用默认滤镜和切换滤镜需要传入FilterInfo对象

Field      | Description
:---------:|:---------:
 isExt     |是否使用扩展的滤镜
 index     |滤镜所在的索引，如果isExt为true，则使用内置滤镜列表，false索引对应扩展滤镜
 
## Dev tips
具体用法参照``Demo``,有``Opengl``基础的可以自定义滤镜


##About Me
* **Email**: jess.yan.effort@gmail.com  

##License
```
 Copyright 2016, jessyan       
 
   Licensed under the Apache License, Version 2.0 (the "License");  
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
