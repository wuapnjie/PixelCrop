package com.xiaopo.flying.pixelcrop;

import android.graphics.PointF;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testUtil(){
        PointF A = new FakePointF(100,100);
        PointF B = new FakePointF(100,300);
        PointF C = new FakePointF(300,100);
        double result = CropUtil.calculatePointToLine(A,B,C);
        System.out.println(result);
    }

    public static class FakePointF extends PointF {
        FakePointF(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}