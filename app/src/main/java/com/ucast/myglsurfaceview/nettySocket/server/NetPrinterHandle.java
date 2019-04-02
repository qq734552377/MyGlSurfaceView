package com.ucast.myglsurfaceview.nettySocket.server;


import com.ucast.myglsurfaceview.exception.ExceptionApplication;
import com.ucast.myglsurfaceview.nettySocket.h264.h264data;
import com.ucast.myglsurfaceview.nettySocket.map.NetPrinterChannelMap;
import com.ucast.myglsurfaceview.nettySocket.queue.H264Queue;
import com.ucast.myglsurfaceview.tools.Common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

/**
 * Created by Administrator on 2016/2/4.
 */
public class NetPrinterHandle extends ChannelInboundHandlerAdapter {


    public NetPrinterHandle() {
    }



    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        try {
            ByteBuf buff = (ByteBuf) msg;
            int len = buff.readableBytes();
            byte[] buffer = new byte[len];
            buff.readBytes(buffer);
            //TODO 处理客户端过来的数据数据的地方

            ReferenceCountUtil.release(msg);
        } catch (Exception e) {
            ctx.close();
            ExceptionApplication.gLogger.info("NetPrinterHandle channelRead : read exception  -->"+ ctx.channel().id()+"  Exception : "+e.toString());
        }
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
//        ExceptionApplication.gLogger.info("channelReadComplete : deblue service start sucess waitting for linked by client");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {//心跳检测还没有测试
        if (!(evt instanceof IdleStateEvent)) {
            return;
        }
        Channel channel = ctx.channel();
        IdleStateEvent event = (IdleStateEvent) evt;
        if(event.state() == IdleState.ALL_IDLE)
        {
            channel.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NetPrinterChannelMap.Add(ctx.channel());
        //客户端连接
//        ByteBuf resp = Unpooled.copiedBuffer(new byte[]{0x01,0x00,0x00});
//        if (h264data.IDR_SEND_ITEM != null)
//            ctx.channel().writeAndFlush(h264data.getIDR());
        byte[] data = Common.getOnePackage(Common.HEAD,new byte[]{0x01,0x02,0x03,0x04,0x05,(byte)0xFB,0x34});
//        ctx.channel().writeAndFlush(data);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NetPrinterChannelMap.Remove(ctx.channel().id().toString());
    }
}
