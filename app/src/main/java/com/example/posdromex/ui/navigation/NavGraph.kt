package com.example.posdromex.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.posdromex.ui.screens.clients.ClientListScreen
import com.example.posdromex.ui.screens.clients.ClientProfileScreen
import com.example.posdromex.ui.screens.main.MainMenuScreen
import com.example.posdromex.ui.screens.quickprint.QuickTextPrintScreen
import com.example.posdromex.ui.screens.sales.NewSaleScreen
import com.example.posdromex.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    object MainMenu : Screen("main_menu")
    object ClientList : Screen("client_list")
    object NewSale : Screen("new_sale")
    object QuickTextPrint : Screen("quick_text_print")
    object Settings : Screen("settings")
    data class ClientProfile(val customerId: Long = -1) : Screen("client_profile/{customerId}") {
        fun createRoute(customerId: Long) = "client_profile/$customerId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.MainMenu.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.MainMenu.route) {
            MainMenuScreen(
                onNavigateToClients = {
                    navController.navigate(Screen.ClientList.route)
                },
                onNavigateToNewSale = {
                    navController.navigate(Screen.NewSale.route)
                },
                onNavigateToQuickTextPrint = {
                    navController.navigate(Screen.QuickTextPrint.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.ClientList.route) {
            ClientListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToClientProfile = { customerId ->
                    navController.navigate(Screen.ClientProfile().createRoute(customerId))
                }
            )
        }

        composable(
            route = "client_profile/{customerId}",
            arguments = listOf(
                navArgument("customerId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getLong("customerId") ?: -1L
            ClientProfileScreen(
                customerId = customerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.NewSale.route) {
            NewSaleScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.QuickTextPrint.route) {
            QuickTextPrintScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

