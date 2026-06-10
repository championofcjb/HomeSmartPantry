package com.example.homesmartpantry

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.homesmartpantry.presentation.component.BottomNavBar
import com.example.homesmartpantry.presentation.navigation.AppNavHost
import com.example.homesmartpantry.presentation.navigation.NavRoutes
import com.example.homesmartpantry.ui.theme.HomeSmartPantryTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val app = application as HomeSmartPantryApp
        val homeViewModel = app.createHomeViewModel()
        val addIngredientViewModel = app.createAddIngredientViewModel()
        val recipeViewModel = app.createRecipeViewModel()

        enableEdgeToEdge()
        setContent {
            HomeSmartPantryTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                var isSelectionMode by remember { mutableStateOf(false) }

                Scaffold(
                    bottomBar = {
                        if (!isSelectionMode) {
                            BottomNavBar(
                                navController = navController,
                                todayCookCount = recipeViewModel.todayCookCount
                            )
                        }
                    },
                    floatingActionButton = {
                        if (!isSelectionMode) {
                            when (currentRoute) {
                                NavRoutes.HOME -> FloatingActionButton(
                                    onClick = { navController.navigate(NavRoutes.ADD_INGREDIENT) },
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) { Icon(Icons.Default.Add, contentDescription = "添加食材") }

                                NavRoutes.RECIPES -> FloatingActionButton(
                                    onClick = { navController.navigate(NavRoutes.ADD_RECIPE) },
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) { Icon(Icons.Default.Add, contentDescription = "添加菜谱") }
                            }
                        }
                    }
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        homeViewModel = homeViewModel,
                        addIngredientViewModel = addIngredientViewModel,
                        recipeViewModel = recipeViewModel,
                        repository = app.repository,
                        modifier = Modifier.padding(innerPadding),
                        onSelectionModeChanged = { isSelectionMode = it }
                    )
                }
            }
        }
    }
}
