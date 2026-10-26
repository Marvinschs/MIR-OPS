package com.marvinschs.mirops.backend.config

import com.marvinschs.mirops.backend.config.data.application.ApplicationConfig
import com.marvinschs.mirops.frontend.render.MainWindow
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class ConfigLoader {
    
    private val applicationPath: Path
    private val applicationConfigPath: Path
    private val applicationConfigFile: Path
    var applicationConfig: ApplicationConfig = ApplicationConfig()
        private set
    
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }
    
    private var configChanged = false
    
    init {
        val path = this.findRootFolder(Paths.get(this.javaClass.protectionDomain.codeSource.location.toURI()).toAbsolutePath().normalize())
        this.applicationPath = if (Files.isRegularFile(path)) path.parent else path
        this.applicationConfigPath = this.applicationPath.resolve("config")
        if (!this.applicationConfigPath.exists()) this.applicationConfigPath.createDirectories()
        this.applicationConfigFile = this.applicationConfigPath.resolve("application_config.json")
        MainWindow.addShutdownHook(this::saveConfig)
    }
    
    private fun findRootFolder(currentPath: Path): Path {
        if (!currentPath.endsWith("MIR-OPS")) {
            println("path: $currentPath\n parent: ${currentPath.parent}")
            this.findRootFolder(currentPath.parent)
        }
        return currentPath.parent
    }
    
    fun loadConfig() {
        this.applicationConfig =
            if (this.applicationConfigFile.exists())
                this.json.decodeFromString<ApplicationConfig>(this.applicationConfigFile.readText(Charsets.UTF_8))
            else
                ApplicationConfig(true, false, "1.0.0", emptyList())
        println(this.applicationPath)
    }
    
    fun saveConfig() {
        if (!this.configChanged) return
        val string = this.json.encodeToString(this.applicationConfig)
        applicationConfigFile.writeText(string, Charsets.UTF_8)
    }
}