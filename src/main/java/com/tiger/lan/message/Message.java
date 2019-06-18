package com.tiger.lan.message;

/**
 * Created by tiger on 2019/2/11.
 * 实际发送出去的消息体
 */

public class Message {
    public static final int TYPE_HEART = 100 ;//心跳包
    public static final int TYPE_HEART_ACK = 101 ;//心跳包的回复
    public static final int TYPE_MSG = 200 ;//普通消息
    public static final int TYPE_MSG_ACK = 201 ;//普通消息的ack

    private int msgId ;    //消息的编号
    private boolean isNeedAck = false;//是否需要回复,默认是不需要
    /**
     * @link TYPE_HEART, TYPE_HEART_ACK, TYPE_MSG
     */
    private int msgType ;   //消息的类型
    private String content ;    //消息的内容
    public Message() {

    }

    public boolean isNeedAck() {
        return isNeedAck;
    }

    public void setNeedAck(boolean needAck) {
        isNeedAck = needAck;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "TcpMsgInfo{" +
                "msgId=" + msgId +
                ", isNeedAck=" + isNeedAck +
                ", msgType=" + msgType +
                ", content='" + content + '\'' +
                '}';
    }
}
