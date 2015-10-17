package com.floatlearning.fl3d;

import android.opengl.GLES20;

import com.floatlearning.fl3d.interfaces.Drawable;

/**
 * Provides a sensible way to store color values for OpenGL. Immutable.
 */
public class GLColor implements Drawable {
    // Red channel value, from 0.0 to 1.0.
    public final float red;
    // Green channel value, from 0.0 to 1.0.
    public final float green;
    // Blue channel value, from 0.0 to 1.0.
    public final float blue;
    // Alpha channel value, from 0.0 (fully transparent) to 1.0 (opaque).
    public final float alpha;
    // Internal storage for color values in OpenGL format.
    public final float[] array;

    public GLColor(final float red, final float green, final float blue, final float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;

        array = new float[] { red, green, blue, alpha };
    }

    @Override
    public void draw(Program program) {
        // sends this color value to the shader
        program.setValue(U_COLOR, array);
    }

    @Override
    public void cleanup(Program program) {}

    public void clearTo() {
        GLES20.glClearColor(red, green, blue, alpha);
    }

    @Override
    public void dispose() {}

    private static final String U_COLOR = "u_Color";

    public static final GLColor BLACK = new GLColor(0, 0, 0, 1);
    public static final GLColor WHITE = new GLColor(1, 1, 1, 1);
    public static final GLColor RED = new GLColor(1, 0, 0, 1);
    public static final GLColor GREEN = new GLColor(0, 1, 0, 1);
    public static final GLColor BLUE = new GLColor(0, 0, 1, 1);
}
