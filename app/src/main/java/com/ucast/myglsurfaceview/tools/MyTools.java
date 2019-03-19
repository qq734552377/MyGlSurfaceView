package com.ucast.myglsurfaceview.tools;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;


import com.ucast.myglsurfaceview.MainActivity;
import com.ucast.myglsurfaceview.exception.ExceptionApplication;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by pj on 2019/1/28.
 */
public class MyTools {
    public static final String TOKEN ="info";
    public static final String LOGIN_ID ="login_id";
    public static final String PASSWORD ="password";
    public static final String EMP_NAME ="emp_name";
    public static final String COMPANY_NAME ="company_name";
    public static final String GROUP_ID ="group_id";
    public static final String ROLE ="role";
    public static final String EMP_PHONENUMBER ="emp_phonenumber";
    public static final String EMP_EMIAL ="emp_emial";
    public static final String CREATE_DATE ="create_date";
    public static final String WORK_STATE ="work_state";
    public static final String OVERTIME_ID ="overtime_id";





    public static void writeToFile(String path , String data){
        try{
            File f = new File(path);
            FileOutputStream fout = new FileOutputStream(f , true);
            BufferedOutputStream buff = new BufferedOutputStream(fout);
            buff.write((data + "\r\n").getBytes());
            buff.flush();
            buff.close();
        }catch (Exception e){
            System.out.print(e.toString());
        }
    }

    //将屏幕旋转锁定
    public static int setRoat(Context context) {
        Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
        //得到是否开启
        int flag = Settings.System.getInt(context.getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0);
        return flag;
    }

    public static boolean isNetworkAvailable(Context context) {
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null)
        {
            return false;
        }
        else
        {
            // 获取NetworkInfo对象
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

            if (networkInfo != null && networkInfo.length > 0)
            {
                for (int i = 0; i < networkInfo.length; i++)
                {
                    System.out.println(i + "===状态===" + networkInfo[i].getState());
                    System.out.println(i + "===类型===" + networkInfo[i].getTypeName());
                    // 判断当前网络状态是否为连接状态
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void writeSimpleLog(String log){
        writeToFile(Config.LOGPATH,log);
    }
    public static void writeSimpleLogWithTime(String log){
        writeToFile(Config.LOGPATHWITHTIME,millisToDateStringNoSpace(System.currentTimeMillis()) + "  : " +log);
    }

    /**
     *  将指定byte数组以16进制的形式返回
     * */
    public static String printHexString(byte[] b) {
        StringBuilder r = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            if(b[i] == 0x00){
                r.append("00 ");
                continue;
            }else if(b[i] == 0xFF){
                r.append("FF ");
                continue;
            }
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            r.append(hex.toUpperCase() + " ");
        }
        return r.toString();
    }

    public static byte getSumJiaoYan(byte[] res){
        int sum = 0;
        for (int i = 0; i < res.length; i++) {
            sum += res[i];
        }
        return (byte) (sum & 0xFF);
    }

    public static byte[] getSendData(byte[] res){
        byte jiaoYan = getSumJiaoYan(res);
        byte[] dest = new byte[res.length + 1];
        System.arraycopy(res,0,dest,0,res.length);
        dest[dest.length - 1] = jiaoYan;
        return dest;
    }

    public static String millisToDateStringOnlyYMD(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String date;
        Date curDate = new Date(time);
        date = formatter.format(curDate);
        return date;
    }
    public static String millisToDateStringNoSpace(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String date;
        Date curDate = new Date(time);
        date = formatter.format(curDate);
        return date;
    }

    public static float[] getPicVertex(int width,int height){

        float x = (float) width / (float) ExceptionApplication.SCREENPOINT.x * 0.5f;
        float y = (float) height / (float) ExceptionApplication.SCREENPOINT.y * 0.5f;
        float[] vertex = new float[]{
                -x, -y,
                x , -y,
                -x, y,
                x , y
        };
//        vertex[0] = -x;
//        vertex[1] = -y;
//        vertex[2] = x;
//        vertex[3] = -y;
//        vertex[4] = -x;
//        vertex[5] = y;
//        vertex[6] = x;
//        vertex[7] = y;
        return vertex;
    }
    static float COORD1[] = {
            -0.1f, -0.1f,
            0.1f, -0.1f,
            -0.1f, 0.1f,
            0.1f, 0.1f,
    };
}
