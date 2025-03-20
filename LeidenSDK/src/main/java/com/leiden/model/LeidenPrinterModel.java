package com.leiden.model;

import java.util.ArrayList;
import java.util.List;

public class LeidenPrinterModel {

    //标签的宽度
    private int labelW;
    //宽度的高度
    private int labelH;
    //打印张数
    private int number = 1;

    private List<LeidenSmallBitmapModel> leidenSmallBitmapModels = new ArrayList<>();

    public void addAPLSmallBitmapModel(LeidenSmallBitmapModel leidenSmallBitmapModel){
        leidenSmallBitmapModels.add(leidenSmallBitmapModel);
    }

    public void addAllAPLSmallBitmapModel(List<LeidenSmallBitmapModel> leidenSmallBitmapModels){
        this.leidenSmallBitmapModels.addAll(leidenSmallBitmapModels);
    }

    public int getLabelW() {
        return labelW;
    }

    public void setLabelW(int labelW) {
        this.labelW = labelW;
    }

    public int getLabelH() {
        return labelH;
    }

    public void setLabelH(int labelH) {
        this.labelH = labelH;
    }



    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public List<LeidenSmallBitmapModel> getLeidenSmallBitmapModels() {
        return leidenSmallBitmapModels;
    }

    public void setLeidenSmallBitmapModels(List<LeidenSmallBitmapModel> leidenSmallBitmapModels) {
        this.leidenSmallBitmapModels = leidenSmallBitmapModels;
    }

    @Override
    public String toString() {
        return "APLPrinterModel{" +
                "labelW=" + labelW +
                ", labelH=" + labelH +
                ", number=" + number +
                ", aplSmallBitmapModels=" + leidenSmallBitmapModels +
                '}';
    }


}
