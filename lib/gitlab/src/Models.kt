package com.bieniucieniu.ballscraper.gitlab

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class GitLabGroup(
    val id: Int,
    val name: String,
    val path: String,
    val full_path: String? = null,
    val created_at: Instant? = null
)

@Serializable
data class GitLabProject(
    val id: Int,
    val name: String,
    val path: String,
    val path_with_namespace: String? = null,
    val created_at: Instant? = null,
    val last_activity_at: Instant? = null
)

@Serializable
data class GitLabBranch(
    val name: String,
    val merged: Boolean? = null,
    val protected: Boolean? = null,
    val default: Boolean? = null,
    val commit: GitLabCommitShort? = null
)

@Serializable
data class GitLabCommitShort(
    val id: String,
    val created_at: Instant? = null,
    val title: String? = null,
    val message: String? = null,
    val author_name: String? = null,
    val authored_date: Instant? = null,
    val committed_date: Instant? = null
)

@Serializable
data class GitLabCommit(
    val id: String,
    val short_id: String,
    val title: String,
    val author_name: String,
    val author_email: String,
    val authored_date: Instant,
    val committer_name: String,
    val committer_email: String,
    val committed_date: Instant,
    val created_at: Instant,
    val message: String
)
