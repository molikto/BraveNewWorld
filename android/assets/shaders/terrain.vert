#version 330 core

/**

terrain drawing:

1.
using a uniform array texture 2d
use point primitive
one tile = one vertex = (game position, tile type)


one texture = 16 tile, so position.toInt % 16
texture size is got from textureSize function
*/

uniform mat4 projection; // map -> framebuffer
uniform sampler2DArray texture; // DON'T CHANGE!

 // TODO change all of them to integers
in vec2 position;
in float v_groundType;
flat out int f_groundType;
flat out ivec2 tileTextureIndex; // the texture index of the center of the point


void main()
{
   gl_Position = projection * vec4(position, 0, 0);
   f_groundType = int(v_groundType);
   tileTextureIndex = ivec2(int(position.x) % 16, int(position.y) % 16);
}