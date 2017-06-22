#version 330 core

uniform sampler2D u_texture; // DON'T CHANGE!

in vec4 color;
in vec2 texCoords;

out vec4 fragColor;

void main()
{
  fragColor = color * texture(u_texture, texCoords);
}