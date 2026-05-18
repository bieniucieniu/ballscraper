package com.bieniucieniu.ballscraper.cli

import com.bieniucieniu.ballscraper.cli.shared.AppConfig
import com.bieniucieniu.ballscraper.cli.shared.ConfigManager
import kotlinx.cinterop.*
import platform.posix.*
import net.mamoe.yamlkt.Yaml
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

@OptIn(ExperimentalForeignApi::class)
object PlatformConfigManager : ConfigManager {
    private val yaml = Yaml { encodeDefaultValues = true }
    private var cachedConfig: AppConfig? = null
    private val configPath: String by lazy {
        val configHome = getenv("XDG_CONFIG_HOME")?.toKString()
        if (configHome != null) {
            "$configHome/ballscraper/config.yaml"
        } else {
            val home = getenv("HOME")?.toKString() ?: ""
            "$home/.config/ballscraper/config.yaml"
        }
    }

    override fun load(): AppConfig {
        cachedConfig?.let { return it }

        val file = fopen(configPath, "r") ?: return AppConfig().also { cachedConfig = it }
        try {
            fseek(file, 0, SEEK_END)
            val size = ftell(file).toInt()
            fseek(file, 0, SEEK_SET)
            if (size <= 0) return AppConfig().also { cachedConfig = it }
            
            val buffer = ByteArray(size)
            buffer.usePinned { pinned ->
                fread(pinned.addressOf(0), 1.toULong(), size.toULong(), file)
            }
            return try {
                yaml.decodeFromString<AppConfig>(buffer.decodeToString()).also { cachedConfig = it }
            } catch (e: Exception) {
                AppConfig().also { cachedConfig = it }
            }
        } finally {
            fclose(file)
        }
    }

    override fun save(config: AppConfig) {
        cachedConfig = config
        val dir = configPath.substringBeforeLast("/")
        mkdirs(dir)
        
        val file = fopen(configPath, "w") ?: return
        try {
            val content = yaml.encodeToString(config)
            fputs(content, file)
        } finally {
            fclose(file)
        }
    }

    override fun forceReload(): AppConfig {
        cachedConfig = null
        return load()
    }

    private fun mkdirs(path: String) {
        val segments = path.split("/")
        var current = if (path.startsWith("/")) "/" else ""
        for (segment in segments) {
            if (segment.isEmpty()) continue
            current = if (current == "/" || current.isEmpty()) current + segment else "$current/$segment"
            mkdir(current, 511.toUInt()) // 0777
        }
    }
}
