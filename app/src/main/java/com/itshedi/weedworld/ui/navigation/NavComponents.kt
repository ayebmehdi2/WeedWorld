package com.itshedi.weedworld.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.itshedi.weedworld.ui.BlankPage
import com.itshedi.weedworld.ui.BlankViewModel
import com.itshedi.weedworld.ui.auth.AuthScreen
import com.itshedi.weedworld.ui.auth.AuthViewModel
import com.itshedi.weedworld.ui.cart.CartScreen
import com.itshedi.weedworld.ui.cart.CartViewModel
import com.itshedi.weedworld.ui.chat.ChatScreen
import com.itshedi.weedworld.ui.chat.ChatScreenViewModel
import com.itshedi.weedworld.ui.feed.FeedScreen
import com.itshedi.weedworld.ui.feed.FeedViewModel
import com.itshedi.weedworld.ui.home.HomeScreen
import com.itshedi.weedworld.ui.home.HomeViewModel
import com.itshedi.weedworld.ui.landing_slides.LandingSlides
import com.itshedi.weedworld.ui.nearby_stores.NearbyStoresScreen
import com.itshedi.weedworld.ui.nearby_stores.NearbyStoresViewModel
import com.itshedi.weedworld.ui.nearby_users.NearbyUsersScreen
import com.itshedi.weedworld.ui.nearby_users.NearbyUsersViewModel
import com.itshedi.weedworld.ui.profile.ProfileScreen
import com.itshedi.weedworld.ui.profile.ProfileViewModel
import com.itshedi.weedworld.ui.profile.edit_profile.EditProfile
import com.itshedi.weedworld.ui.profile.edit_profile.EditProfileViewModel

//note: Outer navigation (Landing, Auth, InnerNavigation)
@Composable
fun Navigation(navController: NavHostController) {
    NavHost(
        navController = navController, startDestination = when (Firebase.auth.currentUser) {
            null -> Screens.LandingScreen.route
            else -> Screens.MainScreen.route
        }
    ) {
        composable(route = Screens.LandingScreen.route) {
            LandingSlides(navController)
        }
        composable(route = Screens.MainScreen.route) {
            HomeScreen(viewModel = hiltViewModel<HomeViewModel>(), navController = navController)
        }
        composable(route = Screens.ProfileScreen.route) {
            ProfileScreen(
                viewModel = hiltViewModel<ProfileViewModel>(),
                navController = navController
            )
        }
        composable(route = Screens.EditProfile.route) {
            EditProfile(
                viewModel = hiltViewModel<EditProfileViewModel>(),
                navController = navController
            )
        }
        composable(route = Screens.AuthScreen.route) {
            AuthScreen(
                navController, viewModel = hiltViewModel<AuthViewModel>()
            )
        }
    }
}

//note: navigation for homescreen and its inner childrens (Drawer items / bottom nav items?)
@Composable
fun InnerNavigation(innerNavController: NavHostController, onOpenDrawer: () -> Unit) {
    NavHost(
        navController = innerNavController, startDestination = DrawerScreens.feed.route
    ) {
        // blank screens for bottom navigation
        composable(route = DrawerScreens.feed.route) {
            FeedScreen(
                viewModel = hiltViewModel<FeedViewModel>(),
                navController = innerNavController
            )
        }
        /*
        Todo here the OrdersScreen in compose

      composable(route = DrawerScreens.orders.route) {
           OrdersScreen(viewModel = hiltViewModel<OrdersViewModel>(), navController = innerNavController)
      }
      */

        composable(route = DrawerScreens.cart.route) {
            CartScreen(viewModel = hiltViewModel<CartViewModel>())
        }
        //Blank pages
        val menuItems = listOf<DrawerScreens>(
            DrawerScreens.trending,
            DrawerScreens.live,
//            DrawerScreens.markets,
//            DrawerScreens.hemp,
//            DrawerScreens.orders,
//            DrawerScreens.cart,
            DrawerScreens.promotions,
//            DrawerScreens.upgrades, // this will be an activity intent
            DrawerScreens.business_resources,
        )

        menuItems.forEach {
            composable(route = it.route) {
                BlankPage(
                    viewModel = hiltViewModel<BlankViewModel>(),
                    navController = innerNavController
                )
            }
        }
        // blank screens for bottom navigation
        composable(route = BottomNavScreens.Clinic.route) {
            NearbyStoresScreen(
                viewModel = hiltViewModel<NearbyStoresViewModel>(),
                onOpenDrawer = onOpenDrawer
            )
        }
        composable(route = BottomNavScreens.Nearby.route) {
            NearbyUsersScreen(viewmodel = hiltViewModel<NearbyUsersViewModel>())
        }
        composable(route = BottomNavScreens.Chat.route) {
            ChatScreen(
                viewModel = hiltViewModel<ChatScreenViewModel>(),
                navController = innerNavController,
                onOpenDrawer = onOpenDrawer
            )
        }

    }
}