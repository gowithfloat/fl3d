// sets the precision for this GPU
precision mediump float;

// receives color data from the CPU
uniform vec4 u_Color;

// the core program that defines how this shader functions
void main() {
    // pass through the color value
    gl_FragColor = u_Color;
}
