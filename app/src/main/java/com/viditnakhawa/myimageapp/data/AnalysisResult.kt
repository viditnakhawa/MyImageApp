package com.viditnakhawa.myimageapp.data

// This data class is now in its own file to be shared across workers.
data class StructuredAnalysis(
    val title: String,
    val summary: String,
    val sourceApp: String,
    val tags: List<String>,
    val formattedOcr: String?
)