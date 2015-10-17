package com.floatlearning.fl3d;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.floatlearning.fl3d.interfaces.Drawable;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * A surface view with some pre-defined values for a program, clear values, and so on.
 */
public abstract class FL3DSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer, Drawable {
    /**
     * A reference to the program which will be used to draw.
     */
    public Program program;
    /**
     * Storage for drawable objects.
     */
    public final ArrayList<Drawable> drawables = new ArrayList<Drawable>();
    /**
     * Prevent errors when trying to draw after calling dispose() in another thread.
     */
    private boolean disposing = false;
    /**
     * Used to lock the drawable list when accessing.
     */
    private final Object memberLock = new Object();
    /**
     * Width of this view, as set in onSurfaceChanged.
     */
    protected int width;
    /**
     * Height of this view, as set in onSurfaceChanged.
     */
    protected int height;

    /**
     * Create a new surface view. Handles its own rendering.
     *
     * @param context    An Android Context reference.
     */
    public FL3DSurfaceView(final Context context) {
        super(context);

        if (BuildConfig.DEBUG) {
            setDebugFlags(DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS);
        }

        setEGLContextClientVersion(2);
        setRenderer(this);
    }

    /**
     * Add an object to be drawn to this view.
     *
     * @param drawable    The object to be drawn.
     * @return  This surface view.
     */
    public final FL3DSurfaceView add(Drawable drawable) {
        synchronized (memberLock) {
            drawables.add(drawable);
        }

        return this;
    }

    /**
     * Remove an object from the list of objects to be drawn.
     *
     * @param drawable    The object to no longer be drawn.
     * @return  This surface view.
     */
    public final FL3DSurfaceView remove(Drawable drawable) {
        synchronized (memberLock) {
            drawables.remove(drawable);
        }

        return this;
    }

    /**
     * Override to define Meshes and Textures to be created and rendered later.
     */
    protected abstract void create();

    @Override
    public void draw(final Program program) {
        synchronized (memberLock) {
            for (Drawable d : drawables) {
                d.draw(program);
            }
        }
    }

    @Override
    public void cleanup(final Program program) {
        synchronized (memberLock) {
            for (Drawable d : drawables) {
                d.cleanup(program);
            }
        }
    }

    @Override
    public void dispose() {
        disposing = true;

        program.dispose();

        for (Drawable d : drawables) {
            d.dispose();
        }

        drawables.clear();
    }

    /**
     * Sets up rendering, then calls the create() method of the extending class.
     */
    @Override
    public void onSurfaceCreated(GL10 ignored, EGLConfig config) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Initializing FL3DSurfaceView with " + Core.getGLSLVersion() + ", Max texture size " + Core.getMaxTextureSize());
        }

        // set clear color to black
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // create a very simple program for the "base" rendering
        program = new Program(getResources(), R.raw.fragment, R.raw.vertex);

        // call the create method
        create();

        // make sure nothing was messed up in create()
        Core.assertStatus();
    }

    /**
     * Updates the OpenGL viewport and the texel information.
     */
    @Override
    public void onSurfaceChanged(GL10 ignored, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        this.width = width;
        this.height = height;
    }

    @Override
    public void onDrawFrame(GL10 ignored) {
        if (isDisposing()) {
            return;
        }

        // render all drawables to the screen
        program.bind();
        program.setTexels(width, height);
        draw(program);
        cleanup(program);
        program.unbind();
    }

    /**
     * Whether or not the UI thread has started disposing this object.
     *
     * @return  True if dispose() has been called, false otherwise.
     */
    protected boolean isDisposing() {
        return disposing;
    }

    private static final String TAG = FL3DSurfaceView.class.getSimpleName();
}
