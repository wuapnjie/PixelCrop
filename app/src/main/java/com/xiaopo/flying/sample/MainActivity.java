package com.xiaopo.flying.sample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.xiaopo.flying.pixelcrop.DegreeSeekBar;
import com.xiaopo.flying.pixelcrop.PixelCropView;
import com.xiaopo.flying.poiphoto.Define;
import com.xiaopo.flying.poiphoto.PhotoPicker;
import com.yalantis.ucrop.task.BitmapCropCallback;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private PixelCropView mPixelCropView;
    private DegreeSeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPixelCropView = (PixelCropView) findViewById(R.id.pixel_crop_view);

        mSeekBar = (DegreeSeekBar) findViewById(R.id.seek_bar);
        mSeekBar.setScrollingListener(new DegreeSeekBar.ScrollingListener() {
            @Override
            public void onScrollStart() {
                mPixelCropView.setRotateState(true);
            }

            @Override
            public void onScroll(int currentDegrees) {
                mPixelCropView.rotate(currentDegrees);
            }

            @Override
            public void onScrollEnd() {
                mPixelCropView.setRotateState(false);
            }
        });
    }

    public void pick(View view) {
        PhotoPicker.newInstance()
                .setMaxCount(1)
                .pick(this);
    }


    public void crop(View view) {
        mPixelCropView.cropAndSaveImage(Bitmap.CompressFormat.JPEG, 90, new BitmapCropCallback() {
            @Override
            public void onBitmapCropped(@NonNull Uri resultUri, int imageWidth, int imageHeight) {
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, resultUri));
                Toast.makeText(MainActivity.this, "Crop Succeed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCropFailure(@NonNull Throwable t) {
                Log.e("PixelCrop", "onCropFailure: ---> ", t);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Define.DEFAULT_REQUEST_CODE) {
            List<String> paths = data.getStringArrayListExtra(Define.PATHS);
            File newFile = FileUtils.getNewFile(this, "PixelCrop");
            mSeekBar.setCurrentDegrees(0);
            mPixelCropView.setCropUri(Uri.parse("file:///" + paths.get(0)), Uri.parse("file:///" + newFile.getAbsolutePath()));
        }
    }

}
