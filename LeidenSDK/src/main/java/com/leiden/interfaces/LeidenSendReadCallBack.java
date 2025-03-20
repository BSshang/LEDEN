package com.leiden.interfaces;

public interface LeidenSendReadCallBack {
    void callBytes(byte[] bytes);

    /**
     * error 错误信息的编码
     * error == 1 当前发送的数据，没有返回值
     * error == 2 当前发送的数据，在处理的时候，发送的异常
     * error == 3 蓝牙未连接
     */
    void callError(int error);
}
