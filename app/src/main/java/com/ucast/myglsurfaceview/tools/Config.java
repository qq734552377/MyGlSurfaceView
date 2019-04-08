package com.ucast.myglsurfaceview.tools;

import com.ucast.myglsurfaceview.exception.CrashHandler;

/**
 * Created by pj on 2019/1/28.
 */
public class Config {
    public static int NET_PRINT_PORT = 8850;
    public static int PORTBAUDRATE = 115200;
    public static boolean ISDEBUG = true;
    public static boolean USESTRINGPATH = false;
    public static boolean isStartRecoding = false;
    public static boolean IsReplyToClient = true;
    public static String PORTPATH = "/dev/ttyS1";

    public static byte[] OPENLED =  new byte[]{(byte)0xFB,(byte)0xBC,(byte)0x0A,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x01};
    public static byte[] CLOSELED = new byte[]{(byte)0xFB,(byte)0xBC,(byte)0x0A,(byte)0x00,(byte)0x00,(byte)0x04,(byte)0x01};

    public static String LOGPATH = CrashHandler.ALBUM_PATH + "/simple_led_Log.txt";
    public static String LOGPATHWITHTIME = CrashHandler.ALBUM_PATH + "/simple_led_time_Log.txt";
}
