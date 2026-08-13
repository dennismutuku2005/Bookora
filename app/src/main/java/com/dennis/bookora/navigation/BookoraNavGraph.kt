package com.dennis.bookora.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.dennis.bookora.repository.auth.FirebaseAuthManager
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dennis.bookora.ui.screens.*

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BookoraNavGraph(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    FirebaseAuthManager.ensureInitialized(context)
    val startDestination = if (FirebaseAuthManager.currentUser() != null) BookoraDestinations.Main else BookoraDestinations.Welcome

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(BookoraDestinations.Welcome) {
            WelcomeScreen(
                onLogin = { navController.navigate(BookoraDestinations.Login) },
                onRegister = { navController.navigate(BookoraDestinations.Register) },
                onViewPolicy = { navController.navigate(BookoraDestinations.Privacy) },
                onViewTerms = { navController.navigate(BookoraDestinations.Terms) }
            )
        }

        composable(BookoraDestinations.Login) {
            LoginScreen(
                onForgotPassword = { navController.navigate(BookoraDestinations.ForgotPassword) },
                onLoginSuccess = {
                    navController.navigate(BookoraDestinations.Main) {
                        popUpTo(BookoraDestinations.Welcome) { inclusive = true }
                    }
                },
                onRegister = { navController.navigate(BookoraDestinations.Register) }
            )
        }

        composable(BookoraDestinations.Register) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(BookoraDestinations.Main) {
                        popUpTo(BookoraDestinations.Welcome) { inclusive = true }
                    }
                },
                onLogin = { navController.navigate(BookoraDestinations.Login) }
            )
        }

        composable(BookoraDestinations.ForgotPassword) {
            ForgotPasswordScreen(onResetSent = { navController.popBackStack() })
        }

        composable(BookoraDestinations.Privacy) {
            PrivacyPolicyScreen(onBack = { navController.popBackStack() })
        }

        composable(BookoraDestinations.Terms) {
            TermsScreen(onBack = { navController.popBackStack() })
        }

        composable(BookoraDestinations.Main) {
            MainScreen(
                onLogout = {
                    navController.navigate(BookoraDestinations.Welcome) {
                        popUpTo(BookoraDestinations.Main) { inclusive = true }
                    }
                },
                onBookClick = { bookId ->
                    navController.navigate(BookoraDestinations.bookDetails(bookId))
                },
                onPrivacyClick = {
                    navController.navigate(BookoraDestinations.Privacy)
                },
                onTermsClick = {
                    navController.navigate(BookoraDestinations.Terms)
                },
                onNotificationClick = { notificationId ->
                    navController.navigate(BookoraDestinations.notificationDetails(notificationId))
                },
                onMyListingsClick = {
                    navController.navigate(BookoraDestinations.MyListings)
                },
                onChatClick = { conversationId ->
                    navController.navigate(BookoraDestinations.chat(conversationId))
                }
            )
        }

        composable(BookoraDestinations.MyListings) {
            MyListingsScreen(
                onBack = { navController.popBackStack() },
                onEditClick = { bookId -> navController.navigate(BookoraDestinations.editListing(bookId)) },
                onBookClick = { bookId -> navController.navigate(BookoraDestinations.bookDetails(bookId)) }
            )
        }

        composable(
            route = BookoraDestinations.EditListingPattern,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            CreateListingScreen(
                bookId = bookId,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(
            route = BookoraDestinations.BookDetailsPattern,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            BookDetailsScreen(
                bookId = bookId,
                onBack = { navController.popBackStack() },
                onOpenChat = { conversationId ->
                    navController.navigate(BookoraDestinations.chat(conversationId))
                }
            )
        }

        composable(
            route = BookoraDestinations.NotificationDetailsPattern,
            arguments = listOf(navArgument("notificationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val notificationId = backStackEntry.arguments?.getString("notificationId") ?: ""
            NotificationDetailScreen(
                notificationId = notificationId,
                onBack = { navController.popBackStack() },
                onOpenChat = { conversationId ->
                    navController.navigate(BookoraDestinations.chat(conversationId))
                }
            )
        }

        composable(
            route = BookoraDestinations.ChatPattern,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            ChatScreen(
                conversationId = conversationId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
