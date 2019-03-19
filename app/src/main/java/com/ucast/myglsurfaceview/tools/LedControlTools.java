package com.ucast.myglsurfaceview.tools;

/**
 * Created by pj on 2019/3/19.
 */
public class LedControlTools {

    public native int[] nativeProcessFrame(long matAddrRgbaOff, long matAddrRgba, int thresh);
}
