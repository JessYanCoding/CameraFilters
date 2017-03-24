package me.jessyan.camerafilters.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import me.jessyan.camerafilters.demo.R;

import me.jessyan.camerafilters.demo.widget.CameraSurfaceView;

public class MainActivity extends AppCompatActivity {

    private CameraSurfaceView mCameraSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCameraSurfaceView = (CameraSurfaceView) findViewById(R.id.cameraView);
        mCameraSurfaceView.setAspectRatio(3,4);
    }


    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_none:
                mCameraSurfaceView.changeNoneFilter();
                break;
            case R.id.bt_inner:
                mCameraSurfaceView.changeInnerFilter();
                break;
            case R.id.bt_extension:
                mCameraSurfaceView.changeExtensionFilter();
                 break;
        }
    }


    @Override protected void onResume() {
        super.onResume();
        mCameraSurfaceView.onResume();
    }

    @Override protected void onPause() {
        mCameraSurfaceView.onPause();
        super.onPause();
    }

    @Override protected void onDestroy() {
        mCameraSurfaceView.onDestroy();
        super.onDestroy();
    }

}
