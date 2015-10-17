package com.floatlearning.fl3d;

import com.floatlearning.fl3d.interfaces.Disposable;

import java.nio.IntBuffer;

/**
 * A dedicated thread to provide OpenGL frames to a listener, on request.
 */
public class FrameProvider extends Thread implements Disposable {
    /**
     * Whether or not this thread should stop running.
     */
    private boolean quit = false;
    /**
     * A listener to be notified when a new frame is available.
     */
    private FrameListener listener;
    /**
     * A buffer used to store frame pixels.
     */
    private final IntBuffer pixelBuffer;
    /**
     * Receives the content of pixelBuffer, and is used to provide frame data to the listener.
     */
    private final int[] pixels;
    /**
     * The width of the frames to provide.
     */
    private final int width;
    /**
     * The height of the frames to provide.
     */
    private final int height;

    /**
     * Construct a new FrameProvider; will automatically start and wait for request() to be called.
     *
     * @param width     The width of frames to provide.
     * @param height    The height of frames to provide.
     */
    public FrameProvider(final int width, final int height) {
        this.width = width;
        this.height = height;
        pixels = new int[width * height];
        pixelBuffer = IntBuffer.wrap(pixels);
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        super.run();

        while (!quit) {
            // waits until notify() is called
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException ignored) {}
            }

            if (!quit) {
                // reads pixels from OpenGL and provides to the listener
                Core.readRenderedPixels(0, 0, width, height, pixelBuffer);
                listener.postProcessFrame(pixels);
            }
        }
    }

    /**
     * Request that a frame be provided.
     *
     * @param listener      The object to notify when a frame is available.
     */
    public void request(final FrameListener listener) {
        this.listener = listener;

        synchronized (this) {
            notify();
        }
    }

    @Override
    public void dispose() {
        quit = true;
        pixelBuffer.clear();
    }

    public interface FrameListener {
        void postProcessFrame(final int[] pixels);
    }
}
