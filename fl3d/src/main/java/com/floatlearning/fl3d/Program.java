package com.floatlearning.fl3d;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.Log;

import com.floatlearning.fl3d.interfaces.Bindable;

import java.nio.FloatBuffer;

/**
 * Manages an OpenGL program, which in turn manages GLSL code and shaders.
 */
public class Program implements Bindable {
    /**
     * Reference to the program in the OpenGL context.
     */
    public final int handle;
    /**
     * This program's fragment and vertex shaders.
     */
    private final Shader[] shaders;
    /**
     * Whether or not one of this program's shaders have texel fields (u_TexelWidth & u_TexelHeight).
     */
    private final boolean hasTexelFields;
    /**
     * Whether or not this program is bound to the OpenGL context.
     */
    private boolean bound = false;

    /**
     * Create a new program with the given fragment and vertex shader code, from resources.
     *
     * @param resources     A reference to this application's resources.
     * @param shaderIDs    The resource IDs of shader code to use.
     */
    public Program(final Resources resources, final int... shaderIDs) {
        final int len = shaderIDs.length;
        boolean texels = false;
        shaders = new Shader[len];

        for (int i = 0; i < len; i++) {
            shaders[i] = new Shader(resources, shaderIDs[i]);
            texels = shaders[i].hasTexelFields || texels;
        }

        hasTexelFields = texels;
        handle = programWithShaders(shaders);

        // verify that shaders and program are valid
        Core.assertStatus();
    }

    /**
     * Safely set texel values for this program, but only if the shader source code has texel properties.
     *
     * @param screenWidth     The width of the screen. The width of one pixel is calculated from this value.
     * @param screenHeight    The height of the screen. The height of one pixel is calculated from this value.
     */
    public void setTexels(final float screenWidth, final float screenHeight) {
        if (hasTexelFields) {
            setValue(Shader.UNIFORM_TEXELWIDTH, 1.0f / screenWidth);
            setValue(Shader.UNIFORM_TEXELHEIGHT, 1.0f / screenHeight);
        }
    }

    /**
     * Sets a float value in the current program.
     *
     * @param valueName    The name of the value to set, e.g. "u_MyValue".
     * @param value        The value to send to the GPU.
     */
    public void setValue(final String valueName, final float value) {
        if (!bound) {
            throw new RuntimeException("Tried to set " + valueName + " to " + value + " on an unbound program.");
        }

        final int valueHandle = getLocation(valueName);
        GLES20.glUniform1f(valueHandle, value);
    }

    /**
     * Sets a float array in the current program, such as a vec4.
     *
     * @param valueName    The name of the value to set, e.g. "a_Color".
     * @param value        The value to send to the GPU.
     */
    public void setValue(final String valueName, final float[] value) {
        if (!bound) {
            throw new RuntimeException("Tried to set " + valueName + " on an unbound program.");
        }

        final int valueHandle = getLocation(valueName);
        GLES20.glUniform4fv(valueHandle, 1, value, 0);
    }

    /**
     * Send float buffer values to the GPU, such as a position attribute in a vertex shader.
     *
     * @param valueName    The value to modify in the GPU, e.g. "a_Position".
     * @param value        The value to send to the GPU.
     */
    public void setValue(final String valueName, final FloatBuffer value) {
        if (!bound) {
            throw new RuntimeException("Tried to set " + valueName + " on an unbound program.");
        }

        final int positionHandle = GLES20.glGetAttribLocation(handle, valueName);
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, Mesh.COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, Mesh.VERTEX_STRIDE, value);
    }

    /**
     * Update the position values in the GPU and draw triangles.
     *
     * @param valueName      The value to modify in the GPU.
     * @param value          The value to send to the GPU.
     * @param vertexCount    The vertex count for the provided float buffer.
     */
    public void drawArrays(final String valueName, final FloatBuffer value, final int vertexCount) {
        setValue(valueName, value);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount);
    }

    /**
     * Returns a float value from the current program.
     *
     * @param valueName    The name of the value to get, e.g. "u_MyValue".
     * @return  The value sent from the GPU.
     */
    public float getValue(final String valueName) {
        if (!bound) {
            throw new RuntimeException("Tried to get " + valueName + " from an inactive program.");
        }

        final int valueHandle = getLocation(valueName);
        final float[] results = new float[1];
        GLES20.glGetUniformfv(handle, valueHandle, results, 0);

        return results[0];
    }

    /**
     * Returns a float array value from the current program.
     *
     * @param valueName    The name of the value to get, e.g. "a_MyValue".
     * @return  The value sent from the GPU.
     */
    public float[] getValue(final String valueName, final int length) {
        if (!bound) {
            throw new RuntimeException("Tried to get " + valueName + " from an inactive program");
        }

        final int valueHandle = getLocation(valueName);
        final float[] results = new float[length];
        GLES20.glGetUniformfv(handle, valueHandle, results, 0);

        return results;
    }

    /**
     * Use this program for OpenGL rendering.
     */
    @Override
    public void bind() {
        if (!bound) {
            GLES20.glUseProgram(handle);
            bound = true;
        }
    }

    /**
     * Disable this program for OpenGL rendering; just sets the active program to 0.
     */
    @Override
    public void unbind() {
        if (bound) {
            GLES20.glUseProgram(0);
            bound = false;
        }
    }

    /**
     * Internal method to get a handle for a GLSL variable, regardless of uniform or attrib type.
     *
     * @param valueName    The name of the value to get; assumes prefix of `u` for uniform, `a` for attrib.
     * @return  The handle to the named value.
     */
    private int getLocation(final String valueName) {
        if (valueName.charAt(0) == 'u') {
            return GLES20.glGetUniformLocation(handle, valueName);
        } else {
            return GLES20.glGetAttribLocation(handle, valueName);
        }
    }

    /**
     * Internal method to generate a program handle from given shaders.
     *
     * @param shaders    The shaders to use.
     * @return  The handle to the newly-created program.
     */
    private static int programWithShaders(final Shader... shaders) {
        int program = GLES20.glCreateProgram();

        // Fail if the program could not be created; this can happen if there is no OpenGL context.
        if (program == 0) {
            throw new RuntimeException("Couldn't create program");
        }

        // Attach each shader to the program.
        for (Shader s : shaders) {
            GLES20.glAttachShader(program, s.handle);
            Core.assertStatus("Tried to attach shader that was already attached: " + s.name);
        }

        GLES20.glLinkProgram(program);

        // Fail if the link was unsuccessful.
        final int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);

        if (linkStatus[0] == 0) {
            throw new RuntimeException("Unable to link program: " + GLES20.glGetProgramInfoLog(program));
        }

        return program;
    }

    @Override
    public void dispose() {
        for (Shader s : shaders) {
            s.dispose();
        }

        GLES20.glDeleteProgram(handle);
    }
}
