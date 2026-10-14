package com.marvinschs.mirops.frontend.render.shader

import org.lwjgl.opengl.GL20.*
import java.io.File
import java.io.FileNotFoundException

open class ShaderProgram(glType: Int, filename: String, private val label: String) {
    
    val id: Int = glCreateShader(glType)
    
    init {
        val code = getFileContents(filename)
        glShaderSource(this.id, code)
        glCompileShader(this.id)
        checkCompileErrors()
    }
    
    // Reads a text file (from the classpath, falling back to the filesystem)
    // and outputs a string with everything in the file
    private fun getFileContents(filename: String): String {
        // Try the classpath first (e.g. src/main/resources) ...
        this::class.java.classLoader.getResourceAsStream("shaders/$filename")?.use { stream ->
            return stream.bufferedReader().readText()
        }
        // fall back to a plain filesystem path
        val file = File(filename)
        if (file.exists()) {
            return file.readText()
        }
        throw FileNotFoundException("Could not find shader file: $filename")
    }
    
    // Checks for shader compilation errors
    private fun checkCompileErrors() {
        val success = glGetShaderi(this.id, GL_COMPILE_STATUS)
        if (success == 0) {
            val log = glGetShaderInfoLog(this.id)
            println("ERROR::SHADER_COMPILATION_ERROR of type: $label\n$log")
        }
    }
    
    // Deletes this individual shader object (call after linking into a program)
    fun delete() {
        glDeleteShader(this.id)
    }
}