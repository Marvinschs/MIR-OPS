package com.marvinschs.mirops.frontend.render.shader

import org.lwjgl.opengl.GL20.*

class FragmentShader(filename: String): ShaderProgram(GL_FRAGMENT_SHADER, filename, "FRAGMENT_SHADER")