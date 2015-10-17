package com.floatlearning.fl3d;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Storage for general-use, utility methods.
 */
public class Core {
    /**
     * Returns the version of GLSL supported on the current device.
     */
    public static String getGLSLVersion() {
        return GLES20.glGetString(GLES20.GL_SHADING_LANGUAGE_VERSION);
    }

    /**
     * Returns the maximum supported texture size on this device.
     */
    public static int getMaxTextureSize() {
        int[] maxTexSize = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxTexSize, 0);
        return maxTexSize[0];
    }

    public static Bitmap getRenderedBitmap(final int width, final int height) {
        return Bitmap.createBitmap(getRenderedPixels(width, height), width, height, Bitmap.Config.ARGB_8888);
    }

    public static void readRenderedPixels(final int width, final int height, final IntBuffer intBuffer) {
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, intBuffer);
    }

    public static int[] getRenderedPixels(final int width, final int height) {
        int[] result = new int[width * height];
        final IntBuffer intBuffer = IntBuffer.wrap(result);
        readRenderedPixels(width, height, intBuffer);

        intBuffer.clear();

        // Red and blue channels need to be swapped
        for(int i = 0; i < result.length; i++) {
            result[i] = (result[i] & (0xFF00FF00)) | ((result[i] >> 16) & 0x000000FF) | ((result[i] << 16) & 0x00FF0000);
        }

        // images tend to be upside down; fix that
        for(int i = 0; i < result.length / 2; i++) {
            int temp = result[i];
            result[i] = result[result.length - i - 1];
            result[result.length - i - 1] = temp;
        }

        return result;
    }

    /**
     * Create a float buffer from the given float array.
     *
     * @param input    The float values that will define the contents of the float buffer.
     * @return  A new FloatBuffer object.
     */
    public static FloatBuffer arrayAsBuffer(final float... input) {
        ByteBuffer bb = ByteBuffer.allocateDirect(input.length * 4);
        bb.order(ByteOrder.nativeOrder());

        FloatBuffer output = bb.asFloatBuffer();
        output.put(input);
        output.position(0);

        return output;
    }

    /**
     * Returns documentation on the current OpenGL error, from the OpenGL docs.
     * Returns null if no error exists.
     *
     * @return  A string representation of the current OpenGL error, or null.
     */
    public static String getCurrentError() {
        int error = GLES20.glGetError();

        switch (error) {
            case GLES20.GL_INVALID_ENUM:
                return "Invalid Enum: An unacceptable value is specified for an enumerated argument.";
            case GLES20.GL_INVALID_VALUE:
                return "Invalid Value: A numeric argument is out of range.";
            case GLES20.GL_INVALID_OPERATION:
                return "Invalid Operation: The specified operation is not allowed in the current state.";
            case GLES20.GL_INVALID_FRAMEBUFFER_OPERATION:
                return "Invalid FrameBuffer Operation: The command is trying to render to or read from the framebuffer while the currently bound framebuffer is not framebuffer complete.";
            case GLES20.GL_OUT_OF_MEMORY:
                return "Out of Memory: There is not enough memory left to execute the command. The state of the GL is undefined, except for the state of the error flags, after this error is recorded.";
            default:
                return null;
        }
    }

    /**
     * Asserts that OpenGL is in a safe state by throwing an exception if an error exists.
     */
    public static void assertStatus() {
        assertStatus("");
    }

    /**
     * Asserts safe state and can provide additional information.
     *
     * @param message    Additional information to include in the error message.
     */
    public static void assertStatus(final String message) {
        String err = getCurrentError();

        if (err != null) {
            throw new RuntimeException(message.isEmpty() ? err : message + ", " + err);
        }
    }
}
