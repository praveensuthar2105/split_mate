package com.splitmate.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.splitmate.android.ui.auth.LoginScreen
import com.splitmate.android.ui.expense.AddExpenseSheet
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
                    
                    var showAddExpenseSheet by remember { mutableStateOf(false) }

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
                                onAddExpenseClick = {
                                    showAddExpenseSheet = true
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
                        composable("itemized_split") {
                            com.splitmate.android.ui.expense.ItemizedSplitScreen(
                                onBackClick = { navController.popBackStack() },
                                onSaveClick = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }

                    if (showAddExpenseSheet) {
                        AddExpenseSheet(
                            onDismiss = { showAddExpenseSheet = false },
                            onSaveClick = { amount, description, splitType ->
                                showAddExpenseSheet = false
                                if (splitType == "Itemized") {
                                    navController.navigate("itemized_split")
                                }
                            },
                            onOcrClick = { },
                            onVoiceClick = { }
                        )
                    }
                }
            }
        }
    }
}
