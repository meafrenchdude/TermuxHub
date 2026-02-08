package com.maazm7d.termuxhub.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
    val currentDestination: NavDestination? = backStackEntry?.destination

    var deepLinkHandled by remember { mutableStateOf(false) }
    var isBottomBarVisible by remember { mutableStateOf(true) }

    // Detect scroll direction
    val scrollBehavior = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: androidx.compose.ui.geometry.Offset,
                source: NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                when {
                    available.y < -5f -> isBottomBarVisible = false // scroll down
                    available.y > 5f -> isBottomBarVisible = true  // scroll up
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }

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

    Scaffold(
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(scrollBehavior)
        ) {
            AppNavHost(
    navController = navController,
    modifier = Modifier.fillMaxSize()
)

            AnimatedVisibility(
                visible = showBottomBar && isBottomBarVisible,
                enter = slideInVertically { it / 2 } + fadeIn(),
                exit = slideOutVertically { it / 2 } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 20.dp)
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
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 10.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentDestination
                    ?.hierarchy
                    ?.any { it.route == item.route } == true

                val scale by animateFloatAsState(
                    targetValue = if (selected) 1.15f else 1f,
                    label = "scale"
                )

                val alpha by animateFloatAsState(
                    targetValue = if (selected) 1f else 0.6f,
                    label = "alpha"
                )

                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(item.route) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(if (item.isHome) 26.dp else 22.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    this.alpha = alpha
                                }
                        )
                    },
                    label = null,
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
