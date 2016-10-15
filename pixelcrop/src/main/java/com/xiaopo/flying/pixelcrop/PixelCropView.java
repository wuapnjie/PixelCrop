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

import static java.lang.Math.atan;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

/**
 * Created by snowbean on 16-10-14.
 */
public class PixelCropView extends View {
    private static final String TAG = "PixelCropView";

    public enum ActionMode {
        NONE,
        DRAG,
        ZOOM,
    }

    private CropWrapper mCropWrapper;
    private int mBorderOffset = 50;

    private Paint mBorderPaint;

    private Border mCropBorder;

    private float mDownX;
    private float mDownY;
    private float mOldDistance;
    private PointF mMidPoint;

    private Matrix mPreMatrix;

    private ActionMode mCurrentMode;

    private boolean mIsRotateState;
    private float mMinScale;

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
        mMidPoint = new PointF();
    }

    public PixelCropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PixelCropView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
                mMidPoint = calculateMidPoint(event);

                if (event.getPointerCount() == 2) {
                    mCurrentMode = ActionMode.ZOOM;
                }

                break;

            case MotionEvent.ACTION_MOVE:
                switch (mCurrentMode) {
                    case NONE:
                        break;
                    case DRAG:
                        handleDragEvent(event);
                        break;
                    case ZOOM:
                        handleZoomEvent(event);
                        break;
                }
                invalidate();
                break;

            case MotionEvent.ACTION_POINTER_UP:
                mCurrentMode = ActionMode.NONE;
                break;

            case MotionEvent.ACTION_UP:
                mCurrentMode = ActionMode.NONE;
                System.out.println(mCropWrapper.getMappedBound().contains(mCropBorder.lineLeft.start.x, mCropBorder.lineLeft.start.y));
                mPreMatrix.set(mCropWrapper.getMatrix());
                invalidate();
                break;
        }

        return true;
    }

    private void handleZoomEvent(MotionEvent event) {

        if (mCropWrapper != null) {
            float newDistance = calculateDistance(event);
            mCropWrapper.getMatrix().set(mPreMatrix);
            mCropWrapper.getMatrix().postScale(newDistance / mOldDistance, newDistance / mOldDistance,
                    mMidPoint.x, mMidPoint.y);
        }
    }

    private void handleDragEvent(MotionEvent event) {
        if (mCropWrapper != null) {
            mCropWrapper.getMatrix().set(mPreMatrix);
            mCropWrapper.getMatrix().postTranslate(event.getX() - mDownX, event.getY() - mDownY);
        }
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

            mMinScale = scaleX;

            Log.d(TAG, "onSizeChanged: scaleX->" + scaleX + ",scaleY->" + scaleY);

            mCropWrapper.getMatrix()
                    .postScale(scaleX, scaleY, mCropWrapper.getMappedCenterPoint().x, mCropWrapper.getMappedCenterPoint().y);

            mPreMatrix.set(mCropWrapper.getMatrix());

            System.out.println(mCropWrapper.getMappedBound().contains(mCropBorder.lineLeft.start.x, mCropBorder.lineLeft.start.y));
//            System.out.println(mCropWrapper.canMoveLeft());
//            System.out.println(mCropWrapper.canMoveRight());
//            System.out.println(mCropWrapper.canMoveTop());
//            System.out.println(mCropWrapper.canMoveBottom());

            invalidate();
        }

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

    //TODO
    public void rotate(int degrees) {
        for (float i = degrees - 1; i <= degrees; i += 0.05) {
            rotate(i);
        }
    }

    private void rotate(float degrees) {
        if (mCropWrapper == null) return;

        if (mCropBorder != null) {
            mCropWrapper.getMatrix().set(mPreMatrix);
            mCropWrapper.getMatrix().postRotate(degrees, mCropBorder.centerX(), mCropBorder.centerY());

            float bw = mCropBorder.width();
            float bh = mCropBorder.height();

            double alpha;
            float scale;
            float diagonal = (float) sqrt(pow(bw, 2) + pow(bh, 2));

            if (bw > bh) {
                alpha = atan(bh / bw);
                scale = (float) (diagonal * sin(toRadians(degrees) + alpha) / bh);
            } else {
                alpha = atan(bw / bh);
                scale = (float) (diagonal * sin(toRadians(degrees) + alpha) / bw);
            }

            mCropWrapper.getMatrix().postScale(scale, scale, mCropBorder.centerX(), mCropBorder.centerY());
        }

        invalidate();
    }
}
