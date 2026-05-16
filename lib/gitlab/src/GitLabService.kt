package com.bieniucieniu.ballscraper.gitlab

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.datetime.Instant

class GitLabService(
    private val client: HttpClient,
    private val baseUrl: String = "https://gitlab.com/api/v4"
) {

    /**
     * GET /groups
     */
    suspend fun getGroups(
        allAvailable: Boolean? = null,
        minAccessLevel: Int? = null,
        orderBy: String? = null,
        owned: Boolean? = null,
        search: String? = null,
        skipGroups: List<Int>? = null,
        sort: String? = null,
        statistics: Boolean? = null,
        withCustomAttributes: Boolean? = null,
        page: Int? = null,
        perPage: Int? = null
    ): List<GitLabGroup> = client.get("$baseUrl/groups") {
        parameter("all_available", allAvailable)
        parameter("min_access_level", minAccessLevel)
        parameter("order_by", orderBy)
        parameter("owned", owned)
        parameter("search", search)
        parameter("skip_groups", skipGroups?.joinToString(","))
        parameter("sort", sort)
        parameter("statistics", statistics)
        parameter("with_custom_attributes", withCustomAttributes)
        parameter("page", page)
        parameter("per_page", perPage)
    }.body()

    /**
     * GET /groups/:id/subgroups
     */
    suspend fun getSubgroups(
        groupId: Int,
        allAvailable: Boolean? = null,
        minAccessLevel: Int? = null,
        orderBy: String? = null,
        owned: Boolean? = null,
        search: String? = null,
        skipGroups: List<Int>? = null,
        sort: String? = null,
        statistics: Boolean? = null,
        withCustomAttributes: Boolean? = null,
        page: Int? = null,
        perPage: Int? = null
    ): List<GitLabGroup> = client.get("$baseUrl/groups/$groupId/subgroups") {
        parameter("all_available", allAvailable)
        parameter("min_access_level", minAccessLevel)
        parameter("order_by", orderBy)
        parameter("owned", owned)
        parameter("search", search)
        parameter("skip_groups", skipGroups?.joinToString(","))
        parameter("sort", sort)
        parameter("statistics", statistics)
        parameter("with_custom_attributes", withCustomAttributes)
        parameter("page", page)
        parameter("per_page", perPage)
    }.body()

    /**
     * GET /groups/:id/projects
     */
    suspend fun getProjectsInGroup(
        groupId: Int,
        archived: Boolean? = null,
        visibility: String? = null,
        orderBy: String? = null,
        sort: String? = null,
        search: String? = null,
        simple: Boolean? = null,
        owned: Boolean? = null,
        starred: Boolean? = null,
        withIssuesEnabled: Boolean? = null,
        withMergeRequestsEnabled: Boolean? = null,
        withShared: Boolean? = null,
        includeSubgroups: Boolean? = null,
        minAccessLevel: Int? = null,
        withCustomAttributes: Boolean? = null,
        withSecurityReports: Boolean? = null,
        page: Int? = null,
        perPage: Int? = null
    ): List<GitLabProject> = client.get("$baseUrl/groups/$groupId/projects") {
        parameter("archived", archived)
        parameter("visibility", visibility)
        parameter("order_by", orderBy)
        parameter("sort", sort)
        parameter("search", search)
        parameter("simple", simple)
        parameter("owned", owned)
        parameter("starred", starred)
        parameter("with_issues_enabled", withIssuesEnabled)
        parameter("with_merge_requests_enabled", withMergeRequestsEnabled)
        parameter("with_shared", withShared)
        parameter("include_subgroups", includeSubgroups)
        parameter("min_access_level", minAccessLevel)
        parameter("with_custom_attributes", withCustomAttributes)
        parameter("with_security_reports", withSecurityReports)
        parameter("page", page)
        parameter("per_page", perPage)
    }.body()

    /**
     * GET /projects/:id/repository/branches
     */
    suspend fun getBranches(
        projectId: Int,
        search: String? = null,
        page: Int? = null,
        perPage: Int? = null
    ): List<GitLabBranch> = client.get("$baseUrl/projects/$projectId/repository/branches") {
        parameter("search", search)
        parameter("page", page)
        parameter("per_page", perPage)
    }.body()

    /**
     * GET /projects/:id/repository/commits
     */
    suspend fun getCommits(
        projectId: Int,
        refName: String? = null,
        since: Instant? = null,
        until: Instant? = null,
        path: String? = null,
        all: Boolean? = null,
        withStats: Boolean? = null,
        firstParent: Boolean? = null,
        order: String? = null,
        page: Int? = null,
        perPage: Int? = null
    ): List<GitLabCommit> = client.get("$baseUrl/projects/$projectId/repository/commits") {
        parameter("ref_name", refName)
        parameter("since", since?.toString())
        parameter("until", until?.toString())
        parameter("path", path)
        parameter("all", all)
        parameter("with_stats", withStats)
        parameter("first_parent", firstParent)
        parameter("order", order)
        parameter("page", page)
        parameter("per_page", perPage)
    }.body()
}
