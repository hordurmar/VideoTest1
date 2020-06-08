package com.example.videotest1;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import android.view.TextureView;

import androidx.core.app.ActivityCompat;

import java.util.Arrays;

public class Video {

    Context context;
    TextureView textureView;
    CameraDevice cameraDevice;
    CaptureRequest.Builder captureRequestBuilder;
    CameraCaptureSession cameraCaptureSession;
    Handler mBackgroundHandler;
    HandlerThread mBackgroundThread;

    public Video(Context context, TextureView textureView) {
        this.context = context;
        this.textureView = textureView;
        textureView.setSurfaceTextureListener(surfaceTextureListener);
    }

    public void openCamera() {
        if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            int requestCode = 17;
            ((Activity) this.context).requestPermissions(new String[] { Manifest.permission.CAMERA }, requestCode);
        }
        else {
            cameraPermissionCallback();
        }
    }

    public void cameraPermissionCallback() {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameras = cameraManager.getCameraIdList();
            Boolean foundCamera = false;
            for (String c : cameras) {
                CameraCharacteristics chars = cameraManager.getCameraCharacteristics(c);
                Integer facing = chars.get(CameraCharacteristics.LENS_FACING);
                if (facing.equals(CameraCharacteristics.LENS_FACING_BACK)) {
                    if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        throw new RuntimeException();
                    }
                    cameraManager.openCamera(c, openCameraCallback, null);
                    foundCamera = true;
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

    protected void updatePreview() {
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            this.cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void createCameraPreview() {
        SurfaceTexture texture = this.textureView.getSurfaceTexture();
        assert texture != null;
        texture.setDefaultBufferSize(135, 240);
        Surface surface = new Surface(texture);
        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(CameraCaptureSession cCS) {
                    cameraCaptureSession = cCS;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    throw new RuntimeException();
                }
            }, null);
        } catch (CameraAccessException | RuntimeException e) {
            e.printStackTrace();
        }
    }

    CameraDevice.StateCallback openCameraCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {

        }

        @Override
        public void onError(CameraDevice camera, int error) {

        }
    };

    public void pause() {
        this.mBackgroundThread.quitSafely();
        try {
            this.mBackgroundThread.join();
            this.mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        if (this.textureView.isAvailable()) {
            this.openCamera();
        } else {
            this.textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };
}
