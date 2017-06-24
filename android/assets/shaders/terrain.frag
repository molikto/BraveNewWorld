#version 330 core

// not actually used, using other textures now
uniform sampler2DArray texture; // DON'T CHANGE!

flat in int f_groundType;
flat in ivec2 tileTextureIndex;

out vec4 fragColor;

void main() {
  vec2 pointCoord = gl_PointCoord * 1.2 - 0.1;
  vec2 texturePos = (pointCoord + tileTextureIndex) / 16;
  if (texturePos.x < 0) {
    texturePos.x = texturePos.x + 1;
  }
  if (texturePos.y < 0) {
    texturePos.y = texturePos.y + 1;
  }
  if (texturePos.x > 1) {
    texturePos.x = texturePos.x - 1;
  }
  if (texturePos.y > 1) {
    texturePos.y = texturePos.y - 1;
  }
  fragColor = texture(texture, vec3(texturePos, f_groundType));
}