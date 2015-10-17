package com.floatlearning.fl3d;

import android.opengl.GLES20;

import com.floatlearning.fl3d.interfaces.Drawable;

public class GLColor implements Drawable {
    public final float red;
    public final float green;
    public final float blue;
    public final float alpha;
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
}
