package com.example.videotest1;

import android.Manifest;
import android.app.Activity;
import android.media.ImageReader;
import android.os.Bundle;
import android.view.TextureView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.net.Inet4Address;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MainActivity extends Activity {
    VideoStreamer videoStreamer;
    Video video;
    VideoServer videoServer;

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

        TextView helloText = findViewById(R.id.helloText);
        helloText.setText(ia);

        TextureView previewTextureView = findViewById(R.id.previewTextureView);

        this.videoServer = new VideoServer(this,8080);
        this.videoStreamer = new VideoStreamer(this, this.videoServer);
        this.video = new Video(this, previewTextureView);

        //this.videoServer.start();
        //ImageReader imageReader = this.videoStreamer.start();
        //this.video.start(imageReader);
    }

    @Override
    protected void onPause() {
        this.video.stop();
        this.videoStreamer.stop();
        this.videoServer.stop();
        super.onPause();
    }

    @Override
    protected void onResume() {
        this.videoServer.start();
        ImageReader imageReader = this.videoStreamer.start();
        this.video.start(imageReader);
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
