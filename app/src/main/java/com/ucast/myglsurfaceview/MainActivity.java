package com.ucast.myglsurfaceview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.opengl.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.ucast.myglsurfaceview.cameraInterface.CameraInterface;
import com.ucast.myglsurfaceview.exception.ExceptionApplication;
import com.ucast.myglsurfaceview.nettySocket.h264.ScreenRecord;
import com.ucast.myglsurfaceview.tools.ApManager;
import com.ucast.myglsurfaceview.tools.Config;
import com.ucast.myglsurfaceview.tools.FullScreenHelper;
import com.ucast.myglsurfaceview.tools.MyTools;
import com.ucast.myglsurfaceview.tools.ToastUtil;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    public static boolean ISPORTRAIT = true;
    CameraGLSurfaceView gl;

    public static int FRAMECALLBACKWIDTH = 1024;
    public static int FRAMECALLBACKHEIGHT = 600;

    private SensorManager mSensorManager;
    private Sensor mRotation;
    TextView msg;
    boolean firstTime = true;
    float firstX = 0f;
    float firstY = 0f;

    private float[] matrix=new float[16];

    private MediaProjectionManager mMediaProjectionManager;
//    public static boolean isRecording = false;
    private ScreenRecord mScreenRecord;
    public static final int REQUEST_CODE_A = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyTools.copyCfg(this,"port1.jpg","port2.jpg","port3.jpg");
        gl =(CameraGLSurfaceView) findViewById(R.id.gl_view);
        msg = findViewById(R.id.msg);
        mSensorManager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        //todo 判断是否存在rotation vector sensor
        mRotation=mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        findViewById(R.id.change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                gl.updateFilterPosition(-0.1f,0.1f);
                gl.isTakePahoto = true;
            }
        });

        Matrix.setIdentityM(matrix,0);

        initVideoSomething();
        ApManager.openHotspotWithNoPassword(this,"firefly");
    }

    public void initVideoSomething(){
        Intent ootStartIntent = new Intent(this, UpdateService.class);
        ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startService(ootStartIntent);
        initMPManager();
        startScreenCapture();
    }

    @Override
    protected void onResume() {
        mSensorManager.registerListener(this,mRotation,SensorManager.SENSOR_DELAY_UI);
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(this);
        CameraInterface.getInstance().doStopCamera();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
//        gl.onPause();
        super.onDestroy();
    }
    // 将纳秒转化为秒
    private static final float NS2S = 1.0f / 1000000000.0f;

    private float timestamp;
    private float[] sdAll = new float[3];
    private float angle[] =new float[3];
    private float x_coefficient = 0.01f;
    private float y_coefficient = -0.01f;
    private float lowvalue = 0.02f;
    private int iCount = 0;
    @Override
    public void onSensorChanged(SensorEvent event) {
        //从 x、y、z 轴的正向位置观看处于原始方位的设备，如果设备逆时针旋转，将会收到正值；否则，为负值
        if(timestamp != 0) {
            // 得到两次检测到手机旋转的时间差（纳秒），并将其转化为秒
            final float dT = (event.timestamp - timestamp) * NS2S;

//            if(iCount < 10)
//            {
//                sdAll[0] += event.values[0]* dT;
//                sdAll[1] += event.values[1]* dT;
//                sdAll[2] += event.values[2]* dT;
//                timestamp = event.timestamp;
//                iCount ++;
//                return;
//            }
//            angle[0] = sdAll[0]  ;
//
//            angle[1] = sdAll[1]  ;
//
//            angle[2] = sdAll[2]  ;

            // 将手机在各个轴上的旋转角度相加，即可得到当前位置相对于初始位置的旋转弧度
//
            angle[0] = event.values[0] * dT;

            angle[1] = event.values[1] * dT;

            angle[2] = event.values[2] * dT;

            // 将弧度转化为角度

            float anglex = (float) Math.toDegrees(angle[0]);

            float angley = (float) Math.toDegrees(angle[1]);

            float anglez = (float) Math.toDegrees(angle[2]);

//            if (anglex > lowvalue || anglex < -lowvalue || angley > lowvalue || angley < -lowvalue){
                Matrix.setIdentityM(matrix,0);
//            Matrix.translateM(matrix,0,-1.5f * anglex * 0.02f,1.0f * angley * -0.02f,0);//银联相机
                Matrix.translateM(matrix,0,4.5f * angley * x_coefficient,1.0f * anglex * y_coefficient,0);
                gl.updateMatrix(matrix);
                msg.setText("anglex------------>" + anglex + "\nangley------------>" + angley + "\nanglez------------>" + anglez
                        + "\n DT->" + dT
                );
//            }


        }
        //将当前时间赋值给timestamp
        timestamp = event.timestamp;
//        sdAll[0] = 0;
//        sdAll[1] = 0;
//        sdAll[2] = 0;
//        iCount = 0;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
    public void startScreenCapture(){
        if (!Config.isStartRecoding) {
            MyTools.writeSimpleLogWithTime("开始截屏");
            Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
            startActivityForResult(captureIntent, REQUEST_CODE_A);
        }
    }


    /**
     * 停止截屏
     * **/
    private void stopScreenCapture(){
        Config.isStartRecoding = false;
        mScreenRecord.release();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_A) {
            try {
                MediaProjection mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
                if (mediaProjection == null) {
                    Toast.makeText(this, "程序发生错误:MediaProjection@1", Toast.LENGTH_SHORT).show();
                    return;
                }
                mScreenRecord = new ScreenRecord(this, mediaProjection);
                mScreenRecord.start();
                Config.isStartRecoding = true;
            } catch (Exception e) {
                MyTools.writeSimpleLog("onActivityResult 开启失败");
                Config.isStartRecoding = false;
            }
        }
    }

    public void starRecodeScreen(){
//        try {
//
//            MediaProjection mediaProjection = mMediaProjectionManager.getMediaProjection(Activity.RESULT_OK, data);
//            if (mediaProjection == null) {
//                Toast.makeText(this, "程序发生错误:MediaProjection@1", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            mScreenRecord = new ScreenRecord(this, mediaProjection);
//            mScreenRecord.start();
//            isRecording = true;
//        } catch (Exception e) {
//
//        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_F3){
            finish();
            ToastUtil.showToast(this,"ARActivity F3 Clicked");
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK){
            finish();
            ToastUtil.showToast(this,"ARActivity Back Clicked");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
