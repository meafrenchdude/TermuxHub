package com.maazm7d.termuxhub.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StarsDto(
    val lastUpdated: String?,
    val stars: Map<String, Int>
)
