package com.xiaopo.flying.sample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.xiaopo.flying.pixelcrop.DegreeSeekBar;
import com.xiaopo.flying.pixelcrop.PixelCropView;
import com.xiaopo.flying.poiphoto.Define;
import com.xiaopo.flying.poiphoto.PhotoPicker;
import com.yalantis.ucrop.task.BitmapCropCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    PixelCropView mPixelCropView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPixelCropView = (PixelCropView) findViewById(R.id.pixel_crop_view);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.demo);

        DegreeSeekBar seekBar = (DegreeSeekBar) findViewById(R.id.seek_bar);
        seekBar.setScrollingListener(new DegreeSeekBar.ScrollingListener() {
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
                Toast.makeText(MainActivity.this, "Crop Succeed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCropFailure(@NonNull Throwable t) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Define.DEFAULT_REQUEST_CODE) {
            //to get path of the selected photos
            List<String> paths = data.getStringArrayListExtra(Define.PATHS);

            Bitmap bitmap = BitmapFactory.decodeFile(paths.get(0));

            File newFile = FileUtils.getNewFile(this, "PixelCrop");

            System.out.println(newFile.getPath());

            mPixelCropView.setCropUri(Uri.parse("file:///" + paths.get(0)), Uri.parse("file:///" + newFile.getAbsolutePath()));
        }
    }


    private void copyFileToDownloads(Uri croppedFileUri) throws Exception {
        String downloadsDirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        String filename = String.format("%d_%s", Calendar.getInstance().getTimeInMillis(), croppedFileUri.getLastPathSegment());

        File saveFile = new File(downloadsDirectoryPath, filename);

        FileInputStream inStream = new FileInputStream(new File(croppedFileUri.getPath()));
        FileOutputStream outStream = new FileOutputStream(saveFile);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }


}
