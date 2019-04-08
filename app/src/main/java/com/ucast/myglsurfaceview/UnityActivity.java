package com.ucast.myglsurfaceview;

import android.content.Intent;
import android.graphics.Point;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import com.ucast.firstvuforia.UnityPlayerActivity;
import com.ucast.myglsurfaceview.beans.BoxMsg;
import com.ucast.myglsurfaceview.nettySocket.h264.ScreenRecord;

import java.util.Map;

public class UnityActivity extends UnityPlayerActivity {

    private Handler handler = new Handler();

    private MediaProjectionManager mMediaProjectionManager;
    private boolean isRecording = false;
    private ScreenRecord mScreenRecord;
    public static final int REQUEST_CODE_A = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                showTakephoto();
                showBoxMsg();

            }
        },30);

        initVideoSomething();
//        int i = 0;
//        String path = BitCreateTools.getBitMapByStringReturnBitmaPath("Number:" + i + 1
//                + "\nWeight:" + (i+15) +"0 kg"
//                + "\nCountry:" + (i+35) +" s", Color.BLUE);
//        String path2 = BitCreateTools.getBitMapByStringReturnBitmaPath("Number:" + i + 1
//                + "\nWeight:" + (i+15) +"0 kg"
//                + "\nCountry:" + (i+35) +" s", Color.RED);

    }

    public void showTakephoto(){
        if(TakephotoActivity.boxs.isEmpty())
            return;

        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, BoxMsg> item : TakephotoActivity.boxs.entrySet()){
            BoxMsg one = item.getValue();
            Point p = one.getPointInScreen();
            if (p != null)
                sb.append(p.x + "#" + p.y + "#2.0#file://" + one.getShowPic() + ",");
        }
        sendMsgToUnity(sb.substring(0,sb.lastIndexOf(",")));
    }

    public void showBoxMsg(){
        String p1 = "file://" + Environment.getExternalStorageDirectory().getPath() + "/Ucast/port1.jpg";
        String p2 = "file://" + Environment.getExternalStorageDirectory().getPath() + "/Ucast/port2.jpg";
        String p3 = "file://" + Environment.getExternalStorageDirectory().getPath() + "/Ucast/port3.jpg";
        sendMsgToUnity("300#300#2.0#" + p1 + ",500#300#2.0#" + p2 + ",700#300#2.0#" + p3);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
    public void initVideoSomething(){
//        Intent ootStartIntent = new Intent(this, UpdateService.class);
//        ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        this.startService(ootStartIntent);

        initMPManager();

        startScreenCapture();
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










//    @Override
//    public void onBackPressed() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                UnityPlayerActivity.this.onPause();
//                UnityPlayerActivity.this.onDestroy();
//            }
//        });
//        super.onBackPressed();
//    }
//    public void sendMsgToUnity(String data){
//        UnityPlayer.UnitySendMessage("scripts","CreateAGameobjByScrenPoint",data);
//    }
}
