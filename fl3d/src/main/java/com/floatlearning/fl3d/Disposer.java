package com.floatlearning.fl3d;

import com.floatlearning.fl3d.interfaces.Disposable;

/**
 * Convenience class to dispose of Disposable objects.
 */
public class Disposer {
    /**
     * Calls dispose() on the given object.
     *
     * @param disposable    The object to dispose.
     * @return  Always returns null.
     */
    public static <T> T dispose(final Disposable disposable) {
        if (disposable != null) {
            disposable.dispose();
        }

        return null;
    }
}
