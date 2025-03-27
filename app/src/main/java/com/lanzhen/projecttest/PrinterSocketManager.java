package com.lanzhen.projecttest;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
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
    private int LabW = 75;  //标签的宽度mm
    private int LabH = 50;  //标签的高度mm
    private int printerAccuracy = 8;  //精度 200dip（8）与300 dip（11.8F;）
    private ExecutorService executorService = Executors.newFixedThreadPool(3);
    public PrinterSocketManager(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public void connect(OnConnectionListener listener) {
        executorService.execute(() -> {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(ipAddress, port), 10000); // 5秒超时时间
                socket.setTcpNoDelay(true);
                socket.setKeepAlive(true);
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
                if (writer != null) {
                    writer.close();
                    writer = null;
                }
                if (reader != null) {
                    reader.close();
                    reader = null;
                }
                if (socket != null) {
                    if (!socket.isClosed()) {
                        socket.close();
                    }
                    socket = null;
                }
                Log.w("TAG", "disconnect: 连接已断开");
            } catch (IOException e) {
                Log.e("TAG", "disconnect: 关闭连接时发生错误 " + e.getMessage());
            }
        });
    }



    public void writeBytes(byte[] data) {
        executorService.execute(() -> {
            int maxRetries = 3; // **最多重试 3 次**
            int attempt = 0;

            while (attempt < maxRetries) {
                try {
                    // **检查 Socket 是否断开**
                    if (socket == null || socket.isClosed() || !socket.isConnected()) {
                        Log.w("TAG", "writeBytes: 连接已断开，尝试重新连接...");
                        reconnect();
                        Thread.sleep(500);  // **等待连接建立**
                    }

                    if (socket != null && socket.isConnected() && !socket.isClosed() && socket.getOutputStream() != null) {
                        int bufferSize = 1024;
                        int offset = 0;

                        while (offset < data.length) {
                            int length = Math.min(bufferSize, data.length - offset);
                            socket.getOutputStream().write(data, offset, length);
                            socket.getOutputStream().flush();
                            offset += length;

                            // **适当延迟，避免发送过快**
                            Thread.sleep(10);
                        }
                        return; // **发送成功，退出循环**
                    }
                } catch (IOException e) {
                    Log.e("TAG", "writeBytes: 发送数据时出错，尝试重试 " + (attempt + 1) + "/" + maxRetries);
                    e.printStackTrace();
                    attempt++; // **增加重试次数**

                    if (attempt < maxRetries) {
                        Log.w("TAG", "writeBytes: 等待 1 秒后重试...");
                        try {
                            Thread.sleep(1000);  // **等待 1 秒后重试**
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;  // **中断异常，直接退出**
                }
            }
            Log.e("TAG", "writeBytes: 多次尝试失败，数据发送终止");
        });
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

                if (code == 0) {
                    escSCommand = new byte[]{27, 'S'}; //发送 <ESC>S //获取状态
                } else if (code == 1) {
                    escSCommand = new byte[]{27, 'C'}; //发送 <ESC>C //清除缓存
                } else if (code == 2) {
                    escSCommand = new byte[]{27, 'G'};  //发送 <ESC>G//获取剩余打印张数
                }
                // 3. 发送 ESC S 指令进行状态读取
                // byte[] escS = new byte[]{27, 83};
                // byte[] escSCommand = stringToBytes("ESCS");  //获取状态
                //byte[] escSCommand2 = stringToBytes("ESCC");  //清除缓存
                sendReadAsync(escSCommand, 500, 10, listener);  // 尝试读取20次，每次等待500ms
            } catch (Exception e) {
                e.printStackTrace();
                Log.w("TAG", "状态读取失败");
                if (listener != null) listener.onStatusReceived("状态读取失败。");
            }
        });
    }

    /**
     * 恢复打印重启
     */
    public void sendReset() {

        sendCommand("\u001B" + "R");  // 继续打印
        sendCommand("RESET\r\n");  // 强制复位
    }

    /**
     * 清除打印缓存
     */
    public void sendClearData() {

        String sescCommand = "SESC TP=1\r\n";
        sendCommand(sescCommand);
        try {
            Thread.sleep(500);  // 等待打印机响应
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        sendCommand("\u001B" + "C");
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



    public void sendCommandWithoutResponse() {
        try {
            sendCommand("JOBE\r\n");  //结束上次打印
            Thread.sleep(100);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     *
     * 通过APL 指令打印
     * @param text
     */
    public void printTextLabel(String text) {
        executorService.execute(() -> {
            if (socket == null || !socket.isConnected()) {
                Log.w("TAG", "printImage: 通信未建立 ");
                return;
            }

            synchronized (this) {
                StringBuilder commandBuilder = new StringBuilder();
                commandBuilder.append("JOB\r\n");
                commandBuilder.append(String.format("DEF PW=%d,PH=%d,SP=2,DK=10,TR=2,MK=2,MO=0,MD=5,PM=1,PO=0,CP=0,CO=0,TO=0,PG=16\r\n",
                        LabW * printerAccuracy, LabH * printerAccuracy));
                commandBuilder.append("START\r\n");
 // RFID     // commandBuilder.append("RFID L=1,MD=1,BX=0,LEN=5,BA=1,APS=00000000\r\n");
                //commandBuilder.append("LZ20250314003\r\n");
// 绘制外边框
                commandBuilder.append("RECT SX=10,SY=20,EX=590,EY=390,WD=2\r\n");

// 第一行分割线
                commandBuilder.append("RECT SX=10,SY=90,EX=590,EY=90,WD=2\r\n");

// 第二行（Encoding 区域）
                commandBuilder.append("RECT SX=10,SY=170,EX=590,EY=170,WD=2\r\n");
                commandBuilder.append("RECT SX=400,SY=90,EX=400,EY=170,WD=2\r\n");//竖线

// 第三行（Quantity 区域）
                commandBuilder.append("RECT SX=400,SY=170,EX=400,EY=250,WD=2\r\n");  //竖线
                commandBuilder.append("RECT SX=200,SY=170,EX=200,EY=250,WD=2\r\n"); //竖线

// 第四行（Transfer number）
                commandBuilder.append("RECT SX=10,SY=250,EX=400,EY=250,WD=2\r\n");
                commandBuilder.append("RECT SX=400,SY=250,EX=400,EY=320,WD=2\r\n"); //竖线

// 第五行（日期区域）
                commandBuilder.append("RECT SX=10,SY=320,EX=590,EY=320,WD=2\r\n");

// 设置字体
                commandBuilder.append("FONT TP=107,WD=20,HT=20,LG=25\r\n");
// Order number
                commandBuilder.append(String.format("TEXT X=%d,Y=%d,L=1\r\n%s\r\n", 20, 40, "Order number"));

// Encoding
                commandBuilder.append(String.format("TEXT X=%d,Y=%d,L=1\r\n%s\r\n", 20, 120, "Encoding"));

// Quantity
                commandBuilder.append(String.format("TEXT X=%d,Y=%d,L=1\r\n%s\r\n", 20, 180, "Quantity"));
                commandBuilder.append(String.format("TEXT X=%d,Y=%d,L=1\r\n%s\r\n", 220, 180, "Torus"));

// Transfer number
                commandBuilder.append(String.format("TEXT X=%d,Y=%d,L=1\r\n%s\r\n", 20, 270, "Transfer number"));

// Date of documentation
                commandBuilder.append(String.format("TEXT X=%d,Y=%d,L=1\r\n%s\r\n", 20, 340, "Dat of documentation"));

 // 设置数值字体
                commandBuilder.append("FONT TP=103,WD=20,HT=20,LG=25\r\n");
                commandBuilder.append(String.format("TEXT X=%d,Y=%d,L=1\r\n%s\r\n", 450, 40, "2024PT42725"));
// Encoding
                commandBuilder.append(String.format("TEXT X=%d,Y=%d,L=1\r\n%s\r\n", 160, 105, "01.07.01.TKD2ORGW9"));
                commandBuilder.append(String.format("TEXT X=%d,Y=%d,L=1\r\n%s\r\n", 160, 130, "32XL-1ER0553715-15"));
                commandBuilder.append(String.format("TEXT X=%d,Y=%d,L=1\r\n%s\r\n", 470, 120, "WC711"));

// Quantity
                commandBuilder.append(String.format("TEXT X=%d,Y=%d,L=1\r\n%s\r\n", 170, 210, "20"));
                commandBuilder.append(String.format("TEXT X=%d,Y=%d,L=1\r\n%s\r\n", 350, 210, "7/16"));

// Transfer number
                commandBuilder.append(String.format("TEXT X=%d,Y=%d,L=1\r\n%s\r\n", 250, 270, "LZ20250225010"));

// Date of documentation
                commandBuilder.append(String.format("TEXT X=%d,Y=%d,L=1\r\n%s\r\n", 380, 340, "2025-02-25 10:00:00"));

// 二维码
                commandBuilder.append(String.format("QR X=%d,Y=%d,DR=1,EL=1,CV=9,CS=6\r\n%s\r\n", 440, 180, "LZ20250225010"));

                sendCommandSync(commandBuilder.toString());
                sendCommandSync("QTY P=1\r\nEND\r\nJOBE\r\n");
                disconnect();
            }
        });
    }



    public void printText(int x, int y, String text) {
        String command = String.format("TEXT X=%d,Y=%d\r\n%s\r\n", x, y, text);
        sendCommand(command);

    }

    /**
     *
     * 打印 图片
     * @param bitmap
     */
    public void printImage(Bitmap bitmap) {
        executorService.execute(() -> {
            if (socket == null || !socket.isConnected()) {
                Log.w("TAG", "printImage: 通信未建立 ");
                return;
            }

            synchronized (this) { // 确保 sendCommandSync() 先执行完，再执行 sendImageDataSync()
                String imageData = generatePrintData(bitmap);
                StringBuilder commandBuilder = new StringBuilder();
                commandBuilder.append("JOB\r\n");
                commandBuilder.append(String.format("DEF PW=%d,PH=%d,SP=2,DK=10,TR=2,MK=2,MO=0,MD=5,PM=1,PO=0,CP=0,CO=0,TO=0,PG=16\r\n",
                        LabW * printerAccuracy, LabH * printerAccuracy));
                commandBuilder.append("START\r\n");


                commandBuilder.append(String.format("GRAPH X=0,Y=0,WD=%d,HT=%d,MD=1\r\n",
                        LabW * printerAccuracy, LabH * printerAccuracy));

                sendCommandSync(commandBuilder.toString());

                boolean success = sendImageDataSync(imageData);
                if (success) {
                    try {
                        Thread.sleep(100);  // **确保数据完全到达**
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    sendCommandSync("QTY P=1\r\nEND\r\nJOBE\r\n");
                    HandlerUtil.getInstance().postDelay(()->{
                        disconnect();
                    },3000);

                } else {
                    Log.e("TAG", "图片数据发送失败，未执行 JOBE！");
                }
            }
        });
    }

    public void sendCommand(String command) {
        executorService.execute(() -> {
            try {
                if (socket != null && socket.isConnected() && !socket.isClosed() && socket.getOutputStream() != null) {
                    try {
                        byte[] commandBytes = command.getBytes("UTF-8");
                        int chunkSize = 1024;  // 每次发送的字节大小
                        int offset = 0;
                        while (offset < commandBytes.length) {
                            int length = Math.min(chunkSize, commandBytes.length - offset);
                            socket.getOutputStream().write(commandBytes, offset, length);
                            socket.getOutputStream().flush();
                            offset += length;
                            System.out.printf("发送进度：%.2f%%\n", (offset / (double) commandBytes.length) * 100);

                        }
                    } catch (Exception e) {
                        System.err.println("发送图片数据时出错2：" + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                System.err.println("发送图片数据时出错：" + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     *
     *
     * flush() 过于频繁：每次写入 1024 字节就调用 flush()，会导致打印机过早解析部分数据，可能导致指令解析错误。
     *
     * 可能导致数据错乱：如果 flush() 过早，可能使打印机接收到不完整的指令，导致不执行打印任务。
     * 延迟 flush()，只在数据完全写入后再 flush()
     * @param command
     */
    private void sendCommandSync(String command) {
        try {
            System.out.printf("发生的数据 sendCommand: " + command.toString().substring(0, Math.min(command.length(), 100)) + "...");
            if (socket != null && socket.isConnected() && !socket.isClosed() && socket.getOutputStream() != null) {
                byte[] commandBytes = command.getBytes("UTF-8");
                int bufferSize = 1024;
                int offset = 0;

                while (offset < commandBytes.length) {
                    int length = Math.min(bufferSize, commandBytes.length - offset);
                    socket.getOutputStream().write(commandBytes, offset, length);
                    offset += length;
                }
                socket.getOutputStream().flush();  // **仅在数据全部写入后 flush()**
            }
        } catch (IOException e) {
            Log.e("PrinterSocketManager", "发送指令时出错：" + e.getMessage());
        }
    }



    private boolean sendImageDataSync(String imageData) {
        try {
            if (socket != null && socket.isConnected() && !socket.isClosed() && socket.getOutputStream() != null) {
                int bufferSize = 1024;
                int offset = 0;

                while (offset < imageData.length()) {
                    int length = Math.min(bufferSize, imageData.length() - offset);
                    byte[] chunk = imageData.substring(offset, offset + length).getBytes("UTF-8");
                    socket.getOutputStream().write(chunk);
                    offset += length;
                }

                socket.getOutputStream().flush(); // **确保所有数据写入流**

                // **确保数据完全送达**
                Thread.sleep(100);

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }







    /**
     * 确保 Bitmap  图片长宽与标签长宽一致 或小于标签长宽，不然打印出错
     *
     * @param bitmap
     * @return
     */
    public String generatePrintData(Bitmap bitmap) {
        if (bitmap.getWidth() != LabW * printerAccuracy || bitmap.getHeight() != LabH * printerAccuracy) {
            bitmap = Bitmap.createScaledBitmap(bitmap, LabW * printerAccuracy, LabH * printerAccuracy, false);
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
            hexData.append("\r\n");  // 添加换行符，每行结束后
        }
        Log.d("TAG", "Bitmap Width: " + width + ", Height: " + height);
        return hexData.toString();
    }

    public static byte[] stringToBytes(String command) {
        // 将字符串中的控制字符名转换为对应的 ASCII 值
        command = command.replace("ESC", String.valueOf((char) 27));  // 替换为 ASCII 27
        return command.getBytes();
    }


    private void reconnect() {
        disconnect();  // 先断开旧连接
        connect(success -> {
            if (success) {
                Log.d("PrinterSocketManager", "重新连接成功！");
            } else {
                Log.e("PrinterSocketManager", "重新连接失败！");
            }
        });
    }

    public interface OnConnectionListener {
        void onConnected(boolean success);
    }

    public interface OnStatusListener {
        void onStatusReceived(String status);
    }

    public interface OnPrintStatusListener {
        void onPrintCompleted(boolean success, String message);
    }
}
