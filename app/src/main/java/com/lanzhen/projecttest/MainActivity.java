package com.lanzhen.projecttest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.lanzhen.projecttest.databinding.ActivityMainBinding;
import com.leiden.interfaces.LeidenPrintfResultCallBack;
import com.leiden.manager.LeidenManager;
import com.leiden.model.LeidenPrinterModel;
import com.leiden.model.LeidenSmallBitmapModel;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private PrinterSocketManager printerManager;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        this.context = this;
        printerManager = new PrinterSocketManager("192.168.10.89", 9100);

        binding.button.setOnClickListener(t->{
            printerManager.disconnect();
        });

        binding.button4.setOnClickListener(t->{
            InputStream is = getResources().openRawResource(R.raw.img_print); // 将你的图片放到 res/raw 目录下
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            printerManager.printImage(bitmap);

        });

        binding.button3.setOnClickListener(t->{
             printerManager.getStatus(0,status -> runOnUiThread(() ->
             Toast.makeText(this, "打印机状态: " + status, Toast.LENGTH_SHORT).show()
            ));
        });


        binding.button5.setOnClickListener(t->{  //清空缓存
            printerManager.getStatus(1,status -> runOnUiThread(() ->
                    Toast.makeText(this, "打印机状态: " + status, Toast.LENGTH_SHORT).show()
            ));
        });

        binding.button6.setOnClickListener(t->{  //剩余打印张数
            printerManager.getStatus(2,status -> runOnUiThread(() ->
                    Toast.makeText(this, "打印机状态: " + status, Toast.LENGTH_SHORT).show()
            ));
        });

        binding.button2.setOnClickListener(t->{
            printerManager.connect(success -> {
                if (success) {
                    System.out.println("连接成功！");
                    runOnUiThread(() -> Toast.makeText(this, "连接成功！", Toast.LENGTH_SHORT).show());
//
//                    // 执行打印操作
                      // printerManager.printText(10, 50, "测试文字");



//                    printerManager.getStatus(status -> runOnUiThread(() ->
//                            Toast.makeText(this, "打印机状态: " + status, Toast.LENGTH_SHORT).show()
//                    ));
                    // 打印完后断开连接
                     // printerManager.disconnect();
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show());
                }
            });
        });
//        if (printerManager.connect()) {
//            System.out.println("连接成功！");
//
//            // 加载图片资源
//            InputStream is = getResources().openRawResource(R.raw.img_print); // 将你的图片放到 res/raw 目录下
//            Bitmap bitmap = BitmapFactory.decodeStream(is);
//
//            // 打印图片
//            printerManager.printImage(bitmap);
//
//            // 打印文字
//           // printerManager.printText(10, 50, "这是测试文字");
//
//            // 获取打印机状态
//            // 获取打印机状态
//            String status = printerManager.getStatus();
//            System.out.println("打印机状态: " + status);
//
//            // 断开连接
//            printerManager.disconnect();
//        } else {
//            System.out.println("连接失败！");
//        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        printerManager.disconnect();
    }
}