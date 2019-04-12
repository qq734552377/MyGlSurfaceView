package com.ucast.myglsurfaceview;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.opengl.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ucast.myglsurfaceview.cameraInterface.CameraInterface;
import com.ucast.myglsurfaceview.nettySocket.h264.ScreenRecord;
import com.ucast.myglsurfaceview.tools.ApManager;
import com.ucast.myglsurfaceview.tools.Config;
import com.ucast.myglsurfaceview.tools.FullScreenHelper;
import com.ucast.myglsurfaceview.tools.MyTools;
import com.ucast.myglsurfaceview.tools.ToastUtil;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    public static boolean ISPORTRAIT = true;
    CameraGLSurfaceView gl;

    public static int FRAMECALLBACKWIDTH = 1024;
    public static int FRAMECALLBACKHEIGHT = 600;

    private SensorManager mSensorManager;

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
        if(mSensorManager != null) {
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_UI);
//            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
//            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_UI);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if(mSensorManager != null)
            mSensorManager.unregisterListener(this);
        CameraInterface.getInstance().doStopCamera();
        super.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
    }

    @Override
    protected void onDestroy() {
//        gl.onPause();
        super.onDestroy();
    }
    // 将纳秒转化为秒
    private static final float NS2S = 1.0f / 1000000000.0f;

    private float timestamp_rotation = 0f;
    private float timestamp_line_ac = 0f;
    private float[] line_ac = new float[3];
    private float[] angle =new float[3];
    private float x_coefficient = 0.01f;
    private float y_coefficient = -0.01f;
    private float lowvalue = 0.01f;
    private int iCount = 0;
    DecimalFormat df = null;
    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        if (df == null)
            df = new DecimalFormat("00.000");
//        if (type == Sensor.TYPE_ACCELEROMETER){
//            Log.i(TAG,"yuanshi event zhi --> xac:" + event.values[0] + "  yac:" + event.values[1] + "  zac:" + event.values[2] );
//        }
        if (type == Sensor.TYPE_LINEAR_ACCELERATION){
//            Log.i(TAG,"yuanshi event zhi --> xlac:" + event.values[0] + "  ylac:" + event.values[1] + "  zlac:" + event.values[2] );
            if (timestamp_line_ac != 0){
                float dt = (event.timestamp - timestamp_line_ac) * NS2S;
//                line_ac[0] = 0.5f * event.values[0] * dt * dt;
//                line_ac[1] = 0.5f * event.values[1] * dt * dt;
//                line_ac[2] = 0.5f * event.values[2] * dt * dt;
                if (iCount < 20) {
                    line_ac[0] += 0.5f * event.values[0] * dt * dt;
                    line_ac[1] += 0.5f * event.values[1] * dt * dt;
                    line_ac[2] += 0.5f * event.values[2] * dt * dt;
                    iCount ++;
                    return;
                }

                Log.i(TAG,"TYPE_LINEAR_ACCELERATION --> xdis:" + df.format(line_ac[0]) + "  ydis:" + df.format(line_ac[1]) + "  zdis:" + df.format(line_ac[2]) + "  DT:" + dt);
                Matrix.setIdentityM(matrix,0);
                Matrix.translateM(matrix,0,80f * line_ac[1] ,80f * line_ac[0] , 0.5f * line_ac[2]);
//                gl.updateMatrix(matrix);
            }
            timestamp_line_ac = event.timestamp;
            iCount = 0;
            line_ac[0] = 0;
            line_ac[1] = 0;
            line_ac[2] = 0;
        }

        if (type == Sensor.TYPE_GYROSCOPE){
            //从 x、y、z 轴的正向位置观看处于原始方位的设备，如果设备逆时针旋转，将会收到正值；否则，为负值
            if(timestamp_rotation != 0) {
                // 得到两次检测到手机旋转的时间差（纳秒），并将其转化为秒
                float dT = (event.timestamp - timestamp_rotation) * NS2S;

//                double value = Math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2]);
//                if (value < 0.001){
//                    timestamp_rotation = event.timestamp;
//                    return;
//                }

                if(Math.abs(event.values[0]) < lowvalue || Math.abs(event.values[1]) < lowvalue || Math.abs(event.values[2]) < lowvalue){
                    timestamp_rotation = event.timestamp;
                    return;
                }

                // 将手机在各个轴上的旋转角度相加，即可得到当前位置相对于初始位置的旋转弧度
                angle[0] = event.values[0] * dT;

                angle[1] = event.values[1] * dT;

                angle[2] = event.values[2] * dT;

                // 将弧度转化为角度
                float anglex = (float) Math.toDegrees(angle[0]);

                float angley = (float) Math.toDegrees(angle[1]);

                float anglez = (float) Math.toDegrees(angle[2]);

                Log.i(TAG,"yuanshi event zhi --> xs:" + event.values[0] + "  ys:" + event.values[1] + "  zs:" + event.values[2] );
                Log.i(TAG,"jisuan de jiaodu  --> xd:" + df.format(anglex) + "  yd:" + df.format(angley) + "  zd:" + df.format(anglez) + "  DT:" + dT);
                Matrix.setIdentityM(matrix,0);
                Matrix.translateM(matrix,0,4.5f * angley * x_coefficient,1.0f * anglex * y_coefficient,0);
                gl.updateMatrix(matrix);
                msg.setText("anglex------------>" + anglex + "\nangley------------>" + angley + "\nanglez------------>" + anglez
                        + "\n DT->" + dT
                );
            }
            //将当前时间赋值给timestamp
            timestamp_rotation = event.timestamp;
        }

    }

    private static final String TAG = "AirPort MainActivity";
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
