// Per-vertex position information we will pass in.
attribute vec4 a_Position;

// Per-vertex texture coordinate information we will pass in.
attribute vec2 a_TexCoord;

// This will be passed into the fragment shader.
varying vec2 v_TexCoord;

// the core program that defines how this shader functions
void main() {
	// Pass through the texture coordinate.
	v_TexCoord = a_TexCoord;

	// Pass through the position information. This *could* eventually use an MVP matrix.
	gl_Position = a_Position;
}
