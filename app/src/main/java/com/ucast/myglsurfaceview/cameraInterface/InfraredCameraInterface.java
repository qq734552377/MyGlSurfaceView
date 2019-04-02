package com.ucast.myglsurfaceview.cameraInterface;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.ucast.myglsurfaceview.MainActivity;
import com.ucast.myglsurfaceview.TakephotoActivity;
import com.ucast.myglsurfaceview.events.TakeLedOffPhotoResult;
import com.ucast.myglsurfaceview.events.TakeLedOnPhotoResult;
import com.ucast.myglsurfaceview.exception.ExceptionApplication;
import com.ucast.myglsurfaceview.tools.Config;
import com.ucast.myglsurfaceview.tools.MyTools;

import org.greenrobot.eventbus.EventBus;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by pj on 2019/2/1.
 */
public class InfraredCameraInterface {
    private static InfraredCameraInterface cameraInterface = new InfraredCameraInterface();
    Camera camera;
    boolean isPreview;

    private String ledId;
    private boolean isLedOn;
    public static InfraredCameraInterface getInstance() {
        return cameraInterface;
    }

    public void doOpenCamera(){
        camera = Camera.open(0);

    }

    public boolean isPreviewing(){
        return isPreview;
    }

    public void doStartPreview(SurfaceHolder surfaceHolder){
        if (camera != null) {
            try {
                if (isPreview)
                    doStopCamera();

                initFromCameraParameters(camera);
                Camera.Parameters parameters = camera.getParameters();
                if (TakephotoActivity.ISPORTRAIT)
                    camera.setDisplayOrientation(90);
//                camera.setDisplayOrientation(90);
                parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
//                parameters.setPreviewSize(screenResolution.y, screenResolution.x);
//                parameters.setPictureFormat(ImageFormat.JPEG);
                //设置图片预览的格式
//                parameters.setPreviewFormat(PixelFormat.RGBA_8888);
//                setZoom(parameters);
//                List<Camera.Size> list = parameters.getSupportedPictureSizes();
//                int paramPosition = 0;
//                final Camera.Size size = list.get(paramPosition);
//                //设置照片分辨率，注意要在摄像头支持的范围内选择
//                parameters.setPictureSize(size.width,size.height);
                camera.setParameters(parameters);
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                isPreview = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    Camera.PictureCallback callback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            MyTools.writeSimpleLogWithTime("相机数据过来  " + System.currentTimeMillis());
            if (Config.USESTRINGPATH){
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
                    e.printStackTrace();
                }finally{
                    if (fos!=null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                MyTools.writeSimpleLogWithTime("存储相机数据完成  " + System.currentTimeMillis());
                if (isLedOn){
                    EventBus.getDefault().postSticky(new TakeLedOnPhotoResult(jpegName,ledId));
                }else {
                    EventBus.getDefault().postSticky(new TakeLedOffPhotoResult(jpegName,ledId));
                }
            }else {

                Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
                MyTools.writeSimpleLogWithTime("转换为bitmap数据完成  " + System.currentTimeMillis());
                Mat src = new Mat();
                Utils.bitmapToMat(bitmap, src);
                MyTools.writeSimpleLogWithTime("转换为mat数据完成  " + System.currentTimeMillis());
                if (isLedOn){
                    EventBus.getDefault().postSticky(new TakeLedOnPhotoResult(src,ledId));
                }else {
                    EventBus.getDefault().postSticky(new TakeLedOffPhotoResult(src,ledId));
                }
            }

            camera.stopPreview();
            camera.startPreview();

        }

    };

    public void takePhoto(String ledId, final boolean isLedOn){
        if (camera != null){
            if (!isPreview)
                return;
            MyTools.writeSimpleLogWithTime("准备相机  " + System.currentTimeMillis());
            Camera.Parameters parameters;
            try{
                parameters = camera.getParameters();
            }catch(Exception e){
                e.printStackTrace();
                return;
            }
            //获取摄像头支持的各种分辨率,因为摄像头数组不确定是按降序还是升序，这里的逻辑有时不是很好找得到相应的尺寸
            //可先确定是按升还是降序排列，再进对对比吧，我这里拢统地找了个，是个不精确的...
            List<Camera.Size> list = parameters.getSupportedPictureSizes();
            int paramPosition = 0;
            final Camera.Size size = list.get(paramPosition);
            //设置照片分辨率，注意要在摄像头支持的范围内选择
            parameters.setPictureSize(size.width,size.height);
            //设置照相机参数
            camera.setParameters(parameters);
            this.ledId = ledId;
            this.isLedOn = isLedOn;
            MyTools.writeSimpleLogWithTime("设置参数完成  " + System.currentTimeMillis());
            camera.takePicture(null, null,callback );
        }
    }



    public void doStopCamera(){
        if (camera != null){
            camera.stopPreview();
            camera.release();
            isPreview = false;
        }
    }
    private Point screenResolution;
    private Point cameraResolution;
    private int previewFormat;
    private String previewFormatString;
    private final Pattern COMMA_PATTERN = Pattern.compile(",");
    private static final String TAG = "CameraInterface";
    private static final int TEN_DESIRED_ZOOM = 15;

    public void initFromCameraParameters(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        previewFormat = parameters.getPreviewFormat();
        previewFormatString = parameters.get("preview-format");
        Log.d(TAG, "Default preview format: " + previewFormat + '/' + previewFormatString);
        WindowManager manager = (WindowManager) ExceptionApplication.context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();

        screenResolution = new Point(display.getWidth(), display.getHeight());
//    display.getSize(screenResolution);
        Log.d(TAG, "Screen resolution: " + screenResolution);

        if(MainActivity.ISPORTRAIT){
//            //为竖屏添加
            Point screenResolutionForCamera = new Point();
            screenResolutionForCamera.x = screenResolution.x;
            screenResolutionForCamera.y = screenResolution.y;
            if (screenResolution.x < screenResolution.y) {
                screenResolutionForCamera.x = screenResolution.y;
                screenResolutionForCamera.y = screenResolution.x;
            }
            // 下句第二参数要根据竖屏修改
            cameraResolution = getCameraResolution(parameters, screenResolutionForCamera);
        }else{
//            横屏下参数
            cameraResolution = getCameraResolution(parameters, screenResolution);
        }

//        cameraResolution = new Point(640,480);
        Log.d(TAG, "Camera resolution: " + cameraResolution);
    }

    private Point getCameraResolution(Camera.Parameters parameters, Point screenResolution) {

        String previewSizeValueString = parameters.get("preview-size-values");
        // saw this on Xperia
        if (previewSizeValueString == null) {
            previewSizeValueString = parameters.get("preview-size-value");
        }

        Point cameraResolution = null;

        if (previewSizeValueString != null) {
            Log.d(TAG, "preview-size-values parameter: " + previewSizeValueString);
            cameraResolution = findBestPreviewSizeValue(previewSizeValueString, screenResolution);
        }

        if (cameraResolution == null) {
            // Ensure that the camera resolution is a multiple of 8, as the screen may not be.
            cameraResolution = new Point(
                    (screenResolution.x >> 3) << 3,
                    (screenResolution.y >> 3) << 3);
        }

        return cameraResolution;
    }
    private Point findBestPreviewSizeValue(CharSequence previewSizeValueString, Point screenResolution) {
        int bestX = 0;
        int bestY = 0;
        int diff = Integer.MAX_VALUE;
        for (String previewSize : COMMA_PATTERN.split(previewSizeValueString)) {

            previewSize = previewSize.trim();
            int dimPosition = previewSize.indexOf('x');
            if (dimPosition < 0) {
                Log.w(TAG, "Bad preview-size: " + previewSize);
                continue;
            }

            int newX;
            int newY;
            try {
                newX = Integer.parseInt(previewSize.substring(0, dimPosition));
                newY = Integer.parseInt(previewSize.substring(dimPosition + 1));
            } catch (NumberFormatException nfe) {
                Log.w(TAG, "Bad preview-size: " + previewSize);
                continue;
            }

            int newDiff = Math.abs(newX - screenResolution.x) + Math.abs(newY - screenResolution.y);
            if (newDiff == 0) {
                bestX = newX;
                bestY = newY;
                break;
            } else if (newDiff < diff) {
                bestX = newX;
                bestY = newY;
                diff = newDiff;
            }

        }

        if (bestX > 0 && bestY > 0) {
            return new Point(bestX, bestY);
        }
        return null;
    }

    private void setZoom(Camera.Parameters parameters) {

        String zoomSupportedString = parameters.get("zoom-supported");
        if (zoomSupportedString != null && !Boolean.parseBoolean(zoomSupportedString)) {
            return;
        }

        int tenDesiredZoom = TEN_DESIRED_ZOOM;

        String maxZoomString = parameters.get("max-zoom");
        if (maxZoomString != null) {
            try {
                int tenMaxZoom = (int) (10.0 * Double.parseDouble(maxZoomString));
                if (tenDesiredZoom > tenMaxZoom) {
                    tenDesiredZoom = tenMaxZoom;
                }
            } catch (NumberFormatException nfe) {
                Log.w(TAG, "Bad max-zoom: " + maxZoomString);
            }
        }

        String takingPictureZoomMaxString = parameters.get("taking-picture-zoom-max");
        if (takingPictureZoomMaxString != null) {
            try {
                int tenMaxZoom = Integer.parseInt(takingPictureZoomMaxString);
                if (tenDesiredZoom > tenMaxZoom) {
                    tenDesiredZoom = tenMaxZoom;
                }
            } catch (NumberFormatException nfe) {
                Log.w(TAG, "Bad taking-picture-zoom-max: " + takingPictureZoomMaxString);
            }
        }

        String motZoomValuesString = parameters.get("mot-zoom-values");
        if (motZoomValuesString != null) {
            tenDesiredZoom = findBestMotZoomValue(motZoomValuesString, tenDesiredZoom);
        }

        String motZoomStepString = parameters.get("mot-zoom-step");
        if (motZoomStepString != null) {
            try {
                double motZoomStep = Double.parseDouble(motZoomStepString.trim());
                int tenZoomStep = (int) (10.0 * motZoomStep);
                if (tenZoomStep > 1) {
                    tenDesiredZoom -= tenDesiredZoom % tenZoomStep;
                }
            } catch (NumberFormatException nfe) {
                // continue
            }
        }

        // Set zoom. This helps encourage the user to pull back.
        // Some devices like the Behold have a zoom parameter
        if (maxZoomString != null || motZoomValuesString != null) {
            parameters.set("zoom", String.valueOf(tenDesiredZoom / 10.0));
        }

        // Most devices, like the Hero, appear to expose this zoom parameter.
        // It takes on values like "27" which appears to mean 2.7x zoom
        if (takingPictureZoomMaxString != null) {
            parameters.set("taking-picture-zoom", tenDesiredZoom);
        }
    }

    private int findBestMotZoomValue(CharSequence stringValues, int tenDesiredZoom) {
        int tenBestValue = 0;
        for (String stringValue : COMMA_PATTERN.split(stringValues)) {
            stringValue = stringValue.trim();
            double value;
            try {
                value = Double.parseDouble(stringValue);
            } catch (NumberFormatException nfe) {
                return tenDesiredZoom;
            }
            int tenValue = (int) (10.0 * value);
            if (Math.abs(tenDesiredZoom - value) < Math.abs(tenDesiredZoom - tenBestValue)) {
                tenBestValue = tenValue;
            }
        }
        return tenBestValue;
    }
}
