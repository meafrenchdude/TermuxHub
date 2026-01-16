package com.maazm7d.termuxhub.data.repository

import com.maazm7d.termuxhub.data.local.HallOfFameDao
import com.maazm7d.termuxhub.data.local.entities.HallOfFameEntity
import com.maazm7d.termuxhub.data.remote.MetadataClient
import com.maazm7d.termuxhub.domain.model.HallOfFameMember
import javax.inject.Inject

class HallOfFameRepository @Inject constructor(
    private val metadataClient: MetadataClient,
    private val dao: HallOfFameDao
) {

    suspend fun loadMembers(): List<HallOfFameMember> {
        return try {
            val indexResponse = metadataClient.fetchHallOfFameIndex()
            if (!indexResponse.isSuccessful) throw Exception()

            val members = indexResponse.body()?.members ?: throw Exception()

            val resolvedMembers = members.map { dto ->
                val markdown = metadataClient
                    .fetchHallOfFameMarkdown(dto.id)
                    .body()
                    .orEmpty()

                HallOfFameMember(
                    id = dto.id,
                    github = dto.github,
                    speciality = dto.speciality,
                    profileUrl = dto.profile,
                    contribution = markdown
                )
            }

            dao.clear()
            dao.insertAll(
                resolvedMembers.map {
                    HallOfFameEntity(
                        id = it.id,
                        github = it.github,
                        speciality = it.speciality,
                        profileUrl = it.profileUrl,
                        contribution = it.contribution
                    )
                }
            )

            resolvedMembers
        } catch (e: Exception) {
            dao.getAll().map {
                HallOfFameMember(
                    id = it.id,
                    github = it.github,
                    speciality = it.speciality,
                    profileUrl = it.profileUrl,
                    contribution = it.contribution
                )
            }
        }
    }
}
