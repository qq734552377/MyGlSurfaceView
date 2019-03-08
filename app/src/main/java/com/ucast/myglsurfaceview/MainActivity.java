package com.ucast.myglsurfaceview;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.ucast.myglsurfaceview.exception.ExceptionApplication;
import com.ucast.myglsurfaceview.tools.FullScreenHelper;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    public static boolean ISPORTRAIT = true;
    CameraGLSurfaceView gl;

    public static int FRAMECALLBACKWIDTH = 720;
    public static int FRAMECALLBACKHEIGHT = 1280;

    private SensorManager mSensorManager;
    private Sensor mRotation;
    TextView msg;
    boolean firstTime = true;
    float firstX = 0f;
    float firstY = 0f;

    private float[] matrix=new float[16];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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



//        Matrix.translateM(matrix,0,0.9f,-0.9f,0);
//        gl.setMatrix(matrix);
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
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gl.onPause();
    }
    // 将纳秒转化为秒
    private static final float NS2S = 1.0f / 1000000000.0f;

    private float timestamp;

    private float angle[] =new float[3];
    @Override
    public void onSensorChanged(SensorEvent event) {
//        DecimalFormat fnum = new DecimalFormat( "##0.00 ");
//        float x = Float.valueOf(fnum.format(event.values[0]));
//        float y = Float.valueOf(fnum.format(event.values[1]));
//        float z = Float.valueOf(fnum.format(event.values[2]));
//
////        float x = (float)Math.toDegrees(event.values[0]);
////        float y = (float)Math.toDegrees(event.values[1]);
////        float z = (float)Math.toDegrees(event.values[2]);
//        if (firstTime){
//            firstX = x;
//            firstY = y;
//            firstTime = false;
//            return;
//        }
//        float distanceX = firstX - x;
//        float distanceY = firstY - y;
//        if (gl != null){
//            if (Math.abs(distanceX) < 0.05 || Math.abs(distanceX) < 0.05)
//                return;
//            gl.updateFilterPosition(distanceX,distanceY);
//        }
//        msg.setText("x->" + x + "\ny->" + y + "\nz->" + z);

//        SensorManager.getRotationMatrixFromVector(matrix,event.values);
//
//        StringBuilder sb = new StringBuilder();
//        DecimalFormat fnum = new DecimalFormat( "##0.00 ");
//        for (int i = 0; i < matrix.length; i++) {
//            sb.append(fnum.format(matrix[i]) + ",");
//        }
//        msg.setText(sb.toString());


        //从 x、y、z 轴的正向位置观看处于原始方位的设备，如果设备逆时针旋转，将会收到正值；否则，为负值
//

        if(timestamp != 0) {

            // 得到两次检测到手机旋转的时间差（纳秒），并将其转化为秒
            final float dT = (event.timestamp - timestamp) * NS2S;

            // 将手机在各个轴上的旋转角度相加，即可得到当前位置相对于初始位置的旋转弧度

            angle[0] = event.values[0] * dT;

            angle[1] = event.values[1] * dT;

            angle[2] = event.values[2] * dT;

            // 将弧度转化为角度

            float anglex = (float) Math.toDegrees(angle[0]);

            float angley = (float) Math.toDegrees(angle[1]);

            float anglez = (float) Math.toDegrees(angle[2]);

            Matrix.setIdentityM(matrix,0);
//            Matrix.translateM(matrix,0,-1.5f * anglex * 0.02f,1.0f * angley * -0.02f,0);//银联相机
            Matrix.translateM(matrix,0,1.5f * angley * 0.01f,1.0f * anglex * -0.01f,0);
            gl.updateMatrix(matrix);
            msg.setText("anglex------------>" + anglex + "\nangley------------>" + angley + "\nanglez------------>" + anglez
                        + "\n DT->" + dT
            );
        }
        //将当前时间赋值给timestamp
        timestamp = event.timestamp;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
