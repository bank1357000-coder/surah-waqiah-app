package com.alwaqiah.quran.presentation.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alwaqiah.quran.R
import com.alwaqiah.quran.domain.model.ArabicScript
import com.alwaqiah.quran.domain.model.ReaderVerse
import com.alwaqiah.quran.presentation.components.VerseNumberBadge
import com.alwaqiah.quran.presentation.theme.WaqiahThemeExtras
import com.alwaqiah.quran.presentation.theme.fontFamily

@Composable
fun VerseItem(
    item: ReaderVerse,
    selected: Boolean,
    isActive: Boolean,
    script: ArabicScript,
    arabicFontSizeSp: Float,
    lineSpacing: Float,
    onClick: () -> Unit,
    onPlayFrom: () -> Unit,
    onToggleBookmark: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val number = item.verse.number
    val extra = WaqiahThemeExtras.colors
    val container by animateColorAsState(
        when {
            isActive -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
            selected -> extra.verseHighlight
            else -> MaterialTheme.colorScheme.surface
        },
        label = "verse-bg",
    )
    val arabicStyle = TextStyle(
        fontFamily = script.fontFamily(),
        fontSize = arabicFontSizeSp.sp,
        lineHeight = (arabicFontSizeSp * lineSpacing).sp,
        textDirection = TextDirection.Rtl,
        textAlign = TextAlign.Start,
        color = MaterialTheme.colorScheme.onSurface,
    )

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().testTag("verse-item-$number"),
        shape = MaterialTheme.shapes.large,
        color = container,
        border = when {
            isActive -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
            selected -> BorderStroke(1.dp, extra.gold)
            else -> null
        },
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row {
                VerseNumberBadge(number)
                Spacer(Modifier.weight(1f))
                if (item.isBookmarked) {
                    Icon(
                        Icons.Filled.Bookmark,
                        contentDescription = stringResource(R.string.cd_bookmarked),
                        tint = extra.gold,
                        modifier = Modifier.testTag("verse-bookmarked-$number"),
                    )
                }
            }
            Text(
                text = if (script == ArabicScript.HAFS_KFGQPC) item.verse.textHafs else item.verse.textTanzil,
                style = arabicStyle,
                modifier = Modifier.fillMaxWidth().testTag("verse-arabic-$number"),
            )
            item.primaryTranslation?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("verse-translation-$number"),
                )
            }
            item.englishTranslation?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDirection = TextDirection.Ltr,
                        textAlign = TextAlign.Left,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().testTag("verse-english-$number"),
                )
            }
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider(color = extra.gold.copy(alpha = 0.4f))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                    ) {
                        VerseAction(
                            label = stringResource(R.string.action_play_from),
                            icon = { Icon(Icons.Filled.PlayArrow, null, Modifier.size(18.dp)) },
                            onClick = onPlayFrom,
                            tag = "verse-action-play-$number",
                        )
                        VerseAction(
                            label = stringResource(if (item.isBookmarked) R.string.action_unbookmark else R.string.action_bookmark),
                            icon = { Icon(if (item.isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder, null, Modifier.size(18.dp)) },
                            onClick = onToggleBookmark,
                            tag = "verse-action-bookmark-$number",
                        )
                        VerseAction(
                            label = stringResource(R.string.action_copy),
                            icon = { Icon(Icons.Filled.ContentCopy, null, Modifier.size(18.dp)) },
                            onClick = onCopy,
                            tag = "verse-action-copy-$number",
                        )
                        VerseAction(
                            label = stringResource(R.string.action_share),
                            icon = { Icon(Icons.Filled.Share, null, Modifier.size(18.dp)) },
                            onClick = onShare,
                            tag = "verse-action-share-$number",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VerseAction(label: String, icon: @Composable () -> Unit, onClick: () -> Unit, tag: String) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = icon,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.testTag(tag),
        colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(containerColor = Color.Transparent),
    )
}
