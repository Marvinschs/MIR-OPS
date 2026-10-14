package com.marvinschs.mirops.frontend.render.shader

import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL20.*
import org.lwjgl.system.MemoryStack

class Shader(vertexFile: String, fragmentFile: String) {
    
    // Reference ID of the Shader Program
    val id: Int
    
    init {
        val vertexShader = VertexShader(vertexFile)
        val fragmentShader = FragmentShader(fragmentFile)
        
        // Create Shader Program Object and get its reference
        id = glCreateProgram()
        // Attach the Vertex and Fragment Shaders to the Shader Program
        glAttachShader(id, vertexShader.id)
        glAttachShader(id, fragmentShader.id)
        // Wrap-up/Link all the shaders together into the Shader Program
        glLinkProgram(id)
        checkCompileErrors(id)
        
        // Delete the now useless Vertex and Fragment Shader objects
        vertexShader.delete()
        fragmentShader.delete()
    }
    
    // Checks for shader compilation
    private fun checkCompileErrors(shader: Int) {
        val success = glGetProgrami(shader, GL_LINK_STATUS)
        if (success == 0) {
            val log = glGetProgramInfoLog(shader)
            println("ERROR::PROGRAM_LINKING_ERROR:\n$log")
        }
    }
    
    // Activates the Shader Program
    fun use() {
        glUseProgram(id)
    }
    
    // Deletes the Shader Program
    fun delete() {
        glDeleteProgram(id)
    }
    
    // Uniform helpers used by SceneNode/Rectangle to feed per-node transform + color in.
    // Assumes this shader's program is already active (call activate() first).
    private fun location(name: String): Int = glGetUniformLocation(id, name)
    
    fun getUniformLocation(name: String): Int = this.location(name)
    
    fun setMat4(name: String, matrix: Matrix4f) {
        MemoryStack.stackPush().use { stack ->
            val buffer = matrix.get(stack.mallocFloat(16))
            glUniformMatrix4fv(location(name), false, buffer)
        }
    }
    
    fun setVec3(location: Int, vector: Vector3f) {
        glUniform3f(location, vector.x, vector.y, vector.z)
    }
    
    fun setVec3(location: Int, vector: FloatArray) {
        setVec3(location, Vector3f(vector))
        
    }
    
    fun setFloat(name: String, value: Float) {
        glUniform1f(location(name), value)
    }
    
    fun setInt(name: String, value: Int) {
        glUniform1i(location(name), value)
    }
}