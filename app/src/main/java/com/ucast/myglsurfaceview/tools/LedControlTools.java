package com.ucast.myglsurfaceview.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

import com.ucast.myglsurfaceview.exception.ExceptionApplication;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

/**
 * Created by pj on 2019/3/19.
 */
public class LedControlTools {

    public native int[] nativeProcessFrame(long matAddrRgbaOff, long matAddrRgba, int thresh);

    private boolean isfirstTime = true;
    private Point picPoint;

    private static LedControlTools ledControlTools = null;
    private LedControlTools() {
    }

    public static LedControlTools getInstance(){
        if (ledControlTools == null){
            synchronized (LedControlTools.class){
                if (ledControlTools == null){
                    ledControlTools = new LedControlTools();
                }
            }
        }
        return ledControlTools;
    }

    public Point getPoint(String ledOffPicPath , String ledOnPicPath){
//        MyTools.writeSimpleLogWithTime("将path转换为Mat前  " + System.currentTimeMillis());
        Mat ledoff = getMatNativeByPath(ledOffPicPath);
//        MyTools.writeSimpleLogWithTime("将path转换为Mat后1  " + System.currentTimeMillis());
        Mat ledon = getMatNativeByPath(ledOnPicPath);
//        MyTools.writeSimpleLogWithTime("将path转换为Mat后2 准备分析图片 " + System.currentTimeMillis());
        int thresh = 200;
        if (ledoff != null && ledon != null){
            int[] result = nativeProcessFrame(ledoff.getNativeObjAddr(),ledon.getNativeObjAddr(),thresh);
            Point screenPoint = ExceptionApplication.PREVIEWSCREENPOINT;
            int x = screenPoint.x * result[0] / picPoint.x ;
            int y = screenPoint.y * result[1] / picPoint.y ;
            return new Point(x,y);
        }
//        MyTools.writeSimpleLogWithTime("分析图片完成  " + System.currentTimeMillis());
        return null;
    }
    public Point getPoint(Mat ledOffMat , Mat ledOnMat){
        if (picPoint == null)
            picPoint = new Point(2592,1944);
//        MyTools.writeSimpleLogWithTime(" 准备分析图片 " + System.currentTimeMillis());
        int thresh = 200;
        if (ledOffMat != null && ledOnMat != null){
            int[] result = nativeProcessFrame(ledOffMat.getNativeObjAddr(),ledOnMat.getNativeObjAddr(),thresh);
            Point screenPoint = ExceptionApplication.PREVIEWSCREENPOINT;
            int x = screenPoint.x * result[0] / picPoint.x ;
            int y = screenPoint.y * result[1] / picPoint.y ;
            return new Point(x,y);
        }
//        MyTools.writeSimpleLogWithTime("分析图片完成  " + System.currentTimeMillis());
        return null;
    }

    public Mat getMatNativeByPath(String path){
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if ( bitmap == null)
            return null;
        Mat src = new Mat();
        Utils.bitmapToMat(bitmap, src);
        if (isfirstTime){
            picPoint = new Point(bitmap.getWidth(),bitmap.getHeight());
            isfirstTime = false;
        }

        return src;
    }
}
