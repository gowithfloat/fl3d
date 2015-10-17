// Per-vertex position information we will pass in.
attribute vec4 a_Position;

// Per-vertex texture coordinate information we will pass in.
attribute vec2 a_TexCoord;

// This will be passed into the fragment shader.
varying vec2 v_TexCoord;
varying vec2 v_blurTexCoords[14];

// the core program that defines how this shader functions
void main() {
	// Pass through the texture coordinate.
	v_TexCoord = a_TexCoord;

	// Pass through the position information. This *could* eventually use an MVP matrix.
	gl_Position = a_Position;

	v_blurTexCoords[ 0] = v_TexCoord + vec2(-0.028, -0.028);
    v_blurTexCoords[ 1] = v_TexCoord + vec2(-0.024, -0.024);
    v_blurTexCoords[ 2] = v_TexCoord + vec2(-0.020, -0.020);
    v_blurTexCoords[ 3] = v_TexCoord + vec2(-0.016, -0.016);
    v_blurTexCoords[ 4] = v_TexCoord + vec2(-0.012, -0.012);
    v_blurTexCoords[ 5] = v_TexCoord + vec2(-0.008, -0.008);
    v_blurTexCoords[ 6] = v_TexCoord + vec2(-0.004, -0.004);
    v_blurTexCoords[ 7] = v_TexCoord + vec2( 0.004,  0.004);
    v_blurTexCoords[ 8] = v_TexCoord + vec2( 0.008,  0.008);
    v_blurTexCoords[ 9] = v_TexCoord + vec2( 0.012,  0.012);
    v_blurTexCoords[10] = v_TexCoord + vec2( 0.016,  0.016);
    v_blurTexCoords[11] = v_TexCoord + vec2( 0.020,  0.020);
    v_blurTexCoords[12] = v_TexCoord + vec2( 0.024,  0.024);
    v_blurTexCoords[13] = v_TexCoord + vec2( 0.028,  0.028);
}
