package com.marvinschs.mirops.frontend.render.elements.basic

import com.marvinschs.mirops.frontend.render.shapes.shape.Rectangle
import com.marvinschs.mirops.frontend.render.types.IRenderable
import com.marvinschs.mirops.frontend.render.types.IResizable

open class Button @JvmOverloads constructor(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    private val defaultColor: FloatArray = floatArrayOf(0.235F, 0.247F, 0.255F),
    private val rightOffset: Number = 0,
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
    
    private var backgroundColor: FloatArray = defaultColor
    private val background: Rectangle = Rectangle(this.x, this.y, this.width, this.height, this.backgroundColor)
    
    override fun onRender() {
        this.background.onRender()
    }
    
    override fun onDelete() {
        this.background.onDelete()
    }
    
    override fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
        return this.background.isMouseOver(mouseX, mouseY)
    }
    
    fun setBackgroundColor(newColor: FloatArray) = apply {
        if (!this.backgroundColor.contentEquals(newColor)) {
            this.backgroundColor = newColor
            this.background.changeColor(this.backgroundColor)
        }
    }
    
    override fun onSizeChanged(newWidth: Float, newHeight: Float) = apply {
        this.x = newWidth - this.width.toFloat() - this.rightOffset.toFloat()
        this.background.setPosition(this.x, this.y)
    }
    
    fun resetColor() = apply {
        this.setBackgroundColor(defaultColor)
    }
    
    fun isDefaultColor() = this.backgroundColor.contentEquals(defaultColor)
}