package com.maazm7d.termuxhub.ui.components

import android.content.Context
import android.content.Intent
import com.maazm7d.termuxhub.domain.model.Tool

object ToolShare {

    fun share(context: Context, tool: Tool) {
        val shareText = buildString {
            appendLine("Tool name:")
            appendLine(tool.name)

            appendLine()
            appendLine("Description:")
            appendLine(tool.description)

            appendLine()
            appendLine()
            appendLine("Open in Termux Hub:")
            appendLine("https://maazm7d.github.io/termuxhub/tool/#${tool.id}")
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
