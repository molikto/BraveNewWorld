#version 330 core

/**

terrain drawing:

1.
using a uniform array texture 2d
use point primitive
one tile = one vertex = (game position, tile type)


*/

uniform mat4 projection; // map -> framebuffer

in vec4 position; // actually vec2 of the center of tile, in map coordinate
in int v_groundType;
out vec2 ;
flat out int f_groundType;

void main()
{
   gl_Position = projection * position;
   f_groundType = v_groundType;
}