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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maazm7d.termuxhub.domain.model.ToolDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolRepoBadgesRow(tool: ToolDetails) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 12.dp)
            .fillMaxWidth()
    ) {
        item { 
            RepoBadge(
                icon = Icons.Outlined.Star,
                text = tool.stars.toString(),
                iconTint = MaterialTheme.colorScheme.secondary
            ) 
        }
        
        item { 
            RepoBadge(
                icon = Icons.Outlined.CallSplit,
                text = tool.forks.toString(),
                iconTint = MaterialTheme.colorScheme.tertiary
            ) 
        }
        
        item { 
            RepoBadge(
                icon = Icons.Outlined.BugReport,
                text = tool.issues.toString(),
                iconTint = MaterialTheme.colorScheme.error
            ) 
        }
        
        item { 
            RepoBadge(
                icon = Icons.Outlined.MergeType,
                text = tool.pullRequests.toString(),
                iconTint = MaterialTheme.colorScheme.primary
            ) 
        }

        tool.license?.let { license ->
            item { 
                RepoBadge(
                    icon = Icons.Outlined.Description,
                    text = license,
                    iconTint = MaterialTheme.colorScheme.primary
                ) 
            }
        }

        item {
            RepoBadge(
                icon = Icons.Outlined.Update,
                text = DateUtils.getRelativeTimeSpanString(
                    tool.lastUpdated,
                    System.currentTimeMillis(),
                    DateUtils.DAY_IN_MILLIS
                ).toString(),
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RepoBadge(
    icon: ImageVector,
    text: String,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Card(
        modifier = Modifier
            .heightIn(min = 32.dp)
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = iconTint
            )
            
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1
            )
        }
    }
}
