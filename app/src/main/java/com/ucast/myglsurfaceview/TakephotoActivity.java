package com.ucast.myglsurfaceview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.ucast.myglsurfaceview.cameraInterface.InfraredCameraInterface;
import com.ucast.myglsurfaceview.tools.FullScreenHelper;

public class TakephotoActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    public static boolean ISPORTRAIT = true;

    SurfaceView sf;
    SurfaceHolder surfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_takephoto);
        sf = findViewById(R.id.sf);
        surfaceHolder = sf.getHolder();
        surfaceHolder.addCallback(this);
        findViewById(R.id.take_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InfraredCameraInterface.getInstance().takePhoto();

            }
        });

    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        InfraredCameraInterface.getInstance().doOpenCamera();
        InfraredCameraInterface.getInstance().doStartPreview(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        InfraredCameraInterface.getInstance().doStopCamera();
    }
}
