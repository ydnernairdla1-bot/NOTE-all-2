package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CategoryMutationResult
import com.example.data.ImageStorageManager
import com.example.data.NoteEntity
import com.example.ui.HomeUiState
import com.example.ui.NotesFilter
import com.example.ui.components.ManageNoteCategoriesSheet
import com.example.ui.components.NoteCard
import com.example.ui.components.ReminderHeaderButton
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.LocalAppButtonSize

@Composable
fun NotesHomeScreen(
    uiState: HomeUiState,
    activeRemindersCount: Int,
    imageStorageManager: ImageStorageManager,
    snackbarHostState: SnackbarHostState,
    onSearchQueryChange: (String) -> Unit,
    onFilterSelected: (NotesFilter) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onCreateCategory: (String, (String) -> String, (CategoryMutationResult) -> Unit) -> Unit,
    onRenameCategory: (String, String, (String) -> String, (CategoryMutationResult) -> Unit) -> Unit,
    onDeleteCategory: (String, (String) -> String) -> Unit,
    onToggleGridView: () -> Unit,
    onOpenNote: (String) -> Unit,
    onCreateNewNote: () -> Unit,
    onTogglePin: (NoteEntity) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit,
    onOpenBackupCenter: () -> Unit,
    onOpenReminders: () -> Unit,
    onOpenThemeCustomizer: () -> Unit,
    onDismissLocalNotice: () -> Unit,
    onClearStatusMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonSize = LocalAppButtonSize.current
    val strings = LocalAppStrings.current
    var notePendingDeletion by remember { mutableStateOf<NoteEntity?>(null) }
    var showManageCategoriesSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.statusMessage) {
        val message = uiState.statusMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            onClearStatusMessage()
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top Editorial Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = strings.notesStatsSummary(uiState.totalNotesCount, uiState.totalImagesCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Surface(
                    onClick = onOpenBackupCenter,
                    shape = CircleShape,
                    color = if (uiState.pendingBackupCount > 0) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.testTag("open_backup_center_button")
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = buttonSize.horizontalPadding,
                            vertical = buttonSize.verticalPadding
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PictureAsPdf,
                            contentDescription = strings.exportPdfShort,
                            tint = if (uiState.pendingBackupCount > 0) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            },
                            modifier = Modifier.size(buttonSize.iconSize)
                        )
                        Text(
                            text = if (uiState.pendingBackupCount > 0) {
                                "${strings.pdfChip} (${uiState.pendingBackupCount})"
                            } else {
                                strings.pdfChip
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.pendingBackupCount > 0) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }
                }

                ReminderHeaderButton(
                    activeCount = activeRemindersCount,
                    onClick = onOpenReminders,
                    testTag = "open_reminders_button"
                )

                IconButton(
                    onClick = onOpenThemeCustomizer,
                    modifier = Modifier
                        .size(buttonSize.height)
                        .testTag("open_theme_customizer_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = strings.openSettingsDesc,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(buttonSize.iconSize + 3.dp)
                    )
                }

                IconButton(
                    onClick = onToggleGridView,
                    modifier = Modifier
                        .size(buttonSize.height)
                        .testTag("toggle_layout_button")
                ) {
                    Icon(
                        imageVector = if (uiState.isGridView) {
                            Icons.Outlined.ViewAgenda
                        } else {
                            Icons.Outlined.GridView
                        },
                        contentDescription = if (uiState.isGridView) {
                            strings.toggleToList
                        } else {
                            strings.toggleToGrid
                        },
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(buttonSize.iconSize + 3.dp)
                    )
                }
            }
        }

        // Compact Single-Line Local Storage Notice Strip
        AnimatedVisibility(visible = uiState.showLocalNoticeBanner) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CloudOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.localNoticeCompact,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = onOpenBackupCenter,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("banner_backup_action")
                    ) {
                        Text(
                            text = strings.exportPdfShort,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    IconButton(
                        onClick = onDismissLocalNotice,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("dismiss_local_banner_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = strings.close,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

            // Search Field
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = strings.searchNotesHint,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.testTag("clear_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = strings.clearSearch
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .testTag("search_notes_input")
            )

            // Filter & Category Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(NotesFilter.entries) { filter ->
                    val selected = uiState.selectedFilter == filter
                    FilterChip(
                        selected = selected,
                        onClick = { onFilterSelected(filter) },
                        label = { Text(strings.notesFilterLabel(filter)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}")
                    )
                }

                items(uiState.categories, key = { it.lowercase() }) { category ->
                    val selected = uiState.selectedCategory != null &&
                        uiState.selectedCategory.equals(category, ignoreCase = true)
                    val count = uiState.categoryNoteCounts.entries
                        .filter { it.key.equals(category, ignoreCase = true) }
                        .sumOf { it.value }
                    FilterChip(
                        selected = selected,
                        onClick = { onCategorySelected(category) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            val labelText = strings.categoryLabel(category)
                            Text(if (count > 0) "$labelText ($count)" else labelText)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.testTag("category_chip_${category.lowercase()}")
                    )
                }

                item(key = "__manage_folders_chip__") {
                    AssistChip(
                        onClick = { showManageCategoriesSheet = true },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.CreateNewFolder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("+ ${strings.folderActionChip}") },
                        modifier = Modifier.testTag("manage_categories_button")
                    )
                }
            }

            // Notes Content or Empty State
            if (uiState.filteredNotes.isEmpty()) {
                val isFilteredEmpty = uiState.allNotes.isNotEmpty()
                EmptyNotesState(
                    isSearchOrFilterResult = isFilteredEmpty,
                    onCreateNewNote = onCreateNewNote,
                    onResetFilters = {
                        onSearchQueryChange("")
                        onFilterSelected(NotesFilter.ALL)
                        onCategorySelected(null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    val columns = when {
                        !uiState.isGridView -> 1
                        maxWidth >= 840.dp -> 4
                        maxWidth >= 600.dp -> 3
                        else -> 2
                    }

                    if (columns == 1) {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = 20.dp,
                                end = 20.dp,
                                top = 4.dp,
                                bottom = 96.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.filteredNotes, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    imageStorageManager = imageStorageManager,
                                    onClick = { onOpenNote(note.id) },
                                    onTogglePin = { onTogglePin(note) },
                                    onDelete = { notePendingDeletion = note },
                                    modifier = Modifier.widthIn(max = 700.dp)
                                )
                            }
                        }
                    } else {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(columns),
                            contentPadding = PaddingValues(
                                start = 20.dp,
                                end = 20.dp,
                                top = 4.dp,
                                bottom = 96.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalItemSpacing = 12.dp,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.filteredNotes, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    imageStorageManager = imageStorageManager,
                                    onClick = { onOpenNote(note.id) },
                                    onTogglePin = { onTogglePin(note) },
                                    onDelete = { notePendingDeletion = note }
                                )
                            }
                        }
                    }
                }
            }
        }

    notePendingDeletion?.let { target ->
        AlertDialog(
            onDismissRequest = { notePendingDeletion = null },
            title = { Text(strings.deleteNoteConfirmTitle) },
            text = { Text(strings.deleteNoteConfirmBody) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteNote(target)
                        notePendingDeletion = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("confirm_delete_note_button")
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { notePendingDeletion = null },
                    modifier = Modifier.testTag("cancel_delete_note_button")
                ) {
                    Text(strings.cancel)
                }
            }
        )
    }

    if (showManageCategoriesSheet) {
        ManageNoteCategoriesSheet(
            categories = uiState.categories,
            selectedCategory = uiState.selectedCategory,
            categoryNoteCounts = uiState.categoryNoteCounts,
            onSelectCategory = { selected ->
                onCategorySelected(selected)
            },
            onCreateCategory = { name, callback ->
                onCreateCategory(name, strings.folderCreatedMessage, callback)
            },
            onRenameCategory = { oldName, newName, callback ->
                onRenameCategory(oldName, newName, strings.folderRenamedMessage, callback)
            },
            onDeleteCategory = { name ->
                onDeleteCategory(name) { strings.folderDeletedMessage(strings.categoryLabel(it)) }
            },
            onDismiss = { showManageCategoriesSheet = false }
        )
    }
}

@Composable
private fun EmptyNotesState(
    isSearchOrFilterResult: Boolean,
    onCreateNewNote: () -> Unit,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Box(
        modifier = modifier.padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.widthIn(max = 420.dp)
        ) {
            if (!isSearchOrFilterResult) {
                Image(
                    painter = painterResource(id = R.drawable.img_empty_notes),
                    contentDescription = strings.emptyNotesTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 220.dp, height = 165.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            Text(
                text = if (isSearchOrFilterResult) {
                    strings.emptySearchTitle
                } else {
                    strings.emptyNotesTitle
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isSearchOrFilterResult) {
                    strings.emptySearchSubtitle
                } else {
                    strings.emptyNotesSubtitle
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))

            val buttonSize = LocalAppButtonSize.current
            if (isSearchOrFilterResult) {
                Button(
                    onClick = onResetFilters,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(
                        horizontal = buttonSize.horizontalPadding,
                        vertical = buttonSize.verticalPadding
                    ),
                    modifier = Modifier
                        .height(buttonSize.height)
                        .testTag("reset_filters_button")
                ) {
                    Text(
                        text = strings.showAll,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            } else {
                Button(
                    onClick = onCreateNewNote,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(
                        horizontal = buttonSize.horizontalPadding,
                        vertical = buttonSize.verticalPadding
                    ),
                    modifier = Modifier
                        .height(buttonSize.height)
                        .testTag("empty_create_note_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(buttonSize.iconSize)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.createNoteButton,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
