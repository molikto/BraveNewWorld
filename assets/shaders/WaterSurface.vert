#version 330 core

uniform mat4 projection; // map -> framebuffer
uniform sampler2DArray texture;

in vec2 position;
in float v_index; // TODO change to integers

flat out int f_index;
flat out ivec2 tileTextureIndex; // the texture index of the center of the point


void main()
{
   gl_Position = projection * vec4(position, 0, 1);
   f_index = int(v_index);
   // every texture covers 16 tiles
   tileTextureIndex = ivec2(int(position.x) % 16, 15 - int(position.y) % 16);
}