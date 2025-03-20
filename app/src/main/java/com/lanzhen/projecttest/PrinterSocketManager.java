package com.lanzhen.projecttest;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrinterSocketManager {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private String ipAddress;
    private int port;
    private int LabW = 80;  //标签的宽度mm
    private int LabH = 40;  //标签的高度mm
    private int printerAccuracy  = 8;  //精度 200dip（8）与300 dip（11.8F;）
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public PrinterSocketManager(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public void connect(OnConnectionListener listener) {
        executorService.execute(() -> {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(ipAddress, port), 5000); // 5秒超时时间
                writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                if (listener != null) listener.onConnected(true);
            } catch (Exception e) {
                e.printStackTrace();
                if (listener != null) listener.onConnected(false);
            }
        });
    }

    public void disconnect() {
        executorService.execute(() -> {
            try {
                if (writer != null) writer.close();
                if (reader != null) reader.close();
                if (socket != null) socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void writeBytes(byte[] data) {
        try {
            if (socket != null && socket.getOutputStream() != null) {
                socket.getOutputStream().write(data);
                socket.getOutputStream().flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendReadAsync(byte[] bytes, int time, int number, OnStatusListener listener) {
        executorService.execute(() -> {
            try {
                writeBytes(bytes);  // 发送指令
                int retries = 0;
                StringBuilder responseBuilder = new StringBuilder();

                while (retries < number) {
                    if (socket != null && socket.getInputStream().available() > 0) {
                        byte[] buffer = new byte[1024];
                        int bytesRead = socket.getInputStream().read(buffer);
                        if (bytesRead > 0) {
                            responseBuilder.append(new String(buffer, 0, bytesRead));
                            break;
                        }
                    }
                    retries++;
                    Thread.sleep(time);
                }

                String response = responseBuilder.toString().trim();
                if (listener != null) {
                    if (response.isEmpty()) {
                        listener.onStatusReceived("无法获取打印机状态。");
                    } else {
                        listener.onStatusReceived(response);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (listener != null) listener.onStatusReceived("状态读取失败。");
            }
        });
    }

    public void getStatus(int code, OnStatusListener listener) {
        executorService.execute(() -> {
            try {
                byte[] escSCommand = new byte[0];
                // 1. 发送 SESC TP=1 指令 (初始化控制字模式)
                String sescCommand = "SESC TP=1\r\n";
                sendCommand(sescCommand);
                Thread.sleep(500);  // 等待打印机响应

                // 2. 尝试读取 SESC 指令的响应
                byte[] buffer = new byte[1024];
                if (socket != null && socket.getInputStream().available() > 0) {
                    int bytesRead = socket.getInputStream().read(buffer);
                    String response = new String(buffer, 0, bytesRead).trim();

                    if (!response.contains("OK")) {
                        listener.onStatusReceived("SESC TP=1 指令未成功响应。");
                        Log.w("TAG", "onCreate:SESC TP=1 指令未成功响应");
                        return;
                    }
                }
                if (code == 0) {
                    escSCommand = stringToBytes("ESCS");  //获取状态
                } else if (code == 1) {
                    escSCommand = stringToBytes("ESCC");  //清除缓存
                } else if (code == 2) {
                    escSCommand = stringToBytes("ESCG");  //获取剩余打印张数
                }
                // 3. 发送 ESC S 指令进行状态读取
                // byte[] escS = new byte[]{27, 83};
                // byte[] escSCommand = stringToBytes("ESCS");  //获取状态
                //byte[] escSCommand2 = stringToBytes("ESCC");  //清除缓存
                sendReadAsync(escSCommand, 500, 20, listener);  // 尝试读取20次，每次等待500ms
            } catch (Exception e) {
                e.printStackTrace();
                Log.w("TAG", "状态读取失败");
                if (listener != null) listener.onStatusReceived("状态读取失败。");
            }
        });
    }

    public void sendCommand(String command) {
        executorService.execute(() -> {
            if (writer != null) {
                writer.print(command);
                writer.flush();
            }
        });
    }

    public void printText(int x, int y, String text) {
        String command = String.format("TEXT X=%d,Y=%d", x, y);
        sendCommand(command + "\r\n" + text);
    }

    public void printImage(Bitmap bitmap) {
        String imageData = generatePrintData(bitmap);  //对图片数据进行处理
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append("JOB\r\n");
        commandBuilder.append(String.format("DEF PW=%d,PH=%d,SP=2,DK=10,TR=2,MK=2,MO=0,MD=5,PM=1,PO=0,CP=0,CO=0,TO=0,PG=16\r\n", LabW * printerAccuracy,  LabH * printerAccuracy));
        commandBuilder.append("START\r\n");
        commandBuilder.append("RFID L=1,MD=1,BX=0,LEN=5,BA=1,APS=00000000\r\n");
        commandBuilder.append("LZ20250314003\r\n");
        commandBuilder.append(String.format("GRAPH X=0,Y=0,WD=%d,HT=%d,MD=1\r\n", LabW * printerAccuracy,  LabH * printerAccuracy));
        commandBuilder.append(imageData).append("\r\n");
        commandBuilder.append("QTY P=1\r\n");
        commandBuilder.append("END\r\n");
        commandBuilder.append("JOBE\r\n");

        sendCommand(commandBuilder.toString());
        Log.w("TAG", "printImage:" + commandBuilder);
    }

    public Bitmap createBitmap(String text) {
        // 创建指定宽高的 Bitmap
        int width = 640;
        int height = 320;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        // 绘制文字
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(40);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);

        Rect textBounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), textBounds);

        int x = width / 2;
        float y = (height / 2) - ((paint.descent() + paint.ascent()) / 2);
        canvas.drawText(text, x, y, paint);

        return bitmap;
    }


    /**
     *
     *  确保 Bitmap  图片长宽与标签长宽一致 或小于标签长宽，不然打印出错
     * @param bitmap
     * @return
     */
    public String generatePrintData(Bitmap bitmap) {
        // 确保 Bitmap 是 640x320
        if (bitmap.getWidth() !=  LabW * printerAccuracy || bitmap.getHeight() !=  LabH * printerAccuracy) {
            bitmap = Bitmap.createScaledBitmap(bitmap,  LabW * printerAccuracy,  LabH * printerAccuracy, true);
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        StringBuilder hexData = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x += 8) {
                int byteValue = 0;
                for (int bit = 0; bit < 8; bit++) {
                    if (x + bit < width) {
                        int pixel = bitmap.getPixel(x + bit, y);
                        int r = Color.red(pixel);
                        int g = Color.green(pixel);
                        int b = Color.blue(pixel);
                        int gray = (r + g + b) / 3;
                        if (gray < 128) {
                            byteValue |= (1 << (7 - bit));
                        }
                    }
                }
                hexData.append(String.format("%02X", byteValue));
            }
        }

        return hexData.toString() ;
    }


    public static byte[] stringToBytes(String command) {
        // 将字符串中的控制字符名转换为对应的 ASCII 值
        command = command.replace("ESC", String.valueOf((char) 27));  // 替换为 ASCII 27
        return command.getBytes();
    }


    public interface OnConnectionListener {
        void onConnected(boolean success);
    }

    public interface OnStatusListener {
        void onStatusReceived(String status);
    }
}
