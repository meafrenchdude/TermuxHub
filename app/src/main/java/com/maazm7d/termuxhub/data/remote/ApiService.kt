package com.maazm7d.termuxhub.data.remote

import com.maazm7d.termuxhub.data.remote.dto.HallOfFameIndexDto
import com.maazm7d.termuxhub.data.remote.dto.MetadataDto
import com.maazm7d.termuxhub.data.remote.dto.StarsDto
import com.maazm7d.termuxhub.data.remote.dto.RepoStatsRootDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("metadata/metadata.json")
    suspend fun getMetadata(): Response<MetadataDto>

    @GET("metadata/readme/{id}.md")
    suspend fun getToolReadme(
        @Path("id") toolId: String
    ): Response<String>

    @GET("metadata/stars.json")
    suspend fun getStars(): Response<StarsDto>

    @GET("metadata/repo_stats.json")
    suspend fun getRepoStats(): Response<RepoStatsRootDto>

    @GET("metadata/halloffame/index.json")
    suspend fun getHallOfFameIndex(): Response<HallOfFameIndexDto>

    @GET("metadata/halloffame/{id}.md")
    suspend fun getHallOfFameMarkdown(
        @Path("id") id: String
    ): Response<String>
}
