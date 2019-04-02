package com.ucast.myglsurfaceview.beans;


import android.graphics.Point;

import org.opencv.core.Mat;

/**
 * Created by pj on 2019/3/19.
 */
public class BoxMsg {
    private String nameKey;//提示框的唯一值
    private String showPic;//需要显示的图片
    private String showMsg;//需要显示的文字
    private boolean isShowPic = false;
    private String ledOffPhotoPath;//关灯时的红外相机的图片路径
    private String ledOnPhotoPath;//开灯时的红外相机的图片路径
    private Mat ledOffMat;
    private Mat ledOnMat;

    private Point pointInScreen;//经过比较两张图片  得到的屏幕上的点
    private String ledId;//led灯的编号

    public BoxMsg() {
    }

    public String getNameKey() {
        return nameKey;
    }

    public void setNameKey(String nameKey) {
        this.nameKey = nameKey;
    }

    public String getShowPic() {
        return showPic;
    }

    public void setShowPic(String showPic) {
        this.showPic = showPic;
    }

    public String getShowMsg() {
        return showMsg;
    }

    public void setShowMsg(String showMsg) {
        this.showMsg = showMsg;
    }

    public boolean isShowPic() {
        return isShowPic;
    }

    public void setShowPic(boolean showPic) {
        isShowPic = showPic;
    }

    public String getLedOffPhotoPath() {
        return ledOffPhotoPath;
    }

    public void setLedOffPhotoPath(String ledOffPhotoPath) {
        this.ledOffPhotoPath = ledOffPhotoPath;
    }

    public String getLedOnPhotoPath() {
        return ledOnPhotoPath;
    }

    public void setLedOnPhotoPath(String ledOnPhotoPath) {
        this.ledOnPhotoPath = ledOnPhotoPath;
    }

    public Mat getLedOffMat() {
        return ledOffMat;
    }

    public void setLedOffMat(Mat ledOffMat) {
        this.ledOffMat = ledOffMat;
    }

    public Mat getLedOnMat() {
        return ledOnMat;
    }

    public void setLedOnMat(Mat ledOnMat) {
        this.ledOnMat = ledOnMat;
    }

    public Point getPointInScreen() {
        return pointInScreen;
    }

    public void setPointInScreen(Point pointInScreen) {
        this.pointInScreen = pointInScreen;
    }

    public String getLedId() {
        return ledId;
    }

    public void setLedId(String ledId) {
        this.ledId = ledId;
    }


}
