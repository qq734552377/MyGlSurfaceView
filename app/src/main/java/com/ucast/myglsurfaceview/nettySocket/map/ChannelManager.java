package com.ucast.myglsurfaceview.nettySocket.map;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import io.netty.channel.Channel;

/**
 * Created by pj on 2019/3/28.
 */
public class ChannelManager {
    private static ArrayBlockingQueue<Channel> channels = new ArrayBlockingQueue<>(10);

    public static void addChannel(Channel channel){
        if (channels.size() >= 10){
            channels.poll();
        }
        channels.add(channel);
    }
}
