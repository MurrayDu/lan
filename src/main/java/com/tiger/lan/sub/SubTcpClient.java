package com.tiger.lan.sub;

import android.content.Context;
import android.util.Log;

import com.tiger.lan.net.Device;
import com.tiger.lan.net.impl.IDeviceListener;
import com.tiger.lan.net.impl.IScanListener;
import com.tiger.lan.util.Contants;
import com.tiger.lan.util.LogUtil;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by tiger on 2019/1/30.
 * 从设备和主设备通讯的类
 */

public class SubTcpClient {
    private final String TAG = "SubTcpClient";
    private MasterFinder masterFinder ;
    private IDeviceListener outerDeviceListener ;
    private Context mContext ;
    private List<Device> deviceList = new ArrayList<>();

    public SubTcpClient(Context context, IDeviceListener outerDeviceListener) {
        mContext = context ;
        this.outerDeviceListener = outerDeviceListener ;
    }

    public void startScan(IScanListener scanListener) {
        LogUtil.i(TAG," ----  startScan  ---- " );
        startScan(scanListener, 30000);
    }

    public void stopScan() {
        if(masterFinder == null) {
            masterFinder = new MasterFinder();
        }
        masterFinder.stopFinder();
    }

    /**
     * 开始搜索服务器
     * @param scanListener
     * @param timeOut   超时时间，默认30s,单位ms
     */
    public void startScan(IScanListener scanListener, int timeOut) {
        if(masterFinder == null) {
            masterFinder = new MasterFinder();
        }
        masterFinder.setScanListener(scanListener);
        masterFinder.startFinder(timeOut);
    }

    /**
     * 连接服务器，不能在主线程中运行
     * @param ip
     */
    public void connect(String ip){
        LogUtil.i(TAG,"connect  ip=" +ip );
        try {
            Socket socket = new Socket(ip, Contants.MASTER_TCP_PORT);
            Device device = new Device(socket, innerDeviceListener);
            device.startHeart();
            deviceList.add(device) ;
            innerDeviceListener.onDeviceConnect(device);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private IDeviceListener innerDeviceListener = new IDeviceListener() {
        @Override
        public void onDeviceDisConnect(Device device) {
            if(deviceList.contains(device)) {
                deviceList.remove(device);
            }
            device.release();
            if(outerDeviceListener != null) {
                outerDeviceListener.onDeviceDisConnect(device);
            }
        }

        @Override
        public void onDeviceConnect(Device device) {
            if(outerDeviceListener != null) {
                outerDeviceListener.onDeviceConnect(device);
            }
        }
    } ;

    private void releaseDeviceList(boolean isNotify) {
        Iterator<Device> iterator = deviceList.iterator();
        while (iterator.hasNext()) {
            Device device = iterator.next();
            if(deviceList.contains(device)) {
                deviceList.remove(device);
            }
            device.release();
            if(outerDeviceListener != null && isNotify) {
                outerDeviceListener.onDeviceDisConnect(device);
            }
        }
    }

    public void clear() {
        releaseDeviceList(true);
        stopScan();
        masterFinder.stopSocket();
    }

    public void release() {
        releaseDeviceList(false);
        stopScan();
        masterFinder.release();
    }
}
