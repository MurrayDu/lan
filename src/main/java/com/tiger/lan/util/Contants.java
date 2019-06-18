package com.tiger.lan.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by tiger on 2019/1/29.
 */

public class Contants {
    public static final int MASTER_BROADCAST_PORT = 18110;   //master广播接收端的端口
    public static final int SUB_BROADCAST_PORT = 18111;   //subordinate广播接收端的端口
    public static final int MASTER_TCP_PORT = 18112;   //master tcp接收端的端口
    private static final String TAG = "Contants";

    /**
     * 获取自己的ip，只支持无线网卡
     * @return
     */
    public static String getSelfIp() {
        /*获取mac地址有一点需要注意的就是android 6.0版本后，以下注释方法不再适用，不管任何手机都会返回"02:00:00:00:00:00"这个默认的mac地址，这是googel官方为了加强权限管理而禁用了
        getSYstemService(Context.WIFI_SERVICE)方法来获得mac地址。*/
        String macAddress = null;
        StringBuffer buf = new StringBuffer();
        NetworkInterface networkInterface = null;
        String hostIp = "" ;
        try {
            networkInterface = NetworkInterface.getByName("wlan0");
            if (networkInterface == null) {
                return null ;
            }
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                if(!ni.getName().equals("wlan0")) {
                    continue;
                }
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
//                        return hostIp;
                        break;
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
            return "02:00:00:00:00:02";
        }
        return hostIp;
    }

    public static boolean isValideIp(String ip) {
        if(TextUtils.isEmpty(ip)) {
            return false;
        }
        if(ip.equals("0.0.0.0")) {
            return false ;
        }
        return true ;
    }

    public static boolean isWifiConnect(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.isConnected()) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    // connected to wifi
                    Log.e(TAG, "当前WIFI网络连接可用 ");
                    return true ;
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    // connected to the mobile provider's data plan
                    Log.e(TAG, "当前移动网络连接可用 ");
                }
            } else {
                Log.e(TAG, "当前没有网络连接");
            }
        } else {   // not connected to the internet
            Log.e(TAG, "当前没有网络连接");
        }
        return false ;
    }
}
