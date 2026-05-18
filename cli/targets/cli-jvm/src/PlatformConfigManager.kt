package com.bieniucieniu.ballscraper.cli

import com.bieniucieniu.ballscraper.cli.shared.AppConfig
import com.bieniucieniu.ballscraper.cli.shared.ConfigManager
import kotlinx.serialization.decodeFromString
import net.mamoe.yamlkt.Yaml
import java.io.File

object PlatformConfigManager : ConfigManager {
    private val yaml = Yaml { encodeDefaultValues = true }
    private var cachedConfig: AppConfig? = null
    private val configFile: File by lazy {
        val os = System.getProperty("os.name").lowercase()
        val path = when {
            os.contains("win") -> File(System.getenv("APPDATA"), "ballscraper/config.yaml")
            os.contains("mac") -> File(
                System.getProperty("user.home"),
                "Library/Application Support/ballscraper/config.yaml"
            )

            else -> {
                val configHome = System.getenv("XDG_CONFIG_HOME")
                if (configHome != null) File(configHome, "ballscraper/config.yaml")
                else File(System.getProperty("user.home"), ".config/ballscraper/config.yaml")
            }
        }
        path
    }

    override fun load(): AppConfig {
        cachedConfig?.let { return it }

        if (!configFile.exists()) {
            return AppConfig().also { cachedConfig = it }
        }
        return try {
            yaml.decodeFromString<AppConfig>(configFile.readText()).also { cachedConfig = it }
        } catch (e: Exception) {
            AppConfig().also { cachedConfig = it }
        }
    }


    override fun save(config: AppConfig) {
        cachedConfig = config
        configFile.parentFile?.mkdirs()
        configFile.writeText(yaml.encodeToString(config))
    }

    override fun forceReload(): AppConfig {
        cachedConfig = null
        return load()
    }
}
