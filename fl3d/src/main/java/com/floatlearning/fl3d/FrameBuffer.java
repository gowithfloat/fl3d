package com.floatlearning.fl3d;

import android.opengl.GLES20;

import com.floatlearning.fl3d.interfaces.Bindable;

/**
 * A FrameBuffer can be bound to the OpenGL context and receive the results of drawing,
 * rather than the default FrameBuffer, which is the display.
 */
public class FrameBuffer implements Bindable {
    /**
     * A reference to this object in the GPU.
     */
    public final int handle;
    /**
     * A reference to the texture representing this frame buffer's contents.
     */
    public final Texture texture;
    /**
     * A reference to the depth render buffer.
     */
    private final int depthRenderBuffer;
    /**
     * The size of this buffer.
     */
    public final int width;
    public final int height;
    /**
     * Whether or not this is the active frame buffer; set with bind(), clear with unbind().
     */
    private boolean bound = false;

    /**
     * Generate a new frame buffer that can be rendered to.
     * This could be used to store future frames, or provide layers of image processing.
     *
     * @param width     The width of the currently displayed frame. Will determine the width of the texture for this frame buffer.
     * @param height    The height of the current frame, used to generate the internal texture.
     */
    public FrameBuffer(final int width, final int height) {
        this.width = width;
        this.height = height;

        handle = generateFrameBuffer();
        depthRenderBuffer = generateDepthRenderBuffer();

        bind();

        texture = new Texture(width, height);

        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthRenderBuffer);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthRenderBuffer);

        assertStatus();

        unbind();
    }

    /**
     * Set this frame buffer to be the active receiver of GPU rendering.
     */
    @Override
    public void bind() {
        if (!bound) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, handle);
            bound = true;
        }
    }

    /**
     * Set the active frame buffer to be the device display.
     */
    @Override
    public void unbind() {
        if (bound) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            bound = false;
        }
    }

    /**
     * Clears this buffer to black (0, 0, 0, 1), provided that it's bound.
     */
    public void clear() {
        if (bound) {
            GLES20.glViewport(0 ,0, width, height);
            GLColor.BLACK.clearTo();
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        }
    }

    /**
     * Verify that the most recent frame buffer was properly set up.
     * Automatically called during instantiation, to verify that this frame buffer was set up properly.
     */
    public void assertStatus() {
        final int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);

        switch (status) {
            case (GLES20.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT):
                throw new RuntimeException("Not all framebuffer attachment points are framebuffer attachment complete.");
            case (GLES20.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS):
                throw new RuntimeException("Not all attached images have the same width and height.");
            case (GLES20.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT):
                throw new RuntimeException("No images are attached to the framebuffer.");
            case (GLES20.GL_FRAMEBUFFER_UNSUPPORTED):
                throw new RuntimeException("The combination of internal formats of the attached images violates an implementation-dependent set of restrictions.");
        }
    }

    @Override
    public void dispose() {
        if (bound) {
            unbind();
        }

        GLES20.glDeleteFramebuffers(1, new int[]{handle}, 0);
        GLES20.glDeleteRenderbuffers(1, new int[]{depthRenderBuffer}, 0);
        texture.dispose();
    }

    /**
     * Internal method to generate a frame buffer in the GPU.
     *
     * @return  A reference to the new frame buffer.
     */
    private static int generateFrameBuffer() {
        int[] frame_buffer = new int[1];
        GLES20.glGenFramebuffers(1, frame_buffer, 0);
        return frame_buffer[0];
    }

    /**
     * Internal method to generate a depth render buffer in the GPU.
     *
     * @return  A reference to the new depth render buffer.
     */
    private static int generateDepthRenderBuffer() {
        int[] depth_render_buffer = new int[1];
        GLES20.glGenRenderbuffers(1, depth_render_buffer, 0);
        return depth_render_buffer[0];
    }
}
