package com.viditnakhawa.myimageapp.ui.navigation

import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.viditnakhawa.myimageapp.ui.analysis.AnalysisContainerScreen
import com.viditnakhawa.myimageapp.ui.camera.CameraScreen
import com.viditnakhawa.myimageapp.ui.collections.CollectionsScreen
import com.viditnakhawa.myimageapp.ui.collections.CreateCollectionDialog
import com.viditnakhawa.myimageapp.ui.collections.SelectScreenshotsScreen
import com.viditnakhawa.myimageapp.ui.common.ViewModelProvider
import com.viditnakhawa.myimageapp.ui.gallery.GalleryScreen
import com.viditnakhawa.myimageapp.ui.modelmanager.ModelManagerViewModel
import com.viditnakhawa.myimageapp.ui.viewer.FullScreenViewerScreen
import com.viditnakhawa.myimageapp.ui.ImageViewModel
import com.viditnakhawa.myimageapp.ui.ModelManagerScreen
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import com.viditnakhawa.myimageapp.ui.AddToCollectionDialog
import com.viditnakhawa.myimageapp.ui.LicensesScreen
import com.viditnakhawa.myimageapp.ui.SettingsScreen

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()
    val imageViewModel: ImageViewModel = viewModel(factory = ViewModelProvider.Factory)
    val modelManagerViewModel: ModelManagerViewModel = viewModel(factory = ViewModelProvider.Factory)
    val scope = rememberCoroutineScope()
    var showCreateCollectionDialog by remember { mutableStateOf(false) }
    var imageUriToAddToCollection by remember { mutableStateOf<Uri?>(null) }
    val collections by imageViewModel.collections.collectAsStateWithLifecycle(emptyList())

    if (imageUriToAddToCollection != null) {
        AddToCollectionDialog(
            collections = collections,
            onDismiss = { imageUriToAddToCollection = null },
            onCollectionSelected = { collectionId ->
                imageViewModel.addImageToCollection(imageUriToAddToCollection.toString(), collectionId)
                // You can add a Toast here for confirmation
                imageUriToAddToCollection = null // Dismiss dialog after adding
            },
            onCreateCollection = { collectionName ->
                // This uses the existing create collection function from the ViewModel
                imageViewModel.createCollection(collectionName)
            }
        )
    }

    if (showCreateCollectionDialog) {
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

        composable(AppRoutes.LICENSES) {
            LicensesScreen(navController = navController)
        }

        composable(
            route = AppRoutes.ANALYSIS_ROUTE,
            arguments = listOf(navArgument(AppRoutes.URI_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val uriString = Uri.decode(backStackEntry.arguments?.getString(AppRoutes.URI_ARG))
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
            arguments = listOf(navArgument(AppRoutes.COLLECTION_ID_ARG) { type = NavType.LongType })
        ) { backStackEntry ->
            val collectionId = backStackEntry.arguments?.getLong(AppRoutes.COLLECTION_ID_ARG) ?: return@composable
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
            val collections by imageViewModel.collectionsWithImages.collectAsStateWithLifecycle()
            CollectionsScreen(
                collections = collections,
                onNavigateBack = { navController.popBackStack() }
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
