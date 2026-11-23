package dz.arabic.konnachetaleb.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dz.arabic.konnachetaleb.data.ArabicLmdCatalog
import dz.arabic.konnachetaleb.data.SessionType
import dz.arabic.konnachetaleb.data.SubjectEntity
import dz.arabic.konnachetaleb.domain.TimetableHelper
import dz.arabic.konnachetaleb.ui.KonnachViewModel

fun iconForKey(key: String): ImageVector = when (key) {
    "quill" -> Icons.Default.HistoryEdu
    "scroll" -> Icons.Default.Description
    "library" -> Icons.Default.LibraryBooks
    "grammar" -> Icons.Default.MenuBook
    "poetry" -> Icons.Default.AutoStories
    "research" -> Icons.Default.Create
    "language" -> Icons.Default.Translate
    else -> Icons.Default.Book
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeNotebooksScreen(
    viewModel: KonnachViewModel,
    onOpenSubject: (String) -> Unit,
    onGoToBackupSettings: () -> Unit
) {
    val prefs by viewModel.preferences.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val nextClass by viewModel.nextClassMatch.collectAsState()

    val semesters = remember(subjects) { subjects.map { it.semester }.distinct() }
    var selectedSemester by remember(semesters) { mutableStateOf("الكل") }
    var editingSubject by remember { mutableStateOf<SubjectEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var deletingSubject by remember { mutableStateOf<SubjectEntity?>(null) }

    val filteredSubjects = remember(subjects, selectedSemester) {
        if (selectedSemester == "الكل") subjects
        else subjects.filter { it.semester == selectedSemester }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة مقياس")
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item(span = { GridItemSpan(2) }) {
                Column {
                    Text(
                        text = "كناش الطالب",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${prefs.cycle} ${prefs.year} — ${prefs.specialty}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 30-Day Gentle Backup Reminder Banner
            if (prefs.shouldRemindBackup()) {
                item(span = { GridItemSpan(2) }) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGoToBackupSettings() }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Backup,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "مرّ أكثر من 30 يوماً دون نسخة احتياطية — اضغط هنا لحفظ دروسك بأمان.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Smart Next / Ongoing Class Banner
            nextClass?.let { match ->
                item(span = { GridItemSpan(2) }) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenSubject(match.subject.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (match.isOngoing) "الحصة الجارية الآن:" else "الحصة القادمة (${TimetableHelper.dayName(match.slot.dayOfWeek)}):",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = match.subject.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                val roomTxt = if (match.slot.room.isNotBlank()) " | القاعة: ${match.slot.room}" else ""
                                Text(
                                    text = "${SessionType.fromCode(match.slot.sessionType).arabicLabel} (${match.slot.startTime}–${match.slot.endTime})$roomTxt",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Semester Filter Chips
            if (semesters.size > 1) {
                item(span = { GridItemSpan(2) }) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedSemester == "الكل",
                            onClick = { selectedSemester = "الكل" },
                            label = { Text("الكل (${subjects.size})") }
                        )
                        semesters.forEach { sem ->
                            FilterChip(
                                selected = selectedSemester == sem,
                                onClick = { selectedSemester = sem },
                                label = { Text(sem) }
                            )
                        }
                    }
                }
            }

            // Colorful Notebook Cards
            items(filteredSubjects, key = { it.id }) { subject ->
                NotebookCard(
                    subject = subject,
                    onClick = { onOpenSubject(subject.id) },
                    onEdit = { editingSubject = subject },
                    onDelete = { deletingSubject = subject }
                )
            }

            item(span = { GridItemSpan(2) }) { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    if (showAddDialog) {
        SubjectEditDialog(
            initial = null,
            defaultSemester = prefs.semester,
            onDismiss = { showAddDialog = false },
            onSave = { name, sem, prof, coeff, cred, color, icon ->
                viewModel.addCustomSubject(name, sem, prof, coeff, cred, color, icon)
                showAddDialog = false
            }
        )
    }

    editingSubject?.let { target ->
        SubjectEditDialog(
            initial = target,
            defaultSemester = target.semester,
            onDismiss = { editingSubject = null },
            onSave = { name, sem, prof, coeff, cred, color, icon ->
                viewModel.saveSubject(
                    target.copy(
                        name = name,
                        semester = sem,
                        professorName = prof,
                        coefficient = coeff,
                        credits = cred,
                        colorHex = color,
                        iconKey = icon
                    )
                )
                editingSubject = null
            }
        )
    }

    deletingSubject?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingSubject = null },
            title = { Text("حذف الكراس") },
            text = { Text("هل تريد حذف مقياس «${target.name}» وجميع دروسه ومهامه؟") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSubject(target)
                    deletingSubject = null
                }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deletingSubject = null }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
private fun NotebookCard(
    subject: SubjectEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val coverColor = Color(subject.colorHex)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = coverColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(164.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconForKey(subject.iconKey),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(17.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFFECACA), modifier = Modifier.size(17.dp))
                    }
                }
            }

            Column {
                Text(
                    text = subject.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                    lineHeight = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                val profText = subject.professorName.ifBlank { "أستاذ المقياس" }
                Text(
                    text = "${subject.semester} • $profText",
                    color = Color(0xFFFEF3C7),
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubjectEditDialog(
    initial: SubjectEntity?,
    defaultSemester: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, Long, String) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var semester by remember { mutableStateOf(initial?.semester ?: defaultSemester) }
    var professor by remember { mutableStateOf(initial?.professorName ?: "") }
    var coeff by remember { mutableStateOf(initial?.coefficient ?: "2") }
    var credits by remember { mutableStateOf(initial?.credits ?: "4") }
    var colorHex by remember { mutableLongStateOf(initial?.colorHex ?: ArabicLmdCatalog.NOTEBOOK_COLORS.first()) }
    var iconKey by remember { mutableStateOf(initial?.iconKey ?: ArabicLmdCatalog.NOTEBOOK_ICONS.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "إضافة مقياس جديد" else "تعديل بطاقة المقياس") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المقياس") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = professor,
                    onValueChange = { professor = it },
                    label = { Text("اسم الأستاذ (اختياري)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = semester,
                        onValueChange = { semester = it },
                        label = { Text("السداسي") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = coeff,
                        onValueChange = { coeff = it },
                        label = { Text("المعامل") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = credits,
                        onValueChange = { credits = it },
                        label = { Text("الرصيد") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("لون الكراس:", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ArabicLmdCatalog.NOTEBOOK_COLORS.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(hex))
                                .border(
                                    width = if (colorHex == hex) 3.dp else 0.dp,
                                    color = if (colorHex == hex) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { colorHex = hex }
                        )
                    }
                }

                Text("أيقونة الكراس:", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ArabicLmdCatalog.NOTEBOOK_ICONS.forEach { key ->
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (iconKey == key) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { iconKey = key },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(iconForKey(key), contentDescription = key, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, semester, professor, coeff, credits, colorHex, iconKey)
                    }
                }
            ) { Text("حفظ") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
