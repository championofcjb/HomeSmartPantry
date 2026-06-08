package com.example.homesmartpantry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.homesmartpantry.presentation.component.BottomNavBar
import com.example.homesmartpantry.presentation.navigation.AppNavHost
import com.example.homesmartpantry.presentation.navigation.NavRoutes
import com.example.homesmartpantry.ui.theme.HomeSmartPantryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as HomeSmartPantryApp
        val homeViewModel = app.createHomeViewModel()

        enableEdgeToEdge()
        setContent {
            HomeSmartPantryTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    bottomBar = {
                        BottomNavBar(navController = navController)
                    },
                    floatingActionButton = {
                        if (currentRoute == NavRoutes.HOME) {
                            FloatingActionButton(
                                onClick = {
                                    navController.navigate(NavRoutes.ADD_INGREDIENT)
                                },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "添加食材"
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        homeViewModel = homeViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
