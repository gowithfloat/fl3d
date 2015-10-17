package com.floatlearning.threadedcamera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import com.floatlearning.fileio.FileIO;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A somewhat intelligent camera implementation in a dedicated thread.
 */
@SuppressWarnings("deprecation")
public class ThreadedCamera extends Thread implements Camera.PreviewCallback {
    /**
     * Internal reference to the camera object.
     */
    private Camera camera;
    /**
     * Storage for camera preview width/height.
     */
    private Camera.Size size;
    /**
     * Storage for assorted camera parameters, such as image format.
     */
    private Camera.Parameters params;
    /**
     * Temporary storage for a surface texture; held until this thread has started.
     */
    private SurfaceTexture pendingTexture;
    /**
     * Temporary storage for a preview callback; held until this thread has started.
     */
    private Camera.PreviewCallback pendingCallback;
    /**
     * Reference to the current preview callback; this class handles the buffer and checks for null data.
     */
    private Camera.PreviewCallback activeCallback;
    /**
     * Reference to a thread which will ensure the camera is focused.
     */
    private AutoFocusThread autoFocusThread;
    /**
     * Storage for a listener to be notified when the camera is ready.
     */
    private OnCameraAvailableListener cameraAvailableListener;
    /**
     * Whether or not this camera should continuously auto-focus.
     */
    private boolean autoFocusRequested = false;
    /**
     * Whether or not the next frame should be saved.
     */
    private boolean frameCaptureRequested = false;
    /**
     * The folder to save the preview image to.
     */
    private String requestedFolder;
    /**
     * The name of the file to save the preview image to.
     */
    private String requestedFilename;
    /**
     * The index of the camera to open.
     */
    private int requestedCamera = 0;
    /**
     * Whether or not the user has requested face detection.
     */
    private boolean pendingFaceDetection = false;
    /**
     * Internal reference to an object which should receive face detection data, once available.
     */
    private Camera.FaceDetectionListener pendingFaceDetectionListener;
    /**
     * Whether or not the user has requested cancelling auto focus.
     */
    private boolean autoFocusCancelRequested = false;
    /**
     * Whether or not we've provided buffers for the camera to use.
     */
    private boolean hasGeneratedBuffers = false;
    /**
     * Whether or not we want a single callback for a frame capture.
     */
    private boolean singleCallbackRequested = false;

    /**
     * Opens the first camera found that is facing in the specified direction.
     * Defaults to the camera at index zero, if none could be found.
     *
     * @param facing    Either CameraTools.Facing.BACK or CameraTools.Facing.FRONT.
     */
    public ThreadedCamera(final Facing facing) {
        this(firstFacing(facing));
    }

    /**
     * Request that a particular camera is opened, as specified by its index.
     */
    public ThreadedCamera(final int requestedCamera) {
        if (requestedCamera < 0) {
            throw new RuntimeException("Invalid camera index, must be >= 0");
        }

        int num = Camera.getNumberOfCameras() - 1;

        if (requestedCamera > num) {
            throw new RuntimeException("Tried to open camera with index " + requestedCamera + " but the highest available camera index is " + num);
        }

        this.requestedCamera = requestedCamera;

        // ensures that this thread will exit when the application thread ends
        setDaemon(true);

        // starts thread at some point in the future
        start();
    }

    /**
     * Starts this thread at an undefined point in the future, which will then start the camera.
     */
    public ThreadedCamera() {
        // ensures that this thread will exit when the application thread ends
        setDaemon(true);

        // starts thread at some point in the future
        start();
    }

    /**
     * Returns the width of the camera preview.
     */
    public final int getWidth() {
        return size.width;
    }

    /**
     * Returns the height of the camera preview.
     */
    public final int getHeight() {
        return size.height;
    }

    /**
     * Returns the image format of the camera preview. See android.graphics.ImageFormat.
     */
    public final int getImageFormat() {
        return params.getPreviewFormat();
    }

    /**
     * Sets the preview texture for the camera, either now or when the thread starts.
     */
    public final void setPreviewTexture(final SurfaceTexture previewTexture) {
        if (camera == null) {
            pendingTexture = previewTexture;
            return;
        }

        try {
            camera.setPreviewTexture(previewTexture);
        } catch (IOException e) {
            throw new RuntimeException("Could not set preview texture: " + e.toString());
        }
    }

    /**
     * Sets the preview callback for the camera, either now or when the thread starts.
     */
    public final void setPreviewCallback(final Camera.PreviewCallback callback) {
        if (camera == null) {
            pendingCallback = callback;
            return;
        }

        if (callback == null) {
            activeCallback = null;
            camera.setPreviewCallbackWithBuffer(null);
        }

        activeCallback = callback;

        if (!hasGeneratedBuffers) {
            int expected = getExpectedPreviewBytes();

            for (int i=0; i < 4; i++) {
                camera.addCallbackBuffer(new byte[expected]);
            }

            hasGeneratedBuffers = true;
        }

        camera.setPreviewCallbackWithBuffer(this);
    }

    /**
     * Set a listener to be notified when the camera thread has been started and the camera opened.
     */
    public final void setCameraAvailableListener(OnCameraAvailableListener listener) {
        cameraAvailableListener = listener;
    }

    /**
     * Request a continuous auto-focus of the camera.
     */
    public final void requestAutoFocus() {
        if (camera == null) {
            autoFocusRequested = true;
            return;
        }

        if (autoFocusThread == null) {
            autoFocusThread = new AutoFocusThread(camera);
        } else {
            autoFocusThread.startAutoFocus();
        }

        autoFocusRequested = false;
    }

    /**
     * Request that the auto-focus process be stopped.
     */
    public final void stopAutoFocus() {
        if (camera == null) {
            autoFocusCancelRequested = true;
            return;
        }

        autoFocusThread.stopAutoFocus();
    }

    /**
     * Start face detection and provide the results to the listener, once the camera has been setup.
     *
     * @param listener    The listener to notify of detected faces.
     */
    public final void startFaceDetection(Camera.FaceDetectionListener listener) {
        if (camera == null) {
            pendingFaceDetection = true;
            pendingFaceDetectionListener = listener;
            return;
        }

        camera.setFaceDetectionListener(listener);
        camera.startFaceDetection();
        pendingFaceDetection = false;
    }

    /**
     * Save the next possible camera preview frame.
     *
     * @param folder    The name of the folder to save to.
     * @param filename  The name of the file to save.
     */
    public final void requestFrameCapture(final String folder, final String filename) {
        frameCaptureRequested = true;
        requestedFolder = folder;
        requestedFilename = filename;

        if (activeCallback == null) {
            singleCallbackRequested = true;

            // set a dummy callback to trigger a frame grab
            setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {}
            });
        }
    }

    /**
     * Computes the size of the byte buffer the camera object will need.
     */
    private int getExpectedPreviewBytes() {
        // https://android.googlesource.com/platform/frameworks/base/+/dd6c8b34f172ba699954e4d3095dba8c0fd5e930/tests/RenderScriptTests/LivePreview/src/com/android/rs/livepreview/CameraPreviewActivity.java
        return getWidth() * getHeight() * ImageFormat.getBitsPerPixel(getImageFormat()) / 8;
    }

    @Override
    public final void run() {
        super.run();

        try {
            camera = Camera.open(requestedCamera);
        } catch (RuntimeException e) {
            throw new RuntimeException("Camera.open() failed: " + e.toString());
        }

        params = camera.getParameters();
        size = params.getPreviewSize();

        // this is an optimization to get a smoother framerate from the camera
        params.setRecordingHint(true);
        camera.setParameters(params);

        // set preview texture if that method was called before camera initialization
        if (pendingTexture != null) {
            setPreviewTexture(pendingTexture);
            pendingTexture = null;
        }

        // set preview callback if that method was called before camera initialization
        if (pendingCallback != null) {
            setPreviewCallback(pendingCallback);
            pendingCallback = null;
        }

        camera.startPreview();

        // start auto focus if that method was called before camera initialization
        if (autoFocusRequested) {
            requestAutoFocus();
        }

        if (autoFocusCancelRequested) {
            stopAutoFocus();
        }

        // let the listener know that the camera is available, if one was set
        if (cameraAvailableListener != null) {
            cameraAvailableListener.onCameraAvailable(camera);
        }

        if (pendingFaceDetection) {
            startFaceDetection(pendingFaceDetectionListener);
        }
    }

    /**
     * Stops utilizing the camera entirely.
     */
    public final void dispose() {
        if (autoFocusThread != null) {
            autoFocusThread.dispose();
        }

        camera.stopPreview();

        try {
            camera.setPreviewTexture(null);
        } catch (IOException ignored) {}

        camera.setPreviewCallbackWithBuffer(null);
        camera.release();
        cameraAvailableListener = null;
        pendingFaceDetectionListener = null;
        pendingTexture = null;
        pendingCallback = null;
        activeCallback = null;
    }

    /**
     * Called when preview data is available from the camera.
     * This method serves to ensure that the requested callback does not receive null data, and save image
     * data to file if requested.
     *
     * @param data      The raw preview bytes from the camera.
     * @param camera    A reference to the camera object.
     */
    @Override
    public final void onPreviewFrame(final byte[] data, final Camera camera) {
        if (data == null) {
            return;
        }

        if (frameCaptureRequested) {
            FileIO.saveImageFromPreviewBytes(data, getWidth(), getHeight(), getImageFormat(), requestedFolder, requestedFilename);
            frameCaptureRequested = false;
        }

        if (activeCallback != null) {
            activeCallback.onPreviewFrame(data, camera);
        }

        if (singleCallbackRequested) {
            setPreviewCallback(null);
            singleCallbackRequested = false;
        }

        camera.addCallbackBuffer(data);
    }

    /**
     * Get an array of information on all cameras on this device.
     *
     * @return  An array of CameraInfo objects for each camera on this device.
     */
    public static Camera.CameraInfo[] getCameraInfoArray() {
        int num = Camera.getNumberOfCameras();
        ArrayList<Camera.CameraInfo> infos = new ArrayList<Camera.CameraInfo>();

        for (int i = 0; i < num; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            infos.add(info);
        }

        return infos.toArray(new Camera.CameraInfo[infos.size()]);
    }

    /**
     * Returns the index of the first camera that faces in the given direction.
     *
     * @param facing    Either Facing.FRONT or Facing.BACK.
     * @return  The integer index, or zero if none could be found.
     */
    public static int firstFacing(final Facing facing) {
        int requested = facing == Facing.BACK ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
        Camera.CameraInfo[] infos = getCameraInfoArray();

        for (int i = 0; i < infos.length; i++) {
            if (infos[i].facing == requested) {
                return i;
            }
        }

        return 0;
    }

    /**
     * Get information on this device's support for the camera.
     *
     * @param context    A valid application context.
     * @return  True if this device has a camera, false otherwise.
     */
    public static boolean hasCamera(final Context context) {
        return hasFeature(context, PackageManager.FEATURE_CAMERA);
    }

    /**
     * Get information on this device's support for auto-focus.
     *
     * @param context    A valid application context.
     * @return  True if this device has autofocus, false otherwise.
     */
    public static boolean hasAutoFocus(final Context context) {
        return hasFeature(context, PackageManager.FEATURE_CAMERA_AUTOFOCUS);
    }

    /**
     * Get information on this device's front-facing camera.
     *
     * @param context    A valid application context.
     * @return  True if this device has a front-facing camera, false otherwise.
     */
    public static boolean hasFrontFacing(final Context context) {
        return hasFeature(context, PackageManager.FEATURE_CAMERA_FRONT);
    }

    /**
     * Internal method to get information on this device's features.
     *
     * @param context    A valid application context.
     * @param feature    The string representation of the feature to check, e.g. PackageManager.FEATURE_CAMERA
     * @return  True if this device has the requested feature, false otherwise.
     */
    private static boolean hasFeature(final Context context, final String feature) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature(feature);
    }

    /**
     * Defines camera facing directions, without relying on integer values.
     */
    public enum Facing {
        BACK,
        FRONT
    }

    /**
     * Defines an object with a method to listen for camera availability.
     */
    public interface OnCameraAvailableListener {
        void onCameraAvailable(Camera camera);
    }
}
