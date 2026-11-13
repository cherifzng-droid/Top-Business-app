package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.WhatsProBorder
import com.example.ui.theme.WhatsProDeepBlack
import com.example.ui.theme.WhatsProElectricBlue
import com.example.ui.theme.WhatsProElectricBlueContainer
import com.example.ui.theme.WhatsProEmerald
import com.example.ui.theme.WhatsProEmeraldDark
import com.example.ui.theme.WhatsProMintContainer
import com.example.ui.theme.WhatsProOnElectricBlue

@Composable
fun SearchWithFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    filtersExpanded: Boolean,
    onToggleFilters: () -> Unit,
    activeFilterCount: Int,
    resultCount: Int,
    onResetAll: () -> Unit,
    searchInputTestTag: String,
    filterButtonTestTag: String,
    clearButtonTestTag: String,
    modifier: Modifier = Modifier,
    filterPanelContent: @Composable ColumnScope.() -> Unit
) {
    val strings = LocalAppStrings.current
    val hasActiveFilterOrQuery = query.isNotBlank() || activeFilterCount > 0

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = if (filtersExpanded || query.isNotBlank()) 1.5.dp else 1.dp,
                color = when {
                    filtersExpanded -> WhatsProElectricBlue
                    query.isNotBlank() -> WhatsProEmerald
                    else -> WhatsProBorder
                }
            ),
            shadowElevation = if (filtersExpanded) 2.dp else 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = if (query.isNotBlank()) WhatsProEmeraldDark else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(WhatsProElectricBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                            .testTag(searchInputTestTag)
                    )
                }

                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag(clearButtonTestTag)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = strings.searchClearDesc,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Filter icon button inside the search bar with active filter badge
                Box(contentAlignment = Alignment.TopEnd) {
                    Surface(
                        onClick = onToggleFilters,
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            filtersExpanded -> WhatsProDeepBlack
                            activeFilterCount > 0 -> WhatsProMintContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag(filterButtonTestTag)
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FilterList,
                                contentDescription = strings.searchFilterIconDesc,
                                tint = when {
                                    filtersExpanded -> Color.White
                                    activeFilterCount > 0 -> WhatsProEmeraldDark
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (activeFilterCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(WhatsProElectricBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activeFilterCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Expandable filter drawer toggled by the filter icon
        AnimatedVisibility(
            visible = filtersExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, WhatsProBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    content = filterPanelContent
                )
            }
        }

        if (hasActiveFilterOrQuery) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = WhatsProElectricBlueContainer
                ) {
                    Text(
                        text = strings.searchResultsCountFormat(resultCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = WhatsProOnElectricBlue,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                TextButton(
                    onClick = onResetAll,
                    modifier = Modifier.testTag("${filterButtonTestTag}_reset")
                ) {
                    Text(
                        text = strings.searchResetFilters,
                        style = MaterialTheme.typography.labelMedium,
                        color = WhatsProElectricBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
