package com.marvinschs.mirops.frontend.render

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window
import org.lwjgl.system.windows.User32
import org.lwjgl.system.MemoryUtil.NULL

/**
 * Lets the user resize an undecorated [window] by dragging its edges and corners, which GLFW
 * does not do on its own without the native window frame.
 */
class WindowResizer(
    private val window: Long,
    private val resizable: Boolean,
) {
    //bitmask of the window edges currently dragged, EDGE_NONE when not resizing
    private var resizeEdges = EDGE_NONE
    private var cursorEdges = EDGE_NONE
    private val resizeCursors = mutableMapOf<Int, Long>()
    private val isWindows = System.getProperty("os.name").startsWith("Windows")

    //window position/size and mouse position (in screen coordinates) when the resize started
    private var startMouseX = 0.0
    private var startMouseY = 0.0
    private var startX = 0
    private var startY = 0
    private var startWidth = 0
    private var startHeight = 0

    private fun isMaximized() = glfwGetWindowAttrib(this.window, GLFW_MAXIMIZED) == GLFW_TRUE

    /**
     * Which edges (as [EDGE_LEFT], [EDGE_RIGHT], ... bitmask) the mouse is on, in window coordinates.
     * The title bar buttons take priority so the close button in the corner stays clickable.
     */
    private fun hitTestEdges(mouseX: Float, mouseY: Float): Int {
        if (!this.resizable || this.isMaximized()) return EDGE_NONE
        if (RenderManager.isMouseOverTitleBarButton(mouseX, mouseY)) return EDGE_NONE

        val w = IntArray(1)
        val h = IntArray(1)
        glfwGetWindowSize(this.window, w, h)

        var edges = EDGE_NONE
        if (mouseX < RESIZE_BORDER) edges = edges or EDGE_LEFT
        if (mouseX >= w[0] - RESIZE_BORDER) edges = edges or EDGE_RIGHT
        if (mouseY < RESIZE_BORDER) edges = edges or EDGE_TOP
        if (mouseY >= h[0] - RESIZE_BORDER) edges = edges or EDGE_BOTTOM
        return edges
    }

    private fun cursorFor(edges: Int): Long {
        val shape = when (edges) {
            EDGE_LEFT, EDGE_RIGHT -> GLFW_RESIZE_EW_CURSOR
            EDGE_TOP, EDGE_BOTTOM -> GLFW_RESIZE_NS_CURSOR
            EDGE_LEFT or EDGE_TOP, EDGE_RIGHT or EDGE_BOTTOM -> GLFW_RESIZE_NWSE_CURSOR
            else -> GLFW_RESIZE_NESW_CURSOR
        }
        return this.resizeCursors.getOrPut(shape) { glfwCreateStandardCursor(shape) }
    }

    /**
     * Starts a resize when the press is on the window border.
     *
     * @return `true` if the press started a resize and must not be handled as a normal click.
     */
    fun onPress(mouseX: Float, mouseY: Float): Boolean {
        val edges = this.hitTestEdges(mouseX, mouseY)
        if (edges == EDGE_NONE) return false

        val x = IntArray(1)
        val y = IntArray(1)
        val w = IntArray(1)
        val h = IntArray(1)
        glfwGetWindowPos(this.window, x, y)
        glfwGetWindowSize(this.window, w, h)

        val mx = DoubleArray(1)
        val my = DoubleArray(1)
        glfwGetCursorPos(this.window, mx, my)

        this.startX = x[0]
        this.startY = y[0]
        this.startWidth = w[0]
        this.startHeight = h[0]
        //the cursor position is relative to the window which moves while resizing, so work in screen coordinates
        this.startMouseX = x[0] + mx[0]
        this.startMouseY = y[0] + my[0]
        this.resizeEdges = edges
        return true
    }

    fun onRelease() {
        this.resizeEdges = EDGE_NONE
    }

    /**
     * Applies a running resize drag and shows the matching resize cursor while hovering an edge.
     */
    fun update(mouseX: Float, mouseY: Float) {
        if (this.resizeEdges == EDGE_NONE) {
            val edges = this.hitTestEdges(mouseX, mouseY)
            if (edges != this.cursorEdges) {
                this.cursorEdges = edges
                glfwSetCursor(this.window, if (edges == EDGE_NONE) NULL else this.cursorFor(edges))
            }
            return
        }

        val x = IntArray(1)
        val y = IntArray(1)
        glfwGetWindowPos(this.window, x, y)
        val mx = DoubleArray(1)
        val my = DoubleArray(1)
        glfwGetCursorPos(this.window, mx, my)

        val dx = (x[0] + mx[0] - this.startMouseX).toInt()
        val dy = (y[0] + my[0] - this.startMouseY).toInt()

        var newX = this.startX
        var newY = this.startY
        var newWidth = this.startWidth
        var newHeight = this.startHeight

        //the opposite edge stays where it is, so moving left/top also moves the window
        if (this.resizeEdges and EDGE_RIGHT != 0) newWidth = (this.startWidth + dx).coerceAtLeast(MIN_WIDTH)
        if (this.resizeEdges and EDGE_BOTTOM != 0) newHeight = (this.startHeight + dy).coerceAtLeast(MIN_HEIGHT)
        if (this.resizeEdges and EDGE_LEFT != 0) {
            newWidth = (this.startWidth - dx).coerceAtLeast(MIN_WIDTH)
            newX = this.startX + this.startWidth - newWidth
        }
        if (this.resizeEdges and EDGE_TOP != 0) {
            newHeight = (this.startHeight - dy).coerceAtLeast(MIN_HEIGHT)
            newY = this.startY + this.startHeight - newHeight
        }

        val w = IntArray(1)
        val h = IntArray(1)
        glfwGetWindowSize(this.window, w, h)
        if (newX == x[0] && newY == y[0] && newWidth == w[0] && newHeight == h[0]) return

        //position and size have to change in one step, doing them one after another shows a frame in between
        if (this.isWindows) {
            User32.SetWindowPos(null, glfwGetWin32Window(this.window), NULL, newX, newY, newWidth, newHeight, SWP_NOZORDER or SWP_NOACTIVATE)
        } else {
            glfwSetWindowPos(this.window, newX, newY)
            glfwSetWindowSize(this.window, newWidth, newHeight)
        }
        RenderManager.updateWindowSize(newWidth.toFloat(), newHeight.toFloat())
    }

    /**
     * Frees the resize cursors, call before GLFW is terminated.
     */
    fun destroy() {
        this.resizeCursors.values.forEach { glfwDestroyCursor(it) }
        this.resizeCursors.clear()
    }

    companion object {
        private const val SWP_NOZORDER = 0x0004
        private const val SWP_NOACTIVATE = 0x0010

        private const val EDGE_NONE = 0
        private const val EDGE_LEFT = 1
        private const val EDGE_RIGHT = 2
        private const val EDGE_TOP = 4
        private const val EDGE_BOTTOM = 8

        //thickness of the draggable window border in pixels
        private const val RESIZE_BORDER = 4
        private const val MIN_WIDTH = 800
        private const val MIN_HEIGHT = 800
    }
}
