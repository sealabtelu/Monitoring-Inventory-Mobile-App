package com.example.inventorymonitoring.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.inventorymonitoring.data.ServiceLocator
import com.example.inventorymonitoring.ui.screens.HomeScreen
import com.example.inventorymonitoring.ui.screens.ItemDetailsScreen
import com.example.inventorymonitoring.ui.screens.ItemsScreen
import com.example.inventorymonitoring.ui.screens.SignInScreen

sealed class Screen(val route: String) {
    object SignIn : Screen("signin")
    object Home : Screen("home")
    object Items : Screen("items")
    object ItemDetails : Screen("item_details/{itemId}") {
        fun createRoute(itemId: String) = "item_details/$itemId"
    }
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

    NavHost(
        navController = navController,
        startDestination = Screen.SignIn.route
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
