package com.bieniucieniu.ballscraper.cli

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.buffered
import kotlinx.io.readString
import kotlinx.io.writeString
import net.mamoe.yamlkt.Yaml
import platform.posix.getenv
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString

@OptIn(ExperimentalForeignApi::class)
object ConfigManager {
    private val home = getenv("HOME")?.toKString() ?: "."
    private val configDir = Path(home, ".ballscraper")
    private val configFile = Path(configDir, "config.yaml")

    fun load(): AppConfig {
        if (!SystemFileSystem.exists(configFile)) return AppConfig()
        return try {
            val content = SystemFileSystem.source(configFile).buffered().use { it.readString() }
            Yaml.Default.decodeFromString(AppConfig.serializer(), content)
        } catch (e: Exception) {
            AppConfig()
        }
    }

    fun save(config: AppConfig) {
        try {
            if (!SystemFileSystem.exists(configDir)) {
                SystemFileSystem.createDirectories(configDir)
            }
            val content = Yaml.Default.encodeToString(AppConfig.serializer(), config)
            SystemFileSystem.sink(configFile).buffered().use { it.writeString(content) }
        } catch (e: Exception) {
            // Silently fail for now
        }
    }
}
