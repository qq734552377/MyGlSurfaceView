package com.ucast.myglsurfaceview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ucast.myglsurfaceview.beans.BoxMsg;
import com.ucast.myglsurfaceview.cameraInterface.BackgroundCamera;
import com.ucast.myglsurfaceview.cameraInterface.CameraInterface;
import com.ucast.myglsurfaceview.cameraInterface.InfraredCameraInterface;
import com.ucast.myglsurfaceview.events.StartArEvent;
import com.ucast.myglsurfaceview.events.TakeLedOffPhotoResult;
import com.ucast.myglsurfaceview.events.TakeLedOnPhotoResult;
import com.ucast.myglsurfaceview.nettySocket.h264.ScreenRecord;
import com.ucast.myglsurfaceview.serial.OpenLed;
import com.ucast.myglsurfaceview.serial.SerialPort;
import com.ucast.myglsurfaceview.tools.ApManager;
import com.ucast.myglsurfaceview.tools.Config;
import com.ucast.myglsurfaceview.tools.FullScreenHelper;
import com.ucast.myglsurfaceview.tools.LedControlTools;
import com.ucast.myglsurfaceview.tools.MyDialog;
import com.ucast.myglsurfaceview.tools.MyTools;
import com.ucast.myglsurfaceview.tools.ToastUtil;

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

    public static Object lock = new Object();

    public static boolean isTaking = false;

    OpenLed openLed = null;
    boolean isOpen = false;


    private MediaProjectionManager mMediaProjectionManager;
    private boolean isRecording = false;
    private ScreenRecord mScreenRecord;
    public static final int REQUEST_CODE_A = 10001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_takephoto);
        initMPManager();
        sf = findViewById(R.id.sf);
        surfaceHolder = sf.getHolder();
        surfaceHolder.addCallback(this);
//        BackgroundCamera.getInstance().openCamera();
//        BackgroundCamera.getInstance().initTexture();
        findViewById(R.id.take_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        InfraredCameraInterface.getInstance().takePhoto("1",false);
//                    }
//                }).start();
//                if(openLed != null) {
//                    if (isOpen) {
//                        isOpen = false;
//                        openLed.Send(Config.CLOSELED);
//                        MyDialog.showToast(TakephotoActivity.this,"发送关闭");
//                    } else {
//                        isOpen = true;
//                        openLed.Send(Config.OPENLED);
//                        MyDialog.showToast(TakephotoActivity.this,"发送开启");
//                    }
//                }
//                InfraredCameraInterface.getInstance().takePhoto("1", false);
//                startArAct();
                startTakePhoto();

//                BackgroundCamera.getInstance().takePicture();
            }
        });

        EventBus.getDefault().register(this);
        openLed = new OpenLed(Config.PORTPATH);
        try {
            openLed.Open();

        } catch (Exception e) {
            MyTools.writeSimpleLogWithTime("开启led串口失败，原因-->" + e.toString());
        }
//        String path_on =   Environment.getExternalStorageDirectory().toString() + "/Ucast/photo/on.jpg";
//        String path_off =   Environment.getExternalStorageDirectory().toString() + "/Ucast/photo/off.jpg";
//        Point p = LedControlTools.getInstance().getPoint(path_off,path_on);

//        Toast.makeText(this,p.toString(),Toast.LENGTH_LONG).show();

        Intent ootStartIntent = new Intent(this, UpdateService.class);
        ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startService(ootStartIntent);

        MyTools.copyCfg(this,"port1.jpg","port2.jpg","port3.jpg");

        initData();
//        startScreenCapture();

    }

    public void initData(){
        BoxMsg boxMsg1 = new BoxMsg();
        boxMsg1.setLedId("BA 0A");
        boxMsg1.setShowPic(Environment.getExternalStorageDirectory().getPath() + "/Ucast/port1.jpg");
        boxMsg1.setShowPic(true);
        BoxMsg boxMsg2 = new BoxMsg();
        boxMsg2.setLedId("BB 0A");
        boxMsg2.setShowPic(Environment.getExternalStorageDirectory().getPath() + "/Ucast/port2.jpg");
        boxMsg2.setShowPic(true);
        BoxMsg boxMsg3 = new BoxMsg();
        boxMsg3.setLedId("BC 0A");
        boxMsg3.setShowPic(Environment.getExternalStorageDirectory().getPath() + "/Ucast/port3.jpg");
        boxMsg3.setShowPic(true);

        boxs.put(boxMsg1.getLedId(),boxMsg1);
        boxs.put(boxMsg2.getLedId(),boxMsg2);
        boxs.put(boxMsg3.getLedId(),boxMsg3);
    }

    public void startTakePhoto(){
        if (isTaking)
            return;
        isTaking = true;
        TakePhotoThread a = new TakePhotoThread();
        a.start();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
    }
    @Override
    protected void onResume() {
        super.onResume();
//        float[] a= MyTools.getPicPosition(new Point(400,500));
//        float[] b=MyTools.getPicPosition(new Point(200,100));
//        float[] c=MyTools.getPicPosition(new Point(600,400));
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
        if (!InfraredCameraInterface.getInstance().isPreviewing()) {
            InfraredCameraInterface.getInstance().doOpenCamera();
            InfraredCameraInterface.getInstance().doStartPreview(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND,sticky = true)
    public void handleCameraTakeOk(TakeLedOffPhotoResult takeLedOffPhotoResult){//拍摄完led 关闭时的照片完成后的操作
        BoxMsg one = boxs.get(takeLedOffPhotoResult.getLedId());
        if (takeLedOffPhotoResult.getPath() != null)
            one.setLedOffPhotoPath(takeLedOffPhotoResult.getPath());
        if (takeLedOffPhotoResult.getMat() != null)
            one.setLedOffMat(takeLedOffPhotoResult.getMat());
        //获取灯的Id
        String idStr = one.getLedId();
        byte[] id = MyTools.getBytesByString(idStr);
        for (int i = 0; i < id.length; i++) {
            Config.OPENLED[1 + i] = id[i];
        }
        if (openLed != null){
            MyTools.writeSimpleLog("发送开灯");
            // 开灯
            openLed.Send(Config.OPENLED);
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        synchronized (lock) {
            lock.notifyAll();
        }
    }
    @Subscribe(threadMode = ThreadMode.BACKGROUND,sticky = true)
    public void handleCameraTakeOk(TakeLedOnPhotoResult takeLedOnPhotoResult){//拍摄完led 亮着的照片完成后的操作
        BoxMsg one = boxs.get(takeLedOnPhotoResult.getLedId());
        if (takeLedOnPhotoResult.getPath() != null)
            one.setLedOnPhotoPath(takeLedOnPhotoResult.getPath());
        if (takeLedOnPhotoResult.getMat() != null)
            one.setLedOnMat(takeLedOnPhotoResult.getMat());
        //获取灯的Id
        String idStr = one.getLedId();
        byte[] id = MyTools.getBytesByString(idStr);
        for (int i = 0; i < id.length; i++) {
            Config.CLOSELED[1 + i] = id[i];
        }
        if (openLed != null){
            MyTools.writeSimpleLog("发送关灯");
            // 关灯
            openLed.Send(Config.CLOSELED);
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        InfraredCameraInterface.getInstance().doStopCamera();
    }



    public class TakePhotoThread extends Thread{
        @Override
        public void run() {
            if (boxs.size() > 0) {
                try {
//                    MyTools.writeSimpleLogWithTime("拍照线程开始  " + System.currentTimeMillis());
                    for (Map.Entry<String, BoxMsg> item : boxs.entrySet()) {
                        BoxMsg one = item.getValue();
                        InfraredCameraInterface.getInstance().takePhoto(one.getLedId(), false);
//                        BackgroundCamera.getInstance().takePhotoWithParam(one.getLedId(), false);
                        synchronized (lock) {
                            lock.wait();
                        }
//                        MyTools.writeSimpleLogWithTime("拍照线程被唤醒1  " + System.currentTimeMillis());
                        InfraredCameraInterface.getInstance().takePhoto(one.getLedId(), true);
//                        BackgroundCamera.getInstance().takePhotoWithParam(one.getLedId(), true);
                        synchronized (lock) {
                            lock.wait();
                        }
//                        MyTools.writeSimpleLogWithTime("拍照线程被唤醒2  " + System.currentTimeMillis());
                        //图片处理 存入对象中

                        Point p = null;
                        if(Config.USESTRINGPATH){
                            p = LedControlTools.getInstance().getPoint(one.getLedOffPhotoPath(), one.getLedOnPhotoPath());
                        }else{
                            p = LedControlTools.getInstance().getPoint(one.getLedOffMat(), one.getLedOnMat());
                            one.getLedOffMat().release();
                            one.getLedOnMat().release();
                            one.setLedOffMat(null);
                            one.setLedOnMat(null);
                        }
                        one.setPointInScreen(p);
                        MyTools.writeSimpleLogWithTime("处理一张图片结束  " + System.currentTimeMillis());
                    }
                    MyTools.writeSimpleLogWithTime("准备开启Unity界面  " + System.currentTimeMillis());

                    //当所有的信息处理完成  发送消息给主线程  启动ar界面
                    EventBus.getDefault().postSticky(new StartArEvent());
                } catch (InterruptedException e) {
                    MyTools.writeSimpleLogWithTime("拍照线程异常终止了");
                }

            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void startARActivity(StartArEvent event){

        isTaking = false;
        int l  = boxs.size();
//        ToastUtil.showToast(TakephotoActivity.this,"拍照完成-》" + boxs.get("BA 0A").getPointInScreen());
        for (Map.Entry<String, BoxMsg> item : boxs.entrySet()) {
            BoxMsg one = item.getValue();
            MyTools.writeSimpleLog(one.getLedId() + "  点位置--》" + one.getPointInScreen() );
        }
        MyTools.writeSimpleLog("------------------------>");
        InfraredCameraInterface.getInstance().doStopCamera();
        CameraInterface.getInstance();
        try{
            Thread.sleep(500);
        }catch (Exception e){

        }
        startArAct();
    }

    public void startArAct(){
        Intent intent = new Intent(TakephotoActivity.this,MainActivity.class);
        startActivity(intent);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_F2){
            ToastUtil.showToast(this,"点击了F2");
            startTakePhoto();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_CODE_A) {
            try {
                MediaProjection mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
                if (mediaProjection == null) {
                    Toast.makeText(this, "程序发生错误:MediaProjection@1", Toast.LENGTH_SHORT).show();
                    return;
                }
                mScreenRecord = new ScreenRecord(this, mediaProjection);
                mScreenRecord.start();
                isRecording = true;
            } catch (Exception e) {

            }
        }
    }


    /**
     * 初始化MediaProjectionManager
     * **/
    private void initMPManager(){
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
    }

    /**
     * 开始截屏
     * **/
    private void startScreenCapture(){
        if (!isRecording) {
            Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
            startActivityForResult(captureIntent, REQUEST_CODE_A);
        }
    }


    /**
     * 停止截屏
     * **/
    private void stopScreenCapture(){
        isRecording = false;
        mScreenRecord.release();
    }
}
