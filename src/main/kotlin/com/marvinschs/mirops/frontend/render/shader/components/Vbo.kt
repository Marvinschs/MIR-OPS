package com.marvinschs.mirops.frontend.render.shader.components

import org.lwjgl.opengl.GL15.*
import org.lwjgl.system.MemoryUtil

class Vbo(vertices: FloatArray) {
    
    // Reference ID of the Vertex Buffer Object
    val id = glGenBuffers()
    
    init {
        // Constructor that generates a Vertex Buffer Object and links it to vertices
        val buffer = MemoryUtil.memAllocFloat(vertices.size)
        buffer.put(vertices).flip()
        
        glBindBuffer(GL_ARRAY_BUFFER, id)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)
        
        MemoryUtil.memFree(buffer)
    }
    
    // Binds the VBO
    fun bind() {
        glBindBuffer(GL_ARRAY_BUFFER, id)
    }
    
    // Unbinds the VBO
    fun unbind() {
        glBindBuffer(GL_ARRAY_BUFFER, 0)
    }
    
    // Deletes the VBO
    fun delete() {
        glDeleteBuffers(id)
    }
}