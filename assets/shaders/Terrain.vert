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
uniform sampler2DArray texture;

in vec2 position;
in float in_index; // TODO change to integers
in vec4 in_tintColor;

flat out int index;
flat out ivec2 tileTextureIndex; // the texture index of the center of the point
flat out vec4 tintColor;


void main()
{
   gl_Position = projection * vec4(position, 0, 1);
   index = int(in_index);
   // every texture covers 16 tiles
   tileTextureIndex = ivec2(int(position.x) % 16, 15 - int(position.y) % 16);
   tintColor = in_tintColor;
}