package com.marvinschs.mirops.frontend.render.shader.components

import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*

class Vao {
    
    // ID reference for the Vertex Array Object
    val id = glGenVertexArrays()
    
    // Links a VBO to the VAO using a certain layout
    fun linkVBO(vbo: Vbo, layout: Int, size: Int) {
        vbo.bind()
        glVertexAttribPointer(layout, size, GL_FLOAT, false, size * Float.SIZE_BYTES, 0L)
        glEnableVertexAttribArray(layout)
        vbo.unbind()
    }
    
    // Links a VBO with interleaved attributes: [stride] and [offset] are in floats
    fun linkVBO(vbo: Vbo, layout: Int, size: Int, stride: Int, offset: Int) {
        vbo.bind()
        glVertexAttribPointer(layout, size, GL_FLOAT, false, stride * Float.SIZE_BYTES, offset.toLong() * Float.SIZE_BYTES)
        glEnableVertexAttribArray(layout)
        vbo.unbind()
    }

    // Binds the VAO
    fun bind() {
        glBindVertexArray(id)
    }
    
    // Unbinds the VAO
    fun unbind() {
        glBindVertexArray(0)
    }
    
    // Deletes the VAO
    fun delete() {
        glDeleteVertexArrays(id)
    }
}