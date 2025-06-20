package com.viditnakhawa.myimageapp.ui.collections

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCollectionDialog(
    onDismissRequest: () -> Unit,
    onCreateClicked: (String) -> Unit
) {
    var collectionName by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("New collection") },
        text = {
            OutlinedTextField(
                value = collectionName,
                onValueChange = { collectionName = it },
                label = { Text("New collection title") },
                singleLine = true,
                modifier = Modifier.focusRequester(focusRequester)
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (collectionName.isNotBlank()) {
                        onCreateClicked(collectionName)
                    }
                },
                enabled = collectionName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }
}
