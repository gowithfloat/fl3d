// sets the precision for this GPU
precision mediump float;

// texture coordinate information from the vertex shader.
varying vec2 v_TexCoord;

// storage for texture data
uniform sampler2D u_Texture;

// width and height of a GPU pixel in screen space
uniform float u_TexelWidth;
uniform float u_TexelHeight;

const float SIZE = 1.0;
const float BLUR0 = 0.05 * SIZE;
const float BLUR1 = 0.09 * SIZE;
const float BLUR2 = 0.12 * SIZE;
const float BLUR3 = 0.15 * SIZE;
const float BLUR4 = 0.18 * SIZE;

/**
 * A gaussian blur shader.
 */
void main() {
    vec2 offset = vec2(u_TexelWidth, u_TexelHeight);
    float multiplier = 0.0;
    vec2 blur_step = vec2(0.0, 0.0);
    vec4 colors[9];

    for (int i = 0; i < 9; i++) {
        multiplier = float(i) - 4.0;
        blur_step = multiplier * offset;
        colors[i] = texture2D(u_Texture, v_TexCoord.xy + blur_step);
    }

    // compute the blur color
    vec3 sum = vec3(0,0,0);
    sum += colors[0].rgb * BLUR0;
    sum += colors[1].rgb * BLUR1;
    sum += colors[2].rgb * BLUR2;
    sum += colors[3].rgb * BLUR3;
    sum += colors[4].rgb * BLUR4;
    sum += colors[5].rgb * BLUR3;
    sum += colors[6].rgb * BLUR2;
    sum += colors[7].rgb * BLUR1;
    sum += colors[8].rgb * BLUR0;

    // assumes alpha is 1
    gl_FragColor = vec4(sum, 1.0);
}
