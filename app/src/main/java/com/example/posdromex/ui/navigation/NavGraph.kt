package com.example.posdromex.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.posdromex.ui.screens.categories.CategoriesScreen
import com.example.posdromex.ui.screens.clients.ClientListScreen
import com.example.posdromex.ui.screens.clients.ClientProfileScreen
import com.example.posdromex.ui.screens.conversions.ConversionsScreen
import com.example.posdromex.ui.screens.drivers.DriversScreen
import com.example.posdromex.ui.screens.items.ItemsScreen
import com.example.posdromex.ui.screens.main.MainMenuScreen
import com.example.posdromex.ui.screens.quickprint.QuickTextPrintScreen
import com.example.posdromex.ui.screens.sales.NewSaleScreen
import com.example.posdromex.ui.screens.settings.SettingsScreen
import com.example.posdromex.ui.screens.tax.TaxSettingsScreen
import com.example.posdromex.ui.screens.trucks.TrucksScreen

sealed class Screen(val route: String) {
    object MainMenu : Screen("main_menu")
    object ClientList : Screen("client_list")
    object NewSale : Screen("new_sale")
    object QuickTextPrint : Screen("quick_text_print")
    object Settings : Screen("settings")
    object Drivers : Screen("drivers")
    object Trucks : Screen("trucks")
    object Categories : Screen("categories")
    object Items : Screen("items")
    object TaxSettings : Screen("tax_settings")
    object Conversions : Screen("conversions")
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
                },
                onNavigateToDrivers = {
                    navController.navigate(Screen.Drivers.route)
                },
                onNavigateToTrucks = {
                    navController.navigate(Screen.Trucks.route)
                },
                onNavigateToCategories = {
                    navController.navigate(Screen.Categories.route)
                },
                onNavigateToItems = {
                    navController.navigate(Screen.Items.route)
                },
                onNavigateToTaxSettings = {
                    navController.navigate(Screen.TaxSettings.route)
                },
                onNavigateToConversions = {
                    navController.navigate(Screen.Conversions.route)
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

        composable(Screen.Drivers.route) {
            DriversScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Trucks.route) {
            TrucksScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Categories.route) {
            CategoriesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Items.route) {
            ItemsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TaxSettings.route) {
            TaxSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Conversions.route) {
            ConversionsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

