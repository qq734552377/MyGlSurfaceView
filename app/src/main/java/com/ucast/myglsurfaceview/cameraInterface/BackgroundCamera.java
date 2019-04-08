package com.ucast.myglsurfaceview.cameraInterface;


import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import com.ucast.myglsurfaceview.events.TakeLedOffPhotoResult;
import com.ucast.myglsurfaceview.events.TakeLedOnPhotoResult;
import com.ucast.myglsurfaceview.tools.CameraSave;
import com.ucast.myglsurfaceview.tools.MyTools;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by pj on 2019/3/25.
 */
public class BackgroundCamera {
    private static BackgroundCamera instance ;
    private Camera camera;
    private SurfaceTexture mTexture;
    private BackgroundCamera() {
    }

    public static BackgroundCamera getInstance(){
        if (instance == null){
            synchronized (BackgroundCamera.class){
                if (instance == null){
                    instance = new BackgroundCamera();
                }
            }
        }
        return instance;
    }

    public void openCamera(){
        if (camera == null){
            camera = Camera.open();
        }
    }
    public void initTexture() {
        mTexture = new SurfaceTexture(0);

        try {
            camera.setPreviewTexture(mTexture);
            camera.startPreview();
        } catch (IOException e) {
            Log.e(TAG, "initiate camera failed, e: " + e.getMessage());
        }
    }

    public void takePicture() {
        try {
            MyTools.writeSimpleLogWithTime("准备相机  " + System.currentTimeMillis());
            camera.takePicture(null, null, mPictureCallback);
        } catch (Exception e) {
            Log.d(TAG, "call takePicture failed, e:" + e.getMessage());
            camera.release();
            camera = null;
        }
    }


    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            MyTools.writeSimpleLogWithTime("相机数据过来  " + System.currentTimeMillis());
            File outputFile = CameraSave.getOutputMediaFile(CameraSave.DIRECTORY_PUBLIC);

            if(outputFile == null) {
                Log.e(TAG, "generate output file failed");
                return;
            }

            try{
                FileOutputStream fops = new FileOutputStream(outputFile);
                fops.write(data);
                fops.close();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "generate picture failed : " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "write picture failed : " + e.getMessage());
            }
            MyTools.writeSimpleLogWithTime("存储数据完成  " + System.currentTimeMillis());
//            camera.release();
//            camera = null;
        }
    };

    public void takePhotoWithParam(final String ledId, final boolean isLedOn){
        try {
//            MyTools.writeSimpleLogWithTime("准备相机  " + System.currentTimeMillis());
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
//                    MyTools.writeSimpleLogWithTime("相机数据过来  " + System.currentTimeMillis());
                    String path =   Environment.getExternalStorageDirectory().toString() + "/Ucast/photo";
                    File folder=new File(path);
                    if(!folder.exists()){
                        boolean isOk =  folder.mkdirs();
                        if (!isOk)
                            return;
                    }
                    long dataTake = System.currentTimeMillis();
                    final String jpegName = path + "/" + dataTake +".jpg";
                    File file=new File(jpegName);
                    FileOutputStream fos=null;
                    try {
                        fos=new FileOutputStream(file);
                        fos.write(data);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }finally{
                        if (fos!=null) {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                    camera.stopPreview();
                    camera.startPreview();
//                    MyTools.writeSimpleLogWithTime("存储相机数据完成  " + System.currentTimeMillis());
                    if (isLedOn){
                        EventBus.getDefault().postSticky(new TakeLedOnPhotoResult(jpegName,ledId));
                    }else {
                        EventBus.getDefault().postSticky(new TakeLedOffPhotoResult(jpegName,ledId));
                    }
                }

            });
        } catch (Exception e) {
            Log.d(TAG, "call takePicture failed, e:" + e.getMessage());
            camera.release();
            camera = null;
        }
    }

    public void releaseCamera(){
        if (camera != null){
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private static final String TAG = "BackgroundCamera";
}
