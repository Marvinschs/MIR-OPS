package com.marvinschs.mirops.frontend.render.shapes.shape

import com.marvinschs.mirops.frontend.render.shader.components.Ebo
import com.marvinschs.mirops.frontend.render.RenderManager
import com.marvinschs.mirops.frontend.render.shader.Shader
import com.marvinschs.mirops.frontend.render.shader.components.Vao
import com.marvinschs.mirops.frontend.render.shader.components.Vbo
import com.marvinschs.mirops.frontend.render.types.IRenderable
import com.marvinschs.mirops.frontend.render.types.IResizable
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11.GL_TRIANGLES
import org.lwjgl.opengl.GL11.GL_UNSIGNED_INT
import org.lwjgl.opengl.GL11.glDrawElements
import org.lwjgl.opengl.GL20.glUniformMatrix4fv
import org.lwjgl.opengl.GL30.glBindVertexArray
import kotlin.math.cos
import kotlin.math.sin

/**
 * Constructs a Triangle
 *
 * @param x [Number] the X coordinate of the middle in pixel.
 * @param y [Number] the Y coordinate of the middle in pixel.
 * @param width [Number] the width of the triangle in pixels.
 * @param height [Number] the height of the triangle in pixels.
 * @param color [FloatArray] the color of the triangle represented by [Float] values ranging from 0 to 1.
 * @param rotationDeg [Float] the rotation of the triangle in degrees.
 * @param scale [Pair]<[Number],[Number]> the scale of the triangle in X and Y axis values are a multiplier for the size
 */
class Triangle @JvmOverloads constructor(
    x: Number,
    y: Number,
    width: Number,
    height: Number = width,
    private var color: FloatArray = floatArrayOf(1f, 0.75f, 1f),
    private var rotationDeg: Number = 0f,
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
    
    private val shader: Shader = Shader("triangle/triangle.vert", "triangle/triangle.frag")
    
    private val vao = Vao()
    private val vbo: Vbo
    private val ebo: Ebo
    
    //Vertices coordinates
    private val vertices: FloatArray = floatArrayOf(
        -0.5f, 0.5f,
        0.5f, 0.5f,
        0f, -0.5f,
    )
    private val indices: IntArray = intArrayOf(
        0, 1, 2
    )
    private val drawCount: Int = this.indices.size
    
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
    
    override fun onRender() {
        this.shader.use()
        glUniformMatrix4fv(this.uProjection, false, RenderManager.getProjection())
        this.shader.setVec3(this.uColor, this.color)
        
        val model = Matrix4f()
            .translate(this.x.toFloat(), this.y.toFloat(), 0f)
            .rotateZ(Math.toRadians(this.rotationDeg.toDouble()).toFloat())
            .scale(this.width.toFloat(), this.height.toFloat(), 0f)
            .scale(this.scale.first.toFloat(), this.scale.second.toFloat(), 1f)
        
        this.shader.setMat4("uModel", model)
        
        this.vao.bind()
        glDrawElements(GL_TRIANGLES, this.drawCount, GL_UNSIGNED_INT, 0)
        glBindVertexArray(0)
    }
    
    override fun onDelete() {
        this.vao.delete()
        this.vbo.delete()
        this.ebo.delete()
        
        this.shader.delete()
    }
    
    override fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
        val dx = mouseX - this.x.toFloat()
        val dy = mouseY - this.y.toFloat()
        val cos = cos(Math.toRadians(this.rotationDeg.toDouble()).toFloat())
        val sin = sin(Math.toRadians(this.rotationDeg.toDouble()).toFloat())
        val localX = dx * cos + dy * sin
        val localY = -dx * sin + dy * cos
        
        val halfWidth = this.width.toFloat() / 2f
        val halfHeight = this.height.toFloat() / 2f
        
        // Outside vertical bounds
        if (localY < -halfHeight || localY > halfHeight) {
            return false
        }
        
        val normalizedY = (localY + halfHeight) / this.height.toFloat()
        val allowedHalfWidth = halfWidth * normalizedY
        
        return kotlin.math.abs(localX) <= allowedHalfWidth
    }
    
    override fun onSizeChanged(newWidth: Float, newHeight: Float) = apply {
        this.width = newWidth
        this.x = this.width.toFloat() / 2f
        this.height = newHeight
        this.y = this.height.toFloat() / 2f
    }
}