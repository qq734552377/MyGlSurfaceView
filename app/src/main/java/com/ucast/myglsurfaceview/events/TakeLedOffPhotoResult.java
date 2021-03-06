package com.ucast.myglsurfaceview.events;

import org.opencv.core.Mat;

/**
 * Created by pj on 2019/3/19.
 */
public class TakeLedOffPhotoResult {
    String path;
    String ledId;
    Mat mat;

    public TakeLedOffPhotoResult(String path) {
        this.path = path;
    }

    public TakeLedOffPhotoResult(String path, String ledId) {
        this.path = path;
        this.ledId = ledId;
    }

    public TakeLedOffPhotoResult( Mat mat,String ledId) {
        this.ledId = ledId;
        this.mat = mat;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLedId() {
        return ledId;
    }

    public void setLedId(String ledId) {
        this.ledId = ledId;
    }

    public Mat getMat() {
        return mat;
    }

    public void setMat(Mat mat) {
        this.mat = mat;
    }
}
