package com.tiger.lan.net.impl;

import com.tiger.lan.net.master.MasterDevice;

import java.util.List;

/**
 * Created by tiger on 2019/3/18.
 */

public interface IScanListener {
    void onScanResult(List<MasterDevice> serviceList);
    void onScanFinish();
}
