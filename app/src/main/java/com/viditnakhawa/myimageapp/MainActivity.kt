package com.viditnakhawa.myimageapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.viditnakhawa.myimageapp.ui.PermissionsScreen
import com.viditnakhawa.myimageapp.ui.navigation.AppNavigation
import com.viditnakhawa.myimageapp.ui.navigation.AppRoutes
import com.viditnakhawa.myimageapp.ui.theme.MyImageAppTheme
import com.viditnakhawa.myimageapp.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MLKitImgDescProcessor.initialize(applicationContext)
        enableEdgeToEdge()

        setContent {
            MyImageAppTheme {
                val hasCompletedOnboarding by mainViewModel.hasCompletedOnboarding.collectAsStateWithLifecycle()
                var permissionsGranted by remember { mutableStateOf(checkAllPermissions()) }

                if (permissionsGranted) {
                    val navController = rememberNavController()
                    val startDestination = if (hasCompletedOnboarding) AppRoutes.GALLERY else AppRoutes.ONBOARDING
                    AppNavigation(navController = navController, startDestination = startDestination)
                } else {
                    PermissionsScreen(onPermissionsGranted = { permissionsGranted = true })
                }
            }
        }
    }

    private fun checkAllPermissions(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
        }
        return permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
    }
}

