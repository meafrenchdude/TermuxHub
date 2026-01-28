package com.maazm7d.termuxhub.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RepoStatsDto(
    val stars: Int = 0,
    val forks: Int = 0,
    val issues: Int = 0,
    val pullRequests: Int = 0,
    val license: String? = null,
    val lastUpdated: Long = 0L
)
