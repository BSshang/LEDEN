package com.leiden.interfaces;

import com.leiden.manager.ThreadExecutorManager;
import com.leiden.model.LeidenDevice;

public interface LeidenIConnectInterface {

    ThreadExecutorManager threadExecutorManager = ThreadExecutorManager.getInstance();

    //当前设备是否连接
    boolean isConnect();

    boolean connect(LeidenDevice device);

    boolean close();

    byte[] read();

    int write(byte[] data);

    void sendReadAsync(final byte[] bytes, final int time,
                       final int number, final LeidenSendReadCallBack leidenSendReadCallBack);

    void sendRead(byte[] bytes, int time, int number,
                  final LeidenSendReadCallBack leidenSendReadCallBack);

    //自动连接
    boolean autoConnect();

    //开始搜索
    int beginSearch();
    //停止搜索
    void stopSearch();

    LeidenDevice getCurrentDevice();


    //addScanBlueCallBack
    //addConnectResultCallBack

    void setLeidenScanDeviceCallBack(LeidenScanDeviceCallBack leidenScanDeviceCallBack);

    void setLeidenConnectResultCallBack(LeidenConnectResultCallBack leidenConnectResultCallBack);


}
