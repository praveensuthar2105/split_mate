package com.splitmate.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.splitmate.android.ui.auth.LoginScreen
import com.splitmate.android.ui.groups.GroupDetailScreen
import com.splitmate.android.ui.groups.HomeScreen
import com.splitmate.android.ui.theme.SplitMateTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplitMateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Setup Jetpack Navigation with custom animations
                    NavHost(
                        navController = navController, 
                        startDestination = "login",
                        enterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(400)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(400)
                            )
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(400)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(400)
                            )
                        }
                    ) {
                        composable("login") {
                            LoginScreen(
                                onNavigateToMain = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("home") {
                            HomeScreen(
                                onGroupClick = { groupId ->
                                    navController.navigate("group_detail/$groupId")
                                },
                                onCreateGroupClick = {
                                    navController.navigate("create_group")
                                },
                                onProfileClick = {
                                    navController.navigate("profile")
                                }
                            )
                        }
                        composable(
                            route = "group_detail/{groupId}",
                            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
                            GroupDetailScreen(
                                groupId = groupId,
                                onBackClick = { navController.popBackStack() },
                                onAddExpenseClick = { amount, description, splitType ->
                                    if (splitType == "Itemized") {
                                        navController.navigate("itemized_split/$groupId/$amount/$description")
                                    } else if (splitType == "Percentage") {
                                        navController.navigate("percentage_split/$groupId/$amount/$description")
                                    } else if (splitType == "Shares") {
                                        navController.navigate("shares_split/$groupId/$amount/$description")
                                    }
                                }
                            )
                        }
                        composable("profile") {
                            com.splitmate.android.ui.settle.ProfileScoreScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable("create_group") {
                            com.splitmate.android.ui.groups.CreateGroupScreen(
                                onBackClick = { navController.popBackStack() },
                                onGroupCreated = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(
                            route = "itemized_split/{groupId}/{amount}/{description}",
                            arguments = listOf(
                                navArgument("groupId") { type = NavType.StringType },
                                navArgument("amount") { type = NavType.FloatType },
                                navArgument("description") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val gid = backStackEntry.arguments?.getString("groupId") ?: ""
                            val amt = backStackEntry.arguments?.getFloat("amount")?.toDouble() ?: 0.0
                            val desc = backStackEntry.arguments?.getString("description") ?: ""

                            val viewModel: com.splitmate.android.ui.groups.GroupDetailViewModel = hiltViewModel(
                                navController.getBackStackEntry("group_detail/$gid")
                            )
                            val membersState = viewModel.members.collectAsStateWithLifecycle()

                            com.splitmate.android.ui.expense.ItemizedSplitScreen(
                                lineItems = listOf(com.splitmate.android.domain.model.LineItem(desc, amt)),
                                members = membersState.value,
                                onConfirm = { customSplits ->
                                    viewModel.addExpense(amt, desc, "ITEMIZED", customSplits)
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(
                            route = "percentage_split/{groupId}/{amount}/{description}",
                            arguments = listOf(
                                navArgument("groupId") { type = NavType.StringType },
                                navArgument("amount") { type = NavType.FloatType },
                                navArgument("description") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val gid = backStackEntry.arguments?.getString("groupId") ?: ""
                            val amt = backStackEntry.arguments?.getFloat("amount")?.toDouble() ?: 0.0
                            val desc = backStackEntry.arguments?.getString("description") ?: ""

                            val viewModel: com.splitmate.android.ui.groups.GroupDetailViewModel = hiltViewModel(
                                navController.getBackStackEntry("group_detail/$gid")
                            )
                            val membersState = viewModel.members.collectAsStateWithLifecycle()

                            com.splitmate.android.ui.expense.PercentageSplitScreen(
                                totalAmount = amt,
                                members = membersState.value,
                                onConfirm = { customSplits ->
                                    viewModel.addExpense(amt, desc, "PERCENTAGE", customSplits)
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(
                            route = "shares_split/{groupId}/{amount}/{description}",
                            arguments = listOf(
                                navArgument("groupId") { type = NavType.StringType },
                                navArgument("amount") { type = NavType.FloatType },
                                navArgument("description") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val gid = backStackEntry.arguments?.getString("groupId") ?: ""
                            val amt = backStackEntry.arguments?.getFloat("amount")?.toDouble() ?: 0.0
                            val desc = backStackEntry.arguments?.getString("description") ?: ""

                            val viewModel: com.splitmate.android.ui.groups.GroupDetailViewModel = hiltViewModel(
                                navController.getBackStackEntry("group_detail/$gid")
                            )
                            val membersState = viewModel.members.collectAsStateWithLifecycle()

                            com.splitmate.android.ui.expense.SharesSplitScreen(
                                totalAmount = amt,
                                members = membersState.value,
                                onConfirm = { customSplits ->
                                    viewModel.addExpense(amt, desc, "SHARES", customSplits)
                                    navController.popBackStack()
                                }
                            )
                        }
                    }

                }
            }
        }
    }
}
