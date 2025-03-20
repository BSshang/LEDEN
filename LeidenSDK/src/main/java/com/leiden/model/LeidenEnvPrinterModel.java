package com.leiden.model;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LeidenEnvPrinterModel {

    private static Handler handler = new Handler(Looper.getMainLooper());

    /**
     * 打印速度、打印浓度、打印媒介、标签种类、顶部偏移、打印模式、撕纸偏移
     */

    //当前打印机的精度 1 -- 200dip 2 -- 300 dip 3 -- 600 dip
    private int printerAccuracy = 1;
    //打印速度  1 -- 高速  2 -- 标准 3 -- 中速 4 -- 低速
    private int printfSpeed = 2;
    //打印浓度  范围 1 - 15
    private int printfPotency = 8;

    //打印媒介  1 - 热敏    2 - 碳带
    private int printfMedium = 2;
    //标签种类  1 - 黑标    2 - 模切    3 - 连续
    private int labelType = 3;
    //顶部偏移  ±支持标签最大高度  -10 --- +10
    private int topDeviation = 0;

    //打印模式  1 - 标准模式  2 - 连续模式 3 - 剥离模式 4 - 切刀模式
    private int printfModel = 1;

    //剥离方式 1 - 传感器 2 - 按键 当 printfMode = 剥离模式时有效
    private int beStrippedModel = 1;
    //剥离送纸量 当 printfMode = 剥离模式时有效
    private int beStrippedFeedVolume = 0;

    //切纸张数 [0-9999] 当 printfModel == 切刀模式模式时有效
    private int cutNumber = 0;
    //切刀送纸量 当 printfModel == 切刀模式时有效
    private int cutterFeedVolume = 0;

    //标准送纸量 当 printfModel == 标准模式时有效
    private int standardFeedVolume = 0;

    public void setModel(LeidenEnvPrinterModel leidenEnvPrinterModel) {
        setPrinterAccuracy(leidenEnvPrinterModel.getPrinterAccuracy());
        setPrintfSpeed(leidenEnvPrinterModel.getPrintfSpeed());
        setPrintfPotency(leidenEnvPrinterModel.getPrintfPotency());
        setPrintfMedium(leidenEnvPrinterModel.getPrintfMedium());
        setLabelType(leidenEnvPrinterModel.getLabelType());
        setTopDeviation(leidenEnvPrinterModel.getTopDeviation());
        setPrintfModel(leidenEnvPrinterModel.getPrintfModel());
        setBeStrippedModel(leidenEnvPrinterModel.getBeStrippedModel());
        setBeStrippedFeedVolume(leidenEnvPrinterModel.getBeStrippedFeedVolume());
        setCutNumber(leidenEnvPrinterModel.getCutNumber());
        setCutterFeedVolume(leidenEnvPrinterModel.getCutterFeedVolume());
        setStandardFeedVolume(leidenEnvPrinterModel.getStandardFeedVolume());
    }

    public boolean setPrinterAccuracy(final int printerAccuracy) {

        if(this.printerAccuracy == printerAccuracy){
            return false;
        }

        this.printerAccuracy = printerAccuracy;
        handler.post(new Runnable() {
            @Override
            public void run() {
                for(LeidenPrinterModelListener leidenPrinterModelListener:leidenPrinterModelListeners){
                    leidenPrinterModelListener.printerAccuracyChange(printerAccuracy);
                    leidenPrinterModelListener.standardFeedVolumeChange(standardFeedVolume);
                    leidenPrinterModelListener.beStrippedFeedVolumeChange(beStrippedFeedVolume);
                    leidenPrinterModelListener.cutterFeedVolumeChange(cutterFeedVolume);
                }
            }
        });
        return true;
    }

    public boolean setPrintfSpeed(final int printfSpeed){

        if(printfSpeed == this.printfSpeed){
            return false;
        }

        if(Check.checkPrintfSpeed(printfSpeed)){
            this.printfSpeed = printfSpeed;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for(LeidenPrinterModelListener leidenPrinterModelListener :leidenPrinterModelListeners){
                        leidenPrinterModelListener.printfSpeedChange(printfSpeed);
                    }
                }
            });
            return true;
        }
        return false;
    }


    public boolean setPrintfPotency(final int printfPotency){
        if(this.printfPotency == printfPotency){
            return false;
        }
        if(Check.checkPrintfPotency(printfPotency)){
            this.printfPotency = printfPotency;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for(LeidenPrinterModelListener leidenPrinterModelListener :leidenPrinterModelListeners){
                        leidenPrinterModelListener.printfPotencyChange(printfPotency);
                    }
                }
            });
            return true;
        }
        return false;
    }

    public boolean setPrintfMedium(final int printfMedium){
        if(this.printfMedium == printfMedium){
            return false;
        }
        if(Check.checkPrintfMedium(printfMedium)){
            this.printfMedium = printfMedium;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for(LeidenPrinterModelListener leidenPrinterModelListener :leidenPrinterModelListeners){
                        leidenPrinterModelListener.printfMediumChange(printfMedium);
                    }
                }
            });
            return true;
        }
        return false;
    }

    public boolean setLabelType(final int labelType){
        if(this.labelType == labelType){
            return false;
        }
        if(Check.checkLabelType(labelType)){
            this.labelType = labelType;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for(LeidenPrinterModelListener leidenPrinterModelListener :leidenPrinterModelListeners){
                        leidenPrinterModelListener.labelTypeChange(labelType);
                    }
                }
            });
            return true;
        }
        return false;
    }

    public boolean setTopDeviation(final int topDeviation){
        if(this.topDeviation == topDeviation){
            return false;
        }
        this.topDeviation = topDeviation;
        handler.post(new Runnable() {
            @Override
            public void run() {
                for(LeidenPrinterModelListener leidenPrinterModelListener :leidenPrinterModelListeners){
                    leidenPrinterModelListener.topDeviationChange(topDeviation);
                }
            }
        });
        return true;
//        if(topDeviation <= 10 && topDeviation >= -10){
//            return true;
//        }
//        return false;
    }

    ////////////////////////////////////////////////////////////////////////////////////

    public boolean setPrintfModel(final int printfModel){
        if(this.printfModel == printfModel){
            return false;
        }
        if(Check.checkPrintfModel(printfModel)){
            this.printfModel = printfModel;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for(LeidenPrinterModelListener leidenPrinterModelListener :leidenPrinterModelListeners){
                        leidenPrinterModelListener.printfModelChange(printfModel);
                    }
                }
            });
            return true;
        }
        return false;
    }

    public boolean setBeStrippedModel(final int beStrippedModel){
        if(this.beStrippedModel == beStrippedModel){
            return false;
        }
        if(Check.checkBeStrippedModel(beStrippedModel)){
            this.beStrippedModel = beStrippedModel;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for(LeidenPrinterModelListener leidenPrinterModelListener :leidenPrinterModelListeners){
                        leidenPrinterModelListener.beStrippedModelChange(beStrippedModel);
                    }
                }
            });
            return true;
        }
        return false;
    }

    public boolean setBeStrippedFeedVolume(final int beStrippedFeedVolume){
        if(this.beStrippedFeedVolume == beStrippedFeedVolume){
            return false;
        }
        if(Check.checkBeStrippedFeedVolume(printerAccuracy,beStrippedFeedVolume)) {
            this.beStrippedFeedVolume = beStrippedFeedVolume;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (LeidenPrinterModelListener leidenPrinterModelListener : leidenPrinterModelListeners) {
                        leidenPrinterModelListener.beStrippedFeedVolumeChange(beStrippedFeedVolume);
                    }
                }
            });
            return true;
        }
        return false;
    }

    public boolean setCutNumber(final int cutNumber){
        if(this.cutNumber == cutNumber){
            return false;
        }
        if(Check.checkCutNumber(cutNumber)){
            this.cutNumber = cutNumber;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for(LeidenPrinterModelListener leidenPrinterModelListener :leidenPrinterModelListeners){
                        leidenPrinterModelListener.cutNumberChange(cutNumber);
                    }
                }
            });
            return true;
        }
        return false;
    }

    public boolean setCutterFeedVolume(final int cutterFeedVolume){
        if(this.cutterFeedVolume == cutterFeedVolume){
            return false;
        }
        if(Check.checkBeStrippedFeedVolume(printerAccuracy,cutterFeedVolume)) {
            this.cutterFeedVolume = cutterFeedVolume;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for(LeidenPrinterModelListener leidenPrinterModelListener :leidenPrinterModelListeners){
                        leidenPrinterModelListener.cutterFeedVolumeChange(cutterFeedVolume);
                    }
                }
            });
            return true;
        }
        return false;

    }

    public boolean setStandardFeedVolume(final int standardFeedVolume){
        if(this.standardFeedVolume == standardFeedVolume){
            return false;
        }
        if(Check.checkBeStrippedFeedVolume(printerAccuracy,standardFeedVolume)) {
            this.standardFeedVolume = standardFeedVolume;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for(LeidenPrinterModelListener leidenPrinterModelListener :leidenPrinterModelListeners){
                        leidenPrinterModelListener.standardFeedVolumeChange(standardFeedVolume);
                    }
                }
            });
            return true;
        }
        return false;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////


    public int getPrinterAccuracy() {
        return printerAccuracy;
    }

    //1 -- 200dip 2 -- 300 dip
    public float getRealPrinterAccuracy(){
        if(printerAccuracy == 1){
            return 8;
        } else if(printerAccuracy == 2){
            return 11.8F;
        } else {
            return 8;
        }

    }

    public int getPrintfSpeed() {
        return printfSpeed;
    }

    public int getPrintfPotency() {
        return printfPotency;
    }

    public int getPrintfMedium() {
        return printfMedium;
    }

    public int getLabelType() {
        return labelType;
    }

    public int getTopDeviation() {
        return topDeviation;
    }

    public float getMMTopDeviation(){
        if(printerAccuracy == 1){
            return topDeviation / 8F;
        } else if(printerAccuracy == 2){
            return topDeviation / 11.8F;
        }
        return topDeviation / 8F;
    }

    public int getPrintfModel() {
        return printfModel;
    }

    public float getMMBeStrippedModel() {
        if(printerAccuracy == 1){
            return beStrippedModel / 8F;
        } else if(printerAccuracy == 2){
            return beStrippedModel / 11.8F;
        }
        return beStrippedModel / 8F;
    }

    public float getMMBeStrippedFeedVolume() {
        if(printerAccuracy == 1){
            return beStrippedFeedVolume / 8F;
        } else if(printerAccuracy == 2){
            return beStrippedFeedVolume / 11.8F;
        }
        return beStrippedFeedVolume / 8F;
    }

    public float getMMCutterFeedVolume() {
        if(printerAccuracy == 1){
            return cutterFeedVolume / 8F;
        } else if(printerAccuracy == 2){
            return cutterFeedVolume / 11.8F;
        }
        return cutterFeedVolume / 8F;
    }

    public int getBeStrippedModel() {
        return beStrippedModel;
    }

    public int getBeStrippedFeedVolume() {
        return beStrippedFeedVolume;
    }

    public int getCutterFeedVolume() {
        return cutterFeedVolume;
    }

    public int getCutNumber() {
        return cutNumber;
    }


    public int getStandardFeedVolume() {
        return standardFeedVolume;
    }

    public String getCMD(){

        StringBuilder cmdSB = new StringBuilder();
        cmdSB.append("DEF");
        //打印速度
        cmdSB.append(" SP=").append(getPrintfSpeed());
        //打印浓度
        cmdSB.append(",DK=").append(getPrintfPotency());
        //打印媒介
        cmdSB.append(",TR=").append(getPrintfMedium());
        //标签种类
        cmdSB.append(",MK=").append(getLabelType());
        //顶部偏移
        cmdSB.append(",MO=").append(getTopDeviation());
        //打印模式
        cmdSB.append(",MD=").append(getPrintfModel());
        //剥离方式
        cmdSB.append(",PM=").append(getBeStrippedModel());
        //剥离送纸量
        cmdSB.append(",PO=").append(getBeStrippedFeedVolume());
        //切纸张数
        cmdSB.append(",CP=").append(getCutNumber());
        //切刀送纸量
        cmdSB.append(",CO=").append(getCutterFeedVolume());
        //标准送纸量
        cmdSB.append(",TO=").append(getStandardFeedVolume());
        cmdSB.append("\n\r");

        return cmdSB.toString();

    }

    public String getAppCMD(){
        StringBuilder cmdSB = new StringBuilder();
        //打印速度
        cmdSB.append("SP=").append(getPrintfSpeed());
        //打印浓度
        cmdSB.append(",DK=").append(getPrintfPotency());
        //打印媒介
        cmdSB.append(",TR=").append(getPrintfMedium());
        //标签种类
        cmdSB.append(",MK=").append(getLabelType());
        //顶部偏移
        cmdSB.append(",MO=").append(getTopDeviation());
        //打印模式
        cmdSB.append(",MD=").append(getPrintfModel());
        //剥离方式
        cmdSB.append(",PM=").append(getBeStrippedModel());
        //剥离送纸量
        cmdSB.append(",PO=").append(getBeStrippedFeedVolume());
        //切纸张数
        cmdSB.append(",CP=").append(getCutNumber());
        //切刀送纸量
        cmdSB.append(",CO=").append(getCutterFeedVolume());
        //标准送纸量
        cmdSB.append(",TO=").append(getStandardFeedVolume());
        return cmdSB.toString();
    }


    //解析
    //MD=1,PM=1,TR=2,MK=2,DR=1,DK=8,SP=3,SM=537,SG=385,SO=1023,SR=213,VM=53,VG=120,VO=255,VR=130,PS=90,PO=0,CO=0,TO=0,RO=0,MO=0,RMO=0,PH=280,PW=400,PG=25,LM=0,RM=0,TM=0,BM=0,PHM=35,PWM=50,PGM=4,LMM=0,RMM=0,TMM=0,BMM=0,MOM=0.0,RMOM=0.0,XO=0,YO=0,CP=0
    public void analysis(String data){

        String[] split = data.split(",");
        for(String content:split){
            try{
                String[] split1 = content.split("=");
                String key = split1[0];
                int value = (int) Float.parseFloat(split1[1]);

                //打印速度
                if(key.equals("SP")){
                    setPrintfSpeed(value);
                } else if(key.equals("DK")){
                    setPrintfPotency(value);
                } else if(key.equals("TR")){
                    setPrintfMedium(value);
                } else if(key.equals("MK")){
                    setLabelType(value);
                } else if(key.equals("MO")){
                    setTopDeviation(value);
                } else if(key.equals("MD")){
                    setPrintfModel(value);
                } else if(key.equals("PM")){
                    setBeStrippedModel(value);
                } else if(key.equals("PO")){
                    setBeStrippedFeedVolume(value);
                } else if(key.equals("CP")){
                    setCutNumber(value);
                } else if(key.equals("CO")){
                    setCutterFeedVolume(value);
                } else if(key.equals("TO")){
                    setStandardFeedVolume(value);
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private List<LeidenPrinterModelListener> leidenPrinterModelListeners = new ArrayList<>();

    public void addLeidenPrinterModelListener(LeidenPrinterModelListener leidenPrinterModelListener){
        if(!leidenPrinterModelListeners.contains(leidenPrinterModelListener)){
            leidenPrinterModelListeners.add(leidenPrinterModelListener);
            leidenPrinterModelListener.printfSpeedChange(printfSpeed);
            leidenPrinterModelListener.printfPotencyChange(printfPotency);
            leidenPrinterModelListener.printfMediumChange(printfMedium);
            leidenPrinterModelListener.labelTypeChange(labelType);
            leidenPrinterModelListener.topDeviationChange(topDeviation);
            leidenPrinterModelListener.printfModelChange(printfModel);
            leidenPrinterModelListener.beStrippedModelChange(beStrippedModel);
            leidenPrinterModelListener.beStrippedFeedVolumeChange(beStrippedFeedVolume);
            leidenPrinterModelListener.cutNumberChange(cutNumber);
            leidenPrinterModelListener.cutterFeedVolumeChange(cutterFeedVolume);
            leidenPrinterModelListener.standardFeedVolumeChange(standardFeedVolume);
            leidenPrinterModelListener.printerAccuracyChange(printerAccuracy);
        }
    }

    public void removeLeidenPrinterModelListener(LeidenPrinterModelListener leidenPrinterModelListener){
        leidenPrinterModelListeners.remove(leidenPrinterModelListener);
    }

    public interface LeidenPrinterModelListener{

        void printfSpeedChange(int printfSpeed);
        void printfPotencyChange(int PrintfPotency);
        void printfMediumChange(int printfMedium);

        void labelTypeChange(int labelType);
        void topDeviationChange(int topDeviation);
        void printfModelChange(int printfModel);

        void beStrippedModelChange(int beStrippedModel);
        void beStrippedFeedVolumeChange(int beStrippedFeedVolume);
        void cutNumberChange(int cutNumber);

        void cutterFeedVolumeChange(int cutterFeedVolume);
        void standardFeedVolumeChange(int standardFeedVolume);

        void printerAccuracyChange(int printerAccuracy);

    }


    public static class Check{
        public static boolean checkPrintfSpeed(int printfSpeed){
            return printfSpeed >= 1 && printfSpeed <= 4;
        }

        public static boolean checkPrintfPotency(int printfPotency){
            return printfPotency >= 1 && printfPotency <= 15;
        }
        public static boolean checkPrintfMedium(int printfMedium){
            return printfMedium >= 1 && printfMedium <=2;
        }
        public static boolean checkLabelType(int labelType){
            return labelType >= 1 && labelType <=3;
        }
        public static boolean checkPrintfModel(int printfModel){
            return printfModel >= 1 && printfModel <= 4;
        }
        public static boolean checkBeStrippedModel(int beStrippedModel){
            return beStrippedModel >=1 && beStrippedModel <= 2;
        }
        /**

         */
        public static boolean checkBeStrippedFeedVolume(int printerAccuracy,int beStrippedFeedVolume){
            if(printerAccuracy == 1){//200dip
                if(!(beStrippedFeedVolume >= -100 && beStrippedFeedVolume <= 100)){
                    return false;
                }
            } else if(printerAccuracy == 2){//300dip
                if(!(beStrippedFeedVolume >= -150 && beStrippedFeedVolume <= 150)){
                    return false;
                }
            } else if(printerAccuracy == 3) {//600dip
                if (!(beStrippedFeedVolume >= -300 && beStrippedFeedVolume <= 300)) {
                    return false;
                }
            }
            return true;
        }

        public static boolean checkCutNumber(int cutNumber){
            return cutNumber >= 0 && cutNumber <= 9999;
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeidenEnvPrinterModel that = (LeidenEnvPrinterModel) o;
        return printerAccuracy == that.printerAccuracy &&
                printfSpeed == that.printfSpeed &&
                printfPotency == that.printfPotency &&
                printfMedium == that.printfMedium &&
                labelType == that.labelType &&
                topDeviation == that.topDeviation &&
                printfModel == that.printfModel &&
                beStrippedModel == that.beStrippedModel &&
                beStrippedFeedVolume == that.beStrippedFeedVolume &&
                cutNumber == that.cutNumber &&
                cutterFeedVolume == that.cutterFeedVolume &&
                standardFeedVolume == that.standardFeedVolume;
    }

    @Override
    public int hashCode() {
        return Objects.hash(printerAccuracy, printfSpeed, printfPotency, printfMedium, labelType, topDeviation, printfModel, beStrippedModel, beStrippedFeedVolume, cutNumber, cutterFeedVolume, standardFeedVolume);
    }



}
