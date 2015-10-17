package com.floatlearning.fl3d;

import android.opengl.GLES20;

import com.floatlearning.fl3d.interfaces.Drawable;

import java.nio.FloatBuffer;

/**
 * A Mesh is a drawable object in the OpenGL context.
 */
public class Mesh implements Drawable {
    /**
     * The number of vertices in this object; calculated on instantiation.
     */
    protected final int vertexCount;
    /**
     * The buffer containing this object's vertex data.
     */
    protected final FloatBuffer vertexBuffer;
    /**
     * Whether or not this mesh has a color.
     */
    private boolean hasColor = false;
    /**
     * The color of this mesh, as set in setColor.
     */
    private GLColor color;

    /**
     * Create a new mesh with the default coordinates, which is a plane from (1,1,0) to (-1,-1,0).
     */
    public Mesh() {
        this(DEFAULT_COORDS);
    }

    /**
     * Create a new Mesh with the given vertices.
     *
     * @param vertices    The vertices defining this mesh's appearance.
     */
    public Mesh(final float[] vertices) {
        vertexCount = vertices.length / COORDS_PER_VERTEX;
        vertexBuffer = Core.arrayAsBuffer(vertices);
    }

    /**
     * Set the color of this mesh.
     *
     * @param red      The red value of the color, from zero to one.
     * @param green    The green value of the color, from zero to one.
     * @param blue     The blue value of the color, from zero to one.
     * @param alpha    The alpha value of the color, from zero to one.
     */
    public void setColor(final float red, final float green, final float blue, final float alpha) {
        hasColor = true;
        color = new GLColor(red, green, blue, alpha);
    }

    /**
     * Draw this object using the "triangle fan" method.
     */
    @Override
    public void draw(final Program program) {
        if (hasColor) {
            color.draw(program);
        }

        program.drawArrays(A_POSITION, vertexBuffer, vertexCount);
    }

    /**
     * Disable this mesh, to prepare the GPU for other meshes to be drawn.
     */
    @Override
    public void cleanup(final Program program) {
        int positionHandle = GLES20.glGetAttribLocation(program.handle, A_POSITION);
        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    /**
     * Free up memory by clearing the contents of the vertex buffer.
     */
    @Override
    public void dispose() {
        vertexBuffer.clear();
    }

    // defines a default mesh covering the entire view
    protected static final float[] DEFAULT_COORDS = {
            -1.0f,  1.0f, 0.0f,
             1.0f,  1.0f, 0.0f,
             1.0f, -1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f
    };

    // Standard value for the number of coordinates that make up a vertex (x,y,z).
    public final static int COORDS_PER_VERTEX = 3;
    // The number of bytes in a vertex, with four bytes assumed per coordinate.
    public static final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4;
    // Standard value for a vertex shader's position variable.
    public static final String A_POSITION = "a_Position";
}
