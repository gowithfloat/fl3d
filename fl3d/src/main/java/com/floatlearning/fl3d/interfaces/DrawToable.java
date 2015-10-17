package com.floatlearning.fl3d.interfaces;

/**
 * Defines an object that can draw to a drawable.
 */
public interface DrawToable {
    /**
     * Draw this object's contents to a drawable object.
     *
     * @param drawable  The object to draw to.
     * @param unbind    Whether or not to unbind this object's program when done.
     */
    void drawTo(final Drawable drawable, final boolean unbind);
}
