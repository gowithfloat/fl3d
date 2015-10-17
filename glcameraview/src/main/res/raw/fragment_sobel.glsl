// sets the precision for this GPU
precision mediump float;

// texture coordinate information from the vertex shader.
varying vec2 v_TexCoord;

// storage for texture data
uniform sampler2D u_Texture;

// width and height of a GPU pixel in screen space
uniform float u_TexelWidth;
uniform float u_TexelHeight;

// the threshold values used for the filters
const float THRESHOLD = 0.75;
const float UPPER_THRESHOLD = 0.75;
const float LOWER_THRESHOLD = 0.25;

/**
 * Returns the gradient magnitude of three intensity values.
 */
float mag(float f1, float f2, float f3) {
    return f1 + 2.0 * f2 + f3;
}

/**
 * Returns the gradient magnitude of the red channel intensity of three colors.
 */
float mag(vec4 c1, vec4 c2, vec4 c3) {
    return mag(c1.r, c2.r, c3.r);
}

/**
 * Simple 0.0 - 1.0 threshold.
 */
float threshold(float f) {
    return f < THRESHOLD ? 0.0 : 1.0;
}

/**
 * Inverted threshold.
 */
float thresholdi(float f) {
    return f < THRESHOLD ? 1.0 : 0.0;
}

/**
 * A fragment shader with sobel edge detection.
 */
void main() {
    vec2 offset = vec2(u_TexelWidth, u_TexelHeight);
    float multi = 0.0;
    vec2 step = vec2(0.0, 0.0);
    vec4 colors[9];

    for (int i = 0; i < 9; i++) {
        multi = float(i) - 4.0;
        step = multi * offset;
        colors[i] = texture2D(u_Texture, v_TexCoord.xy + step);
    }

    // compute the horizontal and vertical magnitude of the "edginess"
    vec2 gradientDirection;

    gradientDirection.x = mag(colors[6], colors[7], colors[8]) - mag(colors[0], colors[1], colors[2]);
    gradientDirection.y = mag(colors[8], colors[5], colors[2]) - mag(colors[6], colors[3], colors[0]);

    // compute magnitude and normalize
    float gradientMagnitude = length(gradientDirection);
    vec2 normalizedDirection = normalize(gradientDirection);

    // Offset by 1-sin(pi/8) to set to 0 if near axis, 1 if away
    normalizedDirection = sign(normalizedDirection) * floor(abs(normalizedDirection) + 0.617316);

    // Place -1.0 - 1.0 within 0 - 1.0
    normalizedDirection = (normalizedDirection + 1.0) * 0.5;

	gl_FragColor = vec4(gradientMagnitude, normalizedDirection.x, normalizedDirection.y, 1.0);
}
