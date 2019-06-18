package com.tiger.lan.message;

public class MsgStatus {
    public static final int DEFAULT_WAKE_LOCK_GET_DATA_TIMEOUT = 1000;
    public int timeOut = DEFAULT_WAKE_LOCK_GET_DATA_TIMEOUT;   //超时时间
    public static final int SUCESS = 0 ;
    public static final int ERROR_TIMEOUT = 1 ;
    public int sucessId = SUCESS;//0 表示成功, 非0表示失败。1：超时
    public int sendCount;//发送的次数,最多发送3次
    public long sendingTime = 0; //发送的时间
    private final Object lock = new Object();

    public void releaseLock() {
        synchronized (lock) {
            lock.notify();
        }
    }

    public void wakeLock() {
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
