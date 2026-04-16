package com.app.tabl.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.tabl.ui.add.AddMedicationScreen
import com.app.tabl.ui.history.HistoryScreen
import com.app.tabl.ui.home.HomeScreen
import com.app.tabl.ui.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val ADD_MEDICATION = "add_medication"
    const val EDIT_MEDICATION = "edit_medication/{medicationId}"
    const val HISTORY = "history"
    const val SETTINGS = "settings"

    fun editMedication(id: Long) = "edit_medication/$id"
}

@Composable
fun TablNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                onAddMedication = { navController.navigate(Routes.ADD_MEDICATION) },
                onEditMedication = { id -> navController.navigate(Routes.editMedication(id)) },
                onOpenHistory = { navController.navigate(Routes.HISTORY) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.ADD_MEDICATION) {
            AddMedicationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.EDIT_MEDICATION,
            arguments = listOf(navArgument("medicationId") { type = NavType.LongType })
        ) { backStack ->
            AddMedicationScreen(
                medicationId = backStack.arguments?.getLong("medicationId"),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
