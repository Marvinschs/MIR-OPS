package com.marvinschs.mirops.frontend.render.elements.titlebar

import com.marvinschs.mirops.frontend.render.RenderManager
import com.marvinschs.mirops.frontend.render.shapes.shape.Rectangle
import com.marvinschs.mirops.frontend.render.types.IClickable
import com.marvinschs.mirops.frontend.render.types.IHoverable
import com.marvinschs.mirops.frontend.render.types.IRenderable
import com.marvinschs.mirops.frontend.render.types.IResizable

class TitleBar @JvmOverloads constructor(
    private var title: String,
    private var width: Float,
    private var height: Float = 32.0F,
): IRenderable, IResizable, IClickable, IHoverable {
    
    init {
        this.addToRenderManager()
    }
    
    private val background: Rectangle = Rectangle(0.0F, 0.0F, this.width, 32f, floatArrayOf(0.235F, 0.247F, 0.255F))
    //private val closeButton: Button = Button(this.width - 47F, 0.0f, 47f, this.height, floatArrayOf(0.235F, 0.247F, 0.255F))
    private val closeButton = CloseButton(this.width, 0.0f, 47.0f, this.height)
    
    //private var background = Triangle(this.width / 2f, this.height / 2f, this.width , this.width, rotationDeg = 90)
    
    override fun onRender() {
        this.background.onRender()
        this.closeButton.onRender()
    }
    
    override fun onDelete() {
        this.background.onDelete()
        this.closeButton.onDelete()
    }
    
    override fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
        return this.background.isMouseOver(mouseX, mouseY)
    }
    
    override fun onSizeChanged(newWidth: Float, newHeight: Float) = apply {
        this.width = newWidth
        
        this.background.onSizeChanged(this.width, this.height)
        this.closeButton.onSizeChanged(this.width, this.height)
    }
    
    fun changeTitle(newTitle: String) {
        this.title = newTitle
    }
    
    fun addToRenderManager() = apply {
        RenderManager.addElement(this@TitleBar)
    }
    
    override fun onMouseMove(mouseX: Float, mouseY: Float) {
        this.closeButton.onMouseMove(mouseX, mouseY)
    }
    
    override fun onClick(mouseX: Float, mouseY: Float) {
        this.closeButton.onClick(mouseX, mouseY)
    }
}