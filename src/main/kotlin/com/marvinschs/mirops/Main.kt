package com.marvinschs.mirops

import com.marvinschs.mirops.backend.config.ConfigLoader
import com.marvinschs.mirops.frontend.render.MainWindow

class Main {
    
    companion object {
        val configLoader: ConfigLoader = ConfigLoader()
        
        @JvmStatic
        fun main(vararg args: String) {
            val title = "MIR-OPS"
            MainWindow(title)
        }
    }
}