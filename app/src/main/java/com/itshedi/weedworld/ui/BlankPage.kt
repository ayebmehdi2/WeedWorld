package com.itshedi.weedworld.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.itshedi.weedworld.repository.user_repository.UserRepository
import com.itshedi.weedworld.ui.navigation.DrawerScreens
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@Composable
fun BlankPage(navController:NavController, viewModel: BlankViewModel) {
    //Todo: This is used as blank page of layouts that are not made yet

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination

    currentDestination?.let{
        BackHandler(it.route!! != DrawerScreens.feed.route) {
            navController.navigate(DrawerScreens.feed.route) {
                popUpTo(currentDestination.route!!) {
                    inclusive = true
                }
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text ="TODO: ${navController.currentDestination?.route}")
    }
}

@HiltViewModel
class BlankViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {


    fun logout() {
        userRepository.logout()
    }
}