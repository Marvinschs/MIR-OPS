package com.marvinschs.mirops.frontend.render.shapes.shape

import org.lwjgl.opengl.GL11.GL_NEAREST
import org.lwjgl.opengl.GL11.GL_RED
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER
import org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER
import org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S
import org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T
import org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT
import org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE
import org.lwjgl.opengl.GL11.glBindTexture
import org.lwjgl.opengl.GL11.glDeleteTextures
import org.lwjgl.opengl.GL11.glGenTextures
import org.lwjgl.opengl.GL11.glPixelStorei
import org.lwjgl.opengl.GL11.glTexImage2D
import org.lwjgl.opengl.GL11.glTexParameteri
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import org.lwjgl.opengl.GL30.GL_R8
import org.lwjgl.system.MemoryUtil
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.FileNotFoundException
import java.awt.Font as AwtFont

/**
 * A font rasterized by Java2D into a bitmap atlas, the same way IntelliJ gets its text: the JVM's text engine
 * (DirectWrite/FreeType on the JetBrains Runtime) draws every glyph with the platform's hinting and anti-aliasing.
 *
 * The glyphs (printable ASCII) are rasterized once at one fixed [size], so there is one [Font] per size, see [get].
 * [Text] draws them 1:1 on whole pixels, which keeps them as crisp as they were rasterized.
 * Variable fonts are rendered with their default instance.
 *
 * Needs a current OpenGL context. Call [delete] when the font isn't needed anymore.
 *
 * @param resource [String] the file name of the font inside the `fonts` resource folder.
 * @param size [Int] the font size in pixels (the em size, like the point size in IntelliJ).
 */
class Font(
    private val resource: String,
    val size: Int,
) {

    /**
     * Position of one glyph in the atlas and how to place it, all values in pixels.
     * [xOffset] and [yOffset] are the distance of the quad's top left corner to the pen on the baseline.
     */
    class Glyph(
        val u0: Float, val v0: Float, val u1: Float, val v1: Float,
        val xOffset: Float, val yOffset: Float,
        val width: Float, val height: Float,
        val advance: Float,
    )

    val glyphs = HashMap<Int, Glyph>()

    //distance from the baseline to the top of the line, to the bottom of the line and the distance between two lines
    val ascent: Float
    val descent: Float
    val lineHeight: Float

    //the atlas texture
    val textureId: Int

    init {
        val awtFont = (this::class.java.classLoader.getResourceAsStream("fonts/$resource")?.use {
            AwtFont.createFont(AwtFont.TRUETYPE_FONT, it)
        } ?: throw FileNotFoundException("Could not find font file: $resource")).deriveFont(this.size.toFloat())

        //the metrics have to be measured with the same hints the glyphs are drawn with
        val measure = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics()
        measure.applyTextHints()
        val metrics = measure.getFontMetrics(awtFont)
        measure.dispose()

        this.ascent = metrics.ascent.toFloat()
        this.descent = metrics.descent.toFloat()
        this.lineHeight = (metrics.ascent + metrics.descent + metrics.leading).toFloat()

        //every glyph gets a cell of the same size, a pixel of padding keeps overhanging edges
        val cellWidth = metrics.maxAdvance + 2 * PADDING
        val cellHeight = metrics.ascent + metrics.descent + 2 * PADDING
        val columns = ATLAS_WIDTH / cellWidth
        val count = LAST_CODEPOINT - FIRST_CODEPOINT + 1
        val rows = (count + columns - 1) / columns
        val atlasHeight = rows * cellHeight

        val image = BufferedImage(ATLAS_WIDTH, atlasHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        graphics.applyTextHints()
        graphics.font = awtFont
        graphics.color = java.awt.Color.WHITE

        for (codepoint in FIRST_CODEPOINT..LAST_CODEPOINT) {
            val index = codepoint - FIRST_CODEPOINT
            val x = (index % columns) * cellWidth
            val y = (index / columns) * cellHeight

            graphics.setClip(x, y, cellWidth, cellHeight)
            graphics.drawString(codepoint.toChar().toString(), x + PADDING, y + PADDING + metrics.ascent)

            this.glyphs[codepoint] = Glyph(
                x.toFloat() / ATLAS_WIDTH, y.toFloat() / atlasHeight,
                (x + cellWidth).toFloat() / ATLAS_WIDTH, (y + cellHeight).toFloat() / atlasHeight,
                -PADDING.toFloat(), -(PADDING + metrics.ascent).toFloat(),
                cellWidth.toFloat(), cellHeight.toFloat(),
                metrics.charWidth(codepoint.toChar()).toFloat(),
            )
        }
        graphics.dispose()

        //only the coverage is needed, the color comes from the text
        val argb = image.getRGB(0, 0, ATLAS_WIDTH, atlasHeight, null, 0, ATLAS_WIDTH)
        val atlas = MemoryUtil.memAlloc(argb.size)
        for (pixel in argb) {
            atlas.put((pixel ushr 24).toByte())
        }
        atlas.flip()

        this.textureId = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, this.textureId)
        //the atlas is one byte per pixel, which doesn't match the default row alignment of 4
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, ATLAS_WIDTH, atlasHeight, 0, GL_RED, GL_UNSIGNED_BYTE, atlas)
        //the glyphs are drawn 1:1 on whole pixels, so no filtering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        glBindTexture(GL_TEXTURE_2D, 0)

        MemoryUtil.memFree(atlas)
    }

    //characters that are not in the atlas are drawn as a question mark
    fun getGlyph(codepoint: Int): Glyph = this.glyphs[codepoint] ?: this.glyphs.getValue('?'.code)

    fun delete() {
        glDeleteTextures(this.textureId)
        cache.remove(Pair(this.resource, this.size))
    }


    private fun java.awt.Graphics2D.applyTextHints() {
        //grayscale anti-aliasing, subpixel (LCD) text needs an opaque background
        this.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        //whole pixel advances, so every glyph starts on a pixel boundary
        this.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF)
    }

    companion object {
        private const val FIRST_CODEPOINT = 32
        private const val LAST_CODEPOINT = 126
        private const val ATLAS_WIDTH = 512
        private const val PADDING = 1

        const val DEFAULT_RESOURCE = "InterVariable.ttf"

        private val cache = HashMap<Pair<String, Int>, Font>()

        init {
            //there is no window on the AWT side, only its text engine is used
            System.setProperty("java.awt.headless", "true")
        }

        /**
         * The font of the given size, created on first use so a GL context has to be current by then.
         * Fonts are shared between texts.
         */
        fun get(resource: String = DEFAULT_RESOURCE, size: Int = 13): Font =
            cache.getOrPut(Pair(resource, size)) { Font(resource, size) }
    }
}
