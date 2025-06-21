package com.viditnakhawa.myimageapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Open Source Licenses") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(licenses) { license ->
                LicenseItem(license)
            }
        }
    }
}

@Composable
private fun LicenseItem(license: LicenseInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = license.libraryName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text(text = license.license, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private data class LicenseInfo(val libraryName: String, val license: String)

private const val apache2LicenseText = """ (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License."""

private val licenses = listOf(
    LicenseInfo(
        "AndroidX (Jetpack)",
        "Copyright 2018 The Android Open Source Project\n\nLicensed under the Apache License, Version 2.0"
    ),
    LicenseInfo(
        "Kotlin",
        "Copyright 2010-2021 JetBrains s.r.o.\n\nLicensed under the Apache License, Version 2.0"
    ),
    LicenseInfo(
        "Coil (Compose Image Loading)",
        "Copyright 2022 Coil Contributors\n\nLicensed under the Apache License, Version 2.0"
    ),
    LicenseInfo(
        "Accompanist",
        "Copyright 2022 The Accompanist Authors.\n\nLicensed under the Apache License, Version 2.0"
    ),
    LicenseInfo(
        "Google MediaPipe",
        "Copyright 2023 The MediaPipe Authors.\n\nLicensed under the Apache License, Version 2.0"
    ),
    LicenseInfo(
        "Google ML Kit",
        "Copyright 2024 Google LLC\n\nLicensed under the Apache License, Version 2.0"
    )
).map { it.copy(license = it.license + apache2LicenseText) }
