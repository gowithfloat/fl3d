// required to receive texture data from the camera.
#extension GL_OES_EGL_image_external : require

// sets the precision for this GPU
precision mediump float;

// texture coordinate information from the vertex shader.
varying vec2 v_TexCoord;

// holder for texture data from the camera.
uniform samplerExternalOES u_Texture;

/**
 * A simple fragment shader that passes through an external texture, such as from the device camera.
 */
void main() {
    // pass through the texture data
    gl_FragColor = texture2D(u_Texture, v_TexCoord);
}
