package com.xiaopo.flying.sample;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;

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

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setMax(90);
        seekBar.setProgress(45);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPixelCropView.rotate(progress - 45);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mPixelCropView.setRotateState(true);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
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
        System.out.println("Hello");
        mPixelCropView.cropAndSaveImage(Bitmap.CompressFormat.JPEG, 90, new BitmapCropCallback() {
            @Override
            public void onBitmapCropped(@NonNull Uri resultUri, int imageWidth, int imageHeight) {
                try {
                    copyFileToDownloads(resultUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

            mPixelCropView.setCropBitmap(bitmap, paths.get(0), newFile.getPath());
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
