package com.floatlearning.fl3d.interfaces;

import com.floatlearning.fl3d.Program;
import com.floatlearning.fl3d.interfaces.Disposable;

/**
 * Defines an object that can be drawn in OpenGL.
 */
public interface Drawable extends Disposable {
    /**
     * Takes actions necessary to render this object to the screen.
     */
    void draw(final Program program);

    /**
     * Takes actions necessary to clean up this object's requirements to be drawn.
     */
    void cleanup(final Program program);
}
