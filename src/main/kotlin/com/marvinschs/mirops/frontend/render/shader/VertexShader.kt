package com.marvinschs.mirops.frontend.render.shader

import org.lwjgl.opengl.GL20.*

class VertexShader(filename: String): ShaderProgram(GL_VERTEX_SHADER, filename, "VERTEX_SHADER")