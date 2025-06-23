package com.viditnakhawa.myimageapp.ui.gallery

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.viditnakhawa.myimageapp.data.ImageEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A dedicated composable for displaying a single image in the gallery grid.
 * It includes logic to overlay the title if one exists.
 */
@Composable
fun GalleryImageItem(
    image: ImageEntity,
    onImageClick: (ImageEntity) -> Unit,
    isTwoColumnLayout: Boolean,
) {

    val interactionModifier = Modifier
        .pointerInput(image.imageUri) {
            detectTapGestures(
                onTap = { onImageClick(image) }
            )
        }
        .aspectRatio(1f)

    if (isTwoColumnLayout) {
        // --- For the 2-column grid, use the new Card with the interaction modifier ---
        GalleryImageCard(
            image = image,
            modifier = interactionModifier
        )
    } else {
    var showTitleOverlay by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val titleAlpha by animateFloatAsState(
        targetValue = if (showTitleOverlay) 1f else 0f,
        animationSpec = tween(300),
        label = "TitleAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Makes the items square
            .pointerInput(image.imageUri) {
                detectTapGestures(
                    onLongPress = {
                        if (image.sourceApp != null) {
                            scope.launch {
                                showTitleOverlay = true
                                delay(5000L) // Stays visible for 5 seconds
                                showTitleOverlay = false
                            }
                        }
                    },
                    onTap = {
                        onImageClick(image)
                    }
                )
            },
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Background Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(image.imageUri.toUri())
                    .crossfade(true)
                    .size(if (isTwoColumnLayout) 512 else 256)
                    .allowHardware(false)
                    .build(),
                placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                contentDescription = image.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (titleAlpha > 0 && !image.title.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(titleAlpha) // Apply the animated alpha here
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.8f)
                                ),
                            )
                        ),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Text(
                        text = image.title.orEmpty(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Start,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                }
            }
        }
    }
}
//@Preview(showBackground = true)
//@Composable
//fun GalleryImageItemPreview() {
//    GalleryImageItem(
//        image = ImageEntity(
//            imageUri = "https://via.placeholder.com/300", // sample image URL
//            title = "Sample Title",
//            sourceApp = "com.example.app"
//        ),
//        onImageClick = {},
//        isTwoColumnLayout = true
//    )
//}
