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
    private int mPointMargin;

    private boolean mScrollStarted;
    private int mTotalScrollDistance;

    private Path mIndicatorPath = new Path();

    private int mCurrentDegrees = 0;
    private static final String DEGREE = "°";
    private int mPointCount = 50;

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
        mPointMargin = w / mPointCount;

        mBaseLine = (h - mFontMetrics.bottom + mFontMetrics.top) / 2 - mFontMetrics.top;

        mIndicatorPath.moveTo(w / 2, h / 2 + mFontMetrics.top / 2 - 15);
        mIndicatorPath.rLineTo(-8, -8);
        mIndicatorPath.rLineTo(16, 0);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastTouchedPosition = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                if (mScrollingListener != null) {
                    mScrollStarted = false;
                    mScrollingListener.onScrollEnd();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float distance = event.getX() - mLastTouchedPosition;
                if (mCurrentDegrees >= 45 && distance < 0) {
                    mCurrentDegrees = 45;
                    break;
                }
                if (mCurrentDegrees <= -45 && distance > 0) {
                    mCurrentDegrees = -45;
                    break;
                }
                if (distance != 0) {
                    if (!mScrollStarted) {
                        mScrollStarted = true;
                        if (mScrollingListener != null) {
                            mScrollingListener.onScrollStart();
                        }
                    }
                    onScrollEvent(event, distance);
                }
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.drawRect(0, 0, getWidth(), getHeight(), mTextPaint);
//        canvas.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight(), mTextPaint);
//        canvas.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2, mTextPaint);

        canvas.getClipBounds(mCanvasClipBounds);

        int linesCount = mCanvasClipBounds.width() / mPointMargin;
        int deltaX = (mTotalScrollDistance) % (mPointMargin);
//
//        Log.d(TAG, "onDraw: delta space-->" + (mTotalScrollDistance) / (mPointMargin));
//        Log.d(TAG, "onDraw: deltaX-->" + deltaX);
//        System.out.println("lineCount->" + linesCount);

        mPointPaint.setColor(Color.WHITE);
        for (int i = 0; i < linesCount; i++) {
//            if (i < (linesCount / 4)) {
//                mPointPaint.setAlpha((int) (255 * (i / (float) (linesCount / 4))));
//            } else if (i > (linesCount * 3 / 4)) {
//                mPointPaint.setAlpha((int) (255 * ((linesCount - i) / (float) (linesCount / 4))));
//            } else {
//                mPointPaint.setAlpha(255);
//            }

            canvas.drawPoint(mCanvasClipBounds.left + i * mPointMargin,
                    mCanvasClipBounds.centerY(), mPointPaint);
        }

        if (mCurrentDegrees != 0) {
            canvas.drawText("0°", getWidth() / 2 - mTextWidths[0] / 2 - mCurrentDegrees / 2 * mPointMargin,
                    getHeight() / 2 - 10,
                    mTextPaint);
        }

        canvas.drawText("15°", getWidth() / 2 + mPointMargin * 15 / 2 - mTextWidths[0] / 2 * 3 - mCurrentDegrees / 2 * mPointMargin,
                getHeight() / 2 - 10,
                mTextPaint);
        canvas.drawText("30°", getWidth() / 2 + mPointMargin * 30 / 2 - mTextWidths[0] / 2 * 3 - mCurrentDegrees / 2 * mPointMargin,
                getHeight() / 2 - 10,
                mTextPaint);
        canvas.drawText("45°", getWidth() / 2 + mPointMargin * 45 / 2 - mTextWidths[0] / 2 * 3 - mCurrentDegrees / 2 * mPointMargin,
                getHeight() / 2 - 10,
                mTextPaint);
        canvas.drawText("60°", getWidth() / 2 + mPointMargin * 60 / 2 - mTextWidths[0] / 2 * 3 - mCurrentDegrees / 2 * mPointMargin,
                getHeight() / 2 - 10,
                mTextPaint);
        canvas.drawText("75°", getWidth() / 2 + mPointMargin * 75 / 2 - mTextWidths[0] / 2 * 3 - mCurrentDegrees / 2 * mPointMargin,
                getHeight() / 2 - 10,
                mTextPaint);
        canvas.drawText("90°", getWidth() / 2 + mPointMargin * 90 / 2 - mTextWidths[0] / 2 * 3 - mCurrentDegrees / 2 * mPointMargin,
                getHeight() / 2 - 10,
                mTextPaint);

        canvas.drawText("-15°", getWidth() / 2 - mPointMargin * 15 / 2 - mTextWidths[0] / 2 * 2 - mCurrentDegrees / 2 * mPointMargin,
                getHeight() / 2 - 10,
                mTextPaint);
        canvas.drawText("-30°", getWidth() / 2 - mPointMargin * 30 / 2 - mTextWidths[0] / 2 * 2 - mCurrentDegrees / 2 * mPointMargin,
                getHeight() / 2 - 10,
                mTextPaint);
        canvas.drawText("-45°", getWidth() / 2 - mPointMargin * 45 / 2 - mTextWidths[0] / 2 * 2 - mCurrentDegrees / 2 * mPointMargin,
                getHeight() / 2 - 10,
                mTextPaint);
        canvas.drawText("-60°", getWidth() / 2 - mPointMargin * 60 / 2 - mTextWidths[0] / 2 * 2 - mCurrentDegrees / 2 * mPointMargin,
                getHeight() / 2 - 10,
                mTextPaint);
        canvas.drawText("-75°", getWidth() / 2 - mPointMargin * 75 / 2 - mTextWidths[0] / 2 * 2 - mCurrentDegrees / 2 * mPointMargin,
                getHeight() / 2 - 10,
                mTextPaint);
        canvas.drawText("-90°", getWidth() / 2 - mPointMargin * 90 / 2 - mTextWidths[0] / 2 * 2 - mCurrentDegrees / 2 * mPointMargin,
                getHeight() / 2 - 10,
                mTextPaint);


        canvas.drawCircle(getWidth() / 2, getHeight() / 2, 36, mCirclePaint);


        mTextPaint.setTextSize(28f);
        if (mCurrentDegrees >= 10) {
            canvas.drawText(mCurrentDegrees + DEGREE, getWidth() / 2 - mTextWidths[0], mBaseLine, mTextPaint);
        } else if (mCurrentDegrees <= -10) {
            canvas.drawText(mCurrentDegrees + DEGREE, getWidth() / 2 - mTextWidths[0] / 2 * 3, mBaseLine, mTextPaint);
        } else if (mCurrentDegrees < 0) {
            canvas.drawText(mCurrentDegrees + DEGREE, getWidth() / 2 - mTextWidths[0], mBaseLine, mTextPaint);
        } else {
            canvas.drawText(mCurrentDegrees + DEGREE, getWidth() / 2 - mTextWidths[0] / 2, mBaseLine, mTextPaint);
        }

        mTextPaint.setTextSize(24f);
        //画中心三角
        mCirclePaint.setColor(Color.WHITE);
        canvas.drawPath(mIndicatorPath, mCirclePaint);
        mCirclePaint.setColor(Color.BLACK);

    }

    private void onScrollEvent(MotionEvent event, float distance) {
        mTotalScrollDistance -= distance;
        postInvalidate();
        mLastTouchedPosition = event.getX();
        if (mScrollingListener != null) {
            mScrollingListener.onScroll(-distance, mTotalScrollDistance);
            mCurrentDegrees = (int) (mTotalScrollDistance * 2.1f) / mPointMargin;
        }
    }

    public void setScrollingListener(ScrollingListener scrollingListener) {
        mScrollingListener = scrollingListener;
    }

    public interface ScrollingListener {

        void onScrollStart();

        void onScroll(float delta, float totalDistance);

        void onScrollEnd();
    }

}
