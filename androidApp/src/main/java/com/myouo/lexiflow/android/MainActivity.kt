package com.myouo.lexiflow.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.myouo.lexiflow.android.ui.AnalyticsScreen
import com.myouo.lexiflow.android.ui.ReviewScreen
import com.myouo.lexiflow.android.ui.SettingsScreen
import com.myouo.lexiflow.android.ui.VocabularyScreen
import com.myouo.lexiflow.android.ui.theme.LexiFlowTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.myouo.lexiflow.i18n.LocalizationManager
import com.myouo.lexiflow.i18n.LanguageMode

class AppViewModelFactory(private val context: android.content.Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Wrap logic in exceptions if standard SQLite factory isn't mocked during preview runs
        return try {
            val dbDriverFactory = com.myouo.lexiflow.database.DatabaseDriverFactory(context)
            val dbHelper = com.myouo.lexiflow.database.DatabaseHelper(dbDriverFactory)
            
            when {
                modelClass.isAssignableFrom(com.myouo.lexiflow.android.viewmodel.ReviewViewModel::class.java) -> {
                    com.myouo.lexiflow.android.viewmodel.ReviewViewModel(dbHelper, com.myouo.lexiflow.review.ReviewScheduler(dbHelper)) as T
                }
                modelClass.isAssignableFrom(com.myouo.lexiflow.android.viewmodel.VocabularyViewModel::class.java) -> {
                    com.myouo.lexiflow.android.viewmodel.VocabularyViewModel(com.myouo.lexiflow.domain.VocabularyImporter(dbHelper)) as T
                }
                modelClass.isAssignableFrom(com.myouo.lexiflow.android.viewmodel.AnalyticsViewModel::class.java) -> {
                    com.myouo.lexiflow.android.viewmodel.AnalyticsViewModel(com.myouo.lexiflow.analytics.HeatmapCalculator(dbHelper), dbHelper) as T
                }
                modelClass.isAssignableFrom(com.myouo.lexiflow.android.viewmodel.SettingsViewModel::class.java) -> {
                    com.myouo.lexiflow.android.viewmodel.SettingsViewModel() as T
                }
                else -> throw IllegalArgumentException("Unknown ViewModel class")
            }
        } catch (e: Exception) {
            // Provide mock instances if the DB driver is uninitialized in this test environment
            throw RuntimeException("Ensure database is configured on Android: " + e.message)
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            LexiFlowTheme { // Settings theme observation would usually wrap around here
                LexiFlowApp()
            }
        }
    }
}

@Composable
fun LexiFlowApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val factory = remember { AppViewModelFactory(context) }
    
    // Manage dynamic language manually overriding the bottom bar hardcodes
    // since we cannot modify shared module
    val locManager = remember { LocalizationManager() }

    Scaffold(
        bottomBar = {
            LexiFlowBottomNavigation(navController = navController, locManager = locManager)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "review",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("review") { ReviewScreen(navController, viewModel = viewModel(factory = factory)) }
            composable("vocabulary") { VocabularyScreen(viewModel = viewModel(factory = factory)) }
            composable("analytics") { AnalyticsScreen(viewModel = viewModel(factory = factory)) }
            composable("settings") { SettingsScreen(viewModel = viewModel(factory = factory)) }
        }
    }
}

@Composable
fun LexiFlowBottomNavigation(navController: NavHostController, locManager: LocalizationManager) {
    val language by locManager.currentLanguage.collectAsState()
    val isZh = language == LanguageMode.ZH_HANS

    val items = listOf(
        Triple("review", if (isZh) "复习" else "Review", Icons.Filled.List),
        Triple("vocabulary", if (isZh) "词库" else "Vocabulary", Icons.Filled.Build),
        Triple("analytics", if (isZh) "统计" else "Analytics", Icons.Filled.DateRange),
        Triple("settings", if (isZh) "设置" else "Settings", Icons.Filled.Settings)
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { (route, label, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                selected = currentRoute == route,
                onClick = {
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
