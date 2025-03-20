package com.lanzhen.projecttest;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.lanzhen.projecttest.databinding.ActivityLeiddanBinding;
import com.lanzhen.projecttest.ConnectDeviceActivity;
import com.leiden.interfaces.LeidenPrintfResultCallBack;
import com.leiden.manager.LeidenManager;
import com.leiden.manager.connect.LeidenDeviceManager;
import com.leiden.model.LeidenPrinterModel;
import com.leiden.model.LeidenSmallBitmapModel;

public class LeiddanActivity extends AppCompatActivity {

    Button btn_printf_label,btn_connect_device;

    LeidenDeviceManager leidenDeviceManager;

    Context context;

    private ActivityLeiddanBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLeiddanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        this.context = this;

        btn_printf_label = findViewById(R.id.btn_printf_label);

        btn_connect_device = findViewById(R.id.btn_connect_device);

        leidenDeviceManager = LeidenDeviceManager.getInstance(this);



        /**
         * 如果需要打印机环境 可
         * LeidenManager.getInstance(this).getLeidenEnvPrinterModel() 获得 环境对象 进行修改
         */

        btn_connect_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leidenDeviceManager.close();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(context,ConnectDeviceActivity.class);
                        context.startActivity(intent);
                    }
                },1000);
            }
        });


        /**
         * 打印标签本质是打印图片
         */
        btn_printf_label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LeidenPrinterModel printerModel = new LeidenPrinterModel();

                // 200精度的打印机 乘以 8  300 精度的打印机 乘以 11.8
                printerModel.setLabelH(50 * 8);
                printerModel.setLabelW(50 * 8);
                printerModel.setNumber(1);

                LeidenSmallBitmapModel leidenSmallBitmapModel = new LeidenSmallBitmapModel();
                leidenSmallBitmapModel.setBitmap(
                        BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher));
                leidenSmallBitmapModel.setX(0);
                leidenSmallBitmapModel.setY(0);
                // 200精度的打印机 乘以 8  300 精度的打印机 乘以 11.8
                leidenSmallBitmapModel.setHT(30 * 8);
                leidenSmallBitmapModel.setWD(30 * 8);

                printerModel.addAPLSmallBitmapModel(leidenSmallBitmapModel);

                LeidenManager.getInstance(context).printfAPLLabel(printerModel, new LeidenPrintfResultCallBack() {
                    @Override
                    public void callBack(int result) {
                        if (result == LEIDEN_PRINTF_RESULT_SUCCESS) {
                            Toast.makeText(context, "success", Toast.LENGTH_SHORT).show();
                        } else if (result == LEIDEN_PRINTF_RESULT_CMD_ERROR) {
                            Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
                        } else if (result == LEIDEN_PRINTF_RESULT_BLUETOOTH) {
                            Toast.makeText(context, "no connect", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void progress(int total, int progress) {

                    }
                });
            }
        });

    }

}