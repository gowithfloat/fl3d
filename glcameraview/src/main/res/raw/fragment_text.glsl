// sets the precision for this GPU
precision mediump float;

// texture coordinate information from the vertex shader.
varying vec2 v_TexCoord;

// storage for texture data
uniform sampler2D u_Texture;

// width and height of a GPU pixel in screen space
uniform float u_TexelWidth;
uniform float u_TexelHeight;

const vec4 BLACK = vec4(0.0, 0.0, 0.0, 1.0);

float avgArray(float a[5]) {
    return (a[0] + a[1] + a[2] + a[3] + a[4]) / 5.0;
}

float sumArrayRed(vec4 a[5]) {
    return a[0].r + a[1].r + a[2].r + a[3].r + a[4].r;
}

float sumArrayGreen(vec4 a[5]) {
    return a[0].g + a[1].g + a[2].g + a[3].g + a[4].g;
}

float sumArrayBlue(vec4 a[5]) {
    return a[0].b + a[1].b + a[2].b + a[3].b + a[4].b;
}

float avgArrayRed(vec4 a[5]) {
    return sumArrayRed(a) / 5.0;
}

float avgArrayGreen(vec4 a[5]) {
    return sumArrayGreen(a) / 5.0;
}

float avgArrayBlue(vec4 a[5]) {
    return sumArrayBlue(a) / 5.0;
}

/**
 * A fragment shader that determines localized edge directionality.
 */
void main() {
    // defines the relative position of the pixel above this one
    vec2 up = vec2(0.0, u_TexelHeight);

    // defines the relative position of the pixel to the right of this one
    vec2 right = vec2(u_TexelWidth, 0.0);

    // get colors in a local grid from [-2, -2] to [2, 2], indexed 0 - 25
    // first row
    vec4 row1[5];
    row1[0] = texture2D(u_Texture, v_TexCoord - up - up - right - right);
    row1[1] = texture2D(u_Texture, v_TexCoord - up - up - right);
    row1[2] = texture2D(u_Texture, v_TexCoord - up - up);
    row1[3] = texture2D(u_Texture, v_TexCoord - up - up + right);
    row1[4] = texture2D(u_Texture, v_TexCoord - up - up + right + right);
    // second row
    vec4 row2[5];
    row2[0] = texture2D(u_Texture, v_TexCoord - up - right - right);
    row2[1] = texture2D(u_Texture, v_TexCoord - up - right);
    row2[2] = texture2D(u_Texture, v_TexCoord - up);
    row2[3] = texture2D(u_Texture, v_TexCoord - up + right);
    row2[4] = texture2D(u_Texture, v_TexCoord - up + right + right);
    // third row
    vec4 row3[5];
    row3[0] = texture2D(u_Texture, v_TexCoord - right - right);
    row3[1] = texture2D(u_Texture, v_TexCoord - right);
    row3[2] = texture2D(u_Texture, v_TexCoord);
    row3[3] = texture2D(u_Texture, v_TexCoord + right);
    row3[4] = texture2D(u_Texture, v_TexCoord + right + right);
    // fourth row
    vec4 row4[5];
    row4[0] = texture2D(u_Texture, v_TexCoord + up - right - right);
    row4[1] = texture2D(u_Texture, v_TexCoord + up - right);
    row4[2] = texture2D(u_Texture, v_TexCoord + up);
    row4[3] = texture2D(u_Texture, v_TexCoord + up + right);
    row4[4] = texture2D(u_Texture, v_TexCoord + up + right + right);
    // fifth row
    vec4 row5[5];
    row5[0] = texture2D(u_Texture, v_TexCoord + up + up - right - right);
    row5[1] = texture2D(u_Texture, v_TexCoord + up + up - right);
    row5[2] = texture2D(u_Texture, v_TexCoord + up + up);
    row5[3] = texture2D(u_Texture, v_TexCoord + up + up + right);
    row5[4] = texture2D(u_Texture, v_TexCoord + up + up + right + right);

    // sum, get ratios
    float red[5];
    float blu[5];
    red[0] = avgArrayRed(row1);
    blu[0] = avgArrayBlue(row1);
    red[1] = avgArrayRed(row2);
    blu[1] = avgArrayBlue(row2);
    red[2] = avgArrayRed(row3);
    blu[2] = avgArrayBlue(row3);
    red[3] = avgArrayRed(row4);
    blu[3] = avgArrayBlue(row4);
    red[4] = avgArrayRed(row5);
    blu[4] = avgArrayBlue(row5);

    float c = avgArray(red) / avgArray(blu);

    gl_FragColor = vec4(c, c, c, 1.0);
}
