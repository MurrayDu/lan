package com.tiger.lan.message;

/**
 * Created by tiger on 2019/2/11.
 * 维护已发送消息的状态。
 */

public class SendMsgInfo {
    private Message msg ;
    private MsgStatus msgStatus ;

    public SendMsgInfo() {
        msgStatus = new MsgStatus();
    }

    public Message getMsg() {
        return msg;
    }

    public void setMsg(Message msg) {
        this.msg = msg;
    }

    public long getSendingTime() {
        return msgStatus.sendingTime;
    }

    public void setSendingTime(long sendingTime) {
        msgStatus.sendingTime = sendingTime;
    }

    public int getSendCount() {
        return msgStatus.sendCount;
    }

    public void setSendCount(int sendCount) {
        msgStatus.sendCount = sendCount;
    }

    public int getSucessId() {
        return msgStatus.sucessId;
    }

    public void setSucessId(int sucessId) {
        msgStatus.sucessId = sucessId;
    }

    public int getTimeOut() {
        return msgStatus.timeOut;
    }

    public void setTimeOut(int timeOut) {
        msgStatus.timeOut = timeOut;
    }

    public long getMsgId() {
        return msg.getMsgId();
    }

    public void setNeedAck(boolean needAck) {
        msg.setNeedAck(true);
    }

    public void releaseLock() {
        msgStatus.releaseLock();
    }

    public void wakeLock() {
        msgStatus.wakeLock();
    }

    public boolean isTimeOut() {
        if(System.currentTimeMillis() - msgStatus.sendingTime > msgStatus.timeOut) {
            return true ;
        }
        return false ;
    }
}
