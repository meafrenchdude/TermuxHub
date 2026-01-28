package com.maazm7d.termuxhub.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RepoStatsRootDto(
    val lastUpdated: String,
    val stats: Map<String, RepoStatsDto>
)
