package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.CategoryMutationResult
import com.example.data.ImageStorageManager
import com.example.ui.NoteEditorUiState
import com.example.ui.SaveStatus
import com.example.ui.components.CreateNoteCategoryDialog
import com.example.ui.components.FullscreenImageDialog
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.LocalAppButtonSize
import com.example.ui.theme.NotePaperColor
import com.example.ui.utils.formatFullTimestamp
import java.io.File

@Composable
fun NoteEditorScreen(
    uiState: NoteEditorUiState,
    imageStorageManager: ImageStorageManager,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onCreateCategory: (String, (String) -> String, (CategoryMutationResult) -> Unit) -> Unit,
    onColorChange: (String) -> Unit,
    onTogglePin: () -> Unit,
    onAddImagesFromUris: (List<android.net.Uri>) -> Unit,
    onAddImageFromCameraFile: (File) -> Unit,
    onRemoveImage: (String) -> Unit,
    onDeleteNote: () -> Unit,
    onBackPressed: () -> Unit,
    onExportPdf: (android.net.Uri) -> Unit,
    onClearStatusMessage: () -> Unit,
    onClearError: () -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val buttonSize = LocalAppButtonSize.current
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current
    val snackbarHostState = remember { SnackbarHostState() }

    val paperColor = remember(uiState.colorKey) { NotePaperColor.fromKey(uiState.colorKey) }
    val backgroundColor = if (isDark) paperColor.darkBg else paperColor.lightBg

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showCreateCategoryDialog by remember { mutableStateOf(false) }
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }
    var viewerInitialIndex by remember { mutableIntStateOf(-1) }

    BackHandler {
        onBackPressed()
    }

    LaunchedEffect(uiState.errorMessage) {
        val err = uiState.errorMessage
        if (!err.isNullOrBlank()) {
            snackbarHostState.showSnackbar(err)
            onClearError()
        }
    }

    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            onClearStatusMessage()
        }
    }

    val pdfExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
        onResult = { uri ->
            if (uri != null) {
                onExportPdf(uri)
            }
        }
    )

    // Zero-permission Android Photo Picker (Play Policy compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                onAddImagesFromUris(uris)
            }
        }
    )

    // System Camera Capture via FileProvider (no CAMERA permission needed)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            val file = pendingCameraFile
            pendingCameraFile = null
            if (success && file != null) {
                onAddImageFromCameraFile(file)
            } else {
                file?.delete()
            }
        }
    )

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = backgroundColor,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            Surface(
                color = backgroundColor,
                tonalElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .height(buttonSize.height)
                                .testTag("add_gallery_image_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = buttonSize.horizontalPadding),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AddPhotoAlternate,
                                    contentDescription = strings.gallery,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(buttonSize.iconSize)
                                )
                                Text(
                                    text = strings.gallery,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Surface(
                            onClick = {
                                val (uri, tempFile) = imageStorageManager.createCameraCaptureUri()
                                pendingCameraFile = tempFile
                                cameraLauncher.launch(uri)
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .height(buttonSize.height)
                                .testTag("add_camera_image_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = buttonSize.horizontalPadding),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CameraAlt,
                                    contentDescription = strings.camera,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(buttonSize.iconSize)
                                )
                                Text(
                                    text = strings.camera,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Text(
                        text = strings.photoCount(uiState.imageNames.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBackPressed,
                    modifier = Modifier
                        .size(buttonSize.height)
                        .testTag("editor_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.back,
                        modifier = Modifier.size(buttonSize.iconSize + 3.dp)
                    )
                }

                // Auto-save status pill
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (uiState.saveStatus == SaveStatus.SAVING || uiState.isAttachingImages) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(13.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = strings.saving,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = strings.autoSaved,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier
                            .size(buttonSize.height)
                            .testTag("editor_pin_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (uiState.isPinned) {
                                strings.unpinNote
                            } else {
                                strings.pinNote
                            },
                            tint = if (uiState.isPinned) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(buttonSize.iconSize + 3.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val shareText = buildString {
                                if (uiState.title.isNotBlank()) {
                                    appendLine(uiState.title)
                                    appendLine()
                                }
                                append(uiState.content)
                            }.trim()

                            val imageUris = ArrayList(
                                uiState.imageNames.mapNotNull { imageStorageManager.getShareableUri(it) }
                            )

                            val intent = when {
                                imageUris.isEmpty() -> Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText.ifBlank { "Note All" })
                                }
                                imageUris.size == 1 -> Intent(Intent.ACTION_SEND).apply {
                                    type = "image/jpeg"
                                    putExtra(Intent.EXTRA_STREAM, imageUris[0])
                                    if (shareText.isNotBlank()) putExtra(Intent.EXTRA_TEXT, shareText)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                else -> Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                                    type = "image/jpeg"
                                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris)
                                    if (shareText.isNotBlank()) putExtra(Intent.EXTRA_TEXT, shareText)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                            }
                            context.startActivity(Intent.createChooser(intent, strings.shareNote))
                        },
                        modifier = Modifier
                            .size(buttonSize.height)
                            .testTag("editor_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = strings.shareNote,
                            modifier = Modifier.size(buttonSize.iconSize + 3.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val safeSlug = uiState.title
                                .trim()
                                .replace(Regex("[^A-Za-z0-9_-]+"), "_")
                                .trim('_')
                                .take(32)
                                .ifBlank { "Catatan" }
                            pdfExportLauncher.launch("NoteAll_${safeSlug}.pdf")
                        },
                        enabled = !uiState.isExportingPdf,
                        modifier = Modifier
                            .size(buttonSize.height)
                            .testTag("editor_export_pdf_button")
                    ) {
                        if (uiState.isExportingPdf) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(buttonSize.iconSize),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.PictureAsPdf,
                                contentDescription = strings.exportNotePdfDesc,
                                modifier = Modifier.size(buttonSize.iconSize + 3.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .size(buttonSize.height)
                            .testTag("editor_delete_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = strings.deleteNoteAction,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(buttonSize.iconSize + 3.dp)
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .widthIn(max = 760.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 24.dp)
                ) {
                    // Color Palette & Timestamp Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NotePaperColor.entries.forEach { colorOption ->
                                val selected = colorOption.key == uiState.colorKey
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(colorOption.accentDot)
                                        .border(
                                            width = if (selected) 2.5.dp else 1.dp,
                                            color = if (selected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.outline
                                            },
                                            shape = CircleShape
                                        )
                                        .clickable { onColorChange(colorOption.key) }
                                        .testTag("color_dot_${colorOption.key.lowercase()}")
                                )
                            }
                        }

                        Text(
                            text = formatFullTimestamp(uiState.updatedAt, language.locale),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Category Selector Chips
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.availableCategories, key = { it.lowercase() }) { category ->
                            val selected = uiState.category.equals(category, ignoreCase = true)
                            FilterChip(
                                selected = selected,
                                onClick = { onCategoryChange(category) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Folder,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text(strings.categoryLabel(category)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("editor_category_${category.lowercase()}")
                            )
                        }

                        item(key = "__editor_add_folder__") {
                            AssistChip(
                                onClick = { showCreateCategoryDialog = true },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.CreateNewFolder,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text("+ ${strings.folderActionChip}") },
                                modifier = Modifier.testTag("editor_add_category_button")
                            )
                        }
                    }

                    // Attached Image Strip
                    if (uiState.imageNames.isNotEmpty() || uiState.isAttachingImages) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(uiState.imageNames) { index, fileName ->
                                val file = remember(fileName) {
                                    imageStorageManager.getImageFile(fileName)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(width = 148.dp, height = 148.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable { viewerInitialIndex = index }
                                        .testTag("attached_image_$index")
                                ) {
                                    AsyncImage(
                                        model = file,
                                        contentDescription = "Lampiran foto ${index + 1}",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Surface(
                                        onClick = { onRemoveImage(fileName) },
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.65f),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(28.dp)
                                            .testTag("remove_image_$index")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Filled.Close,
                                                contentDescription = strings.removeImageDesc,
                                                tint = MaterialTheme.colorScheme.surface,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (uiState.isAttachingImages) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.size(148.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Editorial Title Field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        if (uiState.title.isEmpty()) {
                            Text(
                                text = strings.editorTitleHint,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                            )
                        }
                        BasicTextField(
                            value = uiState.title,
                            onValueChange = onTitleChange,
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("editor_title_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Body Content Field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                    ) {
                        if (uiState.content.isEmpty()) {
                            Text(
                                text = strings.editorBodyHint,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                            )
                        }
                        BasicTextField(
                            value = uiState.content,
                            onValueChange = onContentChange,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("editor_body_input")
                        )
                    }
                }
            }
        }
    }

    if (viewerInitialIndex >= 0 && uiState.imageNames.isNotEmpty()) {
        FullscreenImageDialog(
            imageNames = uiState.imageNames,
            initialIndex = viewerInitialIndex,
            imageStorageManager = imageStorageManager,
            onDeleteImage = { fileName -> onRemoveImage(fileName) },
            onDismiss = { viewerInitialIndex = -1 }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(strings.deleteNoteConfirmTitle) },
            text = { Text(strings.deleteNoteConfirmBody) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteNote()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("editor_confirm_delete_button")
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false },
                    modifier = Modifier.testTag("editor_cancel_delete_button")
                ) {
                    Text(strings.cancel)
                }
            }
        )
    }

    if (showCreateCategoryDialog) {
        CreateNoteCategoryDialog(
            onCreateCategory = { name, onResult ->
                onCreateCategory(name, strings.folderCreatedMessage, onResult)
            },
            onDismiss = { showCreateCategoryDialog = false }
        )
    }
}
