package com.xiaopo.flying.pixelcrop;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

/**
 * Created by snowbean on 16-10-14.
 */
public class PixelCropView extends View {
    private static final String TAG = "PixelCropView";
    private CropWrapper mCropWrapper;
    private int mBorderOffset = 50;
    private Paint mBorderPaint;
    private Border mCropBorder;
    private float mDownX;
    private float mDownY;
    private float mOldDistance;
    //缩放点
    private PointF mScalePoint;
    private Matrix mPreMatrix;
    private Matrix mPreSizeMatrix;
    //触摸事件开始时的缩放比
    private float mPreZoom;
    private ActionMode mCurrentMode;
    private boolean mIsRotateState;
    //不同旋转角度下的最小缩放比
    private float mMinZoom;
    //Temp Var
    private double mTempAlpha;
    private float mTempScale;
    private float mDiagonal;
    private float mCropBorderWidth;
    private float mCropBorderHeight;
    public PixelCropView(Context context) {
        super(context);
    }

    public PixelCropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBorderPaint = new Paint();
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(Color.parseColor("#ddcbcbcb"));
        mBorderPaint.setStrokeWidth(3);

        mPreMatrix = new Matrix();
        mPreSizeMatrix = new Matrix();

        mScalePoint = new PointF();
    }

    public PixelCropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PixelCropView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCropBorder = new Border(new RectF(mBorderOffset,
                mBorderOffset,
                w - mBorderOffset,
                w - mBorderOffset));

        //根据图片的长宽比确定剪裁边框的大小，并使图片移动缩放至剪裁框内
        if (mCropWrapper != null) {
            int width = mCropWrapper.getWidth();
            int height = mCropWrapper.getHeight();

            if (width > height) {
                int bWidth = w - 2 * mBorderOffset;
                int bHeight = bWidth * height / width;
                mCropBorder = new Border(new RectF(
                        mBorderOffset,
                        (w - bHeight) / 2,
                        w - mBorderOffset,
                        (w + bHeight) / 2
                ));
            } else {
                int bHeight = w - 2 * mBorderOffset;
                int bWidth = bHeight * width / height;
                mCropBorder = new Border(new RectF(
                        (w - bWidth) / 2,
                        mBorderOffset,
                        (w + bWidth) / 2,
                        w - mBorderOffset
                ));
            }

            int offsetX = getWidth() / 2 - mCropWrapper.getWidth() / 2;
            int offsetY = getWidth() / 2 - mCropWrapper.getHeight() / 2;

            mCropWrapper.getMatrix()
                    .setTranslate(offsetX, offsetY);

            float scaleX = mCropBorder.width() / mCropWrapper.getWidth();
            float scaleY = mCropBorder.height() / mCropWrapper.getHeight();

            mMinZoom = scaleX;

            mCropWrapper.getMatrix()
                    .postScale(scaleX, scaleY, mCropWrapper.getMappedCenterPoint().x, mCropWrapper.getMappedCenterPoint().y);

            mPreMatrix.set(mCropWrapper.getMatrix());
            mPreSizeMatrix.set(mCropWrapper.getMatrix());


            invalidate();
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mCropWrapper != null) {
            mCropWrapper.draw(canvas);
        }

        //画边框和阴影
        if (mCropBorder != null) {
            //TODO 边框，4个边角未画
            canvas.drawRect(mCropBorder.getRect(), mBorderPaint);

            mBorderPaint.setStrokeWidth(1);
            if (mIsRotateState) {
                mCropBorder.drawGrid(canvas, mBorderPaint, 9, 9);
            } else if (mCurrentMode == ActionMode.DRAG || mCurrentMode == ActionMode.ZOOM) {
                mCropBorder.drawGrid(canvas, mBorderPaint, 3, 3);
            }
            mBorderPaint.setStrokeWidth(3);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();

                if (mCropWrapper != null) {
                    mPreMatrix.set(mCropWrapper.getMatrix());
                }

                //TODO 判断手指按下的位置确定ActionMode
                mCurrentMode = ActionMode.DRAG;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mOldDistance = calculateDistance(event);
                mScalePoint = calculateMidPoint(event);

                mPreZoom = mCropWrapper.getScaleFactor();

                if (event.getPointerCount() == 2) {
                    mCurrentMode = ActionMode.ZOOM;
                }

                break;

            case MotionEvent.ACTION_MOVE:
                switch (mCurrentMode) {
                    case NONE:
                        break;
                    case DRAG:
//                        handleDragEvent(event);
                        break;
                    case ZOOM:
                        handleZoomEvent(event);
                        break;
                }
                invalidate();
                break;

            case MotionEvent.ACTION_POINTER_UP:
                mCurrentMode = ActionMode.NONE;
                float currentZoom = mCropWrapper.getScaleFactor();
                mPreSizeMatrix.postScale(currentZoom / mPreZoom, currentZoom / mPreZoom,
                        mCropBorder.centerX(), mCropBorder.centerY());
                break;

            case MotionEvent.ACTION_UP:
                switch (mCurrentMode) {
                    case NONE:
                        break;
                    case DRAG:
                        System.out.println("leftTop");
                        mCropWrapper.isInBorder(mCropBorder.leftTop);

                        System.out.println("rightTop");
                        mCropWrapper.isInBorder(mCropBorder.rightTop);

                        System.out.println("leftBottom");
                        mCropWrapper.isInBorder(mCropBorder.leftBottom);

                        System.out.println("rightBottom");
                        mCropWrapper.isInBorder(mCropBorder.rightBottom);

                        break;
                    case ZOOM:

                        break;
                }

                mCurrentMode = ActionMode.NONE;
                invalidate();

                break;
        }

        return true;
    }

    private void handleZoomEvent(MotionEvent event) {
        if (mCropWrapper != null) {
            float newDistance = calculateDistance(event);

            float scale = newDistance / mOldDistance;

//            checkScalePoint();

            //TODO 缩放中心的问题
            if (mPreZoom * scale <= mMinZoom) {
                mCropWrapper.getMatrix().set(mPreMatrix);
                mCropWrapper.getMatrix().postScale(mMinZoom / mCropWrapper.getScaleFactor(), mMinZoom / mCropWrapper.getScaleFactor(),
                        mCropBorder.centerX(), mCropBorder.centerY());
//                mCropWrapper.getMatrix().postScale(mMinZoom / mCropWrapper.getScaleFactor(), mMinZoom / mCropWrapper.getScaleFactor(),
//                        mScalePoint.x,mScalePoint.y);
                return;
            }

            mCropWrapper.getMatrix().set(mPreMatrix);
            mCropWrapper.getMatrix().postScale(scale, scale,
                    mCropBorder.centerX(), mCropBorder.centerY());
//            mCropWrapper.getMatrix().postScale(scale,scale,
//                    mScalePoint.x,mScalePoint.y);

        }
    }

//    private void checkScalePoint() {
//        if ()
//    }


    private void handleDragEvent(MotionEvent event) {

        if (mCropWrapper != null) {
            mCropWrapper.getMatrix().set(mPreMatrix);
            mCropWrapper.getMatrix().postTranslate(event.getX() - mDownX, event.getY() - mDownY);
        }
    }


    //TODO
    public void rotate(int degrees) {
        mCropWrapper.setRotate(degrees);

        if (degrees > 0) {
            for (float i = degrees - 1; i <= degrees; i += 0.1) {
                rotate(i);
            }
        } else {
            for (float i = degrees + 1; i >= degrees; i -= 0.1) {
                rotate(i);
            }
        }

        mMinZoom = mCropWrapper.getScaleFactor();

        if (mCropBorder != null) {

            mCropBorderWidth = mCropBorder.width();
            mCropBorderHeight = mCropBorder.height();

            mDiagonal = (float) sqrt(pow(mCropBorderWidth, 2) + pow(mCropBorderHeight, 2));

            if (mCropBorderWidth > mCropBorderHeight) {
                mTempAlpha = atan(mCropBorderHeight / mCropBorderWidth);
                mTempScale = (float) (mDiagonal * sin(toRadians(abs(degrees)) + mTempAlpha) / mCropBorderHeight);
                float hh = mTempScale * mCropBorderHeight;
                float ww = mTempScale * mCropBorderWidth;
                double temp = (hh * sin(toRadians(abs(degrees))) + ww * cos(toRadians(abs(degrees))));
                mMinZoom = (float) (temp / mCropWrapper.getWidth());
            } else {
                mTempAlpha = atan(mCropBorderWidth / mCropBorderHeight);
                mTempScale = (float) (mDiagonal * sin(toRadians(abs(degrees)) + mTempAlpha) / mCropBorderWidth);

                float hh = mTempScale * mCropBorderHeight;
                float ww = mTempScale * mCropBorderWidth;
                double temp = (ww * sin(toRadians(abs(degrees))) + hh * cos(toRadians(abs(degrees))));
                mMinZoom = (float) (temp / mCropWrapper.getHeight());
            }

        }

//        for (PointF pointF:mCropBorder.getCornerPoints()){
//            Log.d(TAG, "rotate: borderCorner->"+pointF.toString());
//        }
//
//        for (float f:mCropWrapper.getMappedCornerPoints()){
//            Log.d(TAG, "rotate: wrapperCorner->"+f);
//        }


    }

    private void rotate(float degrees) {
        if (mCropWrapper == null) return;

        if (mCropBorder != null) {
            mCropWrapper.getMatrix().set(mPreSizeMatrix);
            mCropWrapper.getMatrix().postRotate(degrees, mCropBorder.centerX(), mCropBorder.centerY());

            mCropBorderWidth = mCropBorder.width();
            mCropBorderHeight = mCropBorder.height();

            mDiagonal = (float) sqrt(pow(mCropBorderWidth, 2) + pow(mCropBorderHeight, 2));

            if (mCropBorderWidth > mCropBorderHeight) {
                mTempAlpha = atan(mCropBorderHeight / mCropBorderWidth);
                mTempScale = (float) (mDiagonal * sin(toRadians(abs(degrees)) + mTempAlpha) / mCropBorderHeight);
            } else {
                mTempAlpha = atan(mCropBorderWidth / mCropBorderHeight);
                mTempScale = (float) (mDiagonal * sin(toRadians(abs(degrees)) + mTempAlpha) / mCropBorderWidth);
            }

            mCropWrapper.getMatrix().postScale(mTempScale, mTempScale, mCropBorder.centerX(), mCropBorder.centerY());

        }


        invalidate();
    }

    private PointF calculateMidPoint(MotionEvent event) {
        if (event == null || event.getPointerCount() < 2) return new PointF();
        float x = (event.getX(0) + event.getX(1)) / 2;
        float y = (event.getY(0) + event.getY(1)) / 2;
        return new PointF(x, y);
    }

    //计算两点间的距离
    private float calculateDistance(MotionEvent event) {
        if (event == null || event.getPointerCount() < 2) return 0f;
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        return (float) sqrt(x * x + y * y);
    }

    public void setCropBitmap(Bitmap bitmap) {
        //TODO Matrix create
        mCropWrapper = new CropWrapper(new BitmapDrawable(getResources(), bitmap), new Matrix());

        invalidate();
    }

    public void setRotateState(boolean rotateState) {
        mIsRotateState = rotateState;
        invalidate();
    }

    public enum ActionMode {
        NONE,
        DRAG,
        ZOOM,
    }
}
