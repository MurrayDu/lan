package com.tiger.lan.sub;
import android.os.Handler;
import android.os.Message;
import com.alibaba.fastjson.JSON;
import com.tiger.lan.net.Finder;
import com.tiger.lan.net.impl.IScanListener;
import com.tiger.lan.net.master.MasterDevice;
import com.tiger.lan.util.Contants;
import com.tiger.lan.util.LogUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by tiger on 2019/1/29.
 * 用来发现主设备
 */

public class MasterFinder extends Finder {
    private MulticastSocket multicastSocket;
    private DatagramSocket datagramSocket ;
    private DatagramPacket datagramPacket ;
    private IScanListener scanListener ;
    public final int FIND_STATUS_NONE = 0 ;
    public final int FIND_STATUS_GET_SELF_IP = 1 ;  //正在获取自己的ip
    public final int FIND_STATUS_GET_MASTER_IP = 2 ;   //正在获取服务器列表
    public final int FIND_STATUS_FIND_MASTER_FINISH = 3 ;   //获取服务器列表结束
    private int findStatus = FIND_STATUS_NONE ;
    private boolean isScan = false ;
    private List<MasterDevice> masterDeviceList  = new ArrayList<>();

    private final int TIME_OUT = 1 ;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIME_OUT:
                    stopFinder();
                    if(scanListener != null) {
                        scanListener.onScanFinish();
                    }
                    break ;
                default:
                    break;
            }
        }
    } ;

    public MasterFinder() {
        super();
    }

    @Override
    public void onIpInited() {
        LogUtil.i(TAG,"onIpInited " );
        findStatus = FIND_STATUS_GET_MASTER_IP;
        sendGetIpRequest();
        recvIpData();
    }

    public void startFinder(int timeOut) {
        LogUtil.i(TAG,"startFinder  findStatus=" + findStatus);
        handler.removeMessages(TIME_OUT);
        handler.sendEmptyMessageDelayed(TIME_OUT, timeOut);
        synchronized (MasterFinder.class) {
            if(findStatus != FIND_STATUS_NONE && findStatus != FIND_STATUS_FIND_MASTER_FINISH) {
                return ;
            }
            findStatus = FIND_STATUS_GET_SELF_IP;
        }
        isScan = true ;
        masterDeviceList.clear();
        startFinder();
    }

    @Override
    public void stopFinder() {
        isScan = false ;
        if(findStatus == FIND_STATUS_NONE) {
            return ;
        }
        super.stopFinder();
        stopSocket();
        handler.removeCallbacksAndMessages(null);
        findStatus = FIND_STATUS_NONE ;
    }

    public void setScanListener(IScanListener scanListener) {
        this.scanListener = scanListener;
    }

    //获取master的ip
    private void recvIpData() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    multicastSocket= new  MulticastSocket (Contants.SUB_BROADCAST_PORT);
                    InetAddress serverAddress = InetAddress.getByName(getBrocastIp());
                    byte[] data = new byte[1024];
                    DatagramPacket pkg = new DatagramPacket(data , data.length,serverAddress, Contants.SUB_BROADCAST_PORT);
                    while (isScan) {
                        if(multicastSocket == null) {
                            break;
                        }
                        multicastSocket.receive(pkg);
                        String recvData = new String(data, 0, pkg.getLength());
                        LogUtil.i(TAG, "recv recvData=" + recvData);
                        addDevice(recvData);
                    }
                    findStatus = FIND_STATUS_FIND_MASTER_FINISH ;
                } catch (IOException e) {
                    e.printStackTrace();
                    stopFinder();
                }
            }
        });
    }

    private void addDevice(String data) {
        MasterDevice masterDevice = JSON.parseObject(data, MasterDevice.class);
        if(masterDevice == null) {
            return ;
        }
        Iterator<MasterDevice> iterator = masterDeviceList.iterator();
        while (iterator.hasNext()) {
            MasterDevice device = iterator.next() ;
            if(device.getMac().equals(masterDevice.getMac())) { //表示是同一个设备
                if(device.getIp().equals(masterDevice.getIp())) {   //表示没有任何改变
                    return;
                } else {    //表示同一个设备,但是ip不一样，那么删除之前的 并添加到list中
                    masterDeviceList.remove(device);
                    break;
                }
            } else {
                if(device.getIp().equals(masterDevice.getIp())) {   //表示不同的设备,但是ip一样，那么删除之前的并添加到list中
                    masterDeviceList.remove(device);
                    break;
                } else {
                    continue;
                }
            }
        }
        masterDeviceList.add(masterDevice);
        notifyScanDevice();
    }

    private void notifyScanDevice() {
        if(scanListener != null) {
            scanListener.onScanResult(masterDeviceList);
        }
    }
    /**
     * 发送请求获取主设备的ip
     */
    private void sendGetIpRequest() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    datagramSocket = new DatagramSocket(Contants.MASTER_BROADCAST_PORT);
                    InetAddress serverAddress = InetAddress.getByName(getBrocastIp());
                    String data = GET_IP;
                    datagramPacket = new DatagramPacket(data.getBytes(),data.length(),serverAddress, Contants.MASTER_BROADCAST_PORT);
                    LogUtil.i(TAG,"GetIpRequest  1111 findStatus=" +findStatus + "  getBrocastIp()=" +getBrocastIp());
                    while (isScan && findStatus != FIND_STATUS_FIND_MASTER_FINISH && findStatus != FIND_STATUS_NONE) {
                        datagramSocket.send(datagramPacket);
                        Thread.sleep(2000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    stopSocket();
                }
                LogUtil.i(TAG,"GetIpRequest  end ");
            }
        });
    }

    public void stopSocket() {
        try {
            if (datagramSocket != null) {
                datagramSocket.close();
                datagramSocket = null;
            }
            if (datagramPacket != null) {
                datagramPacket = null;
            }
            if (multicastSocket != null) {
                multicastSocket.close();
                multicastSocket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void release() {
        super.release();
        isScan = false ;
        stopSocket();
        findStatus = FIND_STATUS_NONE ;
    }
}
