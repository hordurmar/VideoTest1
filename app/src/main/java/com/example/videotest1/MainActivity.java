package com.example.videotest1;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.view.TextureView;
import android.widget.TextView;

import java.net.Inet4Address;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MainActivity extends Activity {
    Video video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String ia = "Could not find my IP.";
        try {
            Inet4Address ia4 = Internet.ip();
            if (ia4 != null) {
                ia = ia4.toString();
            }
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }

        TextView helloText = (TextView) findViewById(R.id.helloText);
        helloText.setText(ia);
        TextureView previewTextureView = (TextureView) findViewById(R.id.previewTextureView);
        video = new Video(this, previewTextureView);
    }

    @Override
    protected void onPause() {
        this.video.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        this.video.resume();
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 17) {
            if (permissions[0].equals(Manifest.permission.CAMERA)) {
                this.video.cameraPermissionCallback();
            }
        }
    }

    public void videoUnavailable() {
        throw new RuntimeException();
    }
}
