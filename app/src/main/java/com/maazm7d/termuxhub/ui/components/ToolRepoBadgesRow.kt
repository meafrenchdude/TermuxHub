package com.maazm7d.termuxhub.ui.components

import android.text.format.DateUtils
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.maazm7d.termuxhub.domain.model.ToolDetails

@Composable
fun ToolRepoBadgesRow(tool: ToolDetails) {
    LazyRow(
        modifier = Modifier.padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item { RepoBadge(Icons.Outlined.StarOutline, tool.stars.toString()) }
        item { RepoBadge(Icons.Outlined.CallSplit, tool.forks.toString()) }
        item { RepoBadge(Icons.Outlined.BugReport, tool.issues.toString()) }
        item { RepoBadge(Icons.Outlined.Merge, tool.pullRequests.toString()) }

        tool.license?.let {
            item { RepoBadge(Icons.Outlined.Description, it) }
        }

        item {
            RepoBadge(
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
private fun RepoBadge(
    icon: ImageVector,
    text: String
) {
    val badgeBackground = Color(0xFFE7F1FF)  
    val badgeContent = Color(0xFF1E3A5F) 

    Surface(
        shape = RoundedCornerShape(50), // pill shape
        color = badgeBackground,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = badgeContent
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = badgeContent
            )
        }
    }
}
