package com.xiaopo.flying.pixelcrop;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.yalantis.ucrop.task.ExifInfo;

/**
 * Created by snowbean on 16-10-14.
 */
class CropWrapper {
    private static final String TAG = "CropWrapper";
    private Drawable mDrawable;
    private Matrix mMatrix;

    private Rect mRealBound;
    private RectF mMappedBound = new RectF();
    private float[] mMatrixValues = new float[9];

    private String mInputPath;
    private String mOutputPath;
    private ExifInfo mExifInfo;



    CropWrapper(Drawable drawable, Matrix matrix, String inputPath, String outputPath, ExifInfo exifInfo) {
        mDrawable = drawable;
        mMatrix = matrix;
        mInputPath = inputPath;
        mOutputPath = outputPath;
        mExifInfo = exifInfo;
        mRealBound = new Rect(0, 0, getWidth(), getHeight());
    }

    void draw(Canvas canvas, int alpha) {
        canvas.save();
        canvas.concat(mMatrix);
        mDrawable.setBounds(mRealBound);
        mDrawable.setAlpha(alpha);
        mDrawable.draw(canvas);
        canvas.restore();
    }

    void draw(Canvas canvas) {
        draw(canvas, 255);
    }

    float[] getBoundPoints() {
        return new float[]{
                0f, 0f,
                getWidth(), 0f,
                getWidth(), getHeight(),
                0f, getHeight()
        };
    }

    float[] getMappedBoundPoints() {
        float[] dst = new float[8];
        mMatrix.mapPoints(dst, getBoundPoints());
        return dst;
    }

    float[] getMappedPoints(float[] src) {
        float[] dst = new float[src.length];
        mMatrix.mapPoints(dst, src);
        return dst;
    }


    RectF getBound() {
        return new RectF(0, 0, getWidth(), getHeight());
    }

    RectF getMappedBound() {
        mMatrix.mapRect(mMappedBound, getBound());
        return mMappedBound;
    }

    PointF getCenterPoint() {
        return new PointF(getWidth() / 2, getHeight() / 2);
    }

    PointF getMappedCenterPoint() {
        PointF pointF = getCenterPoint();
        float[] dst = getMappedPoints(new float[]{
                pointF.x,
                pointF.y
        });
        return new PointF(dst[0], dst[1]);
    }

    float getMappedWidth() {
        return getMappedBound().width();
    }

    float getMappedHeight() {
        return getMappedBound().height();
    }


    int getWidth() {
        return mDrawable.getIntrinsicWidth();
    }

    int getHeight() {
        return mDrawable.getIntrinsicHeight();
    }

    Drawable getDrawable() {
        return mDrawable;
    }

    void setDrawable(Drawable drawable) {
        mDrawable = drawable;
    }

    Matrix getMatrix() {
        return mMatrix;
    }

    Rect getRealBound() {
        return mRealBound;
    }

    void setRealBound(Rect realBound) {
        mRealBound = realBound;
    }

    String getInputPath() {
        return mInputPath;
    }

    void setInputPath(String inputPath) {
        mInputPath = inputPath;
    }

    String getOutputPath() {
        return mOutputPath;
    }

    void setOutputPath(String outputPath) {
        mOutputPath = outputPath;
    }

//    /**
//     * This is not real scale
//     */
//    float getScaleFactor() {
//        if (getWidth() >= getHeight()) {
//            return getMappedWidth() / getWidth();
//        } else {
//            return getMappedHeight() / getHeight();
//        }
//    }

    /**
     * @return - current image scale value.
     * [1.0f - for original image, 2.0f - for 200% scaled image, etc.]
     */
    float getCurrentScale() {
        return getMatrixScale(mMatrix);
    }

    /**
     * This method calculates scale value for given Matrix object.
     */
    private float getMatrixScale(@NonNull Matrix matrix) {
        return (float) Math.sqrt(Math.pow(getMatrixValue(matrix, Matrix.MSCALE_X), 2)
                + Math.pow(getMatrixValue(matrix, Matrix.MSKEW_Y), 2));
    }

    /**
     * @return - current image rotation angle.
     */
    float getCurrentAngle() {
        return getMatrixAngle(mMatrix);
    }

    /**
     * This method calculates rotation angle for given Matrix object.
     */
    private float getMatrixAngle(@NonNull Matrix matrix) {
        return (float) -(Math.atan2(getMatrixValue(matrix, Matrix.MSKEW_X),
                getMatrixValue(matrix, Matrix.MSCALE_X)) * (180 / Math.PI));
    }

    private float getMatrixValue(@NonNull Matrix matrix, @IntRange(from = 0, to = 9) int valueIndex) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[valueIndex];
    }

    ExifInfo getExifInfo() {
        return mExifInfo;
    }

    void setExifInfo(ExifInfo exifInfo) {
        mExifInfo = exifInfo;
    }
}
