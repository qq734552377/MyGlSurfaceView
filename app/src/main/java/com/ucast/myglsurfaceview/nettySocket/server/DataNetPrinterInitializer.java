package com.ucast.myglsurfaceview.nettySocket.server;



import java.util.concurrent.TimeUnit;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Created by Administrator on 2016/2/4.
 */
public class DataNetPrinterInitializer extends ChannelInitializer {


    public DataNetPrinterInitializer() {
    }

    public void initChannel(Channel channel) {
        NetPrinterHandle handle = new NetPrinterHandle();
        channel.pipeline().addLast("idleStateHandler", new IdleStateHandler(300, 300,300, TimeUnit.SECONDS));
        channel.pipeline().addLast("handler", handle);
    }

}
