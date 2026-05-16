package com.bieniucieniu.ballscraper.cli

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.*
import com.bieniucieniu.ballscraper.gitlab.GitLabService
import com.bieniucieniu.ballscraper.jira.JiraService
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlin.time.Instant
import kotlinx.serialization.json.Json

class GitLabCommand : CliktCommand() {
    val token by option("-t", "--token", help = "GitLab Private Token").prompt(requireConfirmation = false, hideInput = true)
    val host by option("--host", help = "GitLab Host").default("https://gitlab.com")
    val from by option("--from", help = "Start date (ISO-8601)").convert { Instant.parse(it) }
    val to by option("--to", help = "End date (ISO-8601)").convert { Instant.parse(it) }

    override fun run() = runBlocking {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val service = GitLabService(client, "$host/api/v4")
        echo("GitLab search from $from to $to on $host")
    }
}

class JiraCommand : CliktCommand() {
    val token by option("-t", "--token", help = "Jira API Token").prompt(requireConfirmation = false, hideInput = true)
    val host by option("--host", help = "Jira Host (e.g. https://domain.atlassian.net)").required()
    val from by option("--from", help = "Start date (ISO-8601)").convert { Instant.parse(it) }
    val to by option("--to", help = "End date (ISO-8601)").convert { Instant.parse(it) }

    override fun run() = runBlocking {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val service = JiraService(client, host)
        echo("Jira search from $from to $to on $host")
    }
}

class BallScraper : CliktCommand() {
    override fun run() {
        val subcommand = currentContext.invokedSubcommand
        if (subcommand == null) {
            if (terminal.terminalInfo.interactive) {
                echo("Opening TUI... (Not implemented yet)")
            } else {
                echo("Error: TUI requires a TTY. Use a subcommand or run in a terminal.", err = true)
                throw PrintHelpMessage(currentContext, error = true)
            }
        }
    }
}

fun main(args: Array<String>) = BallScraper()
    .subcommands(GitLabCommand(), JiraCommand())
    .main(args)
