package com.xiaopo.flying.pixelcrop;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * //TODO 支持wrap_content和padding
 * Created by snowbean on 16-10-24.
 */
public class DegreeSeekBar extends View {
    private static final String TAG = "DegreeSeekBar";
    private Paint mTextPaint;
    private Paint mCirclePaint;
    private Paint.FontMetricsInt mFontMetrics;
    private int mBaseLine;
    private float[] mTextWidths = new float[1];

    private final Rect mCanvasClipBounds = new Rect();

    private ScrollingListener mScrollingListener;
    private float mLastTouchedPosition;

    private Paint mPointPaint;
    private float mPointMargin;

    private boolean mScrollStarted;
    private int mTotalScrollDistance;

    private Path mIndicatorPath = new Path();

    private int mCurrentDegrees = 0;
    private static final String DEGREE = "°";
    private int mPointCount = 51;

    public DegreeSeekBar(Context context) {
        this(context, null);
    }

    public DegreeSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DegreeSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DegreeSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init() {

        mPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointPaint.setStyle(Paint.Style.STROKE);
        mPointPaint.setStrokeWidth(2);

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setStyle(Paint.Style.STROKE);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(24f);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setAlpha(100);

        mFontMetrics = mTextPaint.getFontMetricsInt();

        mTextWidths = new float[1];
        mTextPaint.getTextWidths("0", mTextWidths);

        mCirclePaint = new Paint();
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setAlpha(255);
        mCirclePaint.setAntiAlias(true);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mPointMargin = (float) w / mPointCount;

        mBaseLine = (h - mFontMetrics.bottom + mFontMetrics.top) / 2 - mFontMetrics.top;

        mIndicatorPath.moveTo(w / 2, h / 2 + mFontMetrics.top / 2 - 18);
        mIndicatorPath.rLineTo(-8, -8);
        mIndicatorPath.rLineTo(16, 0);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastTouchedPosition = event.getX();
                if (!mScrollStarted) {
                    mScrollStarted = true;
                    if (mScrollingListener != null) {
                        mScrollingListener.onScrollStart();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mScrollingListener != null) {
                    mScrollStarted = false;
                    mScrollingListener.onScrollEnd();
                }
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                float distance = event.getX() - mLastTouchedPosition;
                if (mCurrentDegrees >= 45 && distance < 0) {
                    mCurrentDegrees = 45;
                    invalidate();
                    break;
                }
                if (mCurrentDegrees <= -45 && distance > 0) {
                    mCurrentDegrees = -45;
                    invalidate();
                    break;
                }
                if (distance != 0) {
                    onScrollEvent(event, distance);
                }
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.getClipBounds(mCanvasClipBounds);

        int zeroIndex = mPointCount / 2 + (0 - mCurrentDegrees) / 2;
        mPointPaint.setColor(Color.WHITE);
        for (int i = 0; i < mPointCount; i++) {

            if (i > zeroIndex - 22 && i < zeroIndex + 22 && mScrollStarted) {
                mPointPaint.setAlpha(255);
            } else {
                mPointPaint.setAlpha(100);
            }

            if (i > mPointCount / 2 - 8 && i < mPointCount / 2 + 8
                    && i > zeroIndex - 22 && i < zeroIndex + 22) {
                if (mScrollStarted)
                    mPointPaint.setAlpha(Math.abs(mPointCount / 2 - i) * 255 / 8);
                else
                    mPointPaint.setAlpha(Math.abs(mPointCount / 2 - i) * 100 / 8);
            }

            canvas.drawPoint(mCanvasClipBounds.centerX() + (i - mPointCount / 2) * mPointMargin,
                    mCanvasClipBounds.centerY(), mPointPaint);

            if (mCurrentDegrees != 0 && i == zeroIndex) {
                if (mScrollStarted) {
                    mTextPaint.setAlpha(255);
                } else {
                    mTextPaint.setAlpha(192);
                }
                mPointPaint.setStrokeWidth(4);
                canvas.drawPoint((mCanvasClipBounds.centerX() + (i - mPointCount / 2) * mPointMargin),
                        mCanvasClipBounds.centerY(), mPointPaint);
                mPointPaint.setStrokeWidth(2);
                mTextPaint.setAlpha(100);
            }
        }

        drawDegreeText(0, canvas, true);
        drawDegreeText(15, canvas, true);
        drawDegreeText(30, canvas, true);
        drawDegreeText(45, canvas, true);
        drawDegreeText(-15, canvas, true);
        drawDegreeText(-30, canvas, true);
        drawDegreeText(-45, canvas, true);

        drawDegreeText(60, canvas, false);
        drawDegreeText(75, canvas, false);
        drawDegreeText(90, canvas, false);
        drawDegreeText(-60, canvas, false);
        drawDegreeText(-76, canvas, false);
        drawDegreeText(-90, canvas, false);

        mTextPaint.setTextSize(28f);
        mTextPaint.setAlpha(255);
        if (mCurrentDegrees >= 10) {
            canvas.drawText(mCurrentDegrees + DEGREE, getWidth() / 2 - mTextWidths[0], mBaseLine, mTextPaint);
        } else if (mCurrentDegrees <= -10) {
            canvas.drawText(mCurrentDegrees + DEGREE, getWidth() / 2 - mTextWidths[0] / 2 * 3, mBaseLine, mTextPaint);
        } else if (mCurrentDegrees < 0) {
            canvas.drawText(mCurrentDegrees + DEGREE, getWidth() / 2 - mTextWidths[0], mBaseLine, mTextPaint);
        } else {
            canvas.drawText(mCurrentDegrees + DEGREE, getWidth() / 2 - mTextWidths[0] / 2, mBaseLine, mTextPaint);
        }
        mTextPaint.setAlpha(100);
        mTextPaint.setTextSize(24f);
        //画中心三角
        mCirclePaint.setColor(Color.WHITE);
        canvas.drawPath(mIndicatorPath, mCirclePaint);
        mCirclePaint.setColor(Color.BLACK);

    }

    private void drawDegreeText(int degrees, Canvas canvas, boolean canReach) {
        if (canReach) {
            if (mScrollStarted) {
                mTextPaint.setAlpha(Math.min(255, Math.abs(degrees - mCurrentDegrees) * 255 / 15));
                if (Math.abs(degrees - mCurrentDegrees) <= 7) {
                    mTextPaint.setAlpha(0);
                }
            } else {
                mTextPaint.setAlpha(100);
                if (Math.abs(degrees - mCurrentDegrees) <= 7) {
                    mTextPaint.setAlpha(0);
                }
            }
        } else {
            mTextPaint.setAlpha(100);
        }
        if (degrees == 0) {
            canvas.drawText("0°", getWidth() / 2 - mTextWidths[0] / 2 - mCurrentDegrees / 2 * mPointMargin,
                    getHeight() / 2 - 10,
                    mTextPaint);
        } else {
            canvas.drawText(degrees + DEGREE,
                    getWidth() / 2 + mPointMargin * degrees / 2 - mTextWidths[0] / 2 * 3 - mCurrentDegrees / 2 * mPointMargin,
                    getHeight() / 2 - 10,
                    mTextPaint);
        }
    }


    private void onScrollEvent(MotionEvent event, float distance) {
        mTotalScrollDistance -= distance;
        postInvalidate();
        mLastTouchedPosition = event.getX();
        if (mScrollingListener != null) {
            mCurrentDegrees = (int) ((mTotalScrollDistance * 2.1f) / mPointMargin);
            mScrollingListener.onScroll(mCurrentDegrees);
        }
    }

    public void setScrollingListener(ScrollingListener scrollingListener) {
        mScrollingListener = scrollingListener;
    }

    public interface ScrollingListener {

        void onScrollStart();

        void onScroll(int currentDegrees);

        void onScrollEnd();
    }

}
