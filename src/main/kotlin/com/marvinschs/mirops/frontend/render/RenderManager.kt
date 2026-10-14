package com.marvinschs.mirops.frontend.render

import com.marvinschs.mirops.frontend.render.elements.titlebar.TitleBar
import com.marvinschs.mirops.frontend.render.types.IClickable
import com.marvinschs.mirops.frontend.render.types.IElement
import com.marvinschs.mirops.frontend.render.types.IHoverable
import com.marvinschs.mirops.frontend.render.types.IRenderable
import com.marvinschs.mirops.frontend.render.types.IResizable
import org.joml.Matrix4f

object RenderManager {
    private val renderElements = mutableListOf<IRenderable>()
    private val resizeableElements = mutableListOf<IResizable>()
    private val clickableElements = mutableListOf<IClickable>()
    private val hoverableElements = mutableListOf<IHoverable>()
    private val allLists: List<MutableList<out IElement>> = listOf(
        renderElements,
        resizeableElements,
        clickableElements,
        hoverableElements
    )
    
    fun init() {
        TitleBar("This is a test", this.screenWidth)
    }
    
    fun addElement(element: IElement): IElement {
        if (IRenderable::class.isInstance(element)) {
            println("${element::class.simpleName} passed renderable Check")
            renderElements.add(element as IRenderable)
        }
        if (IResizable::class.isInstance(element)) {
            println("${element::class.simpleName} passed resizable Check")
            resizeableElements.add(element as IResizable)
        }
        if (IClickable::class.isInstance(element)) {
            println("${element::class.simpleName} passed clickable Check")
            clickableElements.add(element as IClickable)
        }
        if (IHoverable::class.isInstance(element)) {
            println("${element::class.simpleName} passed hoverable Check")
            hoverableElements.add(element as IHoverable)
        }
        return element
    }
    
    fun removeElement(element: IRenderable) {
        allLists.forEach { list -> list.remove(element) }
    }
    
    fun render() {
        this.renderElements.forEach { it.onRender() }
    }
    
    fun deleteRecursively() {
        this.renderElements.forEach { it.onDelete() }
        this.allLists.forEach { it.clear() }
    }
    
    private var screenWidth: Float = 800.0F
    private var screenHeight: Float = 800.0F
    
    fun updateWindowSize(width: Float, height: Float) {
        this.screenWidth = width
        this.screenHeight = height
        
        this.resizeableElements.forEach { it.onSizeChanged(this.screenWidth, this.screenHeight) }
    }
    
    fun dispatchClick(mouseX: Float, mouseY: Float) {
        this.clickableElements.forEach { it.onClick(mouseX, mouseY) }
    }
    
    fun dispatchHover(mouseX: Float, mouseY: Float) {
        this.hoverableElements.forEach { it.onMouseMove(mouseX, mouseY) }
    }
    
    fun getProjection(): FloatArray {
        return Matrix4f()
            .ortho(0f, this.screenWidth, this.screenHeight, 0f, -1f, 1f)
            .get(FloatArray(16))
    }
}