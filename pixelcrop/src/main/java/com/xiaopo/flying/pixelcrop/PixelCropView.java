package com.xiaopo.flying.pixelcrop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.yalantis.ucrop.task.BitmapCropCallback;
import com.yalantis.ucrop.task.BitmapCropTask;
import com.yalantis.ucrop.task.BitmapLoadCallback;
import com.yalantis.ucrop.task.BitmapLoadUtils;
import com.yalantis.ucrop.task.CropParameters;
import com.yalantis.ucrop.task.ExifInfo;
import com.yalantis.ucrop.task.ImageState;

import static java.lang.Math.sqrt;

/**
 * Pixel Crop View
 * Created by snowbean on 16-10-14.
 */
public class PixelCropView extends View {
    private static final String TAG = "PixelCropView";

    public enum ActionMode {
        NONE,
        DRAG,
        ZOOM,
        MOVE_LINE
    }

    private CropWrapper mCropWrapper;
    private int mBorderOffset = 30;
    private int mBorderColor = Color.parseColor("#ddcbcbcb");
    private Paint mBorderPaint;
    private Border mOuterBorder;
    private Border mCropBorder;

    private float mDownX;
    private float mDownY;

    private PointF mDownCenterPoint;

    private float mOldDistance;

    //缩放点
    private PointF mScalePoint;
    private Matrix mPreMatrix;
    private Matrix mPreSizeMatrix;

    //触摸事件开始时的缩放比
    private float mPreZoom;
    private ActionMode mCurrentMode;
    private boolean mIsRotateState;
    private float mRotateDegree;
    //不同旋转角度下的最小缩放比
    private float mMinScale;
    private int mMaxBitmapSize;

    private Line mHandlingLine;

    public PixelCropView(Context context) {
        this(context, null, 0);
    }

    public PixelCropView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PixelCropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBorderPaint = new Paint();
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStrokeWidth(3);

        mPreMatrix = new Matrix();
        mPreSizeMatrix = new Matrix();

        mScalePoint = new PointF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mOuterBorder = new Border(new RectF(0, 0, w, w * 6 / 5));

        mCropBorder = new Border(new RectF(mBorderOffset,
                mBorderOffset,
                w - mBorderOffset,
                w - mBorderOffset));

        setUpCropBorderLineInfo();

        if (mCropWrapper != null) {
            setUpDefaultCropBorder();

            //使图片移动缩放至剪裁框内
            letWrapperToFitBorder();

            mPreMatrix.set(mCropWrapper.getMatrix());
            mPreSizeMatrix.set(mCropWrapper.getMatrix());
        }

    }

    private void setUpCropBorderLineInfo() {
        mCropBorder.lineTop.setLowerLine(mOuterBorder.lineTop);
        mCropBorder.lineTop.setUpperLine(mCropBorder.lineBottom);

        mCropBorder.lineBottom.setLowerLine(mCropBorder.lineTop);
        mCropBorder.lineBottom.setUpperLine(mOuterBorder.lineBottom);

        mCropBorder.lineLeft.setLowerLine(mOuterBorder.lineLeft);
        mCropBorder.lineLeft.setUpperLine(mCropBorder.lineRight);

        mCropBorder.lineRight.setLowerLine(mCropBorder.lineLeft);
        mCropBorder.lineRight.setUpperLine(mOuterBorder.lineRight);

    }

    //根据图片的长宽比确定剪裁边框的大小
    private void setUpDefaultCropBorder() {
        if (mCropWrapper != null) {
            int width = mCropWrapper.getWidth();
            int height = mCropWrapper.getHeight();

            float w = mOuterBorder.width();
            float h = mOuterBorder.height();

            if (width >= height) {
                float bWidth = w - 2 * mBorderOffset;
                float bHeight = bWidth * height / width;
                mCropBorder.setBaseRect(new RectF(
                        mBorderOffset,
                        (h - bHeight) / 2,
                        w - mBorderOffset,
                        (h + bHeight) / 2
                ));
            } else {
                float bHeight = h - 2 * mBorderOffset;
                float bWidth = bHeight * width / height;
                mCropBorder.setBaseRect(new RectF(
                        (w - bWidth) / 2,
                        mBorderOffset,
                        (w + bWidth) / 2,
                        h - mBorderOffset
                ));
            }
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画边框和阴影
        if (mCropBorder != null && mCropWrapper != null) {

            //半透明的完整图片
            mCropWrapper.draw(canvas, 100);

            //图片高亮部分
            canvas.save();
            canvas.clipRect(mCropBorder.getRect());
            mCropWrapper.draw(canvas);
            canvas.restore();

            //边框
            canvas.drawRect(mCropBorder.getRect(), mBorderPaint);

            //网格线
            mBorderPaint.setStrokeWidth(1);
            if (mIsRotateState) {
                mCropBorder.drawGrid(canvas, mBorderPaint, 9, 9);
            } else if (mCurrentMode == ActionMode.DRAG || mCurrentMode == ActionMode.ZOOM) {
                mCropBorder.drawGrid(canvas, mBorderPaint, 3, 3);
            }

            //画边框
            mBorderPaint.setStrokeWidth(5);
            mBorderPaint.setColor(Color.WHITE);
            mCropBorder.drawCorner(canvas, mBorderPaint);

            mBorderPaint.setColor(mBorderColor);
            mBorderPaint.setStrokeWidth(3);
        }


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mCropWrapper == null) return super.onTouchEvent(event);
        final int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();

                mHandlingLine = findHandlingLine();

                if (mHandlingLine != null) {
                    mCurrentMode = ActionMode.MOVE_LINE;
                } else {
                    if (!mIsRotateState) {
                        mCurrentMode = ActionMode.DRAG;
                    } else {
                        mCurrentMode = ActionMode.NONE;
                    }
                }

                if (mCropWrapper != null) {
                    mPreMatrix.set(mCropWrapper.getMatrix());
                    mDownCenterPoint = mCropWrapper.getMappedCenterPoint();
                }


                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mOldDistance = calculateDistance(event);
                mScalePoint = calculateMidPoint(event);

                mPreZoom = mCropWrapper.getScaleFactor();

                if (event.getPointerCount() == 2 && !mIsRotateState) {
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
                    case MOVE_LINE:
                        handleMoveLineEvent(event);
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
                mCurrentMode = ActionMode.NONE;
                mPreMatrix.set(mCropWrapper.getMatrix());
                invalidate();

                PointF currentCenterPoint = mCropWrapper.getMappedCenterPoint();
                mPreSizeMatrix.postTranslate(currentCenterPoint.x - mDownCenterPoint.x, currentCenterPoint.y - mDownCenterPoint.y);

                break;
        }

        return true;
    }

    private void handleMoveLineEvent(MotionEvent event) {
        if (mHandlingLine == null) {
            return;
        }

        if (mHandlingLine.getDirection() == Line.Direction.HORIZONTAL) {
            mHandlingLine.moveTo(event.getY(), mBorderOffset);
        } else if (mHandlingLine.getDirection() == Line.Direction.VERTICAL) {
            mHandlingLine.moveTo(event.getX(), mBorderOffset);
        }

        letBorderInImage(event.getX(), event.getY());
    }

    private void letBorderInImage(float x, float y) {
        if (mHandlingLine == null) return;
        if (!isImageContainsBorder()) {
            Log.d(TAG, "letBorderInImage: -->");
            final float[] imageIndents = CropUtil.calculateImageIndents(mCropWrapper, mCropBorder);
            float deltaX = -(imageIndents[0] + imageIndents[2]);
            float deltaY = -(imageIndents[1] + imageIndents[3]);

            if (mHandlingLine.getDirection() == Line.Direction.HORIZONTAL) {
                mHandlingLine.moveTo(y - deltaY, mBorderOffset);
            } else {
                mHandlingLine.moveTo(x - deltaX, mBorderOffset);
            }
        }
    }

    private void handleZoomEvent(MotionEvent event) {
        if (mCropWrapper != null) {
            float newDistance = calculateDistance(event);

            float scale = newDistance / mOldDistance;

            if (mPreZoom * scale <= mMinScale) {
                postScale(mMinScale / mCropWrapper.getScaleFactor(),
                        mMinScale / mCropWrapper.getScaleFactor(),
                        mScalePoint.x,
                        mScalePoint.y,
                        null);

                letImageContainsBorder(0, 0, null);

            } else {
                postScale(scale,
                        scale,
                        mScalePoint.x,
                        mScalePoint.y,
                        mPreMatrix);

                if (scale < 1f) {
                    letImageContainsBorder(0, 0, null);
                }
            }
        }
    }


    private void handleDragEvent(MotionEvent event) {

        postTranslate(event.getX() - mDownX,
                event.getY() - mDownY,
                mPreMatrix);

        letImageContainsBorder(event.getX() - mDownX, event.getY() - mDownY, mPreMatrix);

    }

    private Line findHandlingLine() {
        if (mCropBorder == null) return null;
        for (Line line : mCropBorder.getLines()) {
            if (line.contains(mDownX, mDownY, 30)) {
                return line;
            }
        }
        return null;
    }

    private void letImageContainsBorder(float preX, float preY, Matrix preMatrix) {
        if (!isImageContainsBorder()) {
            final float[] imageIndents = CropUtil.calculateImageIndents(mCropWrapper, mCropBorder);
            float deltaX = -(imageIndents[0] + imageIndents[2]);
            float deltaY = -(imageIndents[1] + imageIndents[3]);

            postTranslate(preX + deltaX,
                    preY + deltaY,
                    preMatrix);
        }
    }

    private boolean isImageContainsBorder() {
        return CropUtil.judgeIsImageContainsBorder(mCropWrapper, mCropBorder, mRotateDegree);
    }

    public void rotate(int degrees) {
        if (mCropWrapper == null) return;

        mRotateDegree = degrees;

        if (degrees > 0) {
            for (float i = degrees - 1; i <= degrees; i += 0.2) {
                rotate(i);
            }
        } else {
            for (float i = degrees + 1; i >= degrees; i -= 0.2) {
                rotate(i);
            }
        }

        letImageContainsBorder(0, 0, null);

        mMinScale = CropUtil.calculateMinScale(mCropWrapper, mCropBorder, degrees);

    }


    private void rotate(float degrees) {
        if (mCropWrapper == null) return;

        if (mCropBorder != null) {

            postRotate(degrees,
                    mCropBorder.centerX(),
                    mCropBorder.centerY(),
                    mPreSizeMatrix);


            float tempScale = CropUtil.calculateRotateScale(mCropBorder.width(), mCropBorder.height(), degrees);

            postScale(tempScale,
                    tempScale,
                    mCropBorder.centerX(),
                    mCropBorder.centerY(),
                    null);

        }

        invalidate();
    }

    private PointF calculateMidPoint(MotionEvent event) {
        if (event == null || event.getPointerCount() < 2)
            return new PointF();
        float x = (event.getX(0) + event.getX(1)) / 2;
        float y = (event.getY(0) + event.getY(1)) / 2;
        return new PointF(x, y);
    }

    //计算两点间的距离
    private float calculateDistance(MotionEvent event) {
        if (event == null || event.getPointerCount() < 2)
            return 0f;
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        return (float) sqrt(x * x + y * y);
    }

    public int getMaxBitmapSize() {
        if (mMaxBitmapSize <= 0) {
            mMaxBitmapSize = BitmapLoadUtils.calculateMaxBitmapSize(getContext());
        }
        return mMaxBitmapSize;
    }

    public void setRotateState(boolean rotateState) {
        mIsRotateState = rotateState;
        invalidate();
    }


    private void letWrapperToFitBorder() {
        float offsetX = mOuterBorder.centerX() - mCropWrapper.getCenterPoint().x;
        float offsetY = mOuterBorder.centerY() - mCropWrapper.getCenterPoint().y;

        if (mCropWrapper != null) {
            mCropWrapper.getMatrix()
                    .setTranslate(offsetX, offsetY);
        }

        if (mCropBorder != null) {
            float scaleX = mCropBorder.width() / mCropWrapper.getWidth();
            float scaleY = mCropBorder.height() / mCropWrapper.getHeight();

            mMinScale = scaleX;

            mCropWrapper.getMatrix()
                    .postScale(scaleX,
                            scaleY,
                            mCropWrapper.getMappedCenterPoint().x,
                            mCropWrapper.getMappedCenterPoint().y);
        }

        invalidate();
    }

    private void postScale(float sx, float sy, float px, float py, Matrix preMatrix) {
        if (mCropWrapper == null) return;
        if (preMatrix != null) {
            mCropWrapper.getMatrix().set(preMatrix);
        }
        mCropWrapper.getMatrix().postScale(sx, sy, px, py);
    }

    private void postTranslate(float x, float y, Matrix preMatrix) {
        if (mCropWrapper == null) return;
        if (preMatrix != null) {
            mCropWrapper.getMatrix().set(preMatrix);
        }
        mCropWrapper.getMatrix().postTranslate(x, y);
    }

    private void postRotate(float rotateDegrees, float px, float py, Matrix preMatrix) {
        if (mCropWrapper == null) return;
        if (preMatrix != null) {
            mCropWrapper.getMatrix().set(preMatrix);
        }
        mCropWrapper.getMatrix().postRotate(rotateDegrees, px, py);
    }


    /**
     * Cancels all current animations and sets image to fill crop area (without animation).
     * Then creates and executes {@link BitmapCropTask} with proper parameters.
     */
    public void cropAndSaveImage(@NonNull Bitmap.CompressFormat compressFormat, int compressQuality,
                                 @Nullable BitmapCropCallback cropCallback) {
        if (mCropWrapper == null && cropCallback != null) {
            cropCallback.onCropFailure(new NullPointerException("The CropWrapper is null"));
            return;
        }

        letImageContainsBorder(0, 0, null);

        final ImageState imageState = new ImageState(
                mCropBorder.getRect(), CropUtil.trapToRect(mCropWrapper.getMappedBoundPoints()),
                mCropWrapper.getCurrentScale(), mCropWrapper.getCurrentAngle());

        final CropParameters cropParameters = new CropParameters(
                mMaxBitmapSize, mMaxBitmapSize,
                compressFormat, compressQuality,
                mCropWrapper.getInputPath(), mCropWrapper.getOutputPath(), mCropWrapper.getExifInfo());

        new BitmapCropTask(((BitmapDrawable) mCropWrapper.getDrawable()).getBitmap(),
                imageState,
                cropParameters,
                cropCallback)
                .execute();
    }


    public void setCropUri(Uri imageUri, Uri outputUri) {
        int maxBitmapSize = getMaxBitmapSize();

        BitmapLoadUtils.decodeBitmapInBackground(getContext(), imageUri, outputUri, maxBitmapSize, maxBitmapSize,
                new BitmapLoadCallback() {

                    @Override
                    public void onBitmapLoaded(@NonNull Bitmap bitmap, @NonNull ExifInfo exifInfo, @NonNull String imageInputPath, @Nullable String imageOutputPath) {

                        mCropWrapper = new CropWrapper(new BitmapDrawable(getResources(), bitmap), new Matrix(), imageInputPath, imageOutputPath, exifInfo);

                        setUpDefaultCropBorder();
                        setUpCropBorderLineInfo();

                        letWrapperToFitBorder();
                        mPreMatrix.set(mCropWrapper.getMatrix());
                        mPreSizeMatrix.set(mCropWrapper.getMatrix());

                        invalidate();
                    }

                    @Override
                    public void onFailure(@NonNull Exception bitmapWorkerException) {
                        Log.e(TAG, "onFailure: setImageUri", bitmapWorkerException);

                    }
                });
    }
}
