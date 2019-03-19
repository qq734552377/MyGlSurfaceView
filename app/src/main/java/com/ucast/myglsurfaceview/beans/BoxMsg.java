package com.ucast.myglsurfaceview.beans;

/**
 * Created by pj on 2019/3/19.
 */
public class BoxMsg {
    private String nameKey;
    private String showPic;
    private String showMsg;
    private boolean isShowPic = false;
    private String ledOffPhotoPath;
    private String ledOnPhotoPath;
    private int[] point;
    private String ledId;

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

    public int[] getPoint() {
        return point;
    }

    public void setPoint(int[] point) {
        this.point = point;
    }

    public String getLedId() {
        return ledId;
    }

    public void setLedId(String ledId) {
        this.ledId = ledId;
    }


}
