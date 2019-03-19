package com.ucast.myglsurfaceview.tools;

import com.ucast.myglsurfaceview.exception.CrashHandler;

/**
 * Created by pj on 2019/1/28.
 */
public class Config {
    public static int PORTBAUDRATE = 115200;
    public static String PORTPATH = "";

    public static String LOGPATH = CrashHandler.ALBUM_PATH + "/simple_led_Log.txt";
    public static String LOGPATHWITHTIME = CrashHandler.ALBUM_PATH + "/simple_led_time_Log.txt";
}
