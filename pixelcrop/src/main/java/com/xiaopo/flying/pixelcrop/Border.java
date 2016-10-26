package com.xiaopo.flying.pixelcrop;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

import java.util.Arrays;
import java.util.List;

/**
 * the border to layout puzzle piece
 * <p>
 * each border consist of four lines : left,top,right,bottom
 *
 * @see Line
 * <p>
 * Created by snowbean on 16-8-13.
 */
class Border {
    Line lineLeft;
    Line lineTop;
    Line lineRight;
    Line lineBottom;

    PointF leftTop;
    PointF leftBottom;
    PointF rightTop;
    PointF rightBottom;

    private RectF mRect = new RectF();

    Border(Border src) {
        lineLeft = src.lineLeft;
        lineTop = src.lineTop;
        lineRight = src.lineRight;
        lineBottom = src.lineBottom;

        leftTop = src.lineLeft.start;
        leftBottom = src.lineLeft.end;
        rightTop = src.lineRight.start;
        rightBottom = src.lineRight.end;

        mRect.set(src.mRect);
    }

    Border(RectF baseRect) {
        setBaseRect(baseRect);
    }

    void setBaseRect(RectF baseRect) {

        PointF one = new PointF(baseRect.left, baseRect.top);
        PointF two = new PointF(baseRect.right, baseRect.top);
        PointF three = new PointF(baseRect.left, baseRect.bottom);
        PointF four = new PointF(baseRect.right, baseRect.bottom);

        lineLeft = new Line(one, three);
        lineTop = new Line(one, two);
        lineRight = new Line(two, four);
        lineBottom = new Line(three, four);

        leftTop = one;
        leftBottom = three;
        rightTop = two;
        rightBottom = four;

        lineLeft.setAttachLineStart(lineTop);
        lineLeft.setAttachLineEnd(lineBottom);

        lineTop.setAttachLineStart(lineLeft);
        lineTop.setAttachLineEnd(lineRight);

        lineRight.setAttachLineStart(lineTop);
        lineRight.setAttachLineEnd(lineBottom);

        lineBottom.setAttachLineStart(lineLeft);
        lineBottom.setAttachLineEnd(lineRight);

    }

    float width() {
        return lineRight.start.x - lineLeft.start.x;
    }


    float height() {
        return lineBottom.start.y - lineTop.start.y;
    }

    float left() {
        return lineLeft.start.x;
    }

    float top() {
        return lineTop.start.y;
    }

    float right() {
        return lineRight.start.x;
    }

    float bottom() {
        return lineBottom.start.y;
    }

    float centerX() {
        return (right() + left()) * 0.5f;
    }

    float centerY() {
        return (bottom() + top()) * 0.5f;
    }

    List<Line> getLines() {
        return Arrays.asList(lineLeft, lineTop, lineRight, lineBottom);
    }

    RectF getRect() {
        mRect.left = left();
        mRect.top = top();
        mRect.right = right();
        mRect.bottom = bottom();
        return mRect;
    }

    boolean contains(Line line) {
        return lineLeft == line || lineTop == line || lineRight == line || lineBottom == line;
    }

    public void drawGrid(Canvas canvas, Paint paint, int row, int column) {
        for (int i = 1; i < row; i++) {
            canvas.drawLine(left(), top() + height() * i / row,
                    right(), top() + height() * i / row,
                    paint);
        }
        for (int i = 0; i < column; i++) {
            canvas.drawLine(left() + width() * i / column, top(),
                    left() + width() * i / column, bottom(),
                    paint);
        }
    }

    public void drawCorner(Canvas canvas, Paint paint) {
        canvas.drawLine(left() - 1.5f, top(), left() + 32 - 1.5f, top(), paint);
        canvas.drawLine(left(), top() - 1.5f, left(), top() + 32 - 1.5f, paint);

        canvas.drawLine(right(), top(), right() - 32, top(), paint);
        canvas.drawLine(right() - 1.5f, top() - 1.5f, right() - 1.5f, top() + 32, paint);

        canvas.drawLine(left(), bottom() - 1.5f, left() + 32, bottom() - 1.5f, paint);
        canvas.drawLine(left(), bottom(), left(), bottom() - 32, paint);

        canvas.drawLine(right(), bottom() - 1.5f, right() - 32, bottom() - 1.5f, paint);
        canvas.drawLine(right() - 1.5f, bottom(), right() - 1.5f, bottom() - 32, paint);

    }

}
