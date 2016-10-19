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

    float[] cornerPoints = new float[8];

    Border(Border src) {
        lineLeft = src.lineLeft;
        lineTop = src.lineTop;
        lineRight = src.lineRight;
        lineBottom = src.lineBottom;

        leftTop = src.lineLeft.start;
        leftBottom = src.lineLeft.end;
        rightTop = src.lineRight.start;
        rightBottom = src.lineRight.end;

        cornerPoints = src.cornerPoints;
    }

    Border(RectF baseRect) {
        setBaseRect(baseRect);
    }

    private void setBaseRect(RectF baseRect) {

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

        cornerPoints[0] = one.x;
        cornerPoints[1] = one.y;
        cornerPoints[2] = two.x;
        cornerPoints[3] = two.y;
        cornerPoints[4] = three.x;
        cornerPoints[5] = three.y;
        cornerPoints[6] = four.x;
        cornerPoints[7] = four.y;


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
        return new RectF(
                left(),
                top(),
                right(),
                bottom());
    }

    boolean contains(Line line) {
        return lineLeft == line || lineTop == line || lineRight == line || lineBottom == line;
    }

    PointF[] getCornerPoints() {
        return new PointF[]{
                lineTop.start,
                lineTop.end,
                lineBottom.end,
                lineBottom.start
        };
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

    @Override
    public String toString() {
        return "left line:\n" +
                lineLeft.toString() +
                "\ntop line:\n" +
                lineTop.toString() +
                "\nright line:\n" +
                lineRight.toString() +
                "\nbottom line:\n" +
                lineBottom.toString() +
                "\nthe rect is \n" +
                getRect().toString();
    }

}
