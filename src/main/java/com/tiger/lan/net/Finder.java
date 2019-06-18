package com.tiger.lan.net;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import com.tiger.lan.util.Contants;
import com.tiger.lan.util.LogUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tiger on 2019/1/29.
 */

public abstract class Finder {
    public final String TAG = getClass().getSimpleName() ;
    protected final String GET_IP = "get_ip" ;
    private final int TRY_GET_IP = 1 ;
    private String brocastIp ;
    private String selfIp ;
    protected ExecutorService executorService ;
    private boolean isFinding = false ;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TRY_GET_IP:
                    initIp();
                    break ;
                default:
                    break;
            }
        }
    };

    public Finder() {
            executorService = Executors.newCachedThreadPool();
    }

    public void initIp() {
        String ip = Contants.getSelfIp();
        if(TextUtils.isEmpty(ip) && Contants.isValideIp(ip) && isFinding) {
            mHandler.sendEmptyMessageDelayed(TRY_GET_IP, 2000) ;
            return ;
        }
        selfIp = ip ;
        brocastIp = selfIp.substring(0,selfIp.lastIndexOf(".")) + ".255";
        LogUtil.e(TAG,"initIp  brocastIp=" + brocastIp + "  ip=" + ip);
        isFinding = false ;
        onIpInited();
    }

    private void stopFindIp() {
        mHandler.removeMessages(TRY_GET_IP);
    }

    public void startFinder() {
        LogUtil.e(TAG,"startFinder  isFinding=" + isFinding);
        synchronized (Finder.class) {
            if (isFinding) {
                return;
            }
            isFinding = true;
        }
        initIp();
    }

    public void stopFinder() {
        if(isFinding) {
            isFinding = false ;
            stopFindIp();
        }
    }

    public void release() {
        stopFindIp();
        if(executorService != null) {
            executorService.shutdown();
        }
    }

    public abstract void onIpInited();

    public String getBrocastIp() {
        return brocastIp;
    }

    public void setBrocastIp(String brocastIp) {
        this.brocastIp = brocastIp;
    }

    public String getSelfIp() {
        return selfIp;
    }

    public void setSelfIp(String selfIp) {
        this.selfIp = selfIp;
    }
}
