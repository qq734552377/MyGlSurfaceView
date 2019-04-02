package com.ucast.myglsurfaceview.nettySocket.queue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pj on 2019/3/27.
 */
public class H264Queuemanager {
    private List<H264Queue> queues = new ArrayList<>();
    private static H264Queuemanager h264Queuemanager = null;
    private H264Queuemanager() {
        for (int i = 0; i < 1; i++) {
            H264Queue one = new H264Queue(3);
            queues.add(one);
        }
    }

    public static H264Queuemanager getInstance(){
        if (h264Queuemanager == null){
            synchronized (H264Queuemanager.class){
                if (h264Queuemanager == null){
                    h264Queuemanager = new H264Queuemanager();
                }
            }
        }
        return h264Queuemanager;
    }

    public H264Queue getOneH264Queue(int position){
        if (position < 0 || position >= 1) {
            return null;
        }
        return  queues.get(position);
    }
}
