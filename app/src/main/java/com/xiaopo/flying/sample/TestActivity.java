package com.xiaopo.flying.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.xiaopo.flying.pixelcrop.DegreeSeekBar;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        DegreeSeekBar degreeSeekBar = (DegreeSeekBar) findViewById(R.id.degree_seek_bar);
        degreeSeekBar.setScrollingListener(new DegreeSeekBar.ScrollingListener() {
            @Override
            public void onScrollStart() {

            }

            @Override
            public void onScroll(float delta, float totalDistance) {
//                Log.d("seekbar", "onScroll: delta-->" + delta);
//                Log.d("seekbar", "onScroll: totalDistance-->"+totalDistance);
            }

            @Override
            public void onScrollEnd() {

            }
        });
    }
}
