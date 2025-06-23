package com.viditnakhawa.myimageapp.ui.gallery

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.viditnakhawa.myimageapp.data.ImageEntity
import androidx.compose.ui.tooling.preview.Preview
@Composable
fun GalleryImageCard(
    image: ImageEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(image.imageUri.toUri())
                    .crossfade(true)
                    .size(512) // Load a slightly larger image for this view
                    .allowHardware(false)
                    .build(),
                contentDescription = image.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    // The clip is important to ensure the top corners of the image are rounded
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            )

            if (!image.title.isNullOrBlank()) {
                Text(
                    text = image.title!!,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun GalleryImageCardPreview() {
//    val mockImage = ImageEntity(
//        imageUri = "https://picsum.photos/600/400", // sample placeholder image URL
//        title = "Sample Image Title",
//        sourceApp = "com.example.app"
//    )
//
//    GalleryImageCard(
//        image = mockImage,
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp)
//    )
//}
