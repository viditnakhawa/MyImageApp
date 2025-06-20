package com.viditnakhawa.myimageapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.viditnakhawa.myimageapp.data.CollectionEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToCollectionDialog(
    collections: List<CollectionEntity>,
    onDismiss: () -> Unit,
    onCollectionSelected: (Long) -> Unit,
    onCreateCollection: (String) -> Unit
) {
    var newCollectionName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Collection") },
        text = {
            Column {
                // Input for creating a new collection
                OutlinedTextField(
                    value = newCollectionName,
                    onValueChange = { newCollectionName = it },
                    label = { Text("New collection name...") },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (newCollectionName.isNotBlank()) {
                                    onCreateCollection(newCollectionName)
                                    newCollectionName = ""
                                }
                            },
                            enabled = newCollectionName.isNotBlank()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Create Collection")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // List of existing collections
                LazyColumn {
                    items(collections) { collection ->
                        Text(
                            text = collection.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCollectionSelected(collection.id) }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}