package com.ucast.myglsurfaceview.nettySocket.map;


import com.ucast.myglsurfaceview.tools.MyTools;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;

/**
 * Created by Administrator on 2016/2/3.
 */
public class NetPrinterChannelMap {

    public static Map<String, Channel> map = new ConcurrentHashMap<String, Channel>();

    public static void Add(Channel channel) {
        MyTools.writeSimpleLog("客户端连接过来了--》" + channel.id());
        map.put(channel.id().toString(), channel);
    }

    public static Channel GetChannel(String clientId) {

        return map.get(clientId);
    }

    public static int getMapSize(){
        return map.size();
    }

    public static void Remove(String key) {
        MyTools.writeSimpleLog("客户端连接断开了--》" + key);
        map.remove(key);
    }
    public static Set<Map.Entry<String, Channel>> ToList()
    {
        return map.entrySet();
    }
}
