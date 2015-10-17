package com.floatlearning.fl3d;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.floatlearning.fl3d.interfaces.Drawable;

import java.nio.FloatBuffer;

/**
 * A texture object which can be applied to, for example, a Mesh.
 */
public class Texture implements Drawable {
    /**
     * OpenGL stores each texture in an index from 0 - 31.
     */
    private static int nextTextureNumber = 0;
    /**
     * Internal storage of this object's representation in OpenGL.
     */
    public final int handle;
    /**
     * The buffer containing the coordinates of the texture.
     */
    protected final FloatBuffer texBuffer;
    /**
     * The number of this texture, from 0 - 31.
     * TODO: this is currently unused
     */
    private final int textureNumber;

    /**
     * Generates a texture that can be used for a framebuffer. Maybe something else too!
     *
     * @param width     The width of the texture.
     * @param height    The height of the texture.
     */
    public Texture(final int width, final int height) {
        handle = genHandle();

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, handle);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, handle, 0);

        texBuffer = Core.arrayAsBuffer(DEFAULT_TEX_COORDS);

        textureNumber = nextTextureNumber;
        nextTextureNumber++;
    }

    /**
     * Creates a new texture with the given texture coordinates and handle.
     *
     * @param texCoords    The coordinates for this texture.
     * @param handle       The handle for a pre-existing texture object to use.
     */
    public Texture(final float[] texCoords, final int handle) {
        this.handle = handle;
        texBuffer = Core.arrayAsBuffer(texCoords);
        textureNumber = nextTextureNumber;
        nextTextureNumber++;
    }

    /**
     * Create a new texture with the given texture coordinates and bitmap graphic object.
     *
     * @param texCoords    The coordinates for this texture.
     * @param bitmap       The bitmap image that this texture will represent.
     */
    public Texture(final float[] texCoords, final Bitmap bitmap) {
        final int w = bitmap.getWidth();
        final int h = bitmap.getHeight();

        if (w != h || w % 2 != 0) {
            throw new RuntimeException("Tried to create texture from bitmap but not power of two! Dimensions: " + w + "x" + h);
        }

        // create a texture
        handle = create(false);

        // bind that texture to the bitmap image
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        // unbind that texture from the current context
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        texBuffer = Core.arrayAsBuffer(texCoords);

        textureNumber = nextTextureNumber;
        nextTextureNumber++;
    }

    /**
     * Create a new texture with the given texture coordinates and specified graphic resource.
     *
     * @param texCoords     The coordinates for this texture.
     * @param resources     A reference to this application's resources.
     * @param resourceID    The resource ID containing the graphic asset to load.
     */
    public Texture(final float[] texCoords, final Resources resources, final int resourceID) {
        this(texCoords, BitmapFactory.decodeResource(resources, resourceID));
    }

    /**
     * Prepare this texture for drawing by setting it up for the GPU.
     */
    @Override
    public void draw(final Program program) {
        // find the handle for the texture coordinates in the given program
        final int texCoordHandle = GLES20.glGetAttribLocation(program.handle, A_TEX_COORD);

        // activate the 0th texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0/* + textureNumber*/);

        // bind texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, handle);

        // specify texture buffer to be used for texture coordinate handle
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 8, texBuffer);

        // ensure that the values in this attribute array will be accessed when glDrawArrays is called
        GLES20.glEnableVertexAttribArray(texCoordHandle);
    }

    /**
     * Unbind this texture in OpenGL.
     */
    @Override
    public void cleanup(final Program program) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    @Override
    public void dispose() {
        // Delete this texture in the GPU.
        int[] tex = { handle };
        GLES20.glDeleteTextures(1, tex, 0);
    }

    /**
     * Creates a new texture in the GPU and returns the handle.
     *
     * @return  The handle to the new texture object.
     */
    private static int create(final boolean unbind) {
        final int tex = genHandle();

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        if (unbind) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }

        return tex;
    }

    /**
     * Generate a handle for a new texture in the GPU.
     */
    private static int genHandle() {
        final int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);

        if (texture[0] == 0) {
            throw new RuntimeException("Could not generate texture handle");
        }

        return texture[0];
    }

    // a default texture coordinate value
    protected static final float[] DEFAULT_TEX_COORDS = new float[] {
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f
    };

    // Reference to the common texture coordinate value in GLSL.
    public static final String A_TEX_COORD  = "a_TexCoord";
}
