package org.jetbrains.amper.cli

import androidx.compose.runtime.*
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.types.int
import com.jakewharton.mosaic.runMosaic
import com.jakewharton.mosaic.ui.Text
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds

class Hello : CliktCommand() {
    val count by option("-c", "--count", help="Number of greetings").int().default(1)
    
    override fun run() {
        repeat(count) {
            echo("Hello CLI!")
        }
    }
}

class BallScraper : CliktCommand() {
    override val invokeWithoutSubcommand: Boolean get() = true

    override fun run() {
        val subcommand = currentContext.invokedSubcommand
        if (subcommand == null) {
            if (currentContext.terminal.terminalInfo.interactive) {
                runTui()
            } else {
                echo("Error: TUI requires a TTY. Stdout is not a terminal.", err = true)
            }
        }
    }

    private fun runTui() = runBlocking {
        runMosaic {
            var count by remember { mutableStateOf(0) }

            LaunchedEffect(Unit) {
                for (i in 1..20) {
                    delay(100.milliseconds)
                    count = i
                }
            }

            Text("Hello Mosaic TUI! Count: $count")
        }
    }
}

fun main(args: Array<String>) = BallScraper()
    .subcommands(Hello())
    .main(args)
