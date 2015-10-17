// sets the precision for this GPU
precision mediump float;

// texture coordinate information from the vertex shader.
varying vec2 v_TexCoord;

// storage for texture data
uniform sampler2D u_Texture;

// width and height of a GPU pixel in screen space
uniform float u_TexelWidth;
uniform float u_TexelHeight;

const float THRESHOLD = 0.5;
const vec4 BLACK   = vec4(0.0, 0.0, 0.0, 1.0);

/**
 *
 */
bool highTrue(float x1, float x2, float x3, float x4) {
    return (x1 > 0.5) && (x2 > 0.5) && (x3 > 0.5) && (x4 > 0.5);
}

float highBinary(float x1, float x2, float x3, float x4) {
    return highTrue(x1, x2, x3, x4) ? 1.0 : 0.0;
}

/**
 * A fragment shader that determines localized edge directionality.
 */
void main() {
    vec4 thisColor = texture2D(u_Texture, v_TexCoord);

    // don't bother processing black pixels
    if (thisColor == BLACK) {
        gl_FragColor = BLACK;
        return;
    }

    // defines the relative position of the pixel above this one
    vec2 up = vec2(0.0, u_TexelHeight);

    // defines the relative position of the pixel to the right of this one
    vec2 right = vec2(u_TexelWidth, 0.0);

    // get neighboring pixel intensity
    float topIntensity     = texture2D(u_Texture, v_TexCoord + up).r;
    float topIntensity2    = texture2D(u_Texture, v_TexCoord + up + up).r;
    float leftIntensity    = texture2D(u_Texture, v_TexCoord - right).r;
    float leftIntensity2   = texture2D(u_Texture, v_TexCoord - right - right).r;
    float rightIntensity   = texture2D(u_Texture, v_TexCoord + right).r;
    float rightIntensity2  = texture2D(u_Texture, v_TexCoord + right + right).r;
    float bottomIntensity  = texture2D(u_Texture, v_TexCoord - up).r;
    float bottomIntensity2 = texture2D(u_Texture, v_TexCoord - up - up).r;

    // set red channel if vertical line
    float r = highBinary(topIntensity, topIntensity2, bottomIntensity, bottomIntensity2);

    // set blue channel if horizontal line
    float b = highBinary(leftIntensity, leftIntensity2, rightIntensity, rightIntensity2);

    // assign channels based on neighboring lines
    gl_FragColor = vec4(r, 0.0, b, 1.0);
}
