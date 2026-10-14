package com.marvinschs.mirops.frontend.render.shapes.compounds

import com.marvinschs.mirops.frontend.render.shapes.shape.Line
import com.marvinschs.mirops.frontend.render.types.IRenderable
import com.marvinschs.mirops.frontend.render.types.IResizable

class Cross(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
): IRenderable, IResizable {
    //need this to make var not settable from outside this class
    var x: Float = x.toFloat()
        private set
    var y: Float = y.toFloat()
        private set
    var width: Float = width.toFloat()
        private set
    
    val line = Line(
        this.x + 18,
        this.y + 11,
        this.x + 27,
        this.y + 20,
    )
    
    val line2 = Line(
        this.x + 27,
        this.y + 11,
        this.x + 18,
        this.y + 20,
    )
    
    override fun onRender() {
        line.onRender()
        line2.onRender()
    }
    
    override fun onDelete() {
        line.onDelete()
        line2.onDelete()
    }
    
    override fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
        return false
    }
    
    override fun onSizeChanged(newWidth: Float, newHeight: Float) = apply {
    
    }
}