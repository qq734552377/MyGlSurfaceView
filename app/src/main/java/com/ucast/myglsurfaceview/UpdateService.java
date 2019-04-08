package com.ucast.myglsurfaceview;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.ucast.myglsurfaceview.nettySocket.server.NioNetPrintServer;
import com.ucast.myglsurfaceview.tools.MyTools;

import org.greenrobot.eventbus.EventBus;

public class UpdateService extends Service {
    private Thread videoServer;
    private NioNetPrintServer server;
    public static boolean isStart = false;
    public UpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        Notification notification = new Notification();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        startForeground(0, notification);
        super.onCreate();
        MyTools.writeSimpleLogWithTime("服务开启了");
        if (isStart)
            return;
        server = new NioNetPrintServer();
        videoServer = new Thread(server);
        videoServer.start();
        isStart = true;
//        EventBus.getDefault().register(this);
    }

    /**
     * 当服务被杀死时重启服务
     * */
    public void onDestroy() {
        stopForeground(true);
        Intent localIntent = new Intent();
        localIntent.setClass(this, UpdateService.class);
//        EventBus.getDefault().unregister(this);
        isStart= false;
        if (server != null){
            server.Close();
            server = null;
        }
        this.startService(localIntent);    //销毁时重新启动Service
    }
}
