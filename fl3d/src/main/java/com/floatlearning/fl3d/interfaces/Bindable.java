package com.floatlearning.fl3d.interfaces;

/**
 * Defines an object that can be bound to the OpenGL context, as well as unbound.
 */
public interface Bindable extends Disposable {
    /**
     * Bind this object to the OpenGL context, making it "active".
     */
    void bind();

    /**
     * Unbind this object from the OpenGL context, effectively disabling it.
     */
    void unbind();
}
