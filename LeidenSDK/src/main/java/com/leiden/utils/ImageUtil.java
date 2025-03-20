package com.leiden.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtil {

    /**
     * 保存图片的方法
     */
    public static String saveBitmap(Bitmap bitmap, String saveName, String saveRootPath) {

        File file = new File(saveRootPath);
        if(!file.exists()){
            file.mkdirs();
        }


        File f = new File(saveRootPath, saveName);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            Log.i("TAG", "已经保存");
            return f.getAbsolutePath();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 处理图片的大小
     *
     * @param bitmap
     * @param newBitmapW
     * @param newBitmapH
     * @return
     */
    public static Bitmap handleBitmap(Bitmap bitmap, float newBitmapW, float newBitmapH, int rotate) {

        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        // 计算缩放比例
        float scaleWidth = ((float) newBitmapW) / width;
        float scaleHeight = ((float) newBitmapH) / height;

        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        //先旋转，后缩小
        if (rotate % 360 != 0) {
            matrix.setRotate(rotate, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        }

        matrix.postScale(scaleWidth, scaleHeight);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, false);

        return bitmap;
    }

    /**
     * 图片拷贝
     *
     * @param bitmap
     * @return
     */
    public static Bitmap bitmapCopy(Bitmap bitmap) {
        int width = bitmap.getWidth();//获取位图的宽  
        int height = bitmap.getHeight();//获取位图的高  

        int[] pixels = new int[width * height]; //通过位图的大小创建像素点数组  

        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] gray = new int[height * width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];
                gray[width * i + j] = grey; // 灰度转化公式;
            }
        }
        Bitmap mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        mBitmap.setPixels(gray, 0, width, 0, 0, width, height);
        return mBitmap;
    }

}
