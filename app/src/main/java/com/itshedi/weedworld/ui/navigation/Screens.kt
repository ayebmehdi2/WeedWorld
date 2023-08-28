package com.itshedi.weedworld.ui.navigation

import androidx.compose.ui.graphics.Color
import com.itshedi.weedworld.R
import com.itshedi.weedworld.ui.theme.*


// navigation
sealed class Screens(val name:String, val route:String){
    object LandingScreen : Screens("Landing page", "landing_screen")
    object MainScreen : Screens("Main", "main_screen") // contains all screens in BottomNavScreens and DrawerScreens
    object AuthScreen : Screens("Authentication", "auth_screen")
    object ProfileScreen : Screens("Profile", "profile_screen")
    object BusinessPageScreen : Screens("Business page", "business_page_screen")
    object EditProfile : Screens("Edit profile", "edit_profile")

}

// innerNavigation
sealed class BottomNavScreens(val name:String,val route:String, val icon: Int){
    object Feed : BottomNavScreens("Feed", "feed_screen", icon = R.drawable.bottom_nav_feed)
    object Clinic : BottomNavScreens("Clinic", "clinic_screen", icon = R.drawable.bottom_nav_clinic)
    object Nearby : BottomNavScreens("Nearby", "nearby_screen", icon = R.drawable.bottom_nav_nearby)
    object Chat : BottomNavScreens("Chat", "chat_screen", icon = R.drawable.bottom_nav_chat)
}

// innerNavigation
sealed class DrawerScreens(val name:String, val route:String, val icon: Int, val color: Color){
    object feed : DrawerScreens("Feed", "feed_screen", icon = R.drawable.bottom_nav_feed, color = DrawerFeed) //same route as in BottomNavScreens
    // cuz its used both in drawer and bottomVav

    object trending : DrawerScreens("Trending", "trending", icon = R.drawable.drawer_trending, color = DrawerTrending)
    object live : DrawerScreens("Live", "live", icon = R.drawable.drawer_live, color = DrawerLive)
    object markets : DrawerScreens("Markets", "markets", icon = R.drawable.drawer_digital_mall, color = DrawerMarkets)
//    object hemp : DrawerScreens("Hemp", "hemp", icon = R.drawable.drawer_hemp, color = DrawerHemp)
    object orders : DrawerScreens("Orders", "orders", icon = R.drawable.drawer_orders, color = DrawerOrders)
    object cart : DrawerScreens("Cart", "cart", icon = R.drawable.drawer_cart, color = DrawerCart)
    object promotions : DrawerScreens("Promotions", "promotions", icon = R.drawable.drawer_promotion, color = DrawerPromotions)
    object upgrades : DrawerScreens("Upgrades", "upgrades", icon = R.drawable.drawer_membership, color = DrawerMembership)
    object business_resources : DrawerScreens("Business resources", "business_resources", icon = R.drawable.drawer_business_resources, color = DrawerBusinessResources)
}

