package com.tiger.lan.net.master;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;

import com.tiger.lan.net.Device;
import com.tiger.lan.net.impl.IDeviceListener;
import com.tiger.lan.util.Contants;
import com.tiger.lan.util.LogUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by tiger on 2019/1/29.
 */

public class TcpService {
    private static final String TAG = "ExpressionCmdRecv";
    private ServerSocket serverSocket = null;
    private boolean isQuit = false ;
    private IDeviceListener deviceListener ;
    private SubordinateFinder subordinateFinder ;
    private Context mContext ;
    private List<Device> deviceList = new ArrayList<>();

    public TcpService(Context context) {
        mContext = context ;
        subordinateFinder = new SubordinateFinder();
        regiestNet();
    }

    private void regiestNet() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        mContext.registerReceiver(mNetworkChangeListener,filter);
    }

    private void startFinder() {
        subordinateFinder.startFinder();
    }

    private BroadcastReceiver mNetworkChangeListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.i(TAG, "onReceive " + intent.getAction());
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                if(Contants.isWifiConnect(context)) {
                    startService();
                } else {
                    if(subordinateFinder != null) {
                        subordinateFinder.stopFinder();
                    }
                    releaseDeviceList();
                    stopSocket();
                }
            }
        }
    } ;

    private void startService() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(Contants.MASTER_TCP_PORT) ;
                } catch (IOException e) {
                    e.printStackTrace();
                    return ;
                }
                isQuit = false ;
                //必须先启动tcp的服务，才能启动finder，否则有概率造成client找不到master的情况
                startFinder();
                while(!isQuit) {
                    if(serverSocket == null) {
                        break;
                    }
                    try {
                        Socket socket = serverSocket.accept();
                        Device device = new Device(socket, innerDeviceListener);
                        deviceList.add(device) ;
                        innerDeviceListener.onDeviceConnect(device);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void setDeviceListener(IDeviceListener deviceListener) {
        this.deviceListener = deviceListener;
    }

    private IDeviceListener innerDeviceListener = new IDeviceListener() {
        @Override
        public void onDeviceDisConnect(Device device) {
            if(deviceList.contains(device)) {
                deviceList.remove(device);
            }
            device.release();
            if(deviceListener != null) {
                deviceListener.onDeviceDisConnect(device);
            }
        }

        @Override
        public void onDeviceConnect(Device device) {
            if(deviceListener != null) {
                deviceListener.onDeviceConnect(device);
            }
        }
    } ;

    public void release() {
        subordinateFinder.release();
        stopSocket();
        mContext.unregisterReceiver(mNetworkChangeListener);
    }

    private void releaseDeviceList() {
        Iterator<Device> iterator = deviceList.iterator();
        while (iterator.hasNext()) {
            Device device = iterator.next() ;
            iterator.remove();
            device.release();
            if(deviceListener != null) {
                deviceListener.onDeviceDisConnect(device);
            }
        }
    }

    public List<Device> getConnectedDevice() {
        return deviceList ;
    }

    private void stopSocket() {
        isQuit = true ;
        if(serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            serverSocket = null ;
        }
    }

    public void setMasterDeviceMetaData(String masterDeviceMetaData) {
        subordinateFinder.setMetaData(masterDeviceMetaData);
    }

}
