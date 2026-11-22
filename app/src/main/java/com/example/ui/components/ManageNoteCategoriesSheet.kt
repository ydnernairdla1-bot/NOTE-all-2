package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CategoryMutationResult
import com.example.data.NoteEntity
import com.example.data.NoteRepository
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageNoteCategoriesSheet(
    categories: List<String>,
    selectedCategory: String?,
    categoryNoteCounts: Map<String, Int>,
    onSelectCategory: (String?) -> Unit,
    onCreateCategory: (String, (CategoryMutationResult) -> Unit) -> Unit,
    onRenameCategory: (String, String, (CategoryMutationResult) -> Unit) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var newCategoryName by remember { mutableStateOf("") }
    var creationError by remember { mutableStateOf<String?>(null) }

    var renameTarget by remember { mutableStateOf<String?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var renameError by remember { mutableStateOf<String?>(null) }

    var deleteTarget by remember { mutableStateOf<String?>(null) }

    fun submitNewCategory() {
        onCreateCategory(newCategoryName) { result ->
            when (result) {
                is CategoryMutationResult.Success -> {
                    newCategoryName = ""
                    creationError = null
                }
                else -> creationError = mutationErrorText(result, strings)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.CreateNewFolder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.manageFoldersTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = strings.manageFoldersSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Create new folder row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = {
                        if (it.length <= NoteRepository.MAX_CATEGORY_LENGTH) {
                            newCategoryName = it
                            creationError = null
                        }
                    },
                    label = { Text(strings.createFolderFieldLabel) },
                    placeholder = { Text(strings.createFolderFieldPlaceholder) },
                    singleLine = true,
                    isError = creationError != null,
                    supportingText = creationError?.let { err ->
                        { Text(err, color = MaterialTheme.colorScheme.error) }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("new_category_name_input")
                )

                Button(
                    onClick = { submitNewCategory() },
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .heightIn(min = 52.dp)
                        .testTag("create_category_submit_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.createFolderSubmit)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item(key = "__all_notes_filter__") {
                    val isAllSelected = selectedCategory == null
                    Surface(
                        onClick = {
                            onSelectCategory(null)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isAllSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isAllSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("folder_row_all")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isAllSelected) Icons.Filled.Folder else Icons.Outlined.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = strings.showAll,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = strings.folderNoteCount(categoryNoteCounts.values.sum()),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isAllSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                items(categories, key = { it.lowercase() }) { category ->
                    val isSelected = selectedCategory != null &&
                        selectedCategory.equals(category, ignoreCase = true)
                    val isProtected = category.equals(NoteEntity.DEFAULT_CATEGORY, ignoreCase = true)
                    val noteCount = categoryNoteCounts.entries
                        .filter { it.key.equals(category, ignoreCase = true) }
                        .sumOf { it.value }

                    Surface(
                        onClick = {
                            onSelectCategory(category)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("folder_row_${category.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Filled.Folder else Icons.Outlined.Folder,
                                contentDescription = null,
                                tint = if (isSelected) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = strings.categoryLabel(category),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (isProtected) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = strings.defaultFolderBadge,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = strings.folderNoteCount(noteCount),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (!isProtected) {
                                IconButton(
                                    onClick = {
                                        renameTarget = category
                                        renameInput = category
                                        renameError = null
                                    },
                                    modifier = Modifier.testTag("rename_folder_${category.lowercase()}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = strings.renameFolderDesc,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { deleteTarget = category },
                                    modifier = Modifier.testTag("delete_folder_${category.lowercase()}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = strings.deleteFolderDesc,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    renameTarget?.let { oldName ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text(strings.renameFolderTitle) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = {
                        if (it.length <= NoteRepository.MAX_CATEGORY_LENGTH) {
                            renameInput = it
                            renameError = null
                        }
                    },
                    label = { Text(strings.createFolderFieldLabel) },
                    singleLine = true,
                    isError = renameError != null,
                    supportingText = renameError?.let { err ->
                        { Text(err, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rename_folder_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRenameCategory(oldName, renameInput) { result ->
                            when (result) {
                                is CategoryMutationResult.Success -> renameTarget = null
                                else -> renameError = mutationErrorText(result, strings)
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_rename_folder_button")
                ) {
                    Text(strings.saveChanges)
                }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    deleteTarget?.let { targetName ->
        val affectedCount = categoryNoteCounts.entries
            .filter { it.key.equals(targetName, ignoreCase = true) }
            .sumOf { it.value }
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(strings.deleteFolderTitle) },
            text = { Text(strings.deleteFolderConfirmBody(strings.categoryLabel(targetName), affectedCount)) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCategory(targetName)
                        deleteTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_folder_button")
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
fun CreateNoteCategoryDialog(
    onCreateCategory: (String, (CategoryMutationResult) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.addFolderDialogTitle) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    if (it.length <= NoteRepository.MAX_CATEGORY_LENGTH) {
                        name = it
                        errorText = null
                    }
                },
                label = { Text(strings.createFolderFieldLabel) },
                placeholder = { Text(strings.createFolderFieldPlaceholder) },
                singleLine = true,
                isError = errorText != null,
                supportingText = errorText?.let { err ->
                    { Text(err, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editor_new_folder_input")
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreateCategory(name) { result ->
                        when (result) {
                            is CategoryMutationResult.Success -> onDismiss()
                            else -> errorText = mutationErrorText(result, strings)
                        }
                    }
                },
                modifier = Modifier.testTag("editor_confirm_new_folder_button")
            ) {
                Text(strings.createFolderSubmit)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}

private fun mutationErrorText(
    result: CategoryMutationResult,
    strings: AppStrings
): String = when (result) {
    CategoryMutationResult.EmptyName -> strings.folderEmptyNameError
    CategoryMutationResult.TooLong -> strings.folderTooLongError
    CategoryMutationResult.DuplicateName -> strings.folderDuplicateError
    CategoryMutationResult.ProtectedCategory -> strings.folderProtectedError
    is CategoryMutationResult.Success -> ""
}
