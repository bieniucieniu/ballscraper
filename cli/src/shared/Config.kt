package com.bieniucieniu.ballscraper.cli.shared

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val gitlab: GitLabConfig = GitLabConfig(),
    val jira: JiraConfig = JiraConfig()
)

@Serializable
data class GitLabConfig(
    val token: String? = null,
    val host: String? = null,
    val group: String? = null,
    val recentGroups: List<String> = emptyList(),
    val groupToProject: Map<String, String> = emptyMap()
)

@Serializable
data class JiraConfig(
    val token: String? = null,
    val host: String? = null,
    val group: String? = null,
    val recentProjects: List<String> = emptyList(),
    val groupToProject: Map<String, String> = emptyMap()
)
