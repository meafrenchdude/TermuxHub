package com.maazm7d.termuxhub.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun DetailScreenThumbnail(
    toolId: String,
    modifier: Modifier = Modifier
) {
    val thumbnailUrl =
        "https://raw.githubusercontent.com/maazm7d/TermuxHub/main/metadata/thumbnail/$toolId.webp"

    var imageLoading by remember { mutableStateOf(true) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        if (imageLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shimmer()
            )
        }

        AsyncImage(
            model = thumbnailUrl,
            contentDescription = "Tool thumbnail",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            onSuccess = { imageLoading = false },
            onError = { imageLoading = false }
        )
    }
}
