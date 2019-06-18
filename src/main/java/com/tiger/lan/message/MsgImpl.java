package com.tiger.lan.message;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.tiger.lan.net.Device;
import com.tiger.lan.net.RealTcpSender;
import com.tiger.lan.net.impl.IDeviceListener;
import com.tiger.lan.net.impl.IMessageListener;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tiger on 2019/2/11.
 * 消息处理的类，消息格式，包头，解决粘包的问题，发送确认，心跳包等业务层的处理
 */

public class MsgImpl {
    private final String TAG = getClass().getSimpleName();
    private CopyOnWriteArrayList<SendMsgInfo> msgList ;
    private static AtomicInteger autoInteger = new AtomicInteger(0);
    private boolean isQuit = false ;
    private final int NEXT_SEND_INTERVAL = 300;
    private final int MAX_TRY_COUNT = 2 ;   //发送重试的次数
    private RealTcpSender msgSender;
    private IMessageListener messageListener ;
    private String recvData = "";
    public static final String HEAD = "<data20190212>";
    public static final String END = "</data20190212>";

    public MsgImpl(Device device, Socket socket, IDeviceListener deviceListener) throws IOException {
        msgSender = new RealTcpSender(this, device, socket, deviceListener);
        msgList = new CopyOnWriteArrayList<>();
        startSendThread();
    }

    /**
     * 处理新接收的消息
     * @param recvData
     */
    private void dealWithNewMsg(String recvData) {
        Message tcpMsgInfo = JSON.parseObject(recvData, Message.class);
        if(tcpMsgInfo.getMsgType() == Message.TYPE_HEART_ACK
                || tcpMsgInfo.getMsgType() == Message.TYPE_MSG_ACK) {//是回复的消息
            dealWithAckMsg(tcpMsgInfo);
        } else if(tcpMsgInfo.getMsgType() == Message.TYPE_HEART) {//发送的心跳包
            sendAck(tcpMsgInfo);
        } else{//正常发送过来的消息
            sendAck(tcpMsgInfo);
            onNewMessage(tcpMsgInfo.getContent());
        }
    }

    public void onNewMessage(String data) {
        if (messageListener != null) {
            messageListener.onNewMessage(data);
        }
    }

    /**
     * 数据格式为<data>数据</data>,解决粘包的问题
     */
    public void parseData(String data) {
        recvData = recvData + data;
        if (recvData.startsWith(HEAD)) {//按理来说所有的数据必须是<data>开头的，否则就是错误的
            int index;
            List<String> dataList = new ArrayList<>();
            while ((index = recvData.indexOf(END)) != -1) {
                String recv = recvData.substring(0, index);
                recv = recv.replaceFirst(HEAD, "");
                dataList.add(recv);
                recvData = recvData.substring(index + END.length());
            }
            for (String singleData : dataList) {
                dealWithNewMsg(singleData);
            }
        }
    }

    private void sendAck(Message tcpMsgInfo) {
        if(!tcpMsgInfo.isNeedAck()) {
            return ;
        }

        if(tcpMsgInfo.getMsgType() == Message.TYPE_HEART) {
            tcpMsgInfo.setMsgType(Message.TYPE_HEART_ACK);
        } else if(tcpMsgInfo.getMsgType() == Message.TYPE_MSG) {
            tcpMsgInfo.setMsgType(Message.TYPE_MSG_ACK);
        } else {
            return ;
        }
        msgSender.realSendMsg(tcpMsgInfo);
    }

    private void dealWithAckMsg(Message tcpMsgInfo) {
        Iterator<SendMsgInfo> iterator =  msgList.iterator();
        while (iterator.hasNext()) {
            SendMsgInfo msg = iterator.next();
            if(msg.getMsgId() == tcpMsgInfo.getMsgId()) {
                msgList.remove(msg);
                msg.setSucessId(0);
                msg.releaseLock();
                return ;
            }
        }
    }

    private void startSendThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!isQuit) {
                    Iterator<SendMsgInfo> iterator =  msgList.iterator();
                    while (iterator.hasNext()) {
                        SendMsgInfo msg = iterator.next();
                        if(msg.getSendCount() > MAX_TRY_COUNT) {
                            msg.setSucessId(MsgStatus.ERROR_TIMEOUT);
                            msgList.remove(msg);
                            msg.releaseLock();
                            continue;
                        }

                        if(!msg.isTimeOut()) {
                            continue;
                        }

                        msg.setSendCount(msg.getSendCount() + 1);
                        msg.setSendingTime(System.currentTimeMillis());
                        msgSender.realSendMsg(msg);
                    }
                    sleep(NEXT_SEND_INTERVAL);
                }
            }
        }).start();
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送普通的消息
     * @param data
     * @param isNeedAck 是否需要回复，如果需要回复那么阻塞等待回复
     */
    private boolean sendCommonMsg(String data, boolean isNeedAck) {
        return realSendMsg(Message.TYPE_MSG, data, isNeedAck);
    }

    /**
     * 默认的发送不阻塞
     * @param msg
     * @return
     */
    public void sendMsg(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return ;
        }
        sendCommonMsg(msg, false);
    }

    /**
     * 该方法可以知道消息是否成功到达（有低概率出现消息到达了但是还是发回false的情况。）
     * @param msg
     * @return 是否发送成功
     */
    public boolean sendMsgWithAck(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return false;
        }
        boolean isSucess = sendCommonMsg(msg, true);
        return isSucess ;
    }

    /**
     * 发送心跳包
     * @param isNeedAck 是否需要回复，如果需要回复那么阻塞等待回复
     */
    public boolean sendHeartMsg(boolean isNeedAck) {
        return realSendMsg(Message.TYPE_HEART, "", isNeedAck);
    }

    private boolean realSendMsg(int msgType, String data, boolean isNeedAck) {
        Message msg = new Message();
        msg.setMsgId(getNextId());
        msg.setMsgType(msgType);
        msg.setContent(data);

        SendMsgInfo sendMsg = new SendMsgInfo();
        sendMsg.setMsg(msg);
        sendMsg.setNeedAck(isNeedAck);
        if(isNeedAck) {
            msgList.add(sendMsg);
            sendMsg.wakeLock();
        } else {
            msgSender.realSendMsg(sendMsg);
        }
        return sendMsg.getSucessId() == 0 ? true : false ;
    }

    public void startHeart() {
        msgSender.startHeart();
    }

    public void setMessageListener(IMessageListener messageListener) {
        this.messageListener = messageListener;
    }

    private int getNextId() {
        return autoInteger.getAndIncrement();
    }

    public void release() {
        isQuit = true ;
        messageListener = null ;
        msgSender.release();
        msgList.clear();
    }
}
