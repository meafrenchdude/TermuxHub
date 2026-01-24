package com.maazm7d.termuxhub.ui.screens.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.jeziellago.compose.markdowntext.MarkdownText
import com.maazm7d.termuxhub.ui.components.DetailScreenThumbnail
import com.maazm7d.termuxhub.ui.components.shimmer
import com.maazm7d.termuxhub.domain.model.ToolDetails

@Composable
fun ToolDetailScreen(
    toolId: String,
    viewModel: ToolDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(toolId) {
        viewModel.load(toolId)
    }

    when (uiState) {
        is ToolDetailUiState.Loading -> {
            ToolDetailShimmer()
        }
        is ToolDetailUiState.Success -> {
            val tool = (uiState as ToolDetailUiState.Success).tool
            ToolDetailContent(tool = tool, onBack = onBack)
        }
        is ToolDetailUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Failed to load tool",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (uiState as ToolDetailUiState.Error).message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolDetailContent(
    tool: ToolDetails,
    onBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        DetailScreenThumbnail(
            toolId = tool.id,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = tool.title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = tool.description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (tool.readme.isNotBlank()) {
            MarkdownText(
                markdown = tool.readme,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (tool.installCommands.isNotBlank()) {
            Text(
                text = "Installation",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            tool.installCommands
                .lines()
                .filter { it.isNotBlank() }
                .forEach { cmd ->
                    InstallCommandRow(command = cmd)
                }

            Spacer(modifier = Modifier.height(24.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedButton(
                onClick = { tool.repoUrl?.let { uriHandler.openUri(it) } },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Code,
                    contentDescription = "Source Code",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Source Code")
            }

            OutlinedButton(
                onClick = { tool.repoUrl?.let { uriHandler.openUri("$it/issues") } },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.BugReport,
                    contentDescription = "Report Issue",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Report Issue")
            }
        }
    }
}

@Composable
private fun ToolDetailShimmer() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Back button placeholder
        Box(
            modifier = Modifier
                .size(40.dp)
                .shimmer()
        )

        Spacer(Modifier.height(12.dp))

        // Thumbnail placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .padding(horizontal = 8.dp)
                .shimmer()
        )

        Spacer(Modifier.height(16.dp))

        // Title placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(28.dp)
                .shimmer()
                .align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(10.dp))

        // Description placeholder (3 lines)
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .shimmer()
            )
        }

        Spacer(Modifier.height(24.dp))

        // README section placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(20.dp)
                .shimmer()
        )

        Spacer(Modifier.height(8.dp))

        // README content placeholder (6 lines)
        repeat(6) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .padding(vertical = 3.dp)
                    .shimmer()
            )
        }

        Spacer(Modifier.height(24.dp))

        // Installation section placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(20.dp)
                .shimmer()
        )

        Spacer(Modifier.height(8.dp))

        // Installation commands placeholder (2 commands)
        repeat(2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .padding(vertical = 4.dp)
                    .shimmer()
            )
        }

        Spacer(Modifier.height(24.dp))

        // Buttons placeholder
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .shimmer()
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .shimmer()
            )
        }
    }
}
