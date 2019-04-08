package com.ucast.myglsurfaceview.nettySocket.sender;

import com.ucast.myglsurfaceview.nettySocket.map.NetPrinterChannelMap;
import com.ucast.myglsurfaceview.tools.Config;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

/**
 * Created by pj on 2019/3/27.
 */
public class SenderTools {
    public static boolean sendAllNetPrintClient(byte[] Data) {
        Set set = NetPrinterChannelMap.ToList();
        boolean isSendOk = false;
        for (Iterator iter = set.iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            Channel value = (Channel) entry.getValue();
            if (value == null || !value.isActive())
                return false;
            ByteBuf resp = Unpooled.copiedBuffer(Data);
            value.writeAndFlush(resp);
            isSendOk = true;
        }
        return isSendOk;
    }
}
