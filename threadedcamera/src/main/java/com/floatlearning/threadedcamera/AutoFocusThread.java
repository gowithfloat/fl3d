package com.floatlearning.threadedcamera;

import android.hardware.Camera;

/**
 * Calls the auto-focus method of the camera periodically in a dedicated thread.
 */
public class AutoFocusThread extends Thread implements Camera.AutoFocusCallback {
    /**
     * Internal reference to the camera object.
     */
    private final Camera camera;
    /**
     * Whether or not the loop of this thread should stop.
     */
    private boolean quit = false;
    /**
     * An optional method to call when the autofocus is complete.
     */
    private Camera.AutoFocusCallback callback;
    /**
     * The default duration between auto-focus calls.
     */
    private static final long SLEEP_TIME = 5000;

    /**
     * Create and start an auto-focus thread, which will try to focus the passed-in camera object.
     */
    public AutoFocusThread(final Camera camera) {
        this.camera = camera;
        setDaemon(true);
        start();
    }

    /**
     *
     */
    public void startAutoFocus() {
        if (quit) {
            quit = false;
            start();
        }
    }

    /**
     * Cancels current auto-focus and stops this thread. Use this instead of stop().
     */
    public void stopAutoFocus() {
        quit = true;
        camera.cancelAutoFocus();
        setAutoFocusCallback(null);

        synchronized (this) {
            notify();
        }
    }

    /**
     * Set a method to be called when an auto-focus is complete.
     */
    public final void setAutoFocusCallback(Camera.AutoFocusCallback callback) {
        // hold onto this so we can defer the callback until a focus is ready to be performed
        this.callback = callback;
    }

    /**
     * Stops this thread's loop.
     */
    public final void dispose() {
        quit = true;
        callback = null;
    }

    @Override
    public final void run() {
        super.run();

        while (!quit) {
            try {
                synchronized (this) {
                    wait(SLEEP_TIME);
                }
            } catch (InterruptedException ignored) {}

            if (!quit) {
                camera.autoFocus(this);
            }
        }
    }

    @Override
    public final void onAutoFocus(boolean success, Camera camera) {
        if (callback != null) {
            callback.onAutoFocus(success, camera);
        }
    }
}
