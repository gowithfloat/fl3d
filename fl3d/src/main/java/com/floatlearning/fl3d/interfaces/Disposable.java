package com.floatlearning.fl3d.interfaces;

/**
 * Defines an object that can be disposed to free up resources, when needed.
 */
public interface Disposable {
    /**
     * Dispose of any children and free up resources held by this object.
     * Should be called when you're completely done with the object (e.g. in onDestroy()).
     */
    void dispose();
}
