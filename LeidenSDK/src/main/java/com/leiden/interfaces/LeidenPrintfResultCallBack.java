package com.leiden.interfaces;

public interface LeidenPrintfResultCallBack {
    /**
     * result
     *  1 ===> 打印成功(数据发送完成)
     *  2 ===> 指令错误(例如 需要TSPL指令，结果检测到
     *  打印机是ESC指令)
     *  3 ===> 蓝牙未连接
     */
    void callBack(int result);

    /**
     * 传输进度
     */
    void progress(int total,int progress);


    //打印成功
    int LEIDEN_PRINTF_RESULT_SUCCESS = 1;
    //打印错误
    int LEIDEN_PRINTF_RESULT_CMD_ERROR = 2;
    //蓝牙未连接
    int LEIDEN_PRINTF_RESULT_BLUETOOTH = 3;
}
