package com.ucast.myglsurfaceview.tools;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import java.lang.reflect.Method;

/**
 * Created by pj on 2018/11/9.
 */
public class ApManager {

    /**
     * 判断热点是否开启
     *
     * @param context
     * @return
     */
    public static boolean isApOn(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        try {
            Method method = wifimanager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifimanager);
        } catch (Throwable ignored) {
        }
        return false;
    }

    /**
     * 关闭WiFi
     *
     * @param context
     */
    public static void closeWifi(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        if (wifimanager.isWifiEnabled()) {
            wifimanager.setWifiEnabled(false);
        }
    }

    /**
     * 开启热点
     *
     * @param context
     * @param SSID     热点名称
     * @param password 热点密码
     * @return
     */
    public static boolean openAp(Context context, String SSID, String password) {
        if (TextUtils.isEmpty(SSID)) {
            return false;
        }

        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        if (wifimanager.isWifiEnabled()) {
            wifimanager.setWifiEnabled(false);
        }

        WifiConfiguration wifiConfiguration = getApConfig(SSID, password);
        try {
            if (isApOn(context)) {
                wifimanager.setWifiEnabled(false);
                closeAp(context);
            }

            Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifimanager, wifiConfiguration, true);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

 /**
     * 开启热点
     *
     * @param context
     * @param SSID     热点名称
     *
     * @return
     */
    public static boolean openApWithNoPassword(Context context, String SSID) {
        if (TextUtils.isEmpty(SSID)) {
            return false;
        }

        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        if (wifimanager.isWifiEnabled()) {
            wifimanager.setWifiEnabled(false);
        }

        WifiConfiguration wifiConfiguration = getConfigWithNoPawssord(SSID);
        try {
            if (isApOn(context)) {
                wifimanager.setWifiEnabled(false);
                closeAp(context);
            }

            Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifimanager, wifiConfiguration, true);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 关闭热点
     *
     * @param context
     */
    public static void closeAp(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        try {
            Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifimanager, null, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取开启热点后的IP地址
     *
     * @param context
     * @return
     */
    public static String getHotspotLocalIpAddress(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifimanager.getDhcpInfo();
        if (dhcpInfo != null) {
            int address = dhcpInfo.serverAddress;
            return ((address & 0xFF)
                    + "." + ((address >> 8) & 0xFF)
                    + "." + ((address >> 16) & 0xFF)
                    + "." + ((address >> 24) & 0xFF));
        }
        return null;
    }

    /**
     * 设置热点
     *
     * @param SSID     热点名称
     * @param password 热点密码
     * @return
     */
    private static WifiConfiguration getApConfig(String SSID, String password) {
        if (TextUtils.isEmpty(password)) {
            return null;
        }

        WifiConfiguration config = new WifiConfiguration();
        config.SSID = SSID;
        config.preSharedKey = password;
//        config.hiddenSSID = true;
        config.status = WifiConfiguration.Status.ENABLED;
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);



        return config;
    }

    public static WifiConfiguration getConfigWithNoPawssord(String ssid){
        if (TextUtils.isEmpty(ssid)) {
            return null;
        }
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = ssid;
        config.wepKeys[0] = "";
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        config.wepTxKeyIndex = 0;
        return config;
    }

    /**
     * 开启热点
     */
    public static void openHotspot(Context context, String ssid, String password) {
        closeWifi(context);
        if (isApOn(context)) {
            closeAp(context);
        }
        openAp(context, ssid, password);
    }
    /**
     * 开启热点
     */
    public static void openHotspotWithNoPassword(Context context, String ssid) {
        closeWifi(context);
        if (isApOn(context)) {
            closeAp(context);
        }
        openApWithNoPassword(context, ssid);
    }

//    public static void openNOPasswordWifiHotspot(WifiManager mWifiManager) {
//        WifiConfiguration mWifiConfig = mWifiManager.getWifiApConfiguration();
//
//        WifiInfo wifiInfo = mWifiManager.getConnectionInfo(); 
//
//        //获取AP模式连接设备数量
//
//        List<Station> cdl = mWifiManager.getApConnectedStas();
//
//
//        mWifiConfig.allowedAuthAlgorithms.clear();
//        mWifiConfig.allowedGroupCiphers.clear();
//        mWifiConfig.allowedKeyManagement.clear();
//        mWifiConfig.allowedPairwiseCiphers.clear();
//        mWifiConfig.allowedProtocols.clear();
//        int wifi_ap_state = mWifiManager.getWifiApState();
//        if ((wifi_ap_state == WifiManager.WIFI_AP_STATE_ENABLING) || (wifi_ap_state == WifiManager.WIFI_AP_STATE_ENABLED)) {
//            mWifiManager.setWifiApEnabled(null, false);//关闭wifi的AP模式
//        }
//        //设置NONE = 0模式
//        mWifiConfig.SSID = "none";
//        mWifiConfig.wepKeys[0] = "";
//        mWifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//        mWifiConfig.wepTxKeyIndex = 0;
//        mWifiManager.setWifiApEnabled(mWifiConfig, true);
//    }
}
