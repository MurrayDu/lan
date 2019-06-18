package com.tiger.lan.net.master;

import android.os.Build;

import com.alibaba.fastjson.JSON;
import com.tiger.lan.net.Finder;
import com.tiger.lan.util.Contants;
import com.tiger.lan.util.LogUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

/**
 * Created by tiger on 2019/1/29.
 * 用来发现从设备
 */

public class SubordinateFinder extends Finder {
    private DatagramSocket datagramSocket = null;
    private DatagramPacket datagramPacket = null;
    private MulticastSocket multicastSocket = null;
    private boolean isQuit = false;
    private String metaData;

    public SubordinateFinder() {
        super();
    }

    @Override
    public void onIpInited() {
        startFinderService();
    }

    /**
     * 启动接收客户端搜索的服务
     */
    public void startFinderService() {
        Runnable findThread = new Runnable() {
            @Override
            public void run() {
                try {
                    multicastSocket = new MulticastSocket(Contants.MASTER_BROADCAST_PORT);
                    InetAddress serverAddress = InetAddress.getByName(getBrocastIp());
                    byte buf[] = new byte[1024];
                    DatagramPacket pkg = new DatagramPacket(buf, buf.length, serverAddress, Contants.MASTER_BROADCAST_PORT);
                    LogUtil.i(TAG, "startFinderService  getBrocastIp=" + getBrocastIp());
                    while (!isQuit) {
                        if(multicastSocket == null) {
                            break;
                        }
                        multicastSocket.receive(pkg);
                        String data = new String(buf, 0, pkg.getLength());
                        LogUtil.e(TAG, "startFinderService  data=" + data);
                        if (data.equals(GET_IP)) {
                            sendIp();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        executorService.execute(findThread);
    }

    @Override
    public void startFinder() {
        isQuit = false;
        super.startFinder();
    }

    @Override
    public void stopFinder() {
        super.stopFinder();
        stopSocket();
    }

    /**
     * 发送IP给客户端
     */
    private void sendIp() {
        if (datagramSocket == null) {
            if (!initSocket()) {
                return;
            }
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    datagramSocket.send(datagramPacket);
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean initSocket() {
        try {
            datagramSocket = new DatagramSocket(Contants.SUB_BROADCAST_PORT);
            InetAddress serverAddress = InetAddress.getByName(getBrocastIp());
            MasterDevice masterDevice = new MasterDevice();
            masterDevice.setIp(getSelfIp());
            masterDevice.setMac(Build.SERIAL);
            masterDevice.setMetaData(metaData);
            String sendData = JSON.toJSONString(masterDevice);
            datagramPacket = new DatagramPacket(sendData.getBytes(), sendData.length(), serverAddress, Contants.SUB_BROADCAST_PORT);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void release() {
        super.release();
        stopSocket();
        if (executorService != null) {
            executorService.shutdown();
            executorService = null ;
        }
    }

    public void stopSocket() {
        isQuit = true;
        if (multicastSocket != null) {
            multicastSocket.close();
            multicastSocket = null ;
        }

        if (datagramSocket != null) {
            datagramSocket.close();
            datagramSocket = null ;
        }
    }

    /**
     * 服务器自带的参数，连接之初会发给客户端
     * @param metaData
     */
    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

}
