package com.marvinschs.mirops.frontend.render.shapes.shape

import com.marvinschs.mirops.frontend.render.RenderManager
import com.marvinschs.mirops.frontend.render.shader.Shader
import com.marvinschs.mirops.frontend.render.shader.components.Ebo
import com.marvinschs.mirops.frontend.render.shader.components.Vao
import com.marvinschs.mirops.frontend.render.shader.components.Vbo
import com.marvinschs.mirops.frontend.render.types.IRenderable
import com.marvinschs.mirops.frontend.render.types.IResizable
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * Constructs a circle.
 * @param x [Number] the X coordinate of the top left corner of the circle's bounding box in pixels.
 * @param y [Number] the Y coordinate of the top left corner of the circle's bounding box in pixels.
 * @param radius [Number] the radius of the circle in pixels.
 * @param color [FloatArray] the color of the circle represented by [Float] values ranging from 0 to 1.
 * @param rotationDeg [Float] the rotation of the circle in degrees.
 * @param scale [Pair]<[Number],[Number]> the scale of the circle in X and Y axis.
  */
class Circle @JvmOverloads constructor (
    x: Number,
    y: Number,
    radius: Number,
    private var color: FloatArray = floatArrayOf(1.0f, 0.75f, 1.0f),
    private var rotationDeg: Number = 0f,
    private var scale: Pair<Number, Number> = Pair(1f, 1f),
): IRenderable, IResizable {
    //need this to make var not settable from outside this class
    var x: Float = x.toFloat()
        private set
    var y: Float = y.toFloat()
        private set
    var radius: Float = radius.toFloat()
        private set
    
    private val shader: Shader = Shader("circle/circle.vert", "circle/circle.frag")
    
    private var vao = Vao()
    private var vbo: Vbo
    private var ebo: Ebo
    
    companion object {
        /**
         * Number of segments used to approximate the circle.
         * Higher values produce a smoother circle.
         */
        private const val SEGMENTS = 64
    }
    
    private val vertices = FloatArray(((SEGMENTS + 2) * 3)).apply {
        this[0] = 0f
        this[1] = 0f
        
        for (i in 0 .. SEGMENTS) {
            val angle = (2.0 * Math.PI * i / SEGMENTS).toFloat()
            
            this[(i) * 2] = cos(angle)
            this[(i) * 2 + 1] = sin(angle)
        }
    }
    
    private val indices = IntArray(((SEGMENTS + 2) * 3)).apply {
        for (i in 0 .. SEGMENTS) {
            this[i * 3] = 0
            this[i * 3 + 1] = i + 1
            this[i * 3 + 2] = i + 2
        }
    }
    
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
    
    override fun onRender() {
        this.shader.use()
        glUniformMatrix4fv(this.uProjection, false, RenderManager.getProjection())
        
        this.shader.setVec3(this.uColor, this.color)
        
        val model = Matrix4f()
            .translate(this.x + this.radius, this.y + this.radius, 0f)
            .rotateZ(Math.toRadians(this.rotationDeg.toDouble()).toFloat())
            .translate(-this.radius, -this.radius, 0f)
            .scale(this.radius, this.radius, 1f)
            .scale(this.scale.first.toFloat(), this.scale.second.toFloat(), 1f)
        
        this.shader.setMat4("uModel", model)
        
        this.vao.bind()
        
        glDrawElements(GL_TRIANGLE_STRIP, this.drawCount, GL_UNSIGNED_INT, 0)
        
        glBindVertexArray(0)
    }
    
    override fun onDelete() {
        this.vao.delete()
        this.vbo.delete()
        this.ebo.delete()
        
        this.shader.delete()
    }
    
    override fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
        val cx = this.x + this.radius
        val cy = this.y + this.radius
        
        val scaleX = this.scale.first.toFloat()
        val scaleY = this.scale.second.toFloat()
        
        val dx = mouseX - cx
        val dy = mouseY - cy
        
        val cos = cos(Math.toRadians(this.rotationDeg.toDouble()).toFloat())
        val sin = sin(Math.toRadians(this.rotationDeg.toDouble()).toFloat())
        
        val localX = dx * cos + dy * sin
        val localY = -dx * sin + dy * cos
        
        val unscaledX = localX / scaleX
        val unscaledY = localY / scaleY
        
        return unscaledX.pow(2) + unscaledY.pow(2) <= radius.pow(2)
    }
    
    override fun onSizeChanged(newWidth: Float, newHeight: Float) = apply {
        this.radius = minOf(newWidth, newHeight) / 2f
    }
    
    fun setPosition(x: Float, y: Float) = apply{
        this.x = x
        this.y = y
    }
    
    fun changeColor(newColor: FloatArray) = apply {
        this.color = newColor
    }
    
    fun rotate(deg: Number) = apply {
        this.rotationDeg = this.rotationDeg.toFloat() + deg.toFloat()
    }
    
    fun setRadius(radius: Number) = apply {
        this.radius = radius.toFloat()
    }
    
    fun setScale(x: Number, y: Number) = apply {
        this.scale = Pair(x, y)
    }
    
    fun scale(x: Number, y: Number) = apply {
        this.scale = Pair(this.scale.first.toFloat() + x.toFloat(), this.scale.second.toFloat() + y.toFloat())
    }
}