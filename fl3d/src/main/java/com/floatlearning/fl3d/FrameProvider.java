package com.floatlearning.fl3d;

import com.floatlearning.fl3d.interfaces.Disposable;

import java.nio.IntBuffer;

public class FrameProvider extends Thread implements Disposable {
    private boolean quit = false;
    private FrameListener listener;
    private final IntBuffer pixelBuffer;
    private final int[] pixels;
    private final int width;
    private final int height;

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
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException ignored) {}
            }

            if (!quit) {
                Core.readRenderedPixels(width, height, pixelBuffer);
                listener.postProcessFrame(pixels);
            }
        }
    }

    public void request(FrameListener listener) {
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
