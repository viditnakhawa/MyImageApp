package com.viditnakhawa.myimageapp.ui.collections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
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
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Add to Collection",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // New Collection Input
                OutlinedTextField(
                    value = newCollectionName,
                    onValueChange = { newCollectionName = it },
                    label = { Text("Create new collection") },
                    singleLine = true,
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
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Divider
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "Your Collections",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // List of Existing Collections
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    if (collections.isEmpty()) {
                        item {
                            Text(
                                text = "No collections yet. Create one above!",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(collections) { collection ->
                            Surface(
                                tonalElevation = 2.dp,
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCollectionSelected(collection.id) }
                            ) {
                                Text(
                                    text = collection.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                        }
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

//@Preview(showBackground = true)
//@Composable
//fun AddToCollectionDialogPreview() {
//    // Sample fake collections
//    val sampleCollections = listOf(
//        CollectionEntity(id = 1L, name = "Travel"),
//        CollectionEntity(id = 2L, name = "Family"),
//        CollectionEntity(id = 3L, name = "Work Projects")
//    )
//
//    // Use a basic Material3 theme to preview nicely
//    MaterialTheme {
//        AddToCollectionDialog(
//            collections = sampleCollections,
//            onDismiss = {},
//            onCollectionSelected = {},
//            onCreateCollection = {}
//        )
//    }
//}
