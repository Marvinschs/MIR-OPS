package com.marvinschs.mirops.frontend.render.shader.components

import org.lwjgl.opengl.GL15.*
import org.lwjgl.system.MemoryUtil

class Ebo(indices: IntArray) {
    
    // ID reference of the Elements Buffer Object
    val id: Int = glGenBuffers()
    
    init {
        // Constructor that generates an Elements Buffer Object and links it to indices
        val buffer = MemoryUtil.memAllocInt(indices.size)
        buffer.put(indices).flip()
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW)
        
        MemoryUtil.memFree(buffer)
    }
    
    // Binds the EBO
    fun bind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id)
    }
    
    // Unbinds the EBO
    fun unbind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
    }
    
    // Deletes the EBO
    fun delete() {
        glDeleteBuffers(id)
    }
}