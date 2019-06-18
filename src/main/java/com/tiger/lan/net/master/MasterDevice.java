package com.tiger.lan.net.master;

/**
 * Created by tiger on 2019/3/18.
 */

public class MasterDevice {
    private String ip ;
    private String mac ;
    private String metaData;//服务器的元数据，用来携带服务器自带的数据

    public MasterDevice() {

    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    @Override
    public String toString() {
        return "MasterDevice{" +
                "ip='" + ip + '\'' +
                ", mac='" + mac + '\'' +
                ", metaData='" + metaData + '\'' +
                '}';
    }

}
