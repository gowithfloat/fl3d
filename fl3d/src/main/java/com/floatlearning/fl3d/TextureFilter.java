package com.floatlearning.fl3d;

import android.content.res.Resources;

import com.floatlearning.fl3d.interfaces.Disposable;
import com.floatlearning.fl3d.interfaces.DrawToable;
import com.floatlearning.fl3d.interfaces.Drawable;

/**
 * Generally, can be used to filter on FrameBuffer and render to another, such as post-processing an image.
 */
public class TextureFilter implements Disposable, DrawToable {
    /**
     * The frame buffer that will store this filter's output.
     */
    public final FrameBuffer target;
    /**
     * The program that will be applied to the input texture.
     */
    public final Program program;
    /**
     * The mesh that will be used to draw the texture.
     */
    public final Mesh mesh;

    /**
     * Create a new filter that can apply a program to an input texture or frame.
     *
     * @param width         The width of the output texture.
     * @param height        The height of the output texture.
     * @param resources     A reference to this application's resources.
     * @param shaderIDs     The resource IDs of shader source code to load into this filter's program.
     */
    public TextureFilter(final int width, final int height, final Resources resources, final int... shaderIDs) {
        target = new FrameBuffer(width, height);
        program = new Program(resources, shaderIDs);
        mesh = new Mesh();
    }

    /**
     * Apply this filter's shaders to a texture.
     *
     * @param inTexture     The input texture to process.
     */
    public void processTexture(final Texture inTexture) {
        program.bind();
        program.setTexels(target.width, target.height);
        target.bind();
        target.clear();

        inTexture.draw(program);
        mesh.draw(program);

        program.unbind();
        target.unbind();
    }

    /**
     * Draw this texture filter's effect to a drawable object.
     *
     * @param drawable  The object to draw to.
     * @param unbind    Whether or not to unbind this object's program when done.
     */
    @Override
    public void drawTo(final Drawable drawable, final boolean unbind) {
        program.bind();
        drawable.draw(program);
        drawable.cleanup(program);

        if (unbind) {
            program.unbind();
        }
    }

    /**
     * Returns the internal target texture, which receives the filtered output.
     */
    public Texture getTexture() {
        return target.texture;
    }

    @Override
    public void dispose() {
        target.dispose();
        program.dispose();
        mesh.dispose();
    }
}
