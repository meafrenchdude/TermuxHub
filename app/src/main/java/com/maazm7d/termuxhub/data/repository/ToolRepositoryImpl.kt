package com.maazm7d.termuxhub.data.repository

import android.content.Context
import com.maazm7d.termuxhub.data.local.ToolDao
import com.maazm7d.termuxhub.data.local.entities.ToolEntity
import com.maazm7d.termuxhub.data.remote.MetadataClient
import com.maazm7d.termuxhub.data.remote.RepoStatsLoader
import com.maazm7d.termuxhub.data.remote.dto.MetadataDto
import com.maazm7d.termuxhub.data.remote.dto.ToolDto
import com.maazm7d.termuxhub.domain.model.ToolDetails
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

class ToolRepositoryImpl @Inject constructor(
    private val toolDao: ToolDao,
    private val metadataClient: MetadataClient,
    private val appContext: Context,
    private val assetsFileName: String = "metadata.json"
) : ToolRepository {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val repoStats by lazy {
        RepoStatsLoader(appContext, moshi).stats
    }

    override fun observeAll(): Flow<List<ToolEntity>> =
        toolDao.getAllToolsFlow()

    override fun observeFavorites(): Flow<List<ToolEntity>> =
        toolDao.getFavoritesFlow()

    override suspend fun getToolById(id: String): ToolEntity? =
        toolDao.getToolById(id)

    override suspend fun setFavorite(toolId: String, isFav: Boolean) {
        val current = toolDao.getToolById(toolId) ?: return
        toolDao.update(current.copy(isFavorite = isFav))
    }

    override suspend fun refreshFromRemote(): Boolean {
        return try {
            val response = metadataClient.fetchMetadata()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.tools.forEach { dto ->
                    val existing = toolDao.getToolById(dto.id)
                    val entity = dto.toEntity(existing)
                    if (entity != null) toolDao.insert(entity)
                }
                applyStars()
                true
            } else {
                loadFromAssets()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadFromAssets()
        }
    }

    override suspend fun fetchStars(): Map<String, Int> {
        return try {
            val resp = metadataClient.fetchStars()
            if (resp.isSuccessful) resp.body()?.stars ?: emptyMap()
            else emptyMap()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    private suspend fun applyStars() {
        val starsMap = fetchStars()
        starsMap.forEach { (toolId, starCount) ->
            val tool = toolDao.getToolById(toolId)
            if (tool != null && tool.stars != starCount) {
                toolDao.update(tool.copy(stars = starCount))
            }
        }
    }

    private suspend fun loadFromAssets(): Boolean = withContext(Dispatchers.IO) {
        try {
            val input = appContext.assets.open(assetsFileName)
            val text = BufferedReader(InputStreamReader(input)).use { it.readText() }
            val adapter = moshi.adapter(MetadataDto::class.java)
            val dto = adapter.fromJson(text)

            dto?.tools?.forEach { t ->
                val existing = toolDao.getToolById(t.id)
                val entity = t.toEntity(existing)
                if (entity != null) toolDao.insert(entity)
            }

            applyStars()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun ToolDto.toEntity(existing: ToolEntity? = null): ToolEntity? {
        if (id.isBlank() || name.isBlank()) return null

        val stats = repoStats[id]

        return ToolEntity(
            id = id,
            name = name,
            description = description ?: "",
            category = category ?: "Uncategorized",
            installCommand = install,
            repoUrl = repo,
            author = author ?: "",
            requireRoot = requireRoot ?: false,
            thumbnail = thumbnail,
            stars = stats?.stars ?: existing?.stars ?: 0,
            updatedAt = stats?.lastUpdated ?: (updatedAt ?: 0L),
            isFavorite = existing?.isFavorite ?: false,
            publishedAt = publishedAt
        )
    }

    override suspend fun getToolDetails(id: String): ToolDetails? {
        val tool = toolDao.getToolById(id) ?: return null

        val readmeText = try {
            val resp = metadataClient.fetchReadme(id)
            if (resp.isSuccessful) resp.body() ?: "" else ""
        } catch (_: Exception) {
            ""
        }

        return ToolDetails(
            id = tool.id,
            title = tool.name,
            description = tool.description,
            readme = readmeText,
            installCommands = tool.installCommand ?: "",
            repoUrl = tool.repoUrl
        )
    }
}
