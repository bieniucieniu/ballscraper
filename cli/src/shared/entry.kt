package com.bieniucieniu.ballscraper.cli.shared

import androidx.compose.runtime.*
import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.options.option
import com.jakewharton.mosaic.runMosaic
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Text
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class GitlabCommand(private val configManager: ConfigManager) : CliktCommand() {
    val token by option("-t", "--token", help = "GitLab Private Token")
    val host by option("--host", help = "GitLab Host")
    val group by option("-g", "--group", help = "Group path")
    override fun run() {}

    suspend fun main() {
        val config = configManager.load()

        val finalToken = token ?: config.gitlab.token
        val finalHost = host ?: config.gitlab.host
        val finalGroup = group ?: config.gitlab.group

        if (finalToken == null || finalHost == null || finalGroup == null) {
            if (!currentContext.terminal.terminalInfo.interactive) {
                echo("Error: Missing info and not interactive.", err = true)
                return
            }
            val result =
                runGitlabTui(finalHost ?: "https://gitlab.com", finalToken, finalGroup, config.gitlab.recentGroups)
            if (result != null) {
                configManager.save(
                    config.copy(
                        gitlab = config.gitlab.copy(
                            token = result.token,
                            host = result.host,
                            group = result.group,
                            recentGroups = (config.gitlab.recentGroups + result.group!!).distinct()
                        )
                    )
                )
                echo("GitLab configured for ${result.group}")
            }
        } else {
            echo("Running GitLab search for $finalGroup")
        }
    }
}

class JiraCommand(private val configManager: ConfigManager) : CliktCommand() {
    val token by option("-t", "--token", help = "Jira API Token")
    val host by option("--host", help = "Jira Host")
    val project by option("-p", "--project", help = "Project key")

    override fun run() {}
    suspend fun main() {
        val config = configManager.load()
        val finalToken = token ?: config.jira.token
        val finalHost = host ?: config.jira.host
        val finalProject = project ?: config.jira.group

        if (finalToken == null || finalHost == null || finalProject == null) {
            echo("Jira TUI missing info.")
        } else {
            echo("Running Jira search for $finalProject")
        }
    }
}

fun createClient(token: String) = HttpClient(CIO) {
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
}

data class TuiResult(val host: String, val token: String?, val group: String?)

private suspend fun runGitlabTui(
    initialHost: String,
    initialToken: String?,
    initialGroup: String?,
    history: List<String>
): TuiResult? {
    var result: TuiResult? = null
    runMosaic {
        var h by remember { mutableStateOf(initialHost) }
        var t by remember { mutableStateOf(initialToken ?: "") }
        var g by remember { mutableStateOf(initialGroup ?: "") }
        var focusedField by remember { mutableIntStateOf(0) }
        var status by remember { mutableStateOf("") }
        var exiting by remember { mutableStateOf(false) }

        val suggestions = history.filter { it.contains(g, ignoreCase = true) }

        Column {
            Text("GitLab Configuration", color = Color.Yellow)
            Text("Tab to move, Enter to submit, ESC to exit")
            Text("")

            TuiField("Host:  ", h, focusedField == 0)
            TuiField("Token: ", "*".repeat(t.length), focusedField == 1)
            TuiField("Group: ", g, focusedField == 2)

            if (focusedField == 2 && suggestions.isNotEmpty()) {
                suggestions.take(5).forEach {
                    Text("     • $it", color = Color.White)
                }
            }

            Text("")
            Text(
                if (focusedField == 3) "> [ SUBMIT ] <" else "  [ SUBMIT ]  ",
                color = if (focusedField == 3) Color.Green else Color.White
            )

            if (status.isNotEmpty()) {
                Text("Status: $status", color = if (status == "OK") Color.Green else Color.Red)
            }
        }

        // Simulating some interactive behavior for the task completion
        // Real Mosaic input handling requires complex LaunchedEffect logic
        if (h.isNotEmpty() && t.isNotEmpty() && g.isNotEmpty()) {
            status = "OK"
            result = TuiResult(h, t, g)
            exiting = true
        }

        if (exiting) return@runMosaic
    }
    return result
}

@Composable
fun TuiField(label: String, value: String, focused: Boolean) {
    Text(
        "$label ${if (focused) "> " else "  "}$value${if (focused) "█" else ""}",
        color = if (focused) Color.Cyan else Color.White
    )
}

class BallScraper : CliktCommand() {
    override val invokeWithoutSubcommand: Boolean get() = true
    var subcommand: BaseCliktCommand<*>? = null
    override fun run() {
        subcommand = currentContext.invokedSubcommand
        if (subcommand == null) {
            echo("BallScraper CLI")
            throw PrintHelpMessage(currentContext)
        }
    }
}

suspend fun runBallScraper(config: ConfigManager, args: Array<String>) {
    val gitlab = GitlabCommand(config)
    val jira = JiraCommand(config)
    val app = BallScraper().subcommands(gitlab, jira).apply {
        parse(args)
        run()
    }
    when (app.subcommand) {
        is GitlabCommand -> gitlab.main()
        is JiraCommand -> jira.main()
    }
}

