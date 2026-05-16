package com.bieniucieniu.ballscraper.jira

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class JiraProject(
    val id: String,
    val key: String,
    val name: String,
    val self: String? = null
)

@Serializable
data class JiraIssue(
    val id: String,
    val key: String,
    val self: String? = null,
    val fields: JiraIssueFields
)

@Serializable
data class JiraIssueFields(
    val summary: String,
    val created: Instant,
    val updated: Instant,
    val status: JiraStatus? = null,
    val project: JiraProject? = null
)

@Serializable
data class JiraStatus(
    val name: String
)

@Serializable
data class JiraSearchResult(
    val startAt: Int,
    val maxResults: Int,
    val total: Int,
    val issues: List<JiraIssue>
)
