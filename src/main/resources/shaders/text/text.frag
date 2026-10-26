#version 450 core
in vec2 vUv;
uniform sampler2D uAtlas;
uniform vec3 uColor;
out vec4 fragColor;
void main()
{
    //the atlas stores the distance to the glyph outline, 0.5 is exactly on the edge
    float dist = texture(uAtlas, vUv).r;
    float width = max(fwidth(dist), 1e-4);
    float alpha = smoothstep(0.49 - width, 0.49 + width, dist);
    fragColor = vec4(uColor.rgb, alpha);
}
