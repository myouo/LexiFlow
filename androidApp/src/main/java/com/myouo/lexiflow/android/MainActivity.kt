package com.myouo.lexiflow.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.vector.ImageVector
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
    private val dbDriverFactory by lazy { com.myouo.lexiflow.database.DatabaseDriverFactory(context.applicationContext) }
    private val dbHelper by lazy { 
        android.util.Log.d("PerfLog", "DB initialized")
        com.myouo.lexiflow.database.DatabaseHelper(dbDriverFactory) 
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Wrap logic in exceptions if standard SQLite factory isn't mocked during preview runs
        return try {
            
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
    val recomposeCount = remember { arrayOf(0) }
    androidx.compose.runtime.SideEffect {
        recomposeCount[0]++
        android.util.Log.d("PerfLog", "LexiFlowApp recomposed: ${recomposeCount[0]}")
    }
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
            modifier = Modifier.padding(innerPadding),
            enterTransition = { androidx.compose.animation.EnterTransition.None },
            exitTransition = { androidx.compose.animation.ExitTransition.None },
            popEnterTransition = { androidx.compose.animation.EnterTransition.None },
            popExitTransition = { androidx.compose.animation.ExitTransition.None }
        ) {
            composable("review") { 
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ReviewScreen(navController, viewModel = viewModel(factory = factory)) 
                }
            }
            composable("vocabulary") { 
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    VocabularyScreen(viewModel = viewModel(factory = factory)) 
                }
            }
            composable("analytics") { 
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AnalyticsScreen(viewModel = viewModel(factory = factory)) 
                }
            }
            composable("settings") { 
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    SettingsScreen(viewModel = viewModel(factory = factory)) 
                }
            }
        }
    }
}

@Composable
fun LexiFlowBottomNavigation(navController: NavHostController, locManager: LocalizationManager) {
    val recomposeCount = remember { arrayOf(0) }
    androidx.compose.runtime.SideEffect {
        recomposeCount[0]++
        android.util.Log.d("PerfLog", "LexiFlowBottomNavigation recomposed: ${recomposeCount[0]}")
    }
    val language by locManager.currentLanguage.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = when (language) {
        LanguageMode.EN -> listOf(
            BottomNavItem("review", "Review", Icons.Filled.List),
            BottomNavItem("vocabulary", "Words", Icons.Filled.Build),
            BottomNavItem("analytics", "Stats", Icons.Filled.DateRange),
            BottomNavItem("settings", "Settings", Icons.Filled.Settings)
        )
        else -> listOf(
            BottomNavItem("review", "复习", Icons.Filled.List),
            BottomNavItem("vocabulary", "词汇", Icons.Filled.Build),
            BottomNavItem("analytics", "统计", Icons.Filled.DateRange),
            BottomNavItem("settings", "设置", Icons.Filled.Settings)
        )
    }

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)
