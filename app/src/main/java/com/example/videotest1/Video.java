package com.example.videotest1;

import android.Manifest;
import androidx.annotation.NonNull;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import android.view.TextureView;

import java.util.Arrays;
import java.util.List;

class Video {
    private Context context;
    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession cameraCaptureSession;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private ImageReader imageReader;
    private CameraCharacteristics cameraCharacteristics;

    Video(Context context, TextureView textureView) {
        this.context = context;
        this.textureView = textureView;
    }

    private void openCamera() {
        if (this.context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            int requestCode = 17;
            ((Activity) this.context).requestPermissions(new String[] { Manifest.permission.CAMERA }, requestCode);
        }
        else {
            cameraPermissionCallback();
        }
    }

    void cameraPermissionCallback() {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            assert cameraManager != null;
            String[] cameras = cameraManager.getCameraIdList();
            boolean foundCamera = false;
            for (String c : cameras) {
                CameraCharacteristics chars = cameraManager.getCameraCharacteristics(c);
                Integer facing = chars.get(CameraCharacteristics.LENS_FACING);
                assert facing != null;
                if (facing.equals(CameraCharacteristics.LENS_FACING_BACK)) {
                    if (this.context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        throw new RuntimeException();
                    }
                    cameraManager.openCamera(c, cameraOpenedCallback, null);
                    foundCamera = true;
                    this.cameraCharacteristics = chars;
                    break;
                }
            }
            if (!foundCamera) {
                throw new RuntimeException();
            }
        }
        catch (CameraAccessException | RuntimeException e) {
            e.printStackTrace();
            ((MainActivity) this.context).videoUnavailable();
        }
    }

    private CameraDevice.StateCallback cameraOpenedCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }
        @Override
        public void onDisconnected(@NonNull CameraDevice camera) { ((MainActivity) context).videoUnavailable(); }
        @Override
        public void onError(@NonNull CameraDevice camera, int error) { ((MainActivity) context).videoUnavailable(); }
    };

    private void createCameraPreview() {
        SurfaceTexture texture = this.textureView.getSurfaceTexture();
        texture.setDefaultBufferSize(135, 240);
        Surface previewSurface = new Surface(texture);
        Surface streamSurface = imageReader.getSurface();
        List<Surface> surfaces = Arrays.asList(previewSurface, streamSurface);
        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(previewSurface);
            captureRequestBuilder.addTarget(streamSurface);
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION));
            cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cCS) {
                    cameraCaptureSession = cCS;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) { throw new RuntimeException(); }
            }, null);
        } catch (CameraAccessException | RuntimeException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            this.cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    void stop() {
        this.mBackgroundThread.quitSafely();
        try {
            this.mBackgroundThread.join();
            this.mBackgroundThread = null;
            this.mBackgroundHandler = null;
            this.imageReader = null;
            cameraDevice.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void start(ImageReader imageReader) {
        this.imageReader = imageReader;
        this.mBackgroundThread = new HandlerThread("Camera Background");
        this.mBackgroundThread.start();
        this.mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        if (this.textureView.isAvailable()) {
            this.openCamera();
        } else {
            this.textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
    };
}
