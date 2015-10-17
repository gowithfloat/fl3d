package com.floatlearning.glcameraview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import com.floatlearning.fileio.FileIO;
import com.floatlearning.fl3d.Core;
import com.floatlearning.fl3d.Disposer;
import com.floatlearning.fl3d.ExternalTexturedMesh;
import com.floatlearning.fl3d.FL3DSurfaceView;
import com.floatlearning.fl3d.FrameBuffer;
import com.floatlearning.fl3d.FrameProvider;
import com.floatlearning.fl3d.Program;
import com.floatlearning.fl3d.TextureFilter;
import com.floatlearning.threadedcamera.ThreadedCamera;

import javax.microedition.khronos.opengles.GL10;

/**
 * A view that renders the camera to an OpenGL texture. More flexible than the "standard" camera view implementations.
 */
public class GLCameraView extends FL3DSurfaceView implements SurfaceTexture.OnFrameAvailableListener, FrameProvider.FrameListener {
    /**
     * A reference to the threaded camera object which will provide texture data.
     */
    private ThreadedCamera camera;
    /**
     * The index of the requested camera. Default is zero, the back-facing camera.
     */
    private int requestedCamera = 0;
    /**
     * Whether or not there is a pending request to auto-focus the camera.
     */
    private boolean pendingAutoFocus = false;
    /**
     * Temporary storage for a pending requested callback for face detection.
     */
    private Camera.FaceDetectionListener pendingFaceListener;
    /**
     * Temporary storage for a pending requested callback for text detection.
     */
    private TextDetectionListener pendingTextListener;

    private TextDetectionListener activeTextListener;
    private boolean hasTextListener = false;
    /**
     * Temporary storage for pending frame capture data.
     */
    private boolean pendingCapture = false;
    private String pendingCaptureFolder = "";
    private String pendingCaptureFilename = "";
    /**
     * The frame buffer that stores the camera preview.
     */
    private FrameBuffer imageCaptureFrameBuffer;
    /**
     * The sobel filter performs simple edge detection.
     */
    private TextureFilter blurFilter;
    private TextureFilter sobelFilter;
    private TextureFilter edgeDirectionFilter;
    private TextureFilter edgeToTextFilter;
    private boolean filtersEnabled = false;
    private boolean postProcessFrameCallback = true;
    private FrameProvider frameProvider;

    /**
     * Create a new GLCameraView with the specified camera.
     *
     * @param context    The current application context.
     * @param facing     The camera to open, either CameraTools.Facing.FRONT or CameraTools.Facing.BACK.
     */
    public GLCameraView(final Context context, final ThreadedCamera.Facing facing) {
        this(context, ThreadedCamera.firstFacing(facing));
    }

    /**
     * Create a camera view with a specified camera index.
     * Throws an exception if the camera index is invalid.
     *
     * @param context            A reference to a valid application context.
     * @param requestedCamera    The index of the camera to open.
     */
    public GLCameraView(final Context context, final int requestedCamera) {
        super(context);
        this.requestedCamera = requestedCamera;
        frameProvider = new FrameProvider(getWidth(), getHeight());
    }

    public void enableFilters() {
        filtersEnabled = true;
    }

    public void disableFilters() {
        filtersEnabled = false;
    }

    public void toggleFilters() {
        if (filtersEnabled) {
            disableFilters();
        } else {
            enableFilters();
        }
    }

    /**
     * Create a camera view with the default camera, which is usually the back-facing camera.
     *
     * @param context     A reference to a valid application context.
     */
    public GLCameraView(Context context) {
        this(context, 0);
    }

    /**
     * Request that this view periodically auto-focus the camera.
     */
    public void requestAutoFocus() {
        if (camera != null) {
            camera.requestAutoFocus();
            pendingAutoFocus = false;
        } else {
            pendingAutoFocus = true;
        }
    }

    /**
     * Request that this view provide information on detected faces in the image.
     *
     * @param listener    An object to receive information on detected faces.
     */
    public void requestFaceDetection(Camera.FaceDetectionListener listener) {
        if (camera != null) {
            camera.startFaceDetection(listener);
        } else {
            pendingFaceListener = listener;
        }
    }

    /**
     * Request that this view provide information on detected text in the image.
     *
     * @param listener      An object to receive information on detected text.
     */
    public void requestTextDetection(GLCameraView.TextDetectionListener listener) {
        if (camera != null) {
            activeTextListener = listener;
            hasTextListener = true;
        } else {
            pendingTextListener = listener;
        }
    }

    /**
     * Request that a frame from the camera feed is captured and saved to disk as a JPEG.
     *
     * @param folder    The folder to save to in external storage.
     * @param filename  The name of the file to save. The file extension will be `.jpg`
     */
    public void requestFrameCapture(final String folder, final String filename) {
        pendingCaptureFolder = folder;
        pendingCaptureFilename = filename;
        pendingCapture = true;
    }

    /**
     * Prepares this class to render, called by FL3DSurfaceView.
     */
    @Override
    protected void create() {
        setRenderMode(RENDERMODE_WHEN_DIRTY);

        program.dispose();
        program = null;
        program = new Program(getResources(), R.raw.fragment_texture, R.raw.vertex_texture);

        ExternalTexturedMesh cameraRenderable = new ExternalTexturedMesh();
        cameraRenderable.setOnFrameAvailableListener(this);
        add(cameraRenderable);

        camera = new ThreadedCamera(requestedCamera);
        camera.setPreviewTexture(cameraRenderable.texture.surface);

        final int w = getWidth();
        final int h = getHeight();

        imageCaptureFrameBuffer = new FrameBuffer(w, h);
        blurFilter = new TextureFilter(w, h, getResources(), R.raw.fragment_blur, R.raw.vertex_texture);
        sobelFilter = new TextureFilter(w, h, getResources(), R.raw.fragment_sobel, R.raw.vertex_texture);
        edgeDirectionFilter = new TextureFilter(w, h, getResources(), R.raw.fragment_direct, R.raw.vertex_texture);

        if (pendingAutoFocus) {
            requestAutoFocus();
        }

        if (pendingFaceListener != null) {
            requestFaceDetection(pendingFaceListener);
            pendingFaceListener = null;
        }

        if (pendingTextListener != null) {
            requestTextDetection(pendingTextListener);
            pendingTextListener = null;
        }
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        if (isDisposing()) {
            return;
        }

        if (filtersEnabled) {
            // capture drawn frames on the default frame buffer
            imageCaptureFrameBuffer.bind();
        }

        // render all drawables
        program.bind();
        draw(program);
        cleanup(program);
        program.unbind();

        if (filtersEnabled) {
            blurFilter.processTexture(imageCaptureFrameBuffer.texture);
            sobelFilter.processTexture(blurFilter.target.texture);
            edgeDirectionFilter.processTexture(sobelFilter.target.texture);

            imageCaptureFrameBuffer.unbind();

            edgeDirectionFilter.drawTo(this, false);
        }

        // save the results of the edge detect filter to file, if requested
        if (pendingCapture) {
            Bitmap bitmap = Core.getRenderedBitmap(0, 0, width, height);
            FileIO.saveImageFromBitmapWithTimestamp(bitmap, pendingCaptureFolder, pendingCaptureFilename, true);
            bitmap.recycle();
            pendingCapture = false;
        }

        if (postProcessFrameCallback) {
            frameProvider.request(this);
        }

        if (hasTextListener) {

        }

        if (filtersEnabled) {
            edgeDirectionFilter.program.unbind();
        }
    }

    /**
     * A listener set by `cameraRenderable.setOnFrameAvailableListener`.
     * Requests a new render when the camera data is updated.
     *
     * @param surfaceTexture    The camera renderable's surface texture.
     */
    @Override
    public final void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();
    }

    public void postProcessFrame(int[] pixels) {
        //Log.wtf(TAG, "Got " + pixels.length + " pixels");
    }

    @Override
    public final void dispose() {
        super.dispose();

        if (camera != null) {
            camera.dispose();
        }

        camera = null;
        pendingFaceListener = null;
        pendingTextListener = null;
        frameProvider = null;

        blurFilter = Disposer.dispose(blurFilter);
        sobelFilter = Disposer.dispose(sobelFilter);
        edgeDirectionFilter = Disposer.dispose(edgeDirectionFilter);
        edgeToTextFilter = Disposer.dispose(edgeToTextFilter);
    }

    /**
     * Callback interface for text detected in the preview frame.
     */
    public interface TextDetectionListener {
        /**
         * Notify the listener of the detected text in the preview frame.
         *
         * @param texts   The detected text in a list
         * @param camera  The {@link Camera} service object
         */
        void onTextDetection(Text[] texts, Camera camera);
    }

    /**
     * Class defining properties of detected text.
     */
    public static class Text {
        /**
         * The bounding rect of the discovered text, relative to the camera preview.
         */
        public final RectF rect;
        /**
         * The confidence associated with this text object.
         */
        public final float confidence;

        private Text(final float left, final float top, final float right, final float bottom, final float conf) {
            this.rect = new RectF(left, top, right, bottom);
            this.confidence = conf;
        }
    }

    private static final String TAG = GLCameraView.class.getSimpleName();
}
