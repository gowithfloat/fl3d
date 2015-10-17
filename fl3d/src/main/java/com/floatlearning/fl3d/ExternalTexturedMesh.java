package com.floatlearning.fl3d;

import android.graphics.SurfaceTexture;

/**
 * Simply a mesh with a texture from an external source.
 */
public class ExternalTexturedMesh extends Mesh {
    /**
     * A reference to the external texture that will be applied to this object.
     */
    public final ExternalTexture texture;

    /**
     * Create an external textured mesh with default coordinates and texture coordinates.
     */
    public ExternalTexturedMesh() {
        super();
        texture = new ExternalTexture();
    }

    /**
     * Create a new Mesh with the given vertices.
     *
     * @param vertices The vertices defining this mesh's appearance.
     */
    public ExternalTexturedMesh(final float[] vertices, final float[] texCoords) {
        super(vertices);
        texture = new ExternalTexture(texCoords);
    }

    /**
     * Convenience method to set the frame available listener for this object's texture.
     *
     * @param listener    An object to be notified when new frame data is available.
     */
    public void setOnFrameAvailableListener(final SurfaceTexture.OnFrameAvailableListener listener) {
        texture.setOnFrameAvailableListener(listener);
    }

    /**
     * Prepare the texture for drawing, then draw this mesh.
     */
    @Override
    public void draw(final Program program) {
        texture.draw(program);
        super.draw(program);
    }

    /**
     * Cleanup the texture, then cleanup this mesh.
     */
    @Override
    public void cleanup(final Program program) {
        texture.cleanup(program);
        super.cleanup(program);
    }

    /**
     * Dispose of the texture, then dispose of this object.
     */
    @Override
    public void dispose() {
        texture.dispose();
        super.dispose();
    }
}
