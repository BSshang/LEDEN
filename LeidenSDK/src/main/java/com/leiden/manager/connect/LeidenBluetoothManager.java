package com.leiden.manager.connect;

import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.leiden.interfaces.LeidenIConnectInterface;
import com.leiden.interfaces.LeidenConnectResultCallBack;
import com.leiden.interfaces.LeidenScanDeviceCallBack;
import com.leiden.interfaces.LeidenSendReadCallBack;
import com.leiden.model.LeidenDevice;
import com.leiden.utils.ParameterUtil;
import com.leiden.utils.SharedPreferencesUtil;
import com.leiden.utils.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class LeidenBluetoothManager implements LeidenIConnectInterface {

    //蓝牙扫描接口的广播
    private BluetoothReceiver receiver;

    private Context context;

    private LeidenBluetoothManager(Context context) {
        this.context = context.getApplicationContext();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private static LeidenBluetoothManager instance = null;

    public static LeidenBluetoothManager getInstance(Context context) {
        if (instance == null) {
            synchronized (LeidenBluetoothManager.class) {
                if (instance == null) {
                    instance = new LeidenBluetoothManager(context);
                }
            }
        }

        return instance;
    }

    private BluetoothAdapter bluetoothAdapter;
    private final UUID PRINTER_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    /**
     * 蓝牙的输入输出流
     */
    private InputStream inputStream;
    private OutputStream outputStream;
    private BluetoothSocket bluetoothSocket;

    private LeidenDevice currentDevice = null;

    /**
     * 连接的结果回调
     */
    private LeidenConnectResultCallBack leidenConnectResultCallBack = null;

    //蓝牙扫描监听
    private LeidenScanDeviceCallBack leidenScanDeviceCallBack = null;

    /**
     * 开启蓝牙适配器
     *
     * @param activity
     * @param requestCode 请求码
     */
    public void openBluetoothAdapter(Activity activity, int requestCode) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, requestCode);
        }
    }


    @Override
    public boolean isConnect() {
        return currentDevice != null;
    }


    @Override
    public boolean connect(final LeidenDevice device) {
        if (device == null || device.getBluetoothDevice() == null) {
            Log.e("TAG", "device = null");
            return false;
        }

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        threadExecutorManager.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (LeidenBluetoothManager.class) {

                    close();
                    Util.threadSleep(100);

                    BluetoothDevice bluetoothDevice = device.getBluetoothDevice();

                    try {
                        bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(PRINTER_UUID);
                        bluetoothSocket.connect();
                    } catch (Exception e) {
                        e.printStackTrace();
                        close();
                        Util.threadSleep(1500);

                        try {
                            bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(PRINTER_UUID);
                            bluetoothSocket.connect();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            noticeConnectResult(false);
                            close();
                            return;
                        }
                    }

                    try {
                        inputStream = bluetoothSocket.getInputStream();
                        outputStream = bluetoothSocket.getOutputStream();
                        currentDevice = device;
                        registerBluetoothStateChangeReceiver();

                        //到了这里，就已经连接成功了 所以，这里需要做mac保存
                        String address = device.getMac();
                        SharedPreferencesUtil.setContent("bluetooth_last_mac", address, context);

                        //通知连接结果
                        noticeConnectResult(true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        noticeConnectResult(false);
                        close();
                    }
                }
            }
        });
        return true;
    }

    @Override
    public boolean close() {
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }

            if (outputStream != null) {
                outputStream.close();
            }

            if (inputStream != null) {
                inputStream.close();
            }

            //解除广播
            unRegisterBluetoothStateChangeReceiver();

            if (currentDevice != null) {
                if (leidenConnectResultCallBack != null) {
                    leidenConnectResultCallBack.close(currentDevice);
                }
                currentDevice = null;
            }
        } catch (IOException var2) {
            var2.printStackTrace();
        }
        bluetoothSocket = null;
        outputStream = null;
        inputStream = null;
        currentDevice = null;
        return true;
    }

    @Override
    public byte[] read() {
        byte[] readBuff = null;
        int readLen = 0;
        try {
            readLen = this.inputStream.available();
            Log.e("TAG", "readLen:" + readLen);
            if (this.inputStream != null && (readLen) > 0) {
                readBuff = new byte[readLen];

                if (this.inputStream.read(readBuff) == -1) {
                    return null;
                }
                Log.e("TAG", "readBuff:" + readBuff);

            }
        } catch (IOException var3) {
            var3.printStackTrace();
        }
        return readBuff;
    }

    @Override
    public int write(byte[] data) {
        try {
            if (!isConnect()) {
                return -1;
            }
            if (outputStream != null) {
                outputStream.write(data);
                outputStream.flush();
            } else {
                return -2;
            }
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return -2;
        }
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

    @Override
    public void sendRead(byte[] bytes, int time, int number, final LeidenSendReadCallBack leidenSendReadCallBack) {
        try {
            //总的等待时间，如果超过了20秒，则归结为20秒
            if (time * number > 10 * 2000) {
                time = 500;
                number = 40;
            }
            read();
            if (ParameterUtil.isDebug) {
                StringBuilder sb = new StringBuilder();
                for (byte aByte : bytes) {
                    sb.append(aByte).append(",");
                }
                Log.e("SDK", "我要发送数据了" + sb.toString());
            }

            int write = write(bytes);
            Log.e("TAG", "write: " + write);
            if (write == 1) {
                int i = 0;
                while (i < number) {
                    Util.threadSleep(time);
                    if (isConnect()) {
                        final byte[] read = read();
                        if (read != null) {
                            if (ParameterUtil.isDebug) {
                                StringBuilder sb1 = new StringBuilder();
                                for (byte b : read) {
                                    sb1.append(b).append(",");
                                }
                                Log.e("TAG", "我得到数据了" + sb1.toString());
                            }
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
                        i++;
                    } else {
                        threadExecutorManager.post(new Runnable() {
                            @Override
                            public void run() {
                                if (leidenSendReadCallBack != null) {
                                    leidenSendReadCallBack.callError(3);
                                }
                            }
                        });
                        return;
                    }

                }
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

    /**
     * 注册蓝牙扫描广播
     */
    private void registerBluetoothReceiver() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//蓝牙开关状态
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//蓝牙开始搜索
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//蓝牙搜索结束

        filter.addAction(BluetoothDevice.ACTION_FOUND);//蓝牙发现新设备(未配对的设备)
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);//在系统弹出配对框之前(确认/输入配对码)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//设备配对状态改变
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);//最底层连接建立
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);//最底层连接断开

        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED); //BluetoothAdapter连接状态
        filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED); //BluetoothA2dp连接状态
        receiver = new BluetoothReceiver();
        context.registerReceiver(receiver, filter);
    }

    /**
     * 解除蓝牙扫描广播
     */
    private void unRegisterBluetoothReceiver() {
        if (receiver != null) {
            context.unregisterReceiver(receiver);
            receiver = null;
        }
    }


    /**
     * 停止搜索
     */
    @Override
    public void stopSearch() {
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }
        cacheDevice.clear();
        unRegisterBluetoothReceiver();
    }

    /**
     * 开始搜索
     * return
     * 1 是 开启成功
     * 2 是 蓝牙适配器未打开
     */
    @Override
    public int beginSearch() {
        if (bluetoothAdapter == null) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(enableBtIntent);
            return 2;
        }
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        //注册广播
        unRegisterBluetoothReceiver();
        registerBluetoothReceiver();
        bluetoothAdapter.startDiscovery();
        return 1;
    }

    private BluetoothStateBroadcastReceiver bluetoothStateBroadcastReceiver = null;

    /**
     * 注册蓝牙状态改变广播
     */
    private void registerBluetoothStateChangeReceiver() {
        if (bluetoothStateBroadcastReceiver == null) {
            bluetoothStateBroadcastReceiver = new BluetoothStateBroadcastReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(bluetoothStateBroadcastReceiver, filter);
    }

    /**
     * 解除蓝牙状态改变广播
     */
    private void unRegisterBluetoothStateChangeReceiver() {
        if (bluetoothStateBroadcastReceiver != null) {
            context.unregisterReceiver(bluetoothStateBroadcastReceiver);
            bluetoothStateBroadcastReceiver = null;
        }
    }

    /**
     * 通知连接结果
     */
    private void noticeConnectResult(final boolean isSuccess) {
        if (leidenConnectResultCallBack != null) {
            if (!isSuccess) {
                leidenConnectResultCallBack.fail(currentDevice);
            } else {
                Log.e("TAG", "连接成功");
                leidenConnectResultCallBack.success(currentDevice);
            }
        }
    }

    @Override
    public boolean autoConnect() {
        if (isConnect()) {
            return false;
        }

        String bluetoothLastMac = SharedPreferencesUtil.getContent("bluetooth_last_mac", context);
        if (bluetoothLastMac == null) {
            return false;
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        for (final BluetoothDevice device : bondedDevices) {
            if (device.getAddress().equals(bluetoothLastMac)) {
                threadExecutorManager.execute(new Runnable() {
                    @Override
                    public void run() {
                        LeidenDevice leidenDevice = new LeidenDevice();
                        leidenDevice.setBluetoothDevice(device);
                        connect(leidenDevice);
                    }
                });
                return true;
            }
        }
        return false;
    }

    class BluetoothStateBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) { //蓝牙连接已经断开
                close();
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {//蓝牙关闭
                    close();
                }
            }
        }
    }

    private class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            //扫描到设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                if (device.getType() != BluetoothDevice.DEVICE_TYPE_CLASSIC
                        && device.getType() != BluetoothDevice.DEVICE_TYPE_DUAL) {
                    Log.e("TAG", "不符合SPP device:" + device.getAddress() + " name:" + device.getName());
                    return;
                }
                noticeScanBlueCallBack(device);
            }
        }
    }

    //设备缓存
    Map<String, LeidenDevice> cacheDevice = new HashMap<>();

    /**
     * 执行回调，告诉所有人我扫描到了蓝牙设备
     */
    private void noticeScanBlueCallBack(BluetoothDevice device) {

        String address = device.getAddress();

        if (address == null) {
            return;
        }

        if (cacheDevice.keySet().contains(address)) {
            return;
        }

        final LeidenDevice leidenDevice = new LeidenDevice();
        leidenDevice.setBluetoothDevice(device);

        cacheDevice.put(leidenDevice.getMac(), leidenDevice);

        if (leidenScanDeviceCallBack != null) {
            leidenScanDeviceCallBack.scanDevice(leidenDevice);
        }

    }

    @Override
    public LeidenDevice getCurrentDevice() {
        return currentDevice;
    }

    @Override
    public void setLeidenConnectResultCallBack(LeidenConnectResultCallBack leidenConnectResultCallBack) {
        this.leidenConnectResultCallBack = leidenConnectResultCallBack;
    }


    @Override
    public void setLeidenScanDeviceCallBack(LeidenScanDeviceCallBack leidenScanDeviceCallBack) {
        this.leidenScanDeviceCallBack = leidenScanDeviceCallBack;
    }


}
