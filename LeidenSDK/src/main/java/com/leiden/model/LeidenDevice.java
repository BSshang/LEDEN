package com.leiden.model;

import android.bluetooth.BluetoothDevice;
import android.hardware.usb.UsbDevice;

public class LeidenDevice {

    String mac;
    String name;

    BluetoothDevice bluetoothDevice;//当蓝牙连接时，此变量不为null

    UsbDevice usbDevice;//当USB连接时 此变量不为null

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setUsbDevice(UsbDevice usbDevice) {
        this.usbDevice = usbDevice;
        mac = String.valueOf(usbDevice.getProductId());
        name = usbDevice.getProductName();
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public UsbDevice getUsbDevice() {
        return usbDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
        mac = bluetoothDevice.getAddress();
        name = bluetoothDevice.getName();
        if(name == null){
            name = "";
        }
    }
}
