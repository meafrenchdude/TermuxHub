package com.maazm7d.termuxhub.data.remote

import android.content.Context
import com.maazm7d.termuxhub.data.remote.dto.RepoStatsDto
import com.maazm7d.termuxhub.data.remote.dto.RepoStatsRootDto
import com.squareup.moshi.Moshi

class RepoStatsLoader(
    context: Context,
    moshi: Moshi
) {

    private val adapter = moshi.adapter(RepoStatsRootDto::class.java)

    val stats: Map<String, RepoStatsDto> =
        try {
            context.assets
                .open("metadata/repo_stats.json")
                .bufferedReader()
                .use { reader ->
                    adapter.fromJson(reader.readText())?.stats
                } ?: emptyMap()
        } catch (_: Exception) {
            emptyMap()
        }
}
