package com.ucast.myglsurfaceview.cameraInterface;


import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.ucast.myglsurfaceview.MainActivity;
import com.ucast.myglsurfaceview.exception.ExceptionApplication;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Created by pj on 2019/2/1.
 */
public class CameraInterface {
    private static CameraInterface cameraInterface = new CameraInterface();
    Camera camera;
    boolean isPreview;
    public static CameraInterface getInstance() {
        return cameraInterface;
    }

    public void doOpenCamera(){
        camera = Camera.open(0);

    }

    public boolean isPreviewing(){
        return isPreview;
    }

    public void doStartPreview(SurfaceTexture surfaceTexture){
        if (camera != null) {
            try {
                camera.setPreviewTexture(surfaceTexture);
                initFromCameraParameters(camera);
                Camera.Parameters parameters = camera.getParameters();
                if (MainActivity.ISPORTRAIT)
                    camera.setDisplayOrientation(90);
                parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);  // ucast银联相机
//                parameters.setPreviewSize(screenResolution.y, screenResolution.x);//一般手机
                MainActivity.FRAMECALLBACKWIDTH = screenResolution.x;
                MainActivity.FRAMECALLBACKHEIGHT = screenResolution.y;
//                parameters.setPictureFormat(PixelFormat.JPEG);
                setZoom(parameters);
                camera.setParameters(parameters);
                camera.startPreview();
                isPreview = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

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
