package com.xiaopo.flying.pixelcrop;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

/**
 * Created by snowbean on 16-10-14.
 */
public class CropWrapper {
    private static final String TAG = "CropWrapper";
    private Drawable mDrawable;
    private Matrix mMatrix;

    private Rect mRealBound;
    private RectF mMappedBound = new RectF();
    private float[] mMatrixValues = new float[9];

    private String mInputPath;
    private String mOutputPath;

    public CropWrapper(Drawable drawable, Matrix matrix, String inputPath, String outputPath) {
        mDrawable = drawable;
        mMatrix = matrix;
        mInputPath = inputPath;
        mOutputPath = outputPath;
        mRealBound = new Rect(0, 0, getWidth(), getHeight());
    }

    public void draw(Canvas canvas, int alpha) {
        canvas.save();
        canvas.concat(mMatrix);
        mDrawable.setBounds(mRealBound);
        mDrawable.setAlpha(alpha);
        mDrawable.draw(canvas);
        canvas.restore();
    }

    public void draw(Canvas canvas) {
        draw(canvas, 255);
    }

    public float[] getBoundPoints() {
        return new float[]{
                0f, 0f,
                getWidth(), 0f,
                getWidth(), getHeight(),
                0f, getHeight()
        };
    }

    public float[] getMappedBoundPoints() {
        float[] dst = new float[8];
        mMatrix.mapPoints(dst, getBoundPoints());
        return dst;
    }

    public float[] getMappedPoints(float[] src) {
        float[] dst = new float[src.length];
        mMatrix.mapPoints(dst, src);
        return dst;
    }


    public RectF getBound() {
        return new RectF(0, 0, getWidth(), getHeight());
    }

    public RectF getMappedBound() {
        mMatrix.mapRect(mMappedBound, getBound());
        return mMappedBound;
    }

    public PointF getCenterPoint() {
        return new PointF(getWidth() / 2, getHeight() / 2);
    }

    public PointF getMappedCenterPoint() {
        PointF pointF = getCenterPoint();
        float[] dst = getMappedPoints(new float[]{
                pointF.x,
                pointF.y
        });
        return new PointF(dst[0], dst[1]);
    }

    public float getMappedWidth() {
        return getMappedBound().width();
    }

    public float getMappedHeight() {
        return getMappedBound().height();
    }


    public int getWidth() {
        return mDrawable.getIntrinsicWidth();
    }

    public int getHeight() {
        return mDrawable.getIntrinsicHeight();
    }

    public Drawable getDrawable() {
        return mDrawable;
    }

    public void setDrawable(Drawable drawable) {
        mDrawable = drawable;
    }

    public Matrix getMatrix() {
        return mMatrix;
    }

    public void setMatrix(Matrix matrix) {
        mMatrix = matrix;
    }

    public Rect getRealBound() {
        return mRealBound;
    }

    public void setRealBound(Rect realBound) {
        mRealBound = realBound;
    }

    public float getTranslateX() {
        return getMappedCenterPoint().x - getCenterPoint().x;
    }


    public float getTranslateY() {
        return getMappedCenterPoint().y - getCenterPoint().y;
    }

    public String getInputPath() {
        return mInputPath;
    }

    public void setInputPath(String inputPath) {
        mInputPath = inputPath;
    }

    public String getOutputPath() {
        return mOutputPath;
    }

    public void setOutputPath(String outputPath) {
        mOutputPath = outputPath;
    }

    public float getScaleFactor() {
        if (getWidth() >= getHeight()) {
            return getMappedWidth() / getWidth();
        } else {
            return getMappedHeight() / getHeight();
        }
    }

    /**
     * @return - current image scale value.
     * [1.0f - for original image, 2.0f - for 200% scaled image, etc.]
     */
    public float getCurrentScale() {
        return getMatrixScale(mMatrix);
    }

    /**
     * This method calculates scale value for given Matrix object.
     */
    public float getMatrixScale(@NonNull Matrix matrix) {
        return (float) Math.sqrt(Math.pow(getMatrixValue(matrix, Matrix.MSCALE_X), 2)
                + Math.pow(getMatrixValue(matrix, Matrix.MSKEW_Y), 2));
    }

    /**
     * @return - current image rotation angle.
     */
    public float getCurrentAngle() {
        return getMatrixAngle(mMatrix);
    }

    /**
     * This method calculates rotation angle for given Matrix object.
     */
    public float getMatrixAngle(@NonNull Matrix matrix) {
        return (float) -(Math.atan2(getMatrixValue(matrix, Matrix.MSKEW_X),
                getMatrixValue(matrix, Matrix.MSCALE_X)) * (180 / Math.PI));
    }

    protected float getMatrixValue(@NonNull Matrix matrix, @IntRange(from = 0, to = 9) int valueIndex) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[valueIndex];
    }
}
