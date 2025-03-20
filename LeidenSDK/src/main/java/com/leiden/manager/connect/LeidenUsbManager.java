package com.leiden.manager.connect;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import com.leiden.interfaces.LeidenConnectResultCallBack;
import com.leiden.interfaces.LeidenIConnectInterface;
import com.leiden.interfaces.LeidenScanDeviceCallBack;
import com.leiden.interfaces.LeidenSendReadCallBack;
import com.leiden.model.LeidenDevice;
import com.leiden.utils.ParameterUtil;
import com.leiden.utils.Util;

import java.util.HashMap;
import java.util.Iterator;

public class LeidenUsbManager implements LeidenIConnectInterface {

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private static final String ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE";

    private Context context;

    private LeidenDevice currentDevice;

    private LeidenDevice tempDevice;

    //Android USB设备连接管理器
    private UsbManager usbManager;

    public UsbManager getUsbManager() {
        return usbManager;
    }

    /**
     * 块输出端点
     */
    private UsbEndpoint epBulkOut;
    private UsbEndpoint epBulkIn;

    private UsbDeviceConnection conn = null;

    private static LeidenUsbManager instance = null;

    private LeidenUsbManager(Context context) {
        this.context = context.getApplicationContext();
        beginCheckUSBState();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_USB_PERMISSION);
        context.registerReceiver(broadcastReceiver,intentFilter);
    }

    public static LeidenUsbManager getInstance(Context context) {
        if (instance == null) {
            synchronized (LeidenUsbManager.class) {
                if (instance == null) {
                    instance = new LeidenUsbManager(context);
                }
            }
        }
        return instance;
    }

    //请求权限
    public boolean hasPermission(LeidenDevice device){

        if(device == null || device.getUsbDevice() == null){
            return false;
        }
        return usbManager.hasPermission(device.getUsbDevice());
    }

    public boolean requestPermission(LeidenDevice device){
        if(device == null || device.getUsbDevice() == null){
            return false;
        }
        tempDevice = device;
        usbManager.requestPermission(device.getUsbDevice(),
                PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION),
                        0));

        threadExecutorManager.post(new Runnable() {
            @Override
            public void run() {
                tempDevice = null;
            }
        },10 * 1000);


        return true;
    }


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                if(tempDevice != null) {
                    connect(tempDevice);
                    tempDevice = null;
                }
            }
        }
    };

    @Override
    public boolean isConnect() {
        return currentDevice != null;
    }

    /**
     * 通知连接结果
     */
    private void noticeConnectResult(final boolean isSuccess) {
        if (leidenConnectResultCallBack != null) {
            if (!isSuccess) {
                close();
                leidenConnectResultCallBack.fail(currentDevice);
            } else {
                leidenConnectResultCallBack.success(currentDevice);
            }
        }
    }

    @Override
    public boolean connect(LeidenDevice device) {
        UsbDevice usbDevice = device.getUsbDevice();
        //3)查找设备接口
        if (usbDevice == null) {
            noticeConnectResult(false);
            return false;
        }
        UsbInterface usbInterface = null;
        for (int i = 0; i < usbDevice.getInterfaceCount(); i++) {
            //一个设备上面一般只有一个接口，有两个端点，分别接受和发送数据
            usbInterface = usbDevice.getInterface(i);
            break;
        }
        //4)获取usb设备的通信通道endpoint
        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            UsbEndpoint ep = usbInterface.getEndpoint(i);
            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (UsbConstants.USB_DIR_OUT == ep.getDirection()) {//输出
                    epBulkOut = ep;
                } else {
                    epBulkIn = ep;
                }
            }
        }
        if (epBulkIn == null || epBulkOut == null) {
            noticeConnectResult(false);
            return false;
        }
        //5)打开conn连接通道
        if (usbManager.hasPermission(usbDevice)) {
            //有权限，那么打开
            conn = usbManager.openDevice(usbDevice);
        } else {
            usbManager.requestPermission(usbDevice,
                    PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION),
                            0));
            noticeConnectResult(false);
            return false;
        }
        if (null == conn) {
            noticeConnectResult(false);
            return false;
        }
        //打开设备
        if (conn.claimInterface(usbInterface, true)) {
            currentDevice = device;
            noticeConnectResult(true);
            beginCheckUSBState();
            return true;
        } else {
            noticeConnectResult(false);
            return false;
        }
    }



    private void beginCheckUSBState(){
        threadExecutorManager.execute(new Runnable() {
            @Override
            public void run() {
                while(true) {

                    if(isConnect()) {
                        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
                        if (deviceList.size() == 0) {//连接着
                            if (leidenConnectResultCallBack != null) {
                                close();
                            }
                        }
                    }

                    Util.threadSleep(500);
                }

            }
        });
    }


    @Override
    public boolean close() {
        if (conn != null) {
            conn.close();
        }
        if(leidenConnectResultCallBack != null){
            leidenConnectResultCallBack.close(currentDevice);
        }
        conn = null;
        epBulkIn = null;
        epBulkOut = null;
        currentDevice = null;
        tempDevice = null;
        return false;
    }

    byte[] byte2 = new byte[1024];

    @Override
    public byte[] read() {

        if (!isConnect()) {
            return null;
        }

        int ret = conn.bulkTransfer(epBulkIn, byte2, byte2.length, 3000);
        if(ret == -1){
            return null;
        }

        byte[] readData = new byte[ret];

        System.arraycopy(byte2,0,readData,0,ret);


//        int inMax = epBulkIn.getMaxPacketSize();
//        ByteBuffer byteBuffer = ByteBuffer.allocate(inMax);
//        UsbRequest usbRequest = new UsbRequest();
//        usbRequest.initialize(conn, epBulkIn);
//        usbRequest.queue(byteBuffer, inMax);
//        if (conn.requestWait() == usbRequest) {
//            byte[] retData = byteBuffer.array();
//            return retData;
//        }

        return readData;
    }

    @Override
    public int write(byte[] data) {

        if (!isConnect()) {
            return -1;
        }

        int length = 1024 * 8;
        byte[] sendData = new byte[length];
        int index = 0;
        while(index < data.length){
            if (!isConnect()) {
                return -1;
            }
            if(data.length - index >= length) {
                System.arraycopy(data, index, sendData, 0, length);
            } else {
                sendData = new byte[data.length - index];
                System.arraycopy(data,index,sendData,0,data.length - index);
            }
            index += length;
            if (conn.bulkTransfer(epBulkOut, sendData, sendData.length, 0) >= 0) {
                //0 或者正数表示成功
            } else {
                int i = 0;
                while(i < 20){
                    if(conn.bulkTransfer(epBulkOut, sendData, sendData.length, 0) >= 0){
                        break;
                    } else {
                        i++;
                        Util.threadSleep(100);
                    }
                }
                if(i >= 20){
                    return -1;
                }
            }
        }

        return 1;
    }

    @Override
    public void sendReadAsync(final byte[] bytes, final int time, final int number, final LeidenSendReadCallBack leidenSendReadCallBack) {
        if (!isConnect()) {
            if (leidenSendReadCallBack != null) {
                leidenSendReadCallBack.callError(3);
            }
            return;
        }

        threadExecutorManager.execute(new Runnable() {
            @Override
            public void run() {
                sendRead(bytes, time, number, leidenSendReadCallBack);
            }
        });
    }

    public String bytesToString(byte[] bytes){
        StringBuilder sb = null;
        sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(aByte).append(",");
        }
        return sb.toString();
    }

    @Override
    public void sendRead(byte[] bytes, int time, int number, final LeidenSendReadCallBack leidenSendReadCallBack) {
        try {
            StringBuilder sb = null;
            if (ParameterUtil.isDebug) {
                sb = new StringBuilder();
                for (byte aByte : bytes) {
                    sb.append(aByte).append(",");
                }
            }
            int write = write(bytes);
            if (write == 1) {
                Util.threadSleep(2000);
                final byte[] read = read();
                threadExecutorManager.post(new Runnable() {
                    @Override
                    public void run() {
                        if (leidenSendReadCallBack != null) {
                            leidenSendReadCallBack.callBytes(read);
                        }
                    }
                });
                return;
            }
            threadExecutorManager.post(new Runnable() {
                @Override
                public void run() {
                    if (leidenSendReadCallBack != null) {
                        leidenSendReadCallBack.callError(1);
                    }
                }
            });
        } catch (Exception e) {
            threadExecutorManager.post(new Runnable() {
                @Override
                public void run() {
                    if (leidenSendReadCallBack != null) {
                        leidenSendReadCallBack.callError(2);
                    }
                }
            });
        }

    }

    @Override
    public boolean autoConnect() {
        //USB设备不支持自动连接
        return false;
    }

    @Override
    public int beginSearch() {
        //1)创建usbManager
        if (usbManager == null)
            usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        if (leidenScanDeviceCallBack != null) {
            leidenScanDeviceCallBack.beginScan();
        }

        //2)获取到所有设备 选择出满足的设备
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        //创建返回数据
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            LeidenDevice leidenDevice = new LeidenDevice();
            leidenDevice.setUsbDevice(device);
            if (leidenScanDeviceCallBack != null) {
                leidenScanDeviceCallBack.scanDevice(leidenDevice);
            }
        }
        if (leidenScanDeviceCallBack != null) {
            leidenScanDeviceCallBack.stopScan();
        }

        return 1;
    }

    @Override
    public void stopSearch() {
        if (leidenScanDeviceCallBack != null) {
            leidenScanDeviceCallBack.stopScan();
        }
    }

    @Override
    public LeidenDevice getCurrentDevice() {
        return currentDevice;
    }

    private LeidenScanDeviceCallBack leidenScanDeviceCallBack;
    private LeidenConnectResultCallBack leidenConnectResultCallBack;

    @Override
    public void setLeidenScanDeviceCallBack(LeidenScanDeviceCallBack leidenScanDeviceCallBack) {
        this.leidenScanDeviceCallBack = leidenScanDeviceCallBack;
    }

    @Override
    public void setLeidenConnectResultCallBack(LeidenConnectResultCallBack leidenConnectResultCallBack) {
        this.leidenConnectResultCallBack = leidenConnectResultCallBack;
    }
}
