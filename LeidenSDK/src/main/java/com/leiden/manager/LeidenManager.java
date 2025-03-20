package com.leiden.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.leiden.interfaces.LeidenPrintfResultCallBack;
import com.leiden.manager.connect.LeidenDeviceManager;
import com.leiden.model.LeidenEnvPrinterModel;
import com.leiden.model.LeidenPrinterModel;
import com.leiden.model.LeidenSmallBitmapModel;
import com.leiden.utils.BinaryConversionUtil;
import com.leiden.utils.ImageUtil;
import com.leiden.utils.ParameterUtil;
import com.leiden.utils.SharedPreferencesUtil;
import com.leiden.utils.Util;

import java.util.List;

public class LeidenManager {

    private Context context;
    private LeidenDeviceManager deviceManager;

    private LeidenEnvPrinterModel leidenEnvPrinterModel = new LeidenEnvPrinterModel();

    public LeidenEnvPrinterModel getLeidenEnvPrinterModel() {
        return leidenEnvPrinterModel;
    }

    static class PrintfAPLManagerHolder {
        private static LeidenManager instance = new LeidenManager();
    }

    public static LeidenManager getInstance(final Context context) {
        if (LeidenManager.PrintfAPLManagerHolder.instance.context == null) {
            LeidenManager.PrintfAPLManagerHolder.instance.context
                    = context.getApplicationContext();
            LeidenManager.PrintfAPLManagerHolder.instance.deviceManager = LeidenDeviceManager.getInstance(context);
        }
        return LeidenManager.PrintfAPLManagerHolder.instance;
    }


    /**
     * 设置当前的环境变量
     */
    public int setCurrentEnviron(LeidenEnvPrinterModel leidenEnvPrinterModel) {
        if (!deviceManager.isConnect()) {
            return 1;
        }
        if (leidenEnvPrinterModel != null) {
            if (leidenEnvPrinterModel.equals(this.leidenEnvPrinterModel)) {
                return 2;
            }
        } else {
            leidenEnvPrinterModel = this.leidenEnvPrinterModel;
        }
        final int printerAccuracy = leidenEnvPrinterModel.getPrinterAccuracy();


            String appCMD = leidenEnvPrinterModel.getAppCMD();
            SharedPreferencesUtil.setContent("currentEnviron",appCMD, context);
            getCurrentEnviron();

            if (printerAccuracy != LeidenManager.this.leidenEnvPrinterModel.getPrinterAccuracy()) {
                LeidenManager.this.leidenEnvPrinterModel.setPrinterAccuracy(printerAccuracy);
            }

        return 0;
    }

    public void getCurrentEnviron() {
        getCurrentEnviron(null);
    }

    /**
     * 取得当前环境
     */
    public void getCurrentEnviron(final GetCurrentEnvironCallBack getCurrentEnvironCallBack) {
        String currentEnviron = SharedPreferencesUtil.getContent("currentEnviron", context);
        if(currentEnviron == null){
            currentEnviron = leidenEnvPrinterModel.getAppCMD();
            SharedPreferencesUtil.setContent("currentEnviron",currentEnviron,context);
        }
        leidenEnvPrinterModel.analysis(currentEnviron);
        if (getCurrentEnvironCallBack != null) {
            getCurrentEnvironCallBack.callBack(currentEnviron);
        }
    }


    public interface GetCurrentEnvironCallBack {
        void callBack(String data);

        void error(String error);
    }


    /**
     * 得到打印机的环境设置
     * DEF DK=8,MD=1,PW=384,PH=344
     */
    public String getPrinterEnvironmentSetCMD(LeidenPrinterModel leidenPrinterModel) {

        int labelH = leidenPrinterModel.getLabelH();
        int labelW = leidenPrinterModel.getLabelW();

        StringBuilder sb = new StringBuilder();
        sb.append("DEF ")
                .append("PW=")
                .append(labelW)
                .append(",PH=").append(labelH);
        String appCMD = this.leidenEnvPrinterModel.getAppCMD();
        sb.append(",").append(appCMD).append("\n");

        return sb.toString();
    }


    public void printfAPLLabels(final List<LeidenPrinterModel> leidenPrinterModels, final LeidenPrintfResultCallBack leidenPrintfResultCallBack) {
        final ThreadExecutorManager threadExecutorManager = ThreadExecutorManager.getInstance();

        boolean connect = deviceManager.isConnect();
        if (!connect) {
            if (leidenPrintfResultCallBack != null) {
                threadExecutorManager.post(new Runnable() {
                    @Override
                    public void run() {
                        leidenPrintfResultCallBack.callBack(LeidenPrintfResultCallBack.LEIDEN_PRINTF_RESULT_BLUETOOTH);
                    }
                });
            }
            return;
        }
        threadExecutorManager.execute(new Runnable() {
            @Override
            public void run() {

                final int result = printfLabel(leidenPrinterModels, leidenPrintfResultCallBack);
                if (result != 1) {
                    if (leidenPrintfResultCallBack != null) {
                        threadExecutorManager.post(new Runnable() {
                            @Override
                            public void run() {
                                leidenPrintfResultCallBack.callBack(LeidenPrintfResultCallBack.LEIDEN_PRINTF_RESULT_CMD_ERROR);
                            }
                        });
                    }
                }

                threadExecutorManager.post(new Runnable() {
                    @Override
                    public void run() {
                        leidenPrintfResultCallBack.callBack(LeidenPrintfResultCallBack.LEIDEN_PRINTF_RESULT_SUCCESS);
                    }
                });


            }
        });
    }

    public void printfAPLLabel(final LeidenPrinterModel leidenPrinterModel, final LeidenPrintfResultCallBack leidenPrintfResultCallBack) {

        final ThreadExecutorManager threadExecutorManager = ThreadExecutorManager.getInstance();
        threadExecutorManager.execute(new Runnable() {
            @Override
            public void run() {
                boolean connect = deviceManager.isConnect();
                if (!connect) {
                    if (leidenPrintfResultCallBack != null) {
                        threadExecutorManager.post(new Runnable() {
                            @Override
                            public void run() {
                                leidenPrintfResultCallBack.callBack(LeidenPrintfResultCallBack.LEIDEN_PRINTF_RESULT_BLUETOOTH);
                            }
                        });
                    }
                    return;
                }

                int result = printfLabel(leidenPrinterModel, leidenPrintfResultCallBack);

                /**
                 * -1:数据发送失败 蓝牙未连接
                 * 1:数据发送成功
                 * -2:数据发送失败 抛出异常 失败
                 */
                if (result == 1) {
                    if (leidenPrintfResultCallBack != null) {
                        threadExecutorManager.post(new Runnable() {
                            @Override
                            public void run() {
                                leidenPrintfResultCallBack.callBack(LeidenPrintfResultCallBack.LEIDEN_PRINTF_RESULT_SUCCESS);
                            }
                        });
                    }
                } else {
                    if (leidenPrintfResultCallBack != null) {
                        threadExecutorManager.post(new Runnable() {
                            @Override
                            public void run() {
                                leidenPrintfResultCallBack.callBack(LeidenPrintfResultCallBack.LEIDEN_PRINTF_RESULT_CMD_ERROR);
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * 在一次任务中打印多张 每个model的环境必须是一致的 宽高与打印方向
     *
     * @param leidenPrinterModels
     * @return
     */
    private int printfLabel(List<LeidenPrinterModel> leidenPrinterModels, LeidenPrintfResultCallBack leidenPrintfResultCallBack) {
        return realPrintfLabel(leidenPrinterModels, leidenPrintfResultCallBack);
    }

    private int realPrintfLabel(List<LeidenPrinterModel> leidenPrinterModels, LeidenPrintfResultCallBack leidenPrintfResultCallBack) {
        LeidenPrinterModel tempLeidenPrinterModel = leidenPrinterModels.get(0);

        StringBuilder textSb = new StringBuilder();
        textSb.append("JOB\n");
        textSb.append(getPrinterEnvironmentSetCMD(tempLeidenPrinterModel));

        //如果是USB打印 需要设置 流控模式为软件流控
        if(LeidenDeviceManager.getInstance(context).getConnectType() == 2){
            textSb.append("SCOM F=1\n");
        }

        for (int j = 0; j < leidenPrinterModels.size(); j++) {
            textSb.append("START\n");

            List<LeidenSmallBitmapModel> leidenSmallBitmapModels
                    = leidenPrinterModels.get(j).getLeidenSmallBitmapModels();

            for (int i = 0; i < leidenSmallBitmapModels.size(); i++) {
                LeidenSmallBitmapModel leidenSmallBitmapModel = leidenSmallBitmapModels.get(i);
                String bitmapCMD = getBitmapCMD(leidenSmallBitmapModel);
                textSb.append(bitmapCMD);
            }

            textSb.append("QTY P=").append(tempLeidenPrinterModel.getNumber()).append("\n");
            textSb.append("END\n");
        }
        textSb.append("JOBE\n");

        byte[] data = textSb.toString().getBytes();

        if (!deviceManager.isConnect()) {
            return -1;
        }

        int length = 1024 * 8;
        byte[] sendData = new byte[length];
        int index = 0;
        while(index < data.length){
            if (!deviceManager.isConnect()) {
                return -1;
            }
            if(data.length - index >= length) {
                System.arraycopy(data, index, sendData, 0, length);
            } else {
                sendData = new byte[data.length - index];
                System.arraycopy(data,index,sendData,0,data.length - index);
            }
            index += length;
            if (deviceManager.write(sendData) == 1) {
                if(leidenPrintfResultCallBack != null){
                    leidenPrintfResultCallBack.progress(data.length,index);
                }
                //0 或者正数表示成功
            } else {
                int i = 0;
                while(i < 20){
                    if(deviceManager.write(sendData) == 1){
                        if(leidenPrintfResultCallBack != null){
                            leidenPrintfResultCallBack.progress(data.length,index);
                        }
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

    private int printfLabel(LeidenPrinterModel leidenPrinterModel, LeidenPrintfResultCallBack leidenPrintfResultCallBack) {

        StringBuilder textSb = new StringBuilder();
        textSb.append("JOB\n");
        textSb.append(getPrinterEnvironmentSetCMD(leidenPrinterModel));


        textSb.append("START\n");

        List<LeidenSmallBitmapModel> leidenSmallBitmapModels =
                leidenPrinterModel.getLeidenSmallBitmapModels();

        for (int i = 0; i < leidenSmallBitmapModels.size(); i++) {
            LeidenSmallBitmapModel leidenSmallBitmapModel = leidenSmallBitmapModels.get(i);
            String bitmapCMD = getBitmapCMD(leidenSmallBitmapModel);
            textSb.append(bitmapCMD);
        }

        textSb.append("QTY P=").append(leidenPrinterModel.getNumber()).append("\n");

        textSb.append("END\n");
        textSb.append("JOBE\n");

        byte[] data = textSb.toString().getBytes();

        if (!deviceManager.isConnect()) {
            return -1;
        }

        int length = 1024 * 8;
        byte[] sendData = new byte[length];
        int index = 0;
        while(index < data.length){
            if (!deviceManager.isConnect()) {
                return -1;
            }
            if(data.length - index >= length) {
                System.arraycopy(data, index, sendData, 0, length);
            } else {
                sendData = new byte[data.length - index];
                System.arraycopy(data,index,sendData,0,data.length - index);
            }
            index += length;
            if (deviceManager.write(sendData) == 1) {
                if(leidenPrintfResultCallBack != null){
                    leidenPrintfResultCallBack.progress(data.length,index);
                }
                //0 或者正数表示成功
            } else {
                int i = 0;
                while(i < 20){
                    if(deviceManager.write(sendData) == 1){
                        if(leidenPrintfResultCallBack != null){
                            leidenPrintfResultCallBack.progress(data.length,index);
                        }
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

    public String getBitmapCMD(LeidenSmallBitmapModel leidenSmallBitmapModel) {
        StringBuilder sb = new StringBuilder();
        //处理图片的尺寸

        Bitmap bitmap = leidenSmallBitmapModel.getBitmap();
        if (ParameterUtil.isDebug) {
            String path = ParameterUtil.getSDKRoot();
            String savePath = ImageUtil.saveBitmap(bitmap, "leidenBitmap.png", path);
            Log.e("TAG", "leiden debug 保存路径：" + savePath);
        }
        bitmap = ImageUtil.handleBitmap(bitmap, leidenSmallBitmapModel.getWD(), leidenSmallBitmapModel.getHT(), 0);
        sb.append("GRAPH X=").append(leidenSmallBitmapModel.getX())
                .append(",Y=").append(leidenSmallBitmapModel.getY())
                .append(",WD=").append(bitmap.getWidth())
                .append(",HT=").append(bitmap.getHeight())
                .append(",MD=1\n");
        sb.append(convertToBMW(bitmap, 128));
        sb.append("\n");
        return sb.toString();
    }

    /**
     * 图片二值化
     *
     * @param bmp
     * @return
     */
    public String convertToBMW(Bitmap bmp, int concentration) {

        //求出当前图片的半色调阈值
//        bmp = ImageUtil.convertGreyImgByFloyd(bmp, context);

        StringBuilder sb = new StringBuilder();

        if (concentration <= 0 || concentration >= 255) {
            concentration = 128;
        }
        int width = bmp.getWidth(); // 获取位图的宽
        int height = bmp.getHeight(); // 获取位图的高
        int[] p = new int[8];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width / 8; j++) {
                for (int z = 0; z < 8; z++) {
                    int grey = bmp.getPixel(j * 8 + z, i);
                    int red = ((grey & 0x00FF0000) >> 16);
                    int green = ((grey & 0x0000FF00) >> 8);
                    int blue = (grey & 0x000000FF);
                    int gray = (int) (0.29900 * red + 0.58700 * green + 0.11400 * blue); // 灰度转化公式
//                    int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                    if (gray <= concentration) {
                        gray = 1;
                    } else {
                        gray = 0;

                    }
                    p[z] = gray;
                }
                byte value = (byte) (p[0] * 128 + p[1] * 64 + p[2] * 32 + p[3] * 16 + p[4] * 8 + p[5] * 4 + p[6] * 2 + p[7]);
                sb.append(BinaryConversionUtil.byteToHexFun(value));
            }
        }
        return sb.toString();
    }

}
