package com.marvinschs.mirops.frontend.render.elements.titlebar

import com.marvinschs.mirops.frontend.render.MainWindow
import com.marvinschs.mirops.frontend.render.elements.basic.Button
import com.marvinschs.mirops.frontend.render.shapes.compounds.Maximize
import com.marvinschs.mirops.frontend.render.types.IClickable
import com.marvinschs.mirops.frontend.render.types.IHoverable
import com.marvinschs.mirops.frontend.render.types.IRenderable
import com.marvinschs.mirops.frontend.render.types.IResizable

/**
 * Toggles between maximized and restored window. Sits directly left of the [CloseButton].
 */
class MaximizeButton(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    rightOffset: Number,
): IRenderable, IResizable, IClickable, IHoverable {
    private val defaultColor = floatArrayOf(0.235F, 0.247F, 0.255F)
    private val hoverColor = floatArrayOf(0.329F, 0.341F, 0.349F)

    val button: Button = Button(x.toFloat() - width.toFloat() - rightOffset.toFloat(), y, width, height, this.defaultColor, rightOffset)
    val icon: Maximize = Maximize(x.toFloat() - width.toFloat() - rightOffset.toFloat(), y, width, height, rightOffset)

    override fun onRender() {
        this.button.onRender()
        this.icon.onRender()
    }

    override fun onDelete() {
        this.button.onDelete()
        this.icon.onDelete()
    }

    override fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
        return this.button.isMouseOver(mouseX, mouseY)
    }

    override fun onClick(mouseX: Float, mouseY: Float) {
        if (this.isMouseOver(mouseX, mouseY)) {
            MainWindow.toggleMaximize()
        }
    }

    override fun onSizeChanged(newWidth: Float, newHeight: Float) = apply {
        this.button.onSizeChanged(newWidth, newHeight)
        this.icon.onSizeChanged(newWidth, newHeight)
    }

    override fun onMouseMove(mouseX: Float, mouseY: Float) {
        if (!this.isMouseOver(mouseX, mouseY) && this.button.isDefaultColor()) return
        if (this.button.isMouseOver(mouseX, mouseY)) {
            this.button.setBackgroundColor(this.hoverColor)
        } else {
            this.button.resetColor()
        }
    }
}
