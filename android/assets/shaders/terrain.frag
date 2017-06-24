#version 330 core

// not actually used, using other textures now
uniform sampler2DArray texture; // DON'T CHANGE!

flat in float f_groundType;

out vec4 fragColor;

void main() {
  fragColor = texture(texture, vec3(gl_PointCoord, f_groundType));
}