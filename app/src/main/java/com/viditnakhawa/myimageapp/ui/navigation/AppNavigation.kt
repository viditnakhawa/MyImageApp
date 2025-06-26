package com.viditnakhawa.myimageapp.ui.navigation

//import com.viditnakhawa.myimageapp.ui.LicensesScreen
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.viditnakhawa.myimageapp.ui.ImageViewModel
import com.viditnakhawa.myimageapp.ui.ModelManagerScreen
import com.viditnakhawa.myimageapp.ui.SettingsScreen
import com.viditnakhawa.myimageapp.ui.analysis.AnalysisContainerScreen
import com.viditnakhawa.myimageapp.ui.camera.CameraScreen
import com.viditnakhawa.myimageapp.ui.collections.AddToCollectionDialog
import com.viditnakhawa.myimageapp.ui.collections.CollectionDetailScreen
import com.viditnakhawa.myimageapp.ui.collections.CollectionsScreen
import com.viditnakhawa.myimageapp.ui.collections.CreateCollectionDialog
import com.viditnakhawa.myimageapp.ui.collections.SelectScreenshotsScreen
import com.viditnakhawa.myimageapp.ui.gallery.GalleryScreen
import com.viditnakhawa.myimageapp.ui.modelmanager.ModelManagerViewModel
import com.viditnakhawa.myimageapp.ui.navigation.AppRoutes.COLLECTION_DETAIL
import com.viditnakhawa.myimageapp.ui.navigation.AppRoutes.COLLECTION_ID_ARG
import com.viditnakhawa.myimageapp.ui.viewer.FullScreenViewerScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(navController: NavHostController) {
    val scope = rememberCoroutineScope()
    var showCreateCollectionDialog by remember { mutableStateOf(false) }
    var imageUriToAddToCollection by remember { mutableStateOf<Uri?>(null) }

    if (imageUriToAddToCollection != null) {
        val imageViewModel: ImageViewModel = hiltViewModel()
        val collections by imageViewModel.collections.collectAsStateWithLifecycle(emptyList())
        AddToCollectionDialog(
            collections = collections,
            onDismiss = { imageUriToAddToCollection = null },
            onCollectionSelected = { collectionId ->
                imageViewModel.addImageToCollection(imageUriToAddToCollection.toString(), collectionId)
                imageUriToAddToCollection = null // Dismiss dialog after adding
            },
            onCreateCollection = { collectionName ->
                imageViewModel.createCollection(collectionName)
            }
        )
    }

    if (showCreateCollectionDialog) {
        val imageViewModel: ImageViewModel = hiltViewModel()
        CreateCollectionDialog(
            onDismissRequest = { showCreateCollectionDialog = false },
            onCreateClicked = { collectionName ->
                scope.launch {
                    val newId = imageViewModel.createCollectionAndReturnId(collectionName)
                    navController.navigate(AppRoutes.selectScreenshotsScreen(newId))
                    showCreateCollectionDialog = false
                }
            }
        )
    }

    NavHost(navController = navController, startDestination = AppRoutes.GALLERY) {
        composable(AppRoutes.GALLERY) {
            val imageViewModel: ImageViewModel = hiltViewModel()
            val modelManagerViewModel: ModelManagerViewModel = hiltViewModel()
            GalleryScreen(
                imageViewModel = imageViewModel,
                modelManagerViewModel = modelManagerViewModel,
                onCreateCollection = { showCreateCollectionDialog = true },
                onNavigateToAnalysis = { uri ->
                    navController.navigate(AppRoutes.analysisScreen(Uri.encode(uri.toString())))
                },
                onNavigateToCollections = { navController.navigate(AppRoutes.COLLECTIONS) },
                onNavigateToSettings = { navController.navigate(AppRoutes.SETTINGS) },
                onNavigateToCamera = { navController.navigate(AppRoutes.CAMERA) }
            )
        }

        composable(AppRoutes.SETTINGS) {
            SettingsScreen(navController = navController)
        }

//        composable(AppRoutes.LICENSES) {
//            LicensesScreen(navController = navController)
//        }

        composable(
            route = AppRoutes.ANALYSIS_ROUTE,
            arguments = listOf(navArgument(AppRoutes.URI_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val uriString = Uri.decode(backStackEntry.arguments?.getString(AppRoutes.URI_ARG))
            val imageViewModel: ImageViewModel = hiltViewModel()
            val modelManagerViewModel: ModelManagerViewModel = hiltViewModel()
            AnalysisContainerScreen(
                imageUri = uriString.toUri(),
                imageViewModel = imageViewModel,
                modelManagerViewModel = modelManagerViewModel,
                navController = navController,
                onAddToCollection = { uri -> imageUriToAddToCollection = uri }
            )
        }

        composable(
            route = AppRoutes.VIEWER_ROUTE,
            arguments = listOf(navArgument(AppRoutes.URI_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val uriString = Uri.decode(backStackEntry.arguments?.getString(AppRoutes.URI_ARG))
            FullScreenViewerScreen(
                imageUri = uriString.toUri(),
                onClose = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoutes.SELECT_SCREENSHOTS_ROUTE,
            arguments = listOf(navArgument(COLLECTION_ID_ARG) { type = NavType.LongType })
        ) { backStackEntry ->
            val imageViewModel: ImageViewModel = hiltViewModel()
            val collectionId = backStackEntry.arguments?.getLong(COLLECTION_ID_ARG) ?: return@composable
            val allImages by imageViewModel.images.collectAsStateWithLifecycle()
            SelectScreenshotsScreen(
                allImages = allImages,
                collectionId = collectionId,
                onClose = { navController.popBackStack() },
                onDone = { selectedUris ->
                    val uriStrings = selectedUris.map { it.toString() }
                    imageViewModel.addImagesToCollection(uriStrings, collectionId)
                    navController.navigate(AppRoutes.COLLECTIONS) { popUpTo(AppRoutes.GALLERY) }
                }
            )
        }

        composable(AppRoutes.CAMERA) {
            val imageViewModel: ImageViewModel = hiltViewModel()
            CameraScreen(
                onImageCaptured = { uri ->
                    imageViewModel.addImage(uri)
                    navController.popBackStack()
                },
                onClose = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.MODEL_MANAGER) {
            ModelManagerScreen(onClose = { navController.popBackStack() })
        }

        composable(
            route = "${AppRoutes.VIEWER_ROUTE}/{${AppRoutes.URI_ARG}}",
            arguments = listOf(navArgument(AppRoutes.URI_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val uriString = Uri.decode(backStackEntry.arguments?.getString(AppRoutes.URI_ARG))
            FullScreenViewerScreen(
                imageUri = uriString.toUri(),
                onClose = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.COLLECTIONS) {
            val imageViewModel: ImageViewModel = hiltViewModel()
            val collections by imageViewModel.collectionsWithImages.collectAsStateWithLifecycle()
            CollectionsScreen(
                collections = collections,
                onNavigateBack = { navController.popBackStack() },
                onCollectionClick = { collectionId ->
                    navController.navigate(AppRoutes.collectionDetailRoute(collectionId))
                }
            )
        }

        composable(
            route = "$COLLECTION_DETAIL/{$COLLECTION_ID_ARG}",
            arguments = listOf(navArgument(COLLECTION_ID_ARG) { type = NavType.LongType })
        ) { backStackEntry ->
            val imageViewModel: ImageViewModel = hiltViewModel()
            val modelManagerViewModel: ModelManagerViewModel = hiltViewModel()
            val collectionId = backStackEntry.arguments?.getLong(COLLECTION_ID_ARG) ?: return@composable
            CollectionDetailScreen(
                navController = navController,
                imageViewModel = imageViewModel,
                modelManagerViewModel = modelManagerViewModel,
                collectionId = collectionId
            )
        }


        /* // --- CHAT FEATURE COMMENTED OUT AS REQUESTED ---
      composable(
            route = AppRoutes.CHAT_ROUTE,
            arguments = listOf(navArgument(AppRoutes.URI_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
             val uriString = Uri.decode(backStackEntry.arguments?.getString(AppRoutes.URI_ARG))
             ImageChatScreen(
                 imageUri = Uri.parse(uriString),
                 modelManagerViewModel = modelManagerViewModel,
                 onClose = { navController.popBackStack() }
             )
        }
        */
    }
}
