package com.floatlearning.fl3d;

import android.content.res.Resources;
import android.opengl.GLES20;

import com.floatlearning.fl3d.interfaces.Disposable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A vertex or fragment shader that determines how things are rendered.
 */
public class Shader implements Disposable {
    /**
     * Internal storage for the reference to the OpenGL object.
     */
    public final int handle;
    /**
     * Whether this is a fragment or vertex shader.
     */
    public final ShaderType type;
    /**
     * Whether or not this shader's code has texel fields.
     */
    public final boolean hasTexelFields;
    /**
     * An optional name for this shader.
     */
    public final String name;

    /**
     * Create a new shader using the given code as the source.
     *
     * @param shaderSource    A string representation of the GLSL code for this shader.
     */
    public Shader(final String shaderName, final String shaderSource) {
        if (shaderSource.contains(GL_POSITION)) {
            type = ShaderType.VERTEX;
        } else if (shaderSource.contains(GL_FRAG_COLOR)) {
            type = ShaderType.FRAGMENT;
        } else {
            throw new RuntimeException("Unable to determine shader type.");
        }

        hasTexelFields = shaderSource.contains(UNIFORM_TEXELWIDTH) && shaderSource.contains(UNIFORM_TEXELHEIGHT);
        handle = createHandle(shaderSource, type);
        name = shaderName;
    }

    /**
     * Create a new shader using the code in the given resource as the source.
     *
     * @param resources     This application's resource context.
     * @param resourceId    The resource ID for this shader's code, e.g. "R.raw.fragment"
     */
    public Shader(final Resources resources, final int resourceId) {
        this(resources.getResourceEntryName(resourceId), stringFromResource(resources, resourceId));
    }

    @Override
    public void dispose() {
        GLES20.glDeleteShader(handle);
    }

    /**
     * Create a shader in the GPU and return the handle. Throws errors if unable to create/compile.
     *
     * @param source        The source code for the shader to be created.
     * @param shaderType    Whether this should be a vertex or fragment shader.
     * @return  A reference to the shader in OpenGL.
     */
    private static int createHandle(final String source, final ShaderType shaderType) {
        int handle = GLES20.glCreateShader(shaderType == ShaderType.VERTEX ? GLES20.GL_VERTEX_SHADER : GLES20.GL_FRAGMENT_SHADER);

        if (handle == 0) {
            throw new RuntimeException("Unable to create shader");
        }

        GLES20.glShaderSource(handle, source);
        GLES20.glCompileShader(handle);

        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(handle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        if (compileStatus[0] == 0) {
            throw new RuntimeException("Unable to compile shader, error: " + GLES20.glGetShaderInfoLog(handle));
        }

        return handle;
    }

    /**
     * Get a string representation of the contents of a resource object.
     * Used to convert resources containing GLSL code into a String representation of that code.
     *
     * @param resources     This application's resource context.
     * @param resourceId    The resource ID for this shader's code, e.g. "R.raw.fragment"
     * @return  A String representation of the contents of the resource.
     */
    private static String stringFromResource(final Resources resources, final int resourceId) {
        InputStream inputStream = resources.openRawResource(resourceId);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String line;
        StringBuilder result = new StringBuilder();

        try {
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
                result.append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to get string from resource: " + e.toString());
        }

        try {
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Unable to close streams: " + e.toString());
        }

        return result.toString();
    }

    // References to common values
    public static final String UNIFORM_TEXELWIDTH = "u_TexelWidth";
    public static final String UNIFORM_TEXELHEIGHT = "u_TexelHeight";
    private static final String GL_POSITION = "gl_Position";
    private static final String GL_FRAG_COLOR = "gl_FragColor";

    /**
     * A more logical method of determining shader type than a boolean value.
     */
    public enum ShaderType {
        FRAGMENT,
        VERTEX
    }
}
