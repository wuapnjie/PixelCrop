package com.xiaopo.flying.sample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;

import com.xiaopo.flying.pixelcrop.PixelCropView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final PixelCropView pixelCropView = (PixelCropView) findViewById(R.id.pixel_crop_view);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.demo2);
        pixelCropView.setCropBitmap(bitmap);

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setMax(90);
        seekBar.setProgress(45);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pixelCropView.rotate(progress - 45);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pixelCropView.setRotateState(true);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                pixelCropView.setRotateState(false);
            }
        });
    }
}
