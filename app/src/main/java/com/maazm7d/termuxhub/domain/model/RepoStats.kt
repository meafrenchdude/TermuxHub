package com.maazm7d.termuxhub.domain.model

data class RepoStats(
    val stars: Int,
    val forks: Int,
    val issues: Int,
    val pullRequests: Int,
    val license: String?,
    val lastUpdated: Long
)
