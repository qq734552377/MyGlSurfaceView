package com.ucast.myglsurfaceview.events;

/**
 * Created by pj on 2019/3/19.
 */
public class TakeLedOnPhotoResult {
    String path;
    String ledId;

    public TakeLedOnPhotoResult(String path) {
        this.path = path;
    }

    public TakeLedOnPhotoResult(String path, String ledId) {
        this.path = path;
        this.ledId = ledId;
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
}
