// sets the precision for this GPU
precision mediump float;

// texture coordinate information from the vertex shader.
varying vec2 v_TexCoord;

// storage for texture data
uniform sampler2D u_Texture;

// width and height of a GPU pixel in screen space
uniform float u_TexelWidth;
uniform float u_TexelHeight;

const vec4 RED = vec4(1.0, 0.0, 0.0, 1.0);
const vec4 BLU = vec4(0.0, 0.0, 1.0, 1.0);
const vec4 BLK = vec4(0.0, 0.0, 0.0, 1.0);

/**
 * A fragment shader with sobel edge detection.
 */
void main() {
    vec2 offset = vec2(u_TexelWidth, u_TexelHeight);

    vec4 up    = texture2D(u_Texture, v_TexCoord.xy + (-4.0 * offset));
    vec4 left  = texture2D(u_Texture, v_TexCoord.xy + (-2.0 * offset));
    vec4 right = texture2D(u_Texture, v_TexCoord.xy + 0.0 * offset);
    vec4 down  = texture2D(u_Texture, v_TexCoord.xy + 2.0 * offset);

    float h = left.r * left.g + right.r * right.g;
    float v = up.r * up.b + down.r * down.b;

	gl_FragColor = vec4(h, 0.0, v, 1.0);;
}
