package com.example.videotest1;

import android.content.Context;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;

import java.nio.ByteBuffer;

public class VideoStreamer {
    private Context context;
    private ImageReader imageReader;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private VideoServer videoServer;

    VideoStreamer(Context context, VideoServer server) {
        this.context = context;
        this.videoServer = server;
    }

    public ImageReader start() {
        mBackgroundThread = new HandlerThread("Streamer background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        this.imageReader = ImageReader.newInstance(480, 270, ImageFormat.JPEG, 2);
        this.imageReader.setOnImageAvailableListener(imageListener, mBackgroundHandler);
        return this.imageReader;
    }

    public void stop() {
        this.mBackgroundThread.quitSafely();
        try {
            this.mBackgroundThread.join();
            this.mBackgroundThread = null;
            mBackgroundHandler = null;
            this.imageReader.close();
            this.imageReader = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private ImageReader.OnImageAvailableListener imageListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            if (image == null) { return; }
            int w = image.getWidth();
            int h = image.getHeight();
            Image.Plane plane = image.getPlanes()[0];
            ByteBuffer buffer = plane.getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            long timestamp = SystemClock.elapsedRealtime();
            String jpegHeader = "Content-type: image/jpeg\r\n"
                    + "Content-Length: " + bytes.length + "\r\n"
                    + "X-Timestamp:" + timestamp + "\r\n"
                    + "\r\n";
            String boundaryLine = context.getResources().getString(R.string.boundary_line);
            boundaryLine = "\r\n--36b8cda5-1480-4e2c-8df2-50477bebd28e--\r\n";
            videoServer.streamToClients(jpegHeader.getBytes());
            videoServer.streamToClients(bytes);
            videoServer.streamToClients(boundaryLine.getBytes());
            image.close();
        }
    };
}
