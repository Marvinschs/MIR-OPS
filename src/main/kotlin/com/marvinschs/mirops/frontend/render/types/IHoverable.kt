package com.marvinschs.mirops.frontend.render.types

interface IHoverable: IElement {

    fun onMouseEnter(mouseX: Float, mouseY: Float) {}
    
    fun onMouseLeave(mouseX: Float, mouseY: Float) {}
    
    fun onMouseMove(mouseX: Float, mouseY: Float)
}