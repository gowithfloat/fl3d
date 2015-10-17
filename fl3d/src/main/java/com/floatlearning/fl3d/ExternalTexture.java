package com.floatlearning.fl3d;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;

/**
 * An external texture receives its data from an external source, such as a video or the device camera.
 */
public class ExternalTexture extends Texture {
    /**
     * An internal reference to the surface texture object, which can receive data from an external source.
     */
    public final SurfaceTexture surface;

    /**
     * Create an external texture with the default texture coordinates.
     */
    public ExternalTexture() {
        this(DEFAULT_TEX_COORDS);
    }

    /**
     * Create a new external texture.
     *
     * @param texCoords    The texture coordinates for this object.
     */
    public ExternalTexture(final float[] texCoords) {
        super(texCoords, createExternal());
        surface = new SurfaceTexture(handle);
    }

    /**
     * Set a listener to be notified when the internal surface texture has new data available.
     * Useful if you only want to render when the surface is "dirty".
     *
     * @param frameAvailableListener    An object to listen for new available frames.
     */
    public void setOnFrameAvailableListener(final SurfaceTexture.OnFrameAvailableListener frameAvailableListener) {
        surface.setOnFrameAvailableListener(frameAvailableListener);
    }

    /**
     * Prepares this texture for drawing, and updates the texture image from the surface.
     */
    @Override
    public void draw(final Program program) {
        surface.updateTexImage();

        // find the handle for the texture coordinates in the given program
        final int texCoordHandle = GLES20.glGetAttribLocation(program.handle, A_TEX_COORD);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, handle);
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 8, texBuffer);
        GLES20.glEnableVertexAttribArray(texCoordHandle);
    }

    /**
     * Unbinds this texture. Have to override because we're not using GLES20.GL_TEXTURE_2D.
     */
    @Override
    public void cleanup(final Program program) {
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0);
    }

    @Override
    public void dispose() {
        surface.release();
        super.dispose();
    }

    /**
     * Creates a new external texture in the GPU.
     *
     * @return  The handle for the external texture.
     */
    private static int createExternal() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);

        if (texture[0] == 0) {
            throw new RuntimeException("Could not generate texture handle");
        }

        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, texture[0]);

        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0);

        return texture[0];
    }

    // This is equivalent to GLES11Ext.GL_TEXTURE_EXTERNAL_OES, but that call requires a higher API level.
    private static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
}
