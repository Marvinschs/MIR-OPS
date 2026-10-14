package com.marvinschs.mirops.frontend.render.elements.titlebar

import com.marvinschs.mirops.frontend.render.MainWindow
import com.marvinschs.mirops.frontend.render.elements.basic.Button
import com.marvinschs.mirops.frontend.render.shapes.compounds.Cross
import com.marvinschs.mirops.frontend.render.types.IClickable
import com.marvinschs.mirops.frontend.render.types.IHoverable
import com.marvinschs.mirops.frontend.render.types.IRenderable
import com.marvinschs.mirops.frontend.render.types.IResizable

class CloseButton(
    x: Number,
    y: Number,
    width: Number,
    height: Number
): IRenderable, IResizable, IClickable, IHoverable {
    var x: Float = x.toFloat()
        private set
    var y: Float = y.toFloat()
        private set
    var width: Float = width.toFloat()
        private set
    var height: Float = height.toFloat()
        private set
    
    val button: Button = Button(this.x - this.width, this.y, this.width, this.height, floatArrayOf(0.235F, 0.247F, 0.255F))
    val cross: Cross = Cross(this.x - this.width, this.y, this.width, this.height)
    
    override fun onRender() {
        this.button.onRender()
        this.cross.onRender()
    }
    
    override fun onDelete() {
        this.button.onDelete()
        this.cross.onDelete()
    }
    
    override fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
        return this.button.isMouseOver(mouseX, mouseY)
    }
    
    override fun onClick(mouseX: Float, mouseY: Float) {
        if (this.isMouseOver(mouseX, mouseY)) {
            MainWindow.shutdown()
        }
    }
    
    override fun onSizeChanged(newWidth: Float, newHeight: Float) = apply {
        this.x = newWidth - this.width
        
        //this.cross.onSizeChanged(this.)
    }
    
    override fun onMouseMove(mouseX: Float, mouseY: Float) {
        if (!this.isMouseOver(mouseX, mouseY) && this.button.isDefaultColor()) return
        if (this.button.isMouseOver(mouseX, mouseY)) {
            this.button.setBackgroundColor(floatArrayOf(0.91f, 0.071f, 0.141f))
        } else {
            this.button.resetColor()
        }
    }
    
    
}