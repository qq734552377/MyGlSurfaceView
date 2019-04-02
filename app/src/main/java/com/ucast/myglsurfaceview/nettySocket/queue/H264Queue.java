package com.ucast.myglsurfaceview.nettySocket.queue;

import com.ucast.myglsurfaceview.nettySocket.h264.h264data;
import com.ucast.myglsurfaceview.nettySocket.map.NetPrinterChannelMap;
import com.ucast.myglsurfaceview.nettySocket.sender.SenderTools;
import com.ucast.myglsurfaceview.tools.Common;
import com.ucast.myglsurfaceview.tools.MyTools;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by pj on 2019/3/27.
 */
public class H264Queue {
    public int queuesize = 50;
    public static final byte[] cut_paper_byte = {0x1D,0x56};
    public ArrayBlockingQueue<h264data> h264Queue = new ArrayBlockingQueue<>(queuesize);
    private StringBuilder sb = new StringBuilder();

    public H264Queue(int queuesize) {
        this.queuesize = queuesize;
        new Thread(new Runnable() {
            @Override
            public void run() {
                continueRun();
            }
        }).start();

    }
    private void continueRun(){
        while (true){
            myRun();
        }
    }

    private void myRun() {
        synchronized (this){
            try {
                h264data one = h264Queue.poll();
                if (one != null){
//                    byte[] packageData = new byte[one.data.length + cut_paper_byte.length];
//                    System.arraycopy(one.data,0,packageData,0,one.data.length);
//                    System.arraycopy(cut_paper_byte,0,packageData,one.data.length,cut_paper_byte.length);
//                    SenderTools.sendAllNetPrintClient(packageData);
                    if( NetPrinterChannelMap.getMapSize() > 0) {
//                        sendAsString(one);
                        sendAsByte(one);
//                        Thread.sleep(1);
                    }
                }else {
                    Thread.sleep(1);
                }
            }catch (Exception e){

            }
        }
    }

    public void sendAsString(h264data one){
        sb.append("@h264_data,");
        sb.append(MyTools.encode(one.data));
        sb.append("$");
        SenderTools.sendAllNetPrintClient(sb.toString().getBytes());
        sb.delete(0,sb.length());
    }

    public void sendAsByte(h264data one){
        //数据 02开头03结尾的封装
        byte[] send = Common.getOnePackage(Common.HEAD,one.data);
        SenderTools.sendAllNetPrintClient(send);
    }

    public void addItem(byte[] data){
        synchronized (this){
            putData(data);
        }
    }


    public void putData(byte[] buffer) {
        if (h264Queue.size() >= queuesize) {
            h264Queue.poll();
        }
        h264data data = new h264data();
        data.data = buffer;
        h264Queue.add(data);
    }


}
