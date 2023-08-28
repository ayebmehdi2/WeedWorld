package com.itshedi.weedworld.ui.navigation

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.itshedi.weedworld.R
import com.itshedi.weedworld.ui.theme.bottomNavSelectionColor
import com.itshedi.weedworld.ui.theme.unselectedNavSelectionColor

@Composable
fun MyBottomNavigation(modifier: Modifier, navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination

    val screens = listOf(
        BottomNavScreens.Feed,
        BottomNavScreens.Clinic,
        BottomNavScreens.Nearby,
        BottomNavScreens.Chat,
    )

    BottomNavigation(
        modifier = modifier, contentColor = Color.Black, backgroundColor = Color.White
    ) {
        screens.forEachIndexed { index, screen ->
            BottomNavigationItem(selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                selectedContentColor = bottomNavSelectionColor,
                unselectedContentColor = unselectedNavSelectionColor,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(currentDestination?.route!!) {
                            inclusive = true
                        }
                    }
                },
                icon = { Icon(painter = painterResource(id = screen.icon), contentDescription = null) })

            if (index + 1 == screens.size / 2) { // camera icon in the middle
                BottomNavigationItem(selected = false,
                    selectedContentColor = bottomNavSelectionColor,
                    unselectedContentColor = unselectedNavSelectionColor,
                    onClick = {
                        //todo: open camera
                    },
                    icon = { Icon(painter = painterResource(id = R.drawable.bottom_nav_camera), contentDescription = null) })
            }
        }
    }
}