package com.bieniucieniu.ballscraper.cli.shared


interface ConfigManager {
    fun load(): AppConfig
    fun save(config: AppConfig)
    fun forceReload(): AppConfig
}
