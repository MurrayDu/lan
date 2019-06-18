package com.tiger.lan.net;

import com.tiger.lan.message.MsgImpl;
import com.tiger.lan.net.impl.IDeviceListener;
import com.tiger.lan.net.impl.IMessageListener;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by tiger on 2019/3/20.
 * 服务器和客户端连接的设备对象
 */

public class Device {
    private final String TAG = getClass().getSimpleName();
    private String deviceInfo ;
    private String ip;
    protected MsgImpl msgImpl ;

    public Device(Socket socket, IDeviceListener deviceListener ) {
        try {
            this.msgImpl = new MsgImpl(this, socket, deviceListener);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ip = new String(socket.getInetAddress().getHostAddress()) ;
    }

    final public void startHeart() {
        msgImpl.startHeart();
    }

    public void sendMsg(String msg) {
        if(msgImpl != null) {
            msgImpl.sendMsg(msg);
        }
    }

    public boolean sendMsgWithAck(String msg) {
        if(msgImpl != null) {
            return msgImpl.sendMsgWithAck(msg);
        }
        return false ;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setMessageListener(IMessageListener messageListener) {
        if(msgImpl != null) {
            msgImpl.setMessageListener(messageListener);
        }
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getIp() {
        return ip;
    }

    public void release() {
        if(msgImpl != null) {
            msgImpl.release();
        }
    }
}
