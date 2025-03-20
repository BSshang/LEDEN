package com.lanzhen.projecttest;

import static com.leiden.manager.connect.LeidenDeviceManager.BLUE_TYPE;
import static com.leiden.manager.connect.LeidenDeviceManager.USB_TYPE;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.leiden.interfaces.LeidenConnectResultCallBack;
import com.leiden.interfaces.LeidenCurrentConnectTypeListener;
import com.leiden.interfaces.LeidenScanDeviceCallBack;
import com.leiden.manager.connect.LeidenDeviceManager;
import com.leiden.manager.connect.LeidenUsbManager;
import com.leiden.model.LeidenDevice;

import java.util.ArrayList;
import java.util.List;

public class ConnectDeviceActivity extends Activity {

    Button btn_connect_type;

    RecyclerView rv_content;

    LeidenDeviceManager leidenDeviceManager;

    MyAdapter myAdapter;

    List<LeidenDevice> leidenDevices = new ArrayList<>();

    LeidenCurrentConnectTypeListener leidenCurrentConnectTypeListener = new LeidenCurrentConnectTypeListener() {
        @Override
        public void connectTypeChange(int type) {
            if(myAdapter != null) {
                leidenDevices.clear();
                myAdapter.notifyDataSetChanged();
            }
            if(type == BLUE_TYPE){
                btn_connect_type.setText("转化方式 -- 当前为蓝牙");
            } else {
                btn_connect_type.setText("转化方式 -- 当前为USB");
            }
            leidenDeviceManager.beginSearch();
        }
    };

    LeidenScanDeviceCallBack scanDeviceCallBack = new LeidenScanDeviceCallBack() {
        @Override
        public void scanDevice(LeidenDevice leidenDevice) {
            leidenDevices.add(leidenDevice);
            myAdapter.notifyDataSetChanged();
        }

        @Override
        public void beginScan() {
            leidenDevices.clear();
            myAdapter.notifyDataSetChanged();
        }

        @Override
        public void stopScan() {

        }
    };

    LeidenConnectResultCallBack leidenConnectResultCallBack = new LeidenConnectResultCallBack() {
        @Override
        public void success(LeidenDevice leidenDevice) {
            Toast.makeText(ConnectDeviceActivity.this,"success",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void close(LeidenDevice leidenDevice) {

        }

        @Override
        public void fail(LeidenDevice leidenDevice) {
            Toast.makeText(ConnectDeviceActivity.this,"fail",Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    protected void onDestroy() {
        leidenDeviceManager.removeScanDeviceCallBack(scanDeviceCallBack);
        leidenDeviceManager.removeConnectResultCallBack(leidenConnectResultCallBack);
        leidenDeviceManager.removeCurrentConnectTypeListener(leidenCurrentConnectTypeListener);
        super.onDestroy();

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_device);

        btn_connect_type = findViewById(R.id.btn_connect_type);
        leidenDeviceManager = LeidenDeviceManager.getInstance(this);

        leidenDeviceManager.addCurrentConnectTypeListener(leidenCurrentConnectTypeListener);

        leidenDeviceManager.addScanDeviceCallBack(scanDeviceCallBack);

        leidenDeviceManager.addConnectResultCallBack(leidenConnectResultCallBack);


        rv_content = findViewById(R.id.rv_content);

        leidenDevices = new ArrayList<>();

        myAdapter = new MyAdapter(this,leidenDevices);

        rv_content.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        rv_content.setAdapter(myAdapter);
        rv_content.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.HORIZONTAL));

        btn_connect_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int connectType = leidenDeviceManager.getConnectType();
                if(connectType == BLUE_TYPE){
                    leidenDeviceManager.changeConnect(USB_TYPE);
                } else {
                    leidenDeviceManager.changeConnect(BLUE_TYPE);
                }
            }
        });

        myAdapter.setItemClickListener(new MyAdapter.ItemClickListener() {
            @Override
            public void click(int position) {
                LeidenDevice leidenDevice = leidenDevices.get(position);
                if(leidenDeviceManager.getConnectType() == 2){//如果是USB 需要先判断是否有权限
                    LeidenUsbManager myUsbManager = LeidenUsbManager.getInstance(ConnectDeviceActivity.this);
                    boolean b = myUsbManager.hasPermission(leidenDevice);
                    if(!b){
                        myUsbManager.requestPermission(leidenDevice);
                        return;
                    }
                }

                leidenDeviceManager.connect(leidenDevice);
            }
        });


    }
}
