package com.ucast.myglsurfaceview.nettySocket.h264;

import com.ucast.myglsurfaceview.nettySocket.sender.SenderTools;
import com.ucast.myglsurfaceview.tools.MyTools;

/**
 * Created by user111 on 2018/3/14.
 */

public class h264data {

    public byte[] data;

    public int type;

    public long ts;
    public static byte[] IDR_SEND_ITEM = null;
    public static byte[] getIDR(){
        return IDR_SEND_ITEM;
    }
    public static void setIDR(byte[] data){
        StringBuilder sb = new StringBuilder();
        sb.append("@h264_data,");
        sb.append(MyTools.encode(data));
        sb.append("$");
        IDR_SEND_ITEM = sb.toString().getBytes();
    }
}
