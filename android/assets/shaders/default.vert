#version 330 core

uniform mat4 u_projTrans; // DON'T CHANGE! LibGDX used

in vec2 a_position; // DON'T CHANGE! LibGDX used
in vec4 a_color; // DON'T CHANGE! LibGDX used
in vec2 a_texCoord0; // DON'T CHANGE! LibGDX used

out vec4 color;
out vec2 texCoords;

void main() {
   color = a_color;
   color.a = color.a * (255.0/254.0);
   texCoords = a_texCoord0;
   gl_Position =  u_projTrans * vec4(a_position, 0, 1);
}