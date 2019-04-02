package com.ucast.myglsurfaceview.tools;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;


/**
 * Created by pj on 2017/7/10.
 */

public class MyDialog {

//    public static void showDialog(Context context, String s) {
//        AlertDialog alertDialog = new AlertDialog.Builder(context).setPositiveButton(context.getResources().getString(R
//                .string.queding), null).create();
//        alertDialog.setTitle(context.getResources().getString(R.string.tishi));
//        alertDialog.setMessage(s);
//        alertDialog.show();
//    }

    public static void showToast(Context context, String s) {
        Toast.makeText(context,s,Toast.LENGTH_SHORT).show();
    }
//    public static void showSnack(View view, String s) {
//        Snackbar.make(view,s,Snackbar.LENGTH_SHORT).show();
//    }

    public static ProgressDialog createProgressDialog(Context context, String s){
        ProgressDialog dialog2 = new ProgressDialog(context);
        dialog2.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条的形式为圆形转动的进度条
        dialog2.setCancelable(false);// 设置是否可以通过点击Back键取消
        dialog2.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
//            dialog2.setTitle(mContext.getResources().getString(R.string.tishi));
        dialog2.setMessage(s);
        return dialog2;
    }
}
