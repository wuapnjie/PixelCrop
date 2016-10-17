package com.xiaopo.flying.pixelcrop;

import android.graphics.PointF;

/**
 * Created by snowbean on 16-10-15.
 */
class CropUtil {
    private static final String TAG = "CropUtil";

    //计算点A到BC之间的距离
    public static double calculatePointToLine(PointF A, PointF B, PointF C) {
        if (B.y==C.y){
            return Math.abs(A.y - B.y);
        }

        if (B.x==C.x){
            return Math.abs(A.x - B.x);
        }

        float k = (B.y - C.y) / (B.x - C.x);
        float b = B.y - k * B.x;

        float c = A.y + 1 / k * A.x;

        double x = (c - b) * k / (Math.pow(k, 2) + 1);
        double y = k * x + b;

        return Math.sqrt(Math.pow(A.x - x, 2)+Math.pow(A.y - y, 2));
    }


}
