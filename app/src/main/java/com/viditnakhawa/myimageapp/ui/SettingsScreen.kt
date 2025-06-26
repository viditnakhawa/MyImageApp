package com.viditnakhawa.myimageapp.ui

import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.google.android.datatransport.BuildConfig
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.viditnakhawa.myimageapp.ui.navigation.AppRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            item {
                SettingsItem(
                    icon = Icons.Default.Memory,
                    title = "Manage Gemma Model",
                    subtitle = "Download, initialize, or delete the on-device model.",
                    onClick = { navController.navigate(AppRoutes.MODEL_MANAGER) }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.Subject,
                    title = "Open Source Licenses",
                    subtitle = "View licenses for open source software used in SnapSuite.",
                    onClick = {
                        OssLicensesMenuActivity.setActivityTitle("Open Source Licenses")
                        context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                    }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Lightbulb, // New icon
                    title = "Tips & Features",
                    subtitle = "Learn more about what SnapSuite can do.",
                    onClick = {
                        navController.navigate(AppRoutes.ONBOARDING)
                    }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Email,
                    title = "Send Feedback",
                    subtitle = "Report issues or suggest improvements",
                    onClick = {
                        val appVersion = BuildConfig.VERSION_NAME
                        val deviceInfo = "Device: ${Build.MANUFACTURER} ${Build.MODEL}\n" +
                                "Android Version: ${Build.VERSION.SDK_INT}"
                        val subject = "Feedback for SnapSuite v$appVersion"
                        val body = "Please describe your feedback or the issue you're facing:\n\n\n---\n$deviceInfo"

                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:".toUri()
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("vidtnakhawa@duck.com"))
                            putExtra(Intent.EXTRA_SUBJECT, subject)
                            putExtra(Intent.EXTRA_TEXT, body)
                        }
                        try {
                            context.startActivity(Intent.createChooser(intent, "Send Feedback"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "No email app installed.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    subtitle = "SnapSuite v${BuildConfig.VERSION_NAME}",
                    onClick = { /* Could navigate to an about screen in the future */ }
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
