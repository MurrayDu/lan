package com.tiger.lan.net.impl;

import com.tiger.lan.net.Device;

/**
 * Created by tiger on 2019/3/18.
 */

public interface IDeviceListener {
    /**
     * 设备断开连接
     * @param device
     */
    void onDeviceDisConnect(Device device);

    /**
     * 设备连接成功
     * @param device
     */
    void onDeviceConnect(Device device);
}
