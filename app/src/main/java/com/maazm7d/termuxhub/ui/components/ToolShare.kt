package com.maazm7d.termuxhub.ui.components

import android.content.Context
import android.content.Intent
import com.maazm7d.termuxhub.domain.model.Tool

object AppLinks {
    const val FDROID_URL = "https://f-droid.org/packages/com.maazm7d.termuxhub"
}

object ToolShare {

    fun share(context: Context, tool: Tool) {
        val shareText = buildString {
            appendLine("🔥 Check out this Termux tool!")
            appendLine()
            appendLine("🛠 Tool: ${tool.name}")

            if (tool.description.isNotBlank()) {
                appendLine("📄 ${tool.description}")
            }

            tool.repoUrl?.let {
                appendLine()
                appendLine("🔗 GitHub: $it")
            }

            appendLine()
            appendLine("📦 Get the app on F-Droid:")
            appendLine(AppLinks.FDROID_URL)

            appendLine()
            append("📱 Shared via TermuxHub")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, tool.name)
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        context.startActivity(
            Intent.createChooser(intent, "Share ${tool.name}")
        )
    }
}
