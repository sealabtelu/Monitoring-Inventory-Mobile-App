package com.example.inventorymonitoring.navigation

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.inventorymonitoring.data.ServiceLocator
import com.example.inventorymonitoring.ui.screens.CreateItemScreen
import com.example.inventorymonitoring.ui.screens.EditItemScreen
import com.example.inventorymonitoring.ui.screens.EditProfileScreen
import com.example.inventorymonitoring.ui.screens.HomeScreen
import com.example.inventorymonitoring.ui.screens.ItemDetailsScreen
import com.example.inventorymonitoring.ui.screens.ItemsScreen
import com.example.inventorymonitoring.ui.screens.NotificationScreen
import com.example.inventorymonitoring.ui.screens.ProfileScreen
import com.example.inventorymonitoring.ui.screens.SignInScreen
import com.example.inventorymonitoring.ui.screens.SplashScreen
import kotlinx.coroutines.delay

sealed class Screen(val route: String) {
    object SignIn : Screen("signin")
    object Home : Screen("home")
    object Items : Screen("items")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object Notification: Screen("notification")
    object ItemDetails : Screen("item_details/{itemId}") {
        fun createRoute(itemId: String) = "item_details/$itemId"
    }
    object EditItem : Screen("editItem/{itemId}") {
        fun createRoute(itemId: String? = null) = if (itemId != null) "editItem/$itemId" else "editItem"
    }
    object CreateItem : Screen("createItem/{itemId}") {
        fun createRoute(itemId: String? = null) = if (itemId != null) "createItem/$itemId" else "createItem"
    }
}

@SuppressLint("NewApi")
@Composable
fun AppNavigation(context: Context) {
    val navController = rememberNavController()
    val authRepository = ServiceLocator.provideAuthRepository(context)
    val firestoreRepository = remember { ServiceLocator.firestoreRepository }
    var isLoading by remember { mutableStateOf(true) }
    var isAuthenticated by remember { mutableStateOf(false) }
    var emptyNamaBarangItemId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val currentUser = authRepository.getCurrentUser()
            isAuthenticated = currentUser != null

            if (isAuthenticated) {
                // Fetch items with empty nama_barang
                val items = firestoreRepository.getItemsWithEmptyNamaBarang()
                if (items.isNotEmpty()) {
                    emptyNamaBarangItemId = items.first().id
                }
            }
        } catch (e: Exception) {
            Log.e("AppNavigation", "Error during authentication check: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    // Periodic background task to check for items with empty nama_barang
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            while (true) {
                try {
                    val items = firestoreRepository.getItemsWithEmptyNamaBarang()
                    if (items.isNotEmpty()) {
                        emptyNamaBarangItemId = items.first().id
                        // Navigate to CreateItemScreen if not already on it
                        val currentRoute = navController.currentBackStackEntry?.destination?.route
                        if (currentRoute != Screen.CreateItem.createRoute(emptyNamaBarangItemId!!)) {
                            navController.navigate(Screen.CreateItem.createRoute(emptyNamaBarangItemId!!)) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        }
                    } else {
                        emptyNamaBarangItemId = null
                    }
                } catch (e: Exception) {
                    Log.e("AppNavigation", "Error fetching items with empty nama_barang: ${e.message}")
                }
                delay(30000) // Check every 30 seconds
            }
        }
    }

    if (isLoading) {
        SplashScreen()
    } else {
        Scaffold(
            bottomBar = {
                if (isAuthenticated) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    // Exclude BottomNavBar on SignInScreen and CreateItemScreen
                    if (currentRoute != Screen.SignIn.route && currentRoute != Screen.CreateItem.route && currentRoute != Screen.EditItem.route) {
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
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = when {
                    !isAuthenticated -> Screen.SignIn.route
                    emptyNamaBarangItemId != null -> Screen.CreateItem.createRoute(emptyNamaBarangItemId!!)
                    else -> Screen.Home.route
                },
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.SignIn.route) {
                    SignInScreen(
                        context = context,
                        onSignInSuccess = {
                            isAuthenticated = true
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
                            navController.navigate(Screen.CreateItem.route) // Navigate to CreateItemScreen
                        },
                        onNotificationClick = {
                            navController.navigate(Screen.Notification.route)
                        }
                    )
                }

                composable(Screen.Items.route) {
                    ItemsScreen(
                        onItemClick = { itemId ->
                            navController.navigate(Screen.ItemDetails.createRoute(itemId))
                        },
                        onNotificationClick = {
                            navController.navigate(Screen.Notification.route)
                        }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onEditButtonClick = {
                            navController.navigate(Screen.EditProfile.route)
                        },
                        onLogout = {
                            isAuthenticated = false
                            navController.navigate(Screen.SignIn.route) {
                                popUpTo(Screen.SignIn.route) { inclusive = true }
                            }
                        },
                        onNotificationClick = {
                            navController.navigate(Screen.Notification.route)
                        }
                    )
                }

                composable(Screen.EditProfile.route) {
                    EditProfileScreen(
                        context = context,
                        onUpdateCredentials = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Profile.route) { inclusive = true }
                            }
                        },
                        onClickBack = {
                            navController.navigate(Screen.Profile.route)
                        }
                    )
                }

                composable(Screen.Notification.route) {
                    NotificationScreen(
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(Screen.ItemDetails.route) { backStackEntry ->
                    val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
                    ItemDetailsScreen(
                        itemId = itemId,
                        navController = navController,
                        onBackClick = {
                            navController.navigateUp()
                        }
                    )
                }

                composable(Screen.EditItem.route) { backStackEntry ->
                    val itemId = backStackEntry.arguments?.getString("itemId")
                    if (itemId != null) {
                        EditItemScreen(
                            itemId = itemId,
                            navController = navController,
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onSaveSuccess = {
                                navController.popBackStack() // Navigate back after saving
                            }
                        )
                    } else {
                        // Handle invalid itemId
                        navController.navigateUp()
                    }
                }

                // Single composable for CreateItemScreen
                composable(Screen.CreateItem.route) { backStackEntry ->
                    val itemId = backStackEntry.arguments?.getString("itemId")
                    CreateItemScreen(
                        itemId = itemId,
                        navController = navController,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onSaveSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.CreateItem.route) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}