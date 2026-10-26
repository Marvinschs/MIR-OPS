package com.marvinschs.mirops.frontend.render.shapes.shape

import com.marvinschs.mirops.frontend.render.shader.components.Ebo
import com.marvinschs.mirops.frontend.render.RenderManager
import com.marvinschs.mirops.frontend.render.shader.Shader
import com.marvinschs.mirops.frontend.render.shader.components.Vao
import com.marvinschs.mirops.frontend.render.shader.components.Vbo
import com.marvinschs.mirops.frontend.render.types.IRenderable
import com.marvinschs.mirops.frontend.render.types.IResizable
import org.joml.Matrix4f
import org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray
import org.lwjgl.opengl.GL11.GL_QUADS
import org.lwjgl.opengl.GL11.GL_UNSIGNED_INT
import org.lwjgl.opengl.GL11.glDrawElements
import kotlin.math.cos
import kotlin.math.sin

/**
 * Constructs a rectangle
 *
 * @param x [Number] the X coordinate of the top left corner in pixel.
 * @param y [Number] the Y coordinate of the top left corner in pixel.
 * @param width [Number] the width of the rectangle in pixels.
 * @param height [Number] the height of the rectangle in pixels.
 * @param color [FloatArray] the color of the rectangle represented by [Float] values ranging from 0 to 1.
 * @param rotationDeg [Float] the rotation of the rectangle in degrees.
 * @param scale [Pair]<[Number],[Number]> the scale of the rectangle in X and Y axis values are a multiplier for the size
 */
class Rectangle @JvmOverloads constructor (
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    private var color: FloatArray = floatArrayOf(1f, 0.75f, 1f),
    private var rotationDeg: Number = 0.0f,
    private var scale: Pair<Number, Number> = Pair(1f, 1f),
): IRenderable, IResizable {
    //need this to make var not settable from outside this class
    var x: Number = x
        private set
    var y: Number = y
        private set
    var width: Number = width
        private set
    var height: Number = height
        private set
    
    private val shader: Shader = Shader("rectangle/rect.vert", "rectangle/rect.frag")
    
    private var vao = Vao()
    private var vbo: Vbo
    private var ebo: Ebo
    
    //Vertices coordinates
    private val vertices = floatArrayOf(
        0f, 0f,
        1f, 0f,
        1f, 1f,
        0f, 1f,
        )
    private val indices = intArrayOf(
        0, 1, 2, 3
    )
    private val drawCount = indices.size
    
    private val uProjection = this.shader.getUniformLocation("uProjection")
    private val uColor = this.shader.getUniformLocation("uColor")
    
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
                .translate(this.x.toFloat() + this.width.toFloat() / 2f, this.y.toFloat() + this.height.toFloat() / 2f, 0f)
                .rotateZ(Math.toRadians(this.rotationDeg.toDouble()).toFloat())
                .translate(-0.5f * this.width.toFloat(), -0.5f * this.height.toFloat(), 0f)
                .scale(this.width.toFloat(), this.height.toFloat(), 1f)
                .scale(this.scale.first.toFloat(), this.scale.second.toFloat(), 1f)
            this.modelDirty = false
        }

        this.shader.setMat4Cached(this.uModel, this.model)

        this.vao.bind()
        glDrawElements(GL_QUADS, this.drawCount, GL_UNSIGNED_INT, 0)
        glBindVertexArray(0)
    }
    
    override fun onDelete() {
        this.vao.delete()
        this.vbo.delete()
        this.ebo.delete()
        
        this.shader.delete()
    }
    
    //accounts for the made rotation of this rectangle
    override fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
        val cx = this.x.toFloat() + this.width.toFloat() / 2f
        val cy = this.y.toFloat() + this.height.toFloat() / 2f
        
        val dx = mouseX - cx
        val dy = mouseY - cy
        val cos = cos(Math.toRadians(this.rotationDeg.toDouble()).toFloat())
        val sin = sin(Math.toRadians(this.rotationDeg.toDouble()).toFloat())
        val localX = dx * cos - dy * sin
        val localY = dx * sin - dy * cos
        
        val testX = localX + cx
        val testY = localY + cy
        return !(testX !in this.x.toFloat() .. (this.x.toFloat() + this.width.toFloat()) ||
                testY !in this.y.toFloat() .. (this.y.toFloat() + this.height.toFloat()))
    }
    
    override fun onSizeChanged(newWidth: Float, newHeight: Float) = apply {
        this.width = newWidth
        this.height = newHeight
        this.modelDirty = true
    }

    fun setPosition(x: Number, y: Number) = apply {
        this.x = x.toFloat()
        this.y = y.toFloat()
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