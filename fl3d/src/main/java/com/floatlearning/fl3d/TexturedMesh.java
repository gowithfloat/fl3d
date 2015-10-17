package com.floatlearning.fl3d;

/**
 * A Mesh with a texture applied to it.
 */
public class TexturedMesh extends Mesh {
    /**
     * Storage for the texture object.
     */
    public final Texture texture;

    /**
     * Create a new mesh with a texture associated with it.
     *
     * @param vertices     The vertices defining this mesh's shape.
     * @param texCoords    Texture coordinates for the texture on this object.
     * @param texHandle    The handle for the texture for this object.
     */
    public TexturedMesh(final float[] vertices, final float[] texCoords, final int texHandle) {
        super(vertices);
        texture = new Texture(texCoords, texHandle);
    }

    /**
     * Draw the texture, then draw this mesh.
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
