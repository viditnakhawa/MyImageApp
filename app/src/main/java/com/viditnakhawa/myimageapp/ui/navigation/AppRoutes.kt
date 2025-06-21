package com.viditnakhawa.myimageapp.ui.navigation

object AppRoutes {
    // Base routes
    const val GALLERY = "gallery"
    const val COLLECTIONS = "collections"
    const val MODEL_MANAGER = "model_manager"
    const val CAMERA = "camera"
    const val SETTINGS = "settings"
    const val LICENSES = "licenses"

    // Argument keys
    const val URI_ARG = "uri"
    const val COLLECTION_ID_ARG = "collectionId"

    // Routes with arguments
    const val ANALYSIS_ROUTE = "analysis/{$URI_ARG}"
    const val VIEWER_ROUTE = "viewer/{$URI_ARG}"
    const val CHAT_ROUTE = "chat/{$URI_ARG}"
    const val SELECT_SCREENSHOTS_ROUTE = "select_screenshots/{$COLLECTION_ID_ARG}"

    // Helper functions to build navigation paths
    fun analysisScreen(uri: String) = "analysis/$uri"
    fun viewerScreen(uri: String) = "viewer/$uri"
    fun chatScreen(uri: String) = "chat/$uri"
    fun selectScreenshotsScreen(id: Long) = "select_screenshots/$id"
}