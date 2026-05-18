package com.bieniucieniu.ballscraper.cli

import com.bieniucieniu.ballscraper.cli.shared.runBallScraper

suspend fun main(args: Array<String>) {
    runBallScraper(PlatformConfigManager, args)
}
