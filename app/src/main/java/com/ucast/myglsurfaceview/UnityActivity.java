package com.ucast.myglsurfaceview;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import com.ucast.firstvuforia.UnityPlayerActivity;
import com.ucast.myglsurfaceview.beans.BoxMsg;

import java.util.Map;

public class UnityActivity extends UnityPlayerActivity {

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showTakephoto();
//                showBoxMsg();
            }
        },30);

//        int i = 0;
//        String path = BitCreateTools.getBitMapByStringReturnBitmaPath("Number:" + i + 1
//                + "\nWeight:" + (i+15) +"0 kg"
//                + "\nCountry:" + (i+35) +" s", Color.BLUE);
//        String path2 = BitCreateTools.getBitMapByStringReturnBitmaPath("Number:" + i + 1
//                + "\nWeight:" + (i+15) +"0 kg"
//                + "\nCountry:" + (i+35) +" s", Color.RED);

    }

    public void showTakephoto(){
        if(TakephotoActivity.boxs.isEmpty())
            return;

        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, BoxMsg> item : TakephotoActivity.boxs.entrySet()){
            BoxMsg one = item.getValue();
            Point p = one.getPointInScreen();
            if (p != null)
                sb.append(p.x + "#" + p.y + "#4.0#file://" + one.getShowPic() + ",");
        }
        sendMsgToUnity(sb.substring(0,sb.lastIndexOf(",")));
    }

    public void showBoxMsg(){
        String p1 = "file://" + Environment.getExternalStorageDirectory().getPath() + "/Ucast/port1.jpg";
        String p2 = "file://" + Environment.getExternalStorageDirectory().getPath() + "/Ucast/port2.jpg";
        String p3 = "file://" + Environment.getExternalStorageDirectory().getPath() + "/Ucast/port3.jpg";
        sendMsgToUnity("500#300#3.0#" + p1 + ",700#500#3.0#" + p2 + ",900#700#3.0#" + p3);
    }

    @Override
    protected void onResume() {
        super.onResume();


    }
//    @Override
//    public void onBackPressed() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                UnityPlayerActivity.this.onPause();
//                UnityPlayerActivity.this.onDestroy();
//            }
//        });
//        super.onBackPressed();
//    }
//    public void sendMsgToUnity(String data){
//        UnityPlayer.UnitySendMessage("scripts","CreateAGameobjByScrenPoint",data);
//    }
}
