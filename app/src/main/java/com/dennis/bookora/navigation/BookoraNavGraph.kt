package com.dennis.bookora.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dennis.bookora.ui.screens.ForgotPasswordScreen
import com.dennis.bookora.ui.screens.LoginScreen
import com.dennis.bookora.ui.screens.MainScreen
import com.dennis.bookora.ui.screens.PrivacyPolicyScreen
import com.dennis.bookora.ui.screens.RegisterScreen
import com.dennis.bookora.ui.screens.SplashScreen
import com.dennis.bookora.ui.screens.TermsScreen
import com.dennis.bookora.ui.screens.WelcomeScreen

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BookoraNavGraph(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BookoraDestinations.Splash,
        modifier = modifier
    ) {
        composable(BookoraDestinations.Splash) {
            SplashScreen(onContinue = {
                navController.navigate(BookoraDestinations.Welcome) {
                    popUpTo(BookoraDestinations.Splash) { inclusive = true }
                }
            })
        }

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
            MainScreen(onLogout = {
                navController.navigate(BookoraDestinations.Welcome) {
                    popUpTo(BookoraDestinations.Main) { inclusive = true }
                }
            })
        }
    }
}
