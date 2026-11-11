package com.ankit.familytools

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ankit.familytools.data.repository.FavoritesRepository
import com.ankit.familytools.data.repository.HistoryRepository
import com.ankit.familytools.data.repository.SignatureRepository
import com.ankit.familytools.ui.screens.history.HistoryScreen
import com.ankit.familytools.ui.screens.home.HomeScreen
import com.ankit.familytools.ui.screens.image.ImageCollageScreen
import com.ankit.familytools.ui.screens.image.ImageCompressScreen
import com.ankit.familytools.ui.screens.image.ImageEditScreen
import com.ankit.familytools.ui.screens.pdf.DocumentScanScreen
import com.ankit.familytools.ui.screens.pdf.PdfActionScreen
import com.ankit.familytools.ui.screens.pdf.PdfAnnotateScreen
import com.ankit.familytools.ui.screens.pdf.PdfMergeScreen
import com.ankit.familytools.ui.screens.pdf.PdfSignScreen
import com.ankit.familytools.ui.screens.settings.SettingsScreen
import com.ankit.familytools.ui.screens.video.VideoActionScreen
import com.ankit.familytools.ui.screens.video.VideoCompressScreen
import com.ankit.familytools.ui.theme.FamilyToolsTheme

/**
 * AnkitG Family Tools - Main Entry Activity
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FamilyToolsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val favoritesRepo = remember { FavoritesRepository(applicationContext) }
                    val historyRepo = remember { HistoryRepository(applicationContext) }
                    val signatureRepo = remember { SignatureRepository(applicationContext) }

                    AppNavigation(
                        favoritesRepo = favoritesRepo,
                        historyRepo = historyRepo,
                        signatureRepo = signatureRepo
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    favoritesRepo: FavoritesRepository,
    historyRepo: HistoryRepository,
    signatureRepo: SignatureRepository
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                favoritesRepo = favoritesRepo,
                historyRepo = historyRepo,
                onNavigateToTool = { route ->
                    navController.navigate(route)
                },
                onNavigateToHistory = {
                    navController.navigate("history")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }

        // PDF Signing
        composable("pdf_sign") {
            PdfSignScreen(
                signatureRepo = signatureRepo,
                historyRepo = historyRepo,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Merge PDF
        composable("pdf_merge") {
            PdfMergeScreen(
                historyRepo = historyRepo,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Scan Document & OCR
        composable("doc_scan") {
            DocumentScanScreen(
                historyRepo = historyRepo,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // PDF Annotate Screen
        composable("pdf_annotate") {
            PdfAnnotateScreen(
                historyRepo = historyRepo,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // All Individual PDF Actions
        listOf(
            "images_to_pdf",
            "pdf_split",
            "pdf_reorder",
            "pdf_compress",
            "text_to_pdf",
            "pdf_extract_text",
            "pdf_watermark",
            "pdf_page_numbers",
            "pdf_protect",
            "pdf_unlock"
        ).forEach { actionRoute ->
            composable(actionRoute) {
                PdfActionScreen(
                    action = actionRoute,
                    historyRepo = historyRepo,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // Video Compression
        composable("video_compress") {
            VideoCompressScreen(
                historyRepo = historyRepo,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Video Actions (Trim, Mute, Extract Audio, Extract Frame)
        composable("video_trim") {
            VideoActionScreen(
                action = "trim",
                historyRepo = historyRepo,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "video_trim?action={action}",
            arguments = listOf(navArgument("action") {
                type = NavType.StringType
                defaultValue = "trim"
            })
        ) { backStackEntry ->
            val action = backStackEntry.arguments?.getString("action") ?: "trim"
            VideoActionScreen(
                action = action,
                historyRepo = historyRepo,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Image Editor (Crop, Rotate, Resize, Adjust, Sign, EXIF)
        composable(
            route = "image_edit?action={action}",
            arguments = listOf(navArgument("action") {
                type = NavType.StringType
                defaultValue = "crop"
            })
        ) { backStackEntry ->
            val action = backStackEntry.arguments?.getString("action") ?: "crop"
            ImageEditScreen(
                initialAction = action,
                historyRepo = historyRepo,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Image Compression & Convert
        composable("image_compress") {
            ImageCompressScreen(
                action = "compress",
                historyRepo = historyRepo,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "image_compress?action={action}",
            arguments = listOf(navArgument("action") {
                type = NavType.StringType
                defaultValue = "compress"
            })
        ) { backStackEntry ->
            val action = backStackEntry.arguments?.getString("action") ?: "compress"
            ImageCompressScreen(
                action = action,
                historyRepo = historyRepo,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Photo Collage
        composable("image_collage") {
            ImageCollageScreen(
                historyRepo = historyRepo,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Processing History
        composable("history") {
            HistoryScreen(
                historyRepo = historyRepo,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Settings & About
        composable("settings") {
            SettingsScreen(
                signatureRepo = signatureRepo,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
