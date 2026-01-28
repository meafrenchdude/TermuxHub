package com.maazm7d.termuxhub.ui.components

import android.text.format.DateUtils
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.maazm7d.termuxhub.domain.model.ToolDetails

@Composable
fun ToolRepoBadgesRow(tool: ToolDetails) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Badge(Icons.Outlined.StarOutline, tool.stars.toString()) }
        item { Badge(Icons.Outlined.CallSplit, tool.forks.toString()) }
        item { Badge(Icons.Outlined.BugReport, tool.issues.toString()) }
        item { Badge(Icons.Outlined.Merge, tool.pullRequests.toString()) }

        tool.license?.let {
            item { Badge(Icons.Outlined.Description, it) }
        }

        item {
            Badge(
                Icons.Outlined.Schedule,
                DateUtils.getRelativeTimeSpanString(
                    tool.lastUpdated,
                    System.currentTimeMillis(),
                    DateUtils.DAY_IN_MILLIS
                ).toString()
            )
        }
    }
}

@Composable
private fun Badge(icon: ImageVector, text: String) {
    Surface(
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, style = MaterialTheme.typography.labelMedium)
        }
    }
}
