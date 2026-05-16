package com.bieniucieniu.ballscraper.jira

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.time.Instant

class JiraService(
    private val client: HttpClient,
    private val baseUrl: String // e.g. "https://your-domain.atlassian.net"
) {

    /**
     * GET /rest/api/3/project
     */
    suspend fun getProjects(): List<JiraProject> = 
        client.get("$baseUrl/rest/api/3/project").body()

    /**
     * GET /rest/api/3/search
     * Uses JQL to search for issues.
     */
    suspend fun searchIssues(
        jql: String? = null,
        startAt: Int = 0,
        maxResults: Int = 50,
        fields: List<String> = listOf("summary", "status", "created", "updated", "project")
    ): JiraSearchResult = client.get("$baseUrl/rest/api/3/search") {
        jql?.let { parameter("jql", it) }
        parameter("startAt", startAt)
        parameter("maxResults", maxResults)
        parameter("fields", fields.joinToString(","))
    }.body()

    /**
     * Helper to get issues in project with date range.
     */
    suspend fun getIssues(
        projectKey: String,
        since: Instant? = null,
        until: Instant? = null
    ): JiraSearchResult {
        val jql = buildString {
            append("project = \"$projectKey\"")
            since?.let { append(" AND updated >= \"${it}\"") }
            until?.let { append(" AND updated <= \"${it}\"") }
        }
        return searchIssues(jql)
    }
}
