package com.tiger.lan.net;
import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.tiger.lan.message.MsgImpl;
import com.tiger.lan.message.SendMsgInfo;
import com.tiger.lan.message.Message;
import com.tiger.lan.net.impl.IDeviceListener;
import com.tiger.lan.net.impl.IMessageListener;
import com.tiger.lan.util.LogUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tiger on 2019/1/29.
 * tcp消息收发的类
 */

public class RealTcpSender {
    private final String TAG = getClass().getSimpleName();
    private boolean isQuit = false;
    private Socket socket = null;
    private OutputStream outputStream = null;
    private InputStream inputStream = null;
    private ExecutorService executorService = null;
    private IDeviceListener deviceListener = null ;
    private MsgImpl msgImpl;
    private Device device ;

    public RealTcpSender(MsgImpl msgImpl, Device device, Socket socket, IDeviceListener deviceListener) throws IOException {
        this.executorService = Executors.newCachedThreadPool();
        this.device = device ;
        this.msgImpl = msgImpl;
        this.socket = socket;
        socket.setKeepAlive(true);
        outputStream = socket.getOutputStream();
        this.deviceListener = deviceListener ;
        recvData();
    }

    private void recvData() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                //获取输入流
                LogUtil.i(TAG, "recvData start");
                try {
                    inputStream = socket.getInputStream();
                    while (!isQuit) {
                        byte[] buffer = new byte[1024];
                        int count = inputStream.read(buffer);
                        if (count == -1) {
                            if (deviceListener != null && isQuit == false) {
                                deviceListener.onDeviceDisConnect(device);
                            }
                            break;
                        }
                        String data = new String(buffer, 0, count);
                        msgImpl.parseData(data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeSocket();
                }
                LogUtil.i(TAG, "startAccept end");
            }
        });
    }

    private void closeSocket() {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
            if (inputStream != null) {
                inputStream.close();
                inputStream = null;
            }
            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startHeart() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                int failCount = 0 ;
                while (!isQuit) {
                    boolean isSucess = msgImpl.sendHeartMsg(true);
                    Log.i(TAG,"  isSucess=" +isSucess + "  failCount=" +failCount);
                    if(!isSucess) {
                        failCount ++ ;
                        if (deviceListener != null && isQuit == false && failCount >= 3) {
                            deviceListener.onDeviceDisConnect(device);
                            break;
                        }
                    } else {//成功一次 那么重新计数
                        failCount = 0 ;
                    }

                    try {
                        Thread.sleep(getSleepTime(failCount));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //获取心跳包的时间，根据错误的次数动态更新
    private int getSleepTime(int failCount) {
        if(failCount == 0) {
            return 10000 ;
        } else if(failCount == 1) {
            return 5000 ;
        } else {
            return 2000 ;
        }
    }

    public void realSendMsg(SendMsgInfo msg) {
        realSendMsg(msg.getMsg());
    }

    public void realSendMsg(final Message msg) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socket != null && !socket.isConnected()) {
                        closeSocket();
                        return;
                    }
                    outputStream.write(addHeadForMsg(JSON.toJSONString(msg)).getBytes());
                    outputStream.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String addHeadForMsg(String msg) {
        return MsgImpl.HEAD + msg + MsgImpl.END;
    }

    public void release() {
        isQuit = true;
        closeSocket();
        msgImpl.release();
        deviceListener = null;
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
