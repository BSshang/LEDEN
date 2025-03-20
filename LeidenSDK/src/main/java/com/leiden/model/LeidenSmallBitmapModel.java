package com.leiden.model;

import android.graphics.Bitmap;

public class LeidenSmallBitmapModel {

    private Bitmap bitmap;

    //横向起点，与纵向起点
    private int x;
    private int y;

    //图片的宽度 高度
    private int WD;//宽度
    private int HT;//高度

    //当前图片的二值化阈值
    private int threshold = 128;

    public LeidenSmallBitmapModel(){

    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWD() {
        return WD;
    }

    public void setWD(int WD) {
        this.WD = WD;
    }

    public int getHT() {
        return HT;
    }

    public void setHT(int HT) {
        this.HT = HT;
    }


    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }


    @Override
    public String toString() {
        return "APLSmallBitmapModel{" +
                "bitmap=" + bitmap +
                ", x=" + x +
                ", y=" + y +
                ", WD=" + WD +
                ", HT=" + HT +
                ", threshold=" + threshold +
                '}';
    }


}
