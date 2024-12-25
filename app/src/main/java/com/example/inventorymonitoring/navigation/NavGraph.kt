package com.example.inventorymonitoring.navigation

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.inventorymonitoring.data.ServiceLocator
import com.example.inventorymonitoring.ui.screens.HomeScreen
import com.example.inventorymonitoring.ui.screens.ItemDetailsScreen
import com.example.inventorymonitoring.ui.screens.ItemsScreen
import com.example.inventorymonitoring.ui.screens.ProfileScreen
import com.example.inventorymonitoring.ui.screens.SignInScreen

sealed class Screen(val route: String) {
    object SignIn : Screen("signin")
    object Home : Screen("home")
    object Items : Screen("items")
    object ItemDetails : Screen("item_details/{itemId}") {
        fun createRoute(itemId: String) = "item_details/$itemId"
    }
    object Profile : Screen("profile")
}

@Composable
fun AppNavigation(context: Context) {
    val navController = rememberNavController()
    val authRepository = ServiceLocator.provideAuthRepository(context)

    LaunchedEffect(Unit) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.SignIn.route) { inclusive = true }
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != Screen.SignIn.route) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.SignIn.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.SignIn.route) {
                SignInScreen(
                    context = context,
                    onSignInSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.SignIn.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onItemClick = { itemId ->
                        navController.navigate(Screen.ItemDetails.createRoute(itemId))
                    },
                    onAddNewItem = {
                        navController.navigate(Screen.Items.route)
                    }
                )
            }

            composable(Screen.Items.route) {
                ItemsScreen(
                    onItemClick = { itemId ->
                        navController.navigate(Screen.ItemDetails.createRoute(itemId))
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen()
            }

            composable(
                route = Screen.ItemDetails.route,
                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
                ItemDetailsScreen(
                    itemId = itemId,
                    onBackClick = {
                        navController.navigateUp()
                    }
                )
            }
        }
    }
}