package com.leiden.manager.connect;

import android.content.Context;

import com.leiden.interfaces.LeidenConnectResultCallBack;
import com.leiden.interfaces.LeidenCurrentConnectTypeListener;
import com.leiden.interfaces.LeidenIConnectInterface;
import com.leiden.interfaces.LeidenScanDeviceCallBack;
import com.leiden.interfaces.LeidenSendReadCallBack;
import com.leiden.model.LeidenDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理器 可选择蓝牙连接 USB连接等
 */
public class LeidenDeviceManager implements LeidenIConnectInterface, LeidenConnectResultCallBack, LeidenScanDeviceCallBack {

    private Context context;

    //1 是蓝牙 2是USB
    private int connectType = -1;

    public static final int BLUE_TYPE = 1;
    public static final int USB_TYPE = 2;


    private LeidenDeviceManager(Context context){
        this.context = context.getApplicationContext();
        changeConnect(BLUE_TYPE);
    }

    private static LeidenDeviceManager instance = null;

    public static LeidenDeviceManager getInstance(Context context) {
        if(instance == null) {
            synchronized (LeidenDeviceManager.class) {
                if (instance == null) {
                    instance = new LeidenDeviceManager(context);
                }
            }
        }
        return instance;
    }

    //具体的连接的接口
    LeidenIConnectInterface leidenIConnectInterface;

    //1 是蓝牙 2是USB
    public boolean changeConnect(final int connectType){
        if(this.connectType == connectType){
            return false;
        }

        if(leidenIConnectInterface != null){
            leidenIConnectInterface.stopSearch();
            leidenIConnectInterface.close();
            leidenIConnectInterface.setLeidenScanDeviceCallBack(null);
            leidenIConnectInterface.setLeidenConnectResultCallBack(null);
            leidenIConnectInterface = null;
        }

        if(connectType == BLUE_TYPE){
            leidenIConnectInterface = LeidenBluetoothManager.getInstance(context);
        } else if(connectType == USB_TYPE){
            leidenIConnectInterface = LeidenUsbManager.getInstance(context);
        } else {
            return false;
        }

        leidenIConnectInterface.setLeidenConnectResultCallBack(this);
        leidenIConnectInterface.setLeidenScanDeviceCallBack(this);

        this.connectType = connectType;

        threadExecutorManager.post(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < leidenCurrentConnectTypeListeners.size(); i++){
                    leidenCurrentConnectTypeListeners.get(i).connectTypeChange(connectType);
                }
            }
        });

        return true;
    }

    @Override
    public boolean isConnect() {
        return leidenIConnectInterface.isConnect();
    }

    @Override
    public boolean connect(LeidenDevice device) {
        return leidenIConnectInterface.connect(device);
    }

    @Override
    public boolean close() {
        return leidenIConnectInterface.close();
    }

    @Override
    public byte[] read() {
        return leidenIConnectInterface.read();
    }

    @Override
    public int write(byte[] data) {
        return leidenIConnectInterface.write(data);
    }

    @Override
    public void sendReadAsync(byte[] bytes, int time, int number, LeidenSendReadCallBack leidenSendReadCallBack) {
        leidenIConnectInterface.sendReadAsync(bytes,time,number, leidenSendReadCallBack);
    }

    @Override
    public void sendRead(byte[] bytes, int time, int number, LeidenSendReadCallBack leidenSendReadCallBack) {
        leidenIConnectInterface.sendRead(bytes,time,number, leidenSendReadCallBack);
    }

    @Override
    public boolean autoConnect() {
        return leidenIConnectInterface.autoConnect();
    }

    @Override
    public int beginSearch() {
        return leidenIConnectInterface.beginSearch();
    }

    @Override
    public void stopSearch() {
        leidenIConnectInterface.stopSearch();
    }

    @Override
    public LeidenDevice getCurrentDevice() {
        return leidenIConnectInterface.getCurrentDevice();
    }

    @Override
    public void setLeidenScanDeviceCallBack(LeidenScanDeviceCallBack leidenScanDeviceCallBack) {
        throw new RuntimeException("请使用 addScanDeviceCallBack");
    }

    @Override
    public void setLeidenConnectResultCallBack(LeidenConnectResultCallBack leidenConnectResultCallBack) {
        throw new RuntimeException("请使用 addConnectResultCallBack");
    }

    List<LeidenScanDeviceCallBack> leidenScanDeviceCallBacks = new ArrayList<>();
    List<LeidenConnectResultCallBack> leidenConnectResultCallBacks = new ArrayList<>();

    public void addScanDeviceCallBack(LeidenScanDeviceCallBack leidenScanDeviceCallBack) {
        if(leidenScanDeviceCallBacks.contains(leidenScanDeviceCallBack)){
            return;
        }
        leidenScanDeviceCallBacks.add(leidenScanDeviceCallBack);
    }

    public void removeScanDeviceCallBack(LeidenScanDeviceCallBack leidenScanDeviceCallBack) {
        leidenScanDeviceCallBacks.remove(leidenScanDeviceCallBack);
    }

    public void addConnectResultCallBack(LeidenConnectResultCallBack leidenConnectResultCallBack) {
        if(leidenConnectResultCallBacks.contains(leidenConnectResultCallBack)){
            return;
        }
        leidenConnectResultCallBacks.add(leidenConnectResultCallBack);
        if(leidenIConnectInterface.isConnect()){
            leidenConnectResultCallBack.success(getCurrentDevice());
        }
    }

    public void removeConnectResultCallBack(LeidenConnectResultCallBack leidenConnectResultCallBack) {
        leidenConnectResultCallBacks.remove(leidenConnectResultCallBack);
    }

    public int getConnectType(){
        return connectType;
    }

    @Override
    public void success(final LeidenDevice leidenDevice) {
        threadExecutorManager.post(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < leidenConnectResultCallBacks.size(); i++){
                    leidenConnectResultCallBacks.get(i).success(leidenDevice);
                }
            }
        });
    }

    @Override
    public void close(final LeidenDevice leidenDevice) {
        threadExecutorManager.post(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < leidenConnectResultCallBacks.size(); i++){
                    leidenConnectResultCallBacks.get(i).close(leidenDevice);
                }
            }
        });
    }

    @Override
    public void fail(final LeidenDevice leidenDevice) {
        threadExecutorManager.post(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < leidenConnectResultCallBacks.size(); i++){
                    leidenConnectResultCallBacks.get(i).fail(leidenDevice);
                }
            }
        });
    }

    @Override
    public void scanDevice(final LeidenDevice leidenDevice) {
        threadExecutorManager.post(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < leidenScanDeviceCallBacks.size(); i++){
                    leidenScanDeviceCallBacks.get(i).scanDevice(leidenDevice);
                }
            }
        });
    }

    @Override
    public void beginScan() {
        threadExecutorManager.post(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < leidenScanDeviceCallBacks.size(); i++){
                    leidenScanDeviceCallBacks.get(i).beginScan();
                }
            }
        });
    }

    @Override
    public void stopScan() {
        threadExecutorManager.post(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < leidenScanDeviceCallBacks.size(); i++){
                    leidenScanDeviceCallBacks.get(i).stopScan();
                }
            }
        });
    }

    List<LeidenCurrentConnectTypeListener> leidenCurrentConnectTypeListeners = new ArrayList<>();

    public void addCurrentConnectTypeListener(LeidenCurrentConnectTypeListener leidenCurrentConnectTypeListener){
        if (!leidenCurrentConnectTypeListeners.contains(leidenCurrentConnectTypeListener)) {
            leidenCurrentConnectTypeListeners.add(leidenCurrentConnectTypeListener);
            if (this.connectType != -1) {
                leidenCurrentConnectTypeListener.connectTypeChange(this.connectType);
            }
        }
    }

    public void removeCurrentConnectTypeListener(LeidenCurrentConnectTypeListener leidenCurrentConnectTypeListener){
        leidenCurrentConnectTypeListeners.remove(leidenCurrentConnectTypeListener);
    }

}
