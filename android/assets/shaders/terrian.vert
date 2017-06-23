#version 330 core

uniform mat4 projection;

in vec4 a_position;
out vec2 texCoords;

void main()
{
   texCoords = a_texCoord0;
   gl_Position =  projection * a_position;
}