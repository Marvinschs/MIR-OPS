package com.marvinschs.mirops.frontend.render.types

interface IClickable: IElement {
    
    fun onClick(mouseX: Float, mouseY: Float)
}