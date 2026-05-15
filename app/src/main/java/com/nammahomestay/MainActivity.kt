package com.nammahomestay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nammahomestay.firebase.FirebaseManager
import com.nammahomestay.ui.auth.AuthScreen
import com.nammahomestay.ui.inquiry.InquiryViewModel
import com.nammahomestay.ui.navigation.BottomNavItem
import com.nammahomestay.ui.screens.*
import com.nammahomestay.ui.theme.NammaHomeStayTheme
import com.nammahomestay.utils.DataStoreManager
import com.nammahomestay.utils.NetworkStateMonitor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        val dataStoreManager = DataStoreManager(this)

        setContent {
            NammaHomeStayTheme {
                val onboardingCompleted by dataStoreManager.onboardingCompleted.collectAsStateWithLifecycle(initialValue = false)
                val startDest = if (FirebaseManager.auth.currentUser != null) {
                    if (onboardingCompleted) "main" else "onboarding"
                } else "auth"

                NammaHomeStayApp(startDest, dataStoreManager)
            }
        }
    }
}

@Composable
fun NammaHomeStayApp(startDestination: String, dataStoreManager: DataStoreManager) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("auth") { AuthScreen(navController) }
        composable("onboarding") { OnboardingScreen(navController, dataStoreManager) }
        composable("main") { MainScreen() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val bottomNavController = rememberNavController()
    val inquiryViewModel: InquiryViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity)
    val unreadCount by inquiryViewModel.unreadCount.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    val networkMonitor = remember { NetworkStateMonitor(context) }
    val isOnline by networkMonitor.isOnline.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = { BottomNavigationBar(bottomNavController, unreadCount) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = bottomNavController,
                startDestination = BottomNavItem.Home.route
            ) {
                composable(BottomNavItem.Home.route) { HomeScreen(bottomNavController) }
                composable(BottomNavItem.Menu.route) { MenuScreen() }
                composable(BottomNavItem.Inquiries.route) { InquiryScreen(bottomNavController, inquiryViewModel) }
                composable("inquiryDetail") { InquiryDetailScreen(bottomNavController, inquiryViewModel) }
                composable(BottomNavItem.LocalGuide.route) { LocalGuideScreen() }
                composable(BottomNavItem.Profile.route) { ProfileScreen() }
            }
            
            if (!isOnline) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Red)
                        .padding(8.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Text(
                        text = "You are currently offline. Changes will sync when reconnected.",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(navController: NavHostController, unreadCount: Int) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Menu,
        BottomNavItem.Inquiries,
        BottomNavItem.LocalGuide,
        BottomNavItem.Profile
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { 
                    if (item == BottomNavItem.Inquiries && unreadCount > 0) {
                        BadgedBox(badge = { Badge { Text(unreadCount.toString()) } }) {
                            Icon(imageVector = item.icon, contentDescription = item.title)
                        }
                    } else {
                        Icon(imageVector = item.icon, contentDescription = item.title)
                    }
                },
                label = { Text(text = item.title) },
                selected = currentRoute == item.route || (item == BottomNavItem.Inquiries && currentRoute == "inquiryDetail"),
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
