package com.marvinschs.mirops.frontend.render

import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWVidMode
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.NULL
import java.nio.IntBuffer
import kotlin.use

class MainWindow {
    val title: String
    val resizable: Boolean
    val width: Float
    val height: Float
    
    constructor(title: String): this(title, true)
    
    constructor(title: String, resizable: Boolean): this(title, resizable, 800)
    
    constructor(title: String, resizable: Boolean, width: Number): this(title, resizable, width, 800)
    
    constructor(title: String, resizable: Boolean, width: Number, height: Number) {
        this.title = title
        this.resizable = resizable
        this.width = width.toFloat()
        this.height = height.toFloat()
        this.init()
    }
    
    private var window: Long = 0
    private lateinit var resizer: WindowResizer
    
    fun init() {
        println("Hello LWJGL " + Version.getVersion() + "!")
        
        GLFWErrorCallback.createPrint(System.err)
        
        if (!glfwInit()) throw IllegalStateException("Unable to initialize GLFW")
        
        glfwDefaultWindowHints() // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden until after creation
        
        when(this.resizable) { //if the window will be resizable
            true -> glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
            false -> glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)
        }
        
        //no Title bar
        glfwWindowHint(GLFW_DECORATED, GLFW_FALSE)
        
        // Create the window
        this.window = glfwCreateWindow(this.width.toInt(), this.height.toInt(), this.title, NULL, NULL)
        windowHandle = this.window
        println("Window id is: ${this.window}")
        RenderManager.updateWindowSize(this.width, this.height)
        
        if (this.window == NULL) throw RuntimeException("Failed to create the main window")
        
        this.resizer = WindowResizer(this.window, this.resizable)
        
        // Set up a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(this.window) { window, key, scancode, action, mods ->
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) shutdown()//glfwSetWindowShouldClose(window, true) // We will detect this in the rendering loop
            
        }
        
        glfwSetMouseButtonCallback(this.window) { window, button, action, mods ->
            if (button == GLFW_MOUSE_BUTTON_1 && action == GLFW_RELEASE) this.resizer.onRelease()

            if (action != GLFW_PRESS) return@glfwSetMouseButtonCallback

            when(button) {
                GLFW_MOUSE_BUTTON_1 -> { //left click
                    val xBuf = DoubleArray(1)
                    val yBuf = DoubleArray(1)
                    glfwGetCursorPos(this.window, xBuf, yBuf)
                    val mouseX = xBuf[0].toFloat()
                    val mouseY = yBuf[0].toFloat()

                    //a press on the window border starts a resize instead of being a click
                    if (this.resizer.onPress(mouseX, mouseY)) return@glfwSetMouseButtonCallback

                    RenderManager.dispatchClick(mouseX, mouseY)
                }
            }
        }
        
        //update Window size
        glfwSetFramebufferSizeCallback(this.window) { window, newWidth, newHeight ->
        
            //val width = newWidth.toFloat()
            //val height = newHeight.toFloat()
        
            glViewport(0, 0, newWidth, newHeight)
        
            //RenderManager.updateWindowSize(width, height)
        }
        
        stackPush().use { stack ->
            val pWidth: IntBuffer = stack.mallocInt(1) // int*
            val pHeight: IntBuffer = stack.mallocInt(1) // int*
            
            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(this.window, pWidth, pHeight)
            
            // Get the resolution of the primary monitor
            val vidMode: GLFWVidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())!!
            
            // Center the window
            glfwSetWindowPos(
                this.window,
                (vidMode.width() - pWidth.get(0)) / 2,
                (vidMode.height() - pHeight.get(0)) / 2
            )
        }
        
        // Make the OpenGL context current
        glfwMakeContextCurrent(this.window)
        
        // Enable v-sync
        glfwSwapInterval(1)
        
        
        // Make the window visible
        glfwShowWindow(this.window)
        
        //The Main Loop starts here
        loop()
        
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(this.window)
        glfwDestroyWindow(this.window)
        this.resizer.destroy()
        
        // Terminate GLFW and free the error callback
        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }
    
    private fun loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities()
        
        glViewport(0, 0, this.width.toInt(), this.height.toInt())
        
        RenderManager.init()
        
        
        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!shouldShutdown) {
            val xBuf = DoubleArray(1)
            val yBuf = DoubleArray(1)
            
            //Resizing has to happen before anything is drawn, changing the framebuffer size between clear and swap crashes the driver
            glfwGetCursorPos(this.window, xBuf, yBuf)
            this.resizer.update(xBuf[0].toFloat(), yBuf[0].toFloat())
            
            // Specify the color of the background
            glClearColor(0.07f, 0.13f, 0.17f, 1.0f)
            glClear(GL_COLOR_BUFFER_BIT) // clear the framebuffer
            
            //Hover Control has to be at rendering there is no other way to get the current state/coordinates of the Mouse
            //the cursor is read again as it is relative to the window, which a resize may have moved
            glfwGetCursorPos(this.window, xBuf, yBuf)
            val mouseX = xBuf[0].toFloat()
            val mouseY = yBuf[0].toFloat()
            RenderManager.dispatchHover(mouseX, mouseY)

            RenderManager.render()
            
            
            glfwSwapBuffers(this.window) // swap the color buffers
            
            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents()
        }
        
        RenderManager.deleteRecursively()
        
        Thread.sleep(100)
        
        println("Stopped")
        
        shutdownHooks.forEach { it() }
    }

    companion object {
        private var windowHandle: Long = NULL

        /**
         * Maximizes the window, or restores it when it is already maximized.
         */
        fun toggleMaximize() {
            if (this.windowHandle == NULL) return

            if (glfwGetWindowAttrib(this.windowHandle, GLFW_MAXIMIZED) == GLFW_TRUE) {
                glfwRestoreWindow(this.windowHandle)
            } else {
                glfwMaximizeWindow(this.windowHandle)
            }
        }

        private var shouldShutdown = false
        
        private var shutdownHooks = mutableListOf<() -> Unit>()
        
        fun shutdown() {
            shouldShutdown = true
        }
        
        fun addShutdownHook(function: () -> Unit) {
            shutdownHooks.add(function)
        }
    }
}