
plugins {
    kotlin("jvm") version libs.versions.kotlin
    kotlin("plugin.serialization") version libs.versions.kotlin
    
    application
}

group = "com.marvinschs"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    
    //kotlin coroutines etc.
    implementation(libs.bundles.kotlinxEcosystem)
    implementation(libs.kotlinSerializationJson)
    
    //lwjgl
    implementation(platform(libs.lwjgl.core))
    implementation(libs.bundles.lwjgl)
    runtimeOnly(variantOf(libs.lwjgl) { classifier("natives-windows") })
    
    runtimeOnly("org.lwjgl:lwjgl-stb::natives-windows")
    
    implementation(libs.joml)
    implementation(libs.imgui)
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.marvinschs.mirops.Main")
    sourceSets["main"].resources.srcDirs("src/main/resources")
    applicationDefaultJvmArgs = listOf("-XstartOnFirstThread")
}

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
    java.srcDir(layout.projectDirectory.dir("src/main/kotlin"))
    kotlin.destinationDirectory.set(java.destinationDirectory)
}

tasks.processResources.configure {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.jar.configure {
    archiveFileName.set("MIR-OPS_$version.jar")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    
    manifest {
        attributes(mapOf(
            "Main-Class" to "com.marvinschs.mirops.Main"
        ))
    }
    
    configurations["compileClasspath"].forEach { file -> from(zipTree(file.absoluteFile)) }
    
    doLast {
        println()
        File("${project.buildDir}/libs/MIR-OPS_$version.jar").copyTo(File("E:/run/MIR-OPS/MIR-OPS_$version.jar"), overwrite = true)
        println("Created jar")
    }
}

tasks.test {
    useJUnitPlatform()
}