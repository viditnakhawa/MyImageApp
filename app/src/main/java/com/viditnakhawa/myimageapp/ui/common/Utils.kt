package com.viditnakhawa.myimageapp.ui.common


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.viditnakhawa.myimageapp.data.Model
import com.viditnakhawa.myimageapp.data.Task
import com.viditnakhawa.myimageapp.ui.modelmanager.ModelManagerViewModel
import com.viditnakhawa.myimageapp.ui.theme.customColors
import kotlin.math.ln
import kotlin.math.pow

fun Long.humanReadableSize(si: Boolean = true, extraDecimalForGbAndAbove: Boolean = false): String {
    val bytes = this
    val unit = if (si) 1000 else 1024
    if (bytes < unit) return "$bytes B"
    val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
    val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
    var formatString = "%.1f %sB"
    if (extraDecimalForGbAndAbove && pre.lowercase() != "k" && pre != "M") {
        formatString = "%.2f %sB"
    }
    return String.format(formatString, bytes / unit.toDouble().pow(exp.toDouble()), pre)
}

fun Long.formatToHourMinSecond(): String {
    val ms = this
    if (ms < 0) return "-"
    val seconds = ms / 1000
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60
    val parts = mutableListOf<String>()
    if (hours > 0) parts.add("$hours h")
    if (minutes > 0) parts.add("$minutes min")
    if (remainingSeconds > 0 || (hours == 0L && minutes == 0L)) parts.add("$remainingSeconds sec")
    return parts.joinToString(" ")
}

@Composable
fun getTaskBgColor(task: Task): Color {
    // Return a standard color from your theme, for example, a subtle surface variant.
    return MaterialTheme.colorScheme.surfaceVariant
}

@Composable
fun getTaskIconColor(task: Task): Color {
    // Return the primary color of your theme for the icon.
    return MaterialTheme.colorScheme.primary
}

fun checkNotificationPermissionAndStartDownload(
    context: Context,
    launcher: ManagedActivityResultLauncher<String, Boolean>,
    modelManagerViewModel: ModelManagerViewModel,
    task: Task,
    model: Model
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        modelManagerViewModel.downloadModel(task = task, model = model)
    }
}