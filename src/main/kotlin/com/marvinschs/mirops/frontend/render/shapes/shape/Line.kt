package com.marvinschs.mirops.frontend.render.shapes.shape

import com.marvinschs.mirops.frontend.render.RenderManager
import com.marvinschs.mirops.frontend.render.shader.Shader
import com.marvinschs.mirops.frontend.render.shader.components.Ebo
import com.marvinschs.mirops.frontend.render.shader.components.Vao
import com.marvinschs.mirops.frontend.render.shader.components.Vbo
import com.marvinschs.mirops.frontend.render.types.IRenderable
import org.joml.Matrix4f
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.glBindVertexArray

class Line(
    x: Number,
    y: Number,
    x2: Number,
    y2: Number,
    private var color: FloatArray = floatArrayOf(1f, 1f, 1f),
    private var rotationDeg: Number = 0.0f,
    private var scale: Pair<Number, Number> = Pair(1f, 1f),
): IRenderable {
    //need this to make var not settable from outside this class
    var x: Float = x.toFloat()
        private set
    var y: Float = y.toFloat()
        private set
    var x2: Float = x2.toFloat()
        private set
    var y2: Float = y2.toFloat()
        private set
    
    private val shader: Shader = Shader("default/default.vert", "default/default.frag")
    
    private var vao = Vao()
    private var vbo: Vbo
    private var ebo: Ebo
    
    private val vertices = floatArrayOf(
        0f, 0f,
        1f, 1f
    )
    private val indices = intArrayOf(
        0,1
    )
    private val drawCount = indices.size
    
    private val uProjection = this.shader.getUniformLocation("uProjection")
    private val uColor = shader.getUniformLocation("uColor")
    
    init {
        this.vao.bind()
        this.vbo = Vbo(this.vertices)
        this.ebo = Ebo(this.indices)
        this.vao.linkVBO(this.vbo, 0, 2)
        
        this.vao.unbind()
        this.vbo.unbind()
        this.ebo.unbind()
    }
    
    private val uModel = this.shader.getUniformLocation("uModel")

    private val model = Matrix4f()
    private var modelDirty = true

    override fun onRender() {
        this.shader.use()

        //the shader only forwards values to OpenGL when they changed since the last upload
        this.shader.setMat4Cached(this.uProjection, RenderManager.getProjection())
        this.shader.setVec3Cached(this.uColor, this.color)

        if (this.modelDirty) {
            this.model.identity()
                .translate(this.x + this.center().first, this.y + this.center().second, 0f)
                .rotateZ(Math.toRadians(this.rotationDeg.toDouble()).toFloat())
                .translate(-this.center().first, -this.center().second, 0f)
                .scale(this.scaleX(), this.scaleY(), 1f)
                .scale(this.scale.first.toFloat(), this.scale.second.toFloat(), 1f)
            this.modelDirty = false
        }

        this.shader.setMat4Cached(this.uModel, this.model)

        this.vao.bind()
        glDrawArrays(GL_LINES, 0, this.drawCount)
        glBindVertexArray(0)
    }
    
    private fun center(): Pair<Float, Float> {
        return Pair(((this.x + this.x2) / 2), ((this.y + this.y2) / 2))
    }
    
    private fun scaleX(): Float {
        return (this.x2 - this.x)
    }
    
    private fun scaleY(): Float {
        return (this.y2 - this.y)
    }
    
    override fun onDelete() {
        this.vao.delete()
        this.vbo.delete()
        this.ebo.delete()
        
        this.shader.delete()
    }
    
    override fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
        return false
    }
    
    fun setPosition(x: Number, y: Number, x2: Number, y2: Number) = apply {
        this.x = x.toFloat()
        this.y = y.toFloat()
        this.x2 = x2.toFloat()
        this.y2 = y2.toFloat()
        this.modelDirty = true
    }

    fun changeColor(newColor: FloatArray) = apply {
        this.color = newColor
    }
    
    fun rotate(deg: Number) = apply {
        this.rotationDeg = this.rotationDeg.toFloat() + deg.toFloat()
        this.modelDirty = true
    }

    fun setScale(x: Number, y: Number) = apply {
        this.scale = Pair(x, y)
        this.modelDirty = true
    }

    fun scale(x: Number, y: Number) = apply {
        this.scale = Pair(this.scale.first.toFloat() + x.toFloat(), this.scale.second.toFloat() + y.toFloat())
        this.modelDirty = true
    }
}