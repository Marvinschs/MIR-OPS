package com.marvinschs.mirops.backend.config.data.project

import kotlinx.serialization.Serializable
import java.nio.file.Path

@Serializable
data class Project(
    val name: String,
    val path: Path,
)
