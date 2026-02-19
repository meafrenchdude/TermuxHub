package com.maazm7d.termuxhub.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun TermuxHubAppNav(
    deepLinkToolId: String?
) {
    val navController = rememberNavController()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    var deepLinkHandled by remember { mutableStateOf(false) }

    LaunchedEffect(deepLinkToolId) {
        if (deepLinkToolId.isNullOrBlank()) return@LaunchedEffect
        if (deepLinkHandled) return@LaunchedEffect

        deepLinkHandled = true
        navController.navigate("${Destinations.DETAILS}/$deepLinkToolId") {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    val showBottomBar = when (currentDestination?.route) {
        Destinations.SPLASH -> false
        "${Destinations.DETAILS}/{toolId}" -> false
        else -> true
    }

    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AppNavHost(
                navController = navController,
                modifier = Modifier.fillMaxSize()
            )

            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { it / 2 } + fadeIn(),
                exit = slideOutVertically { it / 2 } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                BottomPillNavBar(
                    currentDestination = currentDestination,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Destinations.TOOLS) {
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
}

@Composable
private fun BottomPillNavBar(
    currentDestination: NavDestination?,
    onNavigate: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp,
        modifier = Modifier.wrapContentWidth() 
    ) {
        NavigationBar(
            modifier = Modifier
                .height(48.dp)
                .wrapContentWidth()               
                .padding(horizontal = 8.dp),     
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentDestination
                    ?.hierarchy
                    ?.any { it.route == item.route } == true

                val scale by animateFloatAsState(
                    targetValue = if (selected) 1.08f else 1f,
                    label = "scale"
                )

                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(item.route) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(if (item.isHome) 22.dp else 20.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                        )
                    },
                    label = null,
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.7f
                        )
                    )
                )
            }
        }
    }
}
