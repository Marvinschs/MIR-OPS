package com.marvinschs.mirops.frontend.render.shapes.compounds

import com.marvinschs.mirops.frontend.render.shapes.shape.Line
import com.marvinschs.mirops.frontend.render.types.IRenderable
import com.marvinschs.mirops.frontend.render.types.IResizable

/**
 * The square icon of a maximize button, drawn with four lines.
 */
class Maximize @JvmOverloads constructor(
    x: Number,
    y: Number,
    width: Number,
    height: Number = 32,
    private val rightOffset: Number = 0,
): IRenderable, IResizable {
    //need this to make var not settable from outside this class
    var x: Float = x.toFloat()
        private set
    var y: Float = y.toFloat()
        private set
    var width: Float = width.toFloat()
        private set
    var height: Float = height.toFloat()
        private set

    private val top1 = Line(0, 0, 0, 0)
    private val bottom1 = Line(0, 0, 0, 0)
    private val left1 = Line(0, 0, 0, 0)
    private val right1 = Line(0, 0, 0, 0)
    
    private val top2 = Line(0, 0, 0, 0)
    private val bottom2 = Line(0, 0, 0, 0)
    private val left2 = Line(0, 0, 0, 0)
    private val right2 = Line(0, 0, 0, 0)

    init {
        this.layout()
    }

    private fun layout() {
        val l = this.x + 19
        val r = this.x + 26
        val t = this.y + 13
        val b = this.y + 20

        this.top1.setPosition(l, t, r, t)
        this.right1.setPosition(r, t, r, b)
        this.bottom1.setPosition(l, b, r, b)
        this.left1.setPosition(l, t, l, b + 1)
        
        this.left2.setPosition(l + 2, t - 2, l + 2, t)
        this.top2.setPosition(l + 2, t - 2, r + 2, t - 2)
        this.right2.setPosition(r + 2, t - 2, r + 2, b - 2)
        this.bottom2.setPosition(r, b - 2, r + 2, b - 2)
    }

    override fun onRender() {
        this.top1.onRender()
        this.bottom1.onRender()
        this.left1.onRender()
        this.right1.onRender()
        
        this.top2.onRender()
        this.bottom2.onRender()
        this.left2.onRender()
        this.right2.onRender()
    }

    override fun onDelete() {
        this.top1.onDelete()
        this.bottom1.onDelete()
        this.left1.onDelete()
        this.right1.onDelete()
        
        this.top2.onDelete()
        this.bottom2.onDelete()
        this.left2.onDelete()
        this.right2.onDelete()
    }

    override fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
        return false
    }

    override fun onSizeChanged(newWidth: Float, newHeight: Float) = apply {
        this.x = newWidth - this.width - this.rightOffset.toFloat()
        this.layout()
    }
}
