package com.maazm7d.termuxhub.data.remote

import android.content.Context
import com.maazm7d.termuxhub.data.remote.dto.RepoStatsDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class RepoStatsLoader(
    context: Context,
    moshi: Moshi
) {
    private val adapter = moshi.adapter<Map<String, RepoStatsDto>>(
        Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            RepoStatsDto::class.java
        )
    )

    val stats: Map<String, RepoStatsDto> =
        try {
            context.assets.open("metadata/repo_stats.json")
                .bufferedReader()
                .use { adapter.fromJson(it.readText()) }
                ?: emptyMap()
        } catch (_: Exception) {
            emptyMap()
        }
}
