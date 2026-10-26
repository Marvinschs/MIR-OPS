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
    
    //unit circle around (0,0): center vertex followed by SEGMENTS + 1 rim vertices (the last one closes the circle)
    private val vertices = FloatArray((SEGMENTS + 2) * 2).apply {
        this[0] = 0f
        this[1] = 0f
        
        for (i in 0 .. SEGMENTS) {
            val angle = (2.0 * Math.PI * i / SEGMENTS).toFloat()
            
            this[(i + 1) * 2] = cos(angle)
            this[(i + 1) * 2 + 1] = sin(angle)
        }
    }
    
    //triangle fan: center, rim0, rim1, ..., rimN
    private val indices = IntArray(SEGMENTS + 2) { it }
    
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
                .translate(this.x, this.y, 0f)
                .rotateZ(Math.toRadians(this.rotationDeg.toDouble()).toFloat())
                .scale(this.radius, this.radius, 1f)
                .scale(this.scale.first.toFloat(), this.scale.second.toFloat(), 1f)
            this.modelDirty = false
        }

        this.shader.setMat4Cached(this.uModel, this.model)

        this.vao.bind()
        
        glDrawElements(GL_TRIANGLE_FAN, this.drawCount, GL_UNSIGNED_INT, 0)
        
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
        this.modelDirty = true
    }

    fun setPosition(x: Float, y: Float) = apply{
        this.x = x
        this.y = y
        this.modelDirty = true
    }
    
    fun changeColor(newColor: FloatArray) = apply {
        this.color = newColor
    }
    
    fun rotate(deg: Number) = apply {
        this.rotationDeg = this.rotationDeg.toFloat() + deg.toFloat()
        this.modelDirty = true
    }

    fun setRadius(radius: Number) = apply {
        this.radius = radius.toFloat()
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