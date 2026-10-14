package com.marvinschs.mirops.frontend.render.types

interface IRenderable: IElement {
    
    fun onRender()
    
    fun onDelete()
    
    fun isMouseOver(mouseX: Float, mouseY: Float): Boolean
}