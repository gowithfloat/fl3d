// Per-vertex position information we will pass in.
attribute vec4 a_Position;

// the core program that defines how this shader functions
void main() {
	// Pass through the position information.
	gl_Position = a_Position;
}
