package com.viditnakhawa.myimageapp.ui.common


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.viditnakhawa.myimageapp.data.Task

@Composable
fun TaskIcon(task: Task, modifier: Modifier = Modifier, width: Dp = 48.dp) {
    Box(
        modifier = modifier
            .width(width)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            task.icon ?: ImageVector.vectorResource(id = android.R.drawable.ic_menu_help), // Placeholder icon
            tint = getTaskIconColor(task = task),
            modifier = Modifier.size(width * 0.6f),
            contentDescription = "",
        )
    }
}