package com.ucast.myglsurfaceview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ucast.myglsurfaceview.exception.ExceptionApplication;

/**
 * Created by pj on 2016/11/21.
 */
public class StartBrocast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent ootStartIntent = new Intent(context, UpdateService.class);
        ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(ootStartIntent);
        Intent start_adv_intent = new Intent(ExceptionApplication.getInstance(), TakephotoActivity.class);
        start_adv_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  //注意，必须添加这个标记，否则启动会失败
        ExceptionApplication.getInstance().startActivity(start_adv_intent);
    }
}
