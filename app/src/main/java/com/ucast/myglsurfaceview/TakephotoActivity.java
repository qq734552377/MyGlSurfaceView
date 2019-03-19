package com.ucast.myglsurfaceview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.ucast.myglsurfaceview.beans.BoxMsg;
import com.ucast.myglsurfaceview.cameraInterface.InfraredCameraInterface;
import com.ucast.myglsurfaceview.events.TakeLedOffPhotoResult;
import com.ucast.myglsurfaceview.events.TakeLedOnPhotoResult;
import com.ucast.myglsurfaceview.serial.OpenLed;
import com.ucast.myglsurfaceview.serial.SerialPort;
import com.ucast.myglsurfaceview.tools.Config;
import com.ucast.myglsurfaceview.tools.FullScreenHelper;
import com.ucast.myglsurfaceview.tools.MyTools;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

public class TakephotoActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    public static boolean ISPORTRAIT = false;

    SurfaceView sf;
    SurfaceHolder surfaceHolder;

    public static Map<String,BoxMsg> boxs = new HashMap<>();

    public Object lock = new Object();

    OpenLed openLed = null;
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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        InfraredCameraInterface.getInstance().takePhoto("1",false);
                    }
                }).start();

            }
        });

        EventBus.getDefault().register(this);
        openLed = new OpenLed(Config.PORTPATH);
        try {
            openLed.Open();
        } catch (Exception e) {
            MyTools.writeSimpleLogWithTime("开启led串口失败，原因-->" + e.toString());
        }

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
        EventBus.getDefault().unregister(this);
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

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void handleCameraTakeOk(TakeLedOffPhotoResult takeLedOffPhotoResult){//拍摄完led 关闭时的照片完成后的操作
        BoxMsg one = boxs.get(takeLedOffPhotoResult.getLedId());
        one.setLedOffPhotoPath(takeLedOffPhotoResult.getPath());
        if (openLed != null){
            // 开灯


        }

    }
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void handleCameraTakeOk(TakeLedOnPhotoResult takeLedOnPhotoResult){//拍摄完led 亮着的照片完成后的操作
        BoxMsg one = boxs.get(takeLedOnPhotoResult.getLedId());
        one.setLedOnPhotoPath(takeLedOnPhotoResult.getPath());
        if (openLed != null){
            // 关灯


        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        InfraredCameraInterface.getInstance().doStopCamera();
    }

    public class TakePhotoThreadRunable implements Runnable{
        @Override
        public void run() {
            if (boxs.size() > 0){
                try {
                    for (Map.Entry<String, BoxMsg> item : boxs.entrySet()) {
                        BoxMsg one = item.getValue();
                        InfraredCameraInterface.getInstance().takePhoto(one.getLedId(), false);
                        lock.wait();
                        InfraredCameraInterface.getInstance().takePhoto(one.getLedId(),true);
                        lock.wait();
                        //图片处理

                    }

                }catch (InterruptedException e){

                }

            }
        }
    }

}
