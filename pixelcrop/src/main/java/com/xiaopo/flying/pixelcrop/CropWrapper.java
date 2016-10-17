package com.xiaopo.flying.pixelcrop;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * Created by snowbean on 16-10-14.
 */
public class CropWrapper {
    private static final String TAG = "CropWrapper";
    private Drawable mDrawable;
    private Matrix mMatrix;

    private Rect mRealBound;
    private RectF mMappedBound = new RectF();

    private float mRotate;

    public static final int LEFT = 1;
    public static final int TOP = 1 << 1;
    public static final int RIGHT = 1 << 2;
    public static final int BOTTOM = 1 << 3;

    public static final int ALL = LEFT | TOP | RIGHT | BOTTOM;
    public static final int NONE = LEFT & TOP & RIGHT & BOTTOM;

    private int mCanMoveDirection = ALL;

    public CropWrapper(Drawable drawable, Matrix matrix) {
        mDrawable = drawable;
        mMatrix = matrix;
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
//        if (!mIsFlipped) {
        return new float[]{
                0f, 0f,
                getWidth(), 0f,
                getWidth(), getHeight(),
                0f, getHeight()
        };
//        } else {
//        return new float[]{
//                getWidth(), 0f,
//                0f, 0f,
//                getWidth(), getHeight(),
//                0f, getHeight()
//        };
//        }
    }

    public float[] getMappedBoundPoints() {
        float[] dst = new float[8];
        mMatrix.mapPoints(dst, getBoundPoints());
        return dst;
    }

    public PointF[] getMappedCornerPoints() {
        float[] dst = new float[8];
        mMatrix.mapPoints(dst, getBoundPoints());
        return new PointF[]{
                new PointF(dst[0], dst[1]),
                new PointF(dst[2], dst[3]),
                new PointF(dst[4], dst[5]),
                new PointF(dst[6], dst[7])

        };
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

    public int getCanMoveDirection() {
        return mCanMoveDirection;
    }

    public void setCanMoveDirection(int canMoveDirection) {
        mCanMoveDirection = canMoveDirection;
    }

    public boolean canMoveLeft() {
        return (mCanMoveDirection & LEFT) != 0;
    }

    public boolean canMoveRight() {
        return (mCanMoveDirection & RIGHT) != 0;
    }

    public boolean canMoveTop() {
        return (mCanMoveDirection & TOP) != 0;
    }

    public boolean canMoveBottom() {
        return (mCanMoveDirection & BOTTOM) != 0;
    }

    public float getTranslateX() {
        return getMappedCenterPoint().x - getCenterPoint().x;
    }


    public float getTranslateY() {
        return getMappedCenterPoint().y - getCenterPoint().y;
    }

    public float getScaleFactor() {
        return getMappedWidth() / getWidth();
    }

    public float getRotate() {
        return mRotate;
    }

    public void setRotate(float rotate) {
        mRotate = rotate;
    }
}
