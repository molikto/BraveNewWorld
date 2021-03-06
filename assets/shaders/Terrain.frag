#version 330 core

// not actually used, using other textures now
uniform sampler2DArray texture; // DON'T CHANGE!

flat in int index;
flat in ivec2 tileTextureIndex;
flat in vec4 tintColor;

out vec4 fragColor;

// in game coordinates
const float paddingSize = 0.2;
const float pointSize = 1 + paddingSize * 2;

void main() {
  vec2 pointCoord = gl_PointCoord * pointSize - paddingSize;
  vec2 diff = max(abs(pointCoord - 0.5) - 0.5, 0);
  vec2 texturePos = fract((pointCoord + tileTextureIndex) / 16);
  fragColor = tintColor * texture(texture, vec3(texturePos, index));
  fragColor.a = 1 - min(distance(diff / paddingSize, vec2(0, 0)), 1);
}