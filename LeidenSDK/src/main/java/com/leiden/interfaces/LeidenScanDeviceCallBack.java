package com.leiden.interfaces;

import com.leiden.model.LeidenDevice;

public interface LeidenScanDeviceCallBack {
    void scanDevice(LeidenDevice leidenDevice);
    void beginScan();
    void stopScan();
}
