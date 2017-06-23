#version 330 core

// not actually used, using other textures now
uniform sampler2DArray u_texture; // DON'T CHANGE!

in vec4 color;
in vec2 texCoords;

out vec4 fragColor;

void main() {
  fragColor = color * texture(u_texture, texCoords);
}