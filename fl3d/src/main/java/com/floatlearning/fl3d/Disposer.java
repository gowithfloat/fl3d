package com.floatlearning.fl3d;

import com.floatlearning.fl3d.interfaces.Disposable;

public class Disposer {
    public static <T> T dispose(Disposable disposable) {
        if (disposable != null) {
            disposable.dispose();
        }

        return null;
    }
}
