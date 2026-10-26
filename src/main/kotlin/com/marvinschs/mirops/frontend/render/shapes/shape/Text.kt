package com.marvinschs.mirops.frontend.render.shapes.shape

import com.marvinschs.mirops.frontend.render.RenderManager
import com.marvinschs.mirops.frontend.render.shader.Shader
import com.marvinschs.mirops.frontend.render.shader.components.Ebo
import com.marvinschs.mirops.frontend.render.shader.components.Vao
import com.marvinschs.mirops.frontend.render.shader.components.Vbo
import com.marvinschs.mirops.frontend.render.types.IRenderable
import org.joml.Matrix4f
import org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray
import org.lwjgl.opengl.GL11.GL_BLEND
import org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA
import org.lwjgl.opengl.GL11.GL_SRC_ALPHA
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11.GL_TRIANGLES
import org.lwjgl.opengl.GL11.GL_UNSIGNED_INT
import org.lwjgl.opengl.GL11.glBindTexture
import org.lwjgl.opengl.GL11.glBlendFunc
import org.lwjgl.opengl.GL11.glDisable
import org.lwjgl.opengl.GL11.glDrawElements
import org.lwjgl.opengl.GL11.glEnable
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import kotlin.math.roundToInt

/**
 * Constructs a text, rendered with a [Font] that is rasterized for exactly the requested size.
 *
 * @param text [String] the text to render, `\n` starts a new line. Characters outside of printable ASCII are drawn as `?`.
 * @param x [Number] the X coordinate of the top left corner in pixel.
 * @param y [Number] the Y coordinate of the top left corner in pixel.
 * @param size [Number] the font size in pixels, rounded to whole pixels (the em size, 13 matches the default size of IntelliJ's UI font).
 * @param color [FloatArray] the color of the text represented by [Float] values ranging from 0 to 1.
 * @param fontResource [String] the file name of the font inside the `fonts` resource folder.
 */
class Text @JvmOverloads constructor(
    text: String,
    x: Number,
    y: Number,
    size: Number = 13,
    private var color: FloatArray = floatArrayOf(1f, 1f, 1f),
    private val fontResource: String = Font.DEFAULT_RESOURCE,
): IRenderable {
    //need this to make var not settable from outside this class
    var text: String = text
        private set
    var x: Number = x
        private set
    var y: Number = y
        private set
    var size: Int = size.toFloat().roundToInt()
        private set

    //shared between texts of the same size and not deleted here
    private var font: Font = Font.get(fontResource, this.size)

    //the measured size of the text in pixels
    var width: Float = 0f
        private set
    var height: Float = 0f
        private set

    private val shader: Shader = Shader("text/text.vert", "text/text.frag")

    private val vao = Vao()
    private var vbo: Vbo? = null
    private var ebo: Ebo? = null
    private var drawCount = 0

    private val uProjection = this.shader.getUniformLocation("uProjection")
    private val uModel = this.shader.getUniformLocation("uModel")
    private val uColor = this.shader.getUniformLocation("uColor")
    private val uAtlas = this.shader.getUniformLocation("uAtlas")

    private val model = Matrix4f()
    private var modelDirty = true

    init {
        this.rebuildMesh()
    }

    //rebuilds the quads of every glyph, x and y of the vertices are relative to the top left corner of the text
    private fun rebuildMesh() {
        val vertices = ArrayList<Float>()
        val indices = ArrayList<Int>()

        var penX = 0f
        var penY = this.font.ascent
        var longestLine = 0f
        var lines = 1

        for (char in this.text) {
            if (char == '\n') {
                longestLine = maxOf(longestLine, penX)
                penX = 0f
                penY += this.font.lineHeight
                lines++
                continue
            }

            val glyph = this.font.getGlyph(char.code)
            if (glyph.width > 0f) {
                val left = penX + glyph.xOffset
                val top = penY + glyph.yOffset
                val right = left + glyph.width
                val bottom = top + glyph.height

                val first = vertices.size / 4
                vertices.addAll(listOf(
                    left, top, glyph.u0, glyph.v0,
                    right, top, glyph.u1, glyph.v0,
                    right, bottom, glyph.u1, glyph.v1,
                    left, bottom, glyph.u0, glyph.v1,
                ))
                indices.addAll(listOf(first, first + 1, first + 2, first + 2, first + 3, first))
            }
            penX += glyph.advance
        }

        this.width = maxOf(longestLine, penX)
        this.height = lines * this.font.lineHeight
        this.drawCount = indices.size

        this.vbo?.delete()
        this.ebo?.delete()
        this.vbo = null
        this.ebo = null

        //nothing to draw for an empty text
        if (this.drawCount == 0) return

        this.vao.bind()
        val newVbo = Vbo(vertices.toFloatArray())
        val newEbo = Ebo(indices.toIntArray())
        //interleaved: position (2 floats) followed by uv (2 floats)
        this.vao.linkVBO(newVbo, 0, 2, 4, 0)
        this.vao.linkVBO(newVbo, 1, 2, 4, 2)

        this.vao.unbind()
        newVbo.unbind()
        newEbo.unbind()

        this.vbo = newVbo
        this.ebo = newEbo
    }

    override fun onRender() {
        if (this.drawCount == 0) return

        this.shader.use()

        //the shader only forwards values to OpenGL when they changed since the last upload
        this.shader.setMat4Cached(this.uProjection, RenderManager.getProjection())
        this.shader.setVec3Cached(this.uColor, this.color)

        if (this.modelDirty) {
            //whole pixels, the glyphs are rasterized for the pixel grid and get blurry in between
            this.model.identity().translate(this.x.toFloat().roundToInt().toFloat(), this.y.toFloat().roundToInt().toFloat(), 0f)
            this.modelDirty = false
        }
        this.shader.setMat4Cached(this.uModel, this.model)

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, this.font.textureId)
        this.shader.setInt("uAtlas", 0)

        //the glyph edges are anti-aliased through the alpha value
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        this.vao.bind()
        glDrawElements(GL_TRIANGLES, this.drawCount, GL_UNSIGNED_INT, 0)
        glBindVertexArray(0)

        glDisable(GL_BLEND)
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    //the font is shared between texts and is not deleted here
    override fun onDelete() {
        this.vao.delete()
        this.vbo?.delete()
        this.ebo?.delete()

        this.shader.delete()
    }

    override fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
        return mouseX in this.x.toFloat() .. (this.x.toFloat() + this.width) &&
                mouseY in this.y.toFloat() .. (this.y.toFloat() + this.height)
    }

    fun setText(newText: String) = apply {
        this.text = newText
        this.rebuildMesh()
    }

    fun setPosition(x: Number, y: Number) = apply {
        this.x = x.toFloat()
        this.y = y.toFloat()
        this.modelDirty = true
    }

    fun setSize(newSize: Number) = apply {
        this.size = newSize.toFloat().roundToInt()
        this.font = Font.get(this.fontResource, this.size)
        this.rebuildMesh()
    }

    fun changeColor(newColor: FloatArray) = apply {
        this.color = newColor
    }
}
