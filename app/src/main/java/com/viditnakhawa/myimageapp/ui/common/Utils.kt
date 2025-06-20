package com.viditnakhawa.myimageapp.ui.common


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.viditnakhawa.myimageapp.data.Model
import com.viditnakhawa.myimageapp.data.Task
import com.viditnakhawa.myimageapp.ui.modelmanager.ModelManagerViewModel
import java.io.File
import kotlin.math.ln
import kotlin.math.pow

private const val LAUNCH_INFO_FILE_NAME = "launch_info"

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
    return MaterialTheme.colorScheme.surfaceVariant
}

@Composable
fun getTaskIconColor(task: Task): Color {
    return MaterialTheme.colorScheme.primary
}

fun checkNotificationPermissionAndStartDownload(
    context: Context,
    launcher: ActivityResultLauncher<String>,
    modelManagerViewModel: ModelManagerViewModel,
    task: Task,
    model: Model
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            // ✅ Start download immediately
            modelManagerViewModel.downloadModel(task = task, model = model)
        } else {
            launcher.launch(permission)
        }
    } else {
        // ✅ No permission needed, start download directly
        modelManagerViewModel.downloadModel(task = task, model = model)
    }
}

data class LaunchInfo(
    val ts: Long
)

fun cleanUpMediapipeTaskErrorMessage(message: String): String {
    val index = message.indexOf("=== Source Location Trace")
    if (index >= 0) {
        return message.substring(0, index)
    }
    return message
}

fun writeLaunchInfo(context: Context) {
    try {
        val gson = Gson()
        val launchInfo = LaunchInfo(ts = System.currentTimeMillis())
        val jsonString = gson.toJson(launchInfo)
        val file = File(context.getExternalFilesDir(null), LAUNCH_INFO_FILE_NAME)
        file.writeText(jsonString)
    } catch (e: Exception) {
        //Log.e(TAG, "Failed to write launch info", e)
    }
}

fun readLaunchInfo(context: Context): LaunchInfo? {
    try {
        val gson = Gson()
        val type = object : TypeToken<LaunchInfo>() {}.type
        val file = File(context.getExternalFilesDir(null), LAUNCH_INFO_FILE_NAME)
        val content = file.readText()
        return gson.fromJson(content, type)
    } catch (e: Exception) {
        //Log.e(TAG, "Failed to read launch info", e)
        return null
    }
}

