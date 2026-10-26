package com.marvinschs.mirops.backend.config.data.application

import com.marvinschs.mirops.backend.config.data.project.Project
import kotlinx.serialization.Serializable

@Serializable
data class ApplicationConfig(
    var firstLaunch: Boolean = true,
    val isDebugMode: Boolean = false,
    val configVersion: String = "1.0.0",
    val recentProjects: List<Project> = emptyList()
)
