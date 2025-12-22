package com.person.ally.ui.components

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import org.commonmark.node.Code
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.Heading
import org.commonmark.node.SoftLineBreak

/**
 * A Composable that renders Markdown text using Markwon library.
 * Supports code blocks, tables, task lists, links, and other Markdown features.
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = LocalContentColor.current,
    linkColor: Color = MaterialTheme.colorScheme.primary,
    codeBackgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    codeTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onLinkClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val textColor = color.toArgb()
    val linkColorArgb = linkColor.toArgb()
    val codeBackgroundArgb = codeBackgroundColor.toArgb()
    val codeTextArgb = codeTextColor.toArgb()
    val textSizeSp = style.fontSize.value

    val markwon = remember(textColor, linkColorArgb, codeBackgroundArgb, codeTextArgb) {
        Markwon.builder(context)
            .usePlugin(CorePlugin.create())
            .usePlugin(HtmlPlugin.create())
            .usePlugin(LinkifyPlugin.create())
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .usePlugin(TaskListPlugin.create(context))
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    builder
                        .codeTextColor(codeTextArgb)
                        .codeBackgroundColor(codeBackgroundArgb)
                        .codeBlockTextColor(codeTextArgb)
                        .codeBlockBackgroundColor(codeBackgroundArgb)
                        .codeTypeface(Typeface.MONOSPACE)
                        .codeBlockTypeface(Typeface.MONOSPACE)
                        .linkColor(linkColorArgb)
                        .headingBreakHeight(0)
                }

                override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                    builder.linkResolver { view, link ->
                        onLinkClick?.invoke(link)
                    }
                }
            })
            .build()
    }

    val processedMarkdown = remember(markdown) {
        preprocessMarkdown(markdown)
    }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            TextView(ctx).apply {
                setTextColor(textColor)
                textSize = textSizeSp
                setLineSpacing(0f, 1.2f)
            }
        },
        update = { textView ->
            textView.setTextColor(textColor)
            textView.textSize = textSizeSp
            markwon.setMarkdown(textView, processedMarkdown)
        }
    )
}

/**
 * Preprocess markdown to handle edge cases and improve rendering
 */
private fun preprocessMarkdown(markdown: String): String {
    return markdown
        // Normalize line endings
        .replace("\r\n", "\n")
        .replace("\r", "\n")
        // Ensure code blocks are properly formatted
        .replace(Regex("```(\\w*)\\n"), "```$1\n")
        // Handle thinking/reasoning blocks (often wrapped in <think> tags)
        .replace(Regex("<think>(.*?)</think>", RegexOption.DOT_MATCHES_ALL)) { match ->
            "> 💭 *Thinking:* ${match.groupValues[1].trim()}"
        }
        // Handle <reasoning> tags
        .replace(Regex("<reasoning>(.*?)</reasoning>", RegexOption.DOT_MATCHES_ALL)) { match ->
            "> 💭 *Reasoning:* ${match.groupValues[1].trim()}"
        }
}

/**
 * A composable for displaying streaming markdown content with typing effect
 */
@Composable
fun StreamingMarkdownText(
    content: String,
    reasoning: String? = null,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = LocalContentColor.current,
    reasoningColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
    showReasoning: Boolean = true
) {
    val combinedContent = remember(content, reasoning, showReasoning) {
        buildString {
            if (showReasoning && !reasoning.isNullOrBlank()) {
                appendLine("> 💭 *Thinking...*")
                appendLine("> ${reasoning.replace("\n", "\n> ")}")
                appendLine()
            }
            append(content)
        }
    }

    MarkdownText(
        markdown = combinedContent,
        modifier = modifier,
        style = style,
        color = color
    )
}

/**
 * A composable specifically for code blocks with syntax highlighting placeholder
 */
@Composable
fun CodeBlock(
    code: String,
    language: String? = null,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val markdown = remember(code, language) {
        buildString {
            appendLine("```${language ?: ""}")
            appendLine(code)
            appendLine("```")
        }
    }

    MarkdownText(
        markdown = markdown,
        modifier = modifier,
        codeBackgroundColor = backgroundColor,
        codeTextColor = textColor
    )
}

/**
 * A composable for displaying tool execution status
 */
@Composable
fun ToolExecutionIndicator(
    toolName: String,
    status: ToolExecutionStatus,
    result: String? = null,
    modifier: Modifier = Modifier
) {
    val markdown = remember(toolName, status, result) {
        buildString {
            val statusEmoji = when (status) {
                ToolExecutionStatus.PENDING -> "⏳"
                ToolExecutionStatus.EXECUTING -> "🔄"
                ToolExecutionStatus.SUCCESS -> "✅"
                ToolExecutionStatus.ERROR -> "❌"
            }

            appendLine("$statusEmoji **Using tool:** `$toolName`")

            if (status == ToolExecutionStatus.SUCCESS && result != null) {
                appendLine()
                appendLine("> $result")
            } else if (status == ToolExecutionStatus.ERROR && result != null) {
                appendLine()
                appendLine("> ⚠️ $result")
            }
        }
    }

    MarkdownText(
        markdown = markdown,
        modifier = modifier
    )
}

enum class ToolExecutionStatus {
    PENDING,
    EXECUTING,
    SUCCESS,
    ERROR
}

/**
 * A composable for displaying reasoning/thinking content
 */
@Composable
fun ReasoningBlock(
    reasoning: String,
    isExpanded: Boolean = false,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
) {
    val markdown = remember(reasoning, isExpanded) {
        if (isExpanded) {
            buildString {
                appendLine("💭 **Thinking process:**")
                appendLine()
                appendLine(reasoning)
            }
        } else {
            val preview = reasoning.take(150).let {
                if (reasoning.length > 150) "$it..." else it
            }
            "💭 *$preview*"
        }
    }

    MarkdownText(
        markdown = markdown,
        modifier = modifier,
        color = color
    )
}
