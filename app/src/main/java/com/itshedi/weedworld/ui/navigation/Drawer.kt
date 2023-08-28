package com.itshedi.weedworld.ui.navigation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberImagePainter
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.itshedi.weedworld.R
import com.itshedi.weedworld.entities.Store
import com.itshedi.weedworld.entities.UserInfo
import com.itshedi.weedworld.ui.orders.OrdersActivity
import com.itshedi.weedworld.ui.business_page.BusinessPageActivity
import com.itshedi.weedworld.ui.market.MarketActivity
import com.itshedi.weedworld.ui.store_registration.StoreRegistrationActivity

@Composable
fun MyDrawer(
    innerNavController: NavController,
    navController: NavController,
    onCloseDrawer: () -> Unit,
    onLogout: () -> Unit,
    userInfo: UserInfo?,
    businessInfo: Store?,
    profilePhoto: String?,
    onShowQRCode: () -> Unit,
    onShowBusinessQRCode: () -> Unit,
    reloadBusinessPage: () -> Unit,
    isLoadingBusiness: Boolean = true,
    modifier: Modifier,
    cartCount: Int,
) {
    Log.i("coolinfo", "My uid ${userInfo?.uid.toString()}")
    val navBackStackEntry = innerNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination
    val menuItems = listOf<DrawerScreens>(
//        DrawerScreens.feed,
        DrawerScreens.trending,
        DrawerScreens.live,
        DrawerScreens.markets,
//        DrawerScreens.hemp,
        DrawerScreens.orders,
        DrawerScreens.cart,
        DrawerScreens.promotions,
        DrawerScreens.upgrades,
//        DrawerScreens.business_resources, //todo: check if account is premium
    )
    Column(modifier = modifier.fillMaxSize()) {

        val context = LocalContext.current
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            val launcher =
                rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    when (result.resultCode) {
                        Activity.RESULT_OK -> {
                            Log.i("kiki", "reloading business page")
                            reloadBusinessPage()
                        }

                        Activity.RESULT_CANCELED -> {
                        }
                    }
                }

            // todo: replace let with shimmer loading
            DrawerHeader(userInfo = userInfo,
                businessInfo = businessInfo,
                profilePhoto = profilePhoto,
                onShowQRCode = {
                    onShowQRCode()
                },
                onProfileClick = {
                    navController.navigate(Screens.ProfileScreen.route) // dont popup so user can pres back to go to main screen
                },
                onBusinessClick = {
                    businessInfo?.storeId?.let {
                        val intent = Intent(context, BusinessPageActivity::class.java)
                        val bundle = Bundle()
                        bundle.putString("storeId", it)
                        bundle.putBoolean("isMyStore", true)
                        intent.putExtra("bundle", bundle)
                        context.startActivity(intent)
                    }
                },
                onShowBusinessQRCode = { onShowBusinessQRCode() })


            menuItems.forEach { screen ->

                if (screen != DrawerScreens.upgrades || (screen == DrawerScreens.upgrades && businessInfo == null && !isLoadingBusiness)) {
                    DrawerMenuItem(icon = screen.icon,
                        color = screen.color,
                        text = screen.name,
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onItemClick = {
                            when (screen) {
                                DrawerScreens.upgrades -> {
                                    val intent =
                                        Intent(context, StoreRegistrationActivity::class.java)
                                    launcher.launch(intent)
                                }

                                DrawerScreens.orders -> {
                                    val intent = Intent(
                                        context,
                                        OrdersActivity::class.java
                                    ) // Todo here use OrdersActivity.kt instead of composable
                                    launcher.launch(intent)
                                }

                                DrawerScreens.markets -> {
                                    val intent = Intent(context, MarketActivity::class.java)
                                    context.startActivity(intent)
                                }

                                else -> {
                                    innerNavController.navigate(screen.route) {
                                        popUpTo(currentDestination?.route!!) {
                                            inclusive = true
                                        }
                                    }
                                }
                            }

                            onCloseDrawer()
                        },
                        counter =
                        when (screen.route) {
                            DrawerScreens.cart.route -> cartCount.takeIf { it > 0 }
                            else -> null
                        }
                    )
                }
            }
        }


        Box(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(horizontal = 16.dp)
                .padding(bottom = 20.dp)
                .clip(
                    RoundedCornerShape(10.dp)
                )
                .clickable { onLogout() }
                .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Outlined.Logout, contentDescription = null)
                Spacer(Modifier.padding(end = 16.dp))
                Text("Logout")
            }
        }

    }


}

@Composable
private fun DrawerMenuItem(
    icon: Int,
    color: Color,
    text: String,
    selected: Boolean,
    onItemClick: () -> Unit,
    counter: Int? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(
                color = when (selected) {
                    true -> Color.Gray.copy(0.3f)
                    false -> Color.Transparent
                }, shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable { onItemClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Icon(
            painter = painterResource(icon), contentDescription = null, tint = color
        )
        Text(text = text, modifier = Modifier
            .weight(1f)
            .padding(horizontal = 12.dp))
        counter?.let {
            Box(
                modifier = Modifier
                    .background(
                        color = Color.Red, shape = RoundedCornerShape(4.dp)
                    )
                    .padding(vertical = 3.dp, horizontal = 5.dp)
            ) {
                Text(
                    it.toString(),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }

    }
}

@Composable
fun DrawerHeader(
    userInfo: UserInfo?,
    businessInfo: Store?,
    profilePhoto: String?,
    onShowQRCode: () -> Unit,
    onShowBusinessQRCode: () -> Unit,
    onProfileClick: () -> Unit,
    onBusinessClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.weedworld),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 26.dp, bottom = 34.dp, end = 16.dp)
        )
        DrawerProfileItem(text = userInfo?.username.toString(),
            photo = profilePhoto,
            onQRCode = { onShowQRCode() },
            onClick = { onProfileClick() })
        Spacer(modifier = Modifier.padding(top = 10.dp))
        businessInfo?.let {
            DrawerProfileItem(text = it.businessName.toString(),
                photo = businessInfo.photo,
                onQRCode = { onShowBusinessQRCode() },
                onClick = { onBusinessClick() })
        }
    }
}

@Composable
fun DrawerProfileItem(text: String?, photo: String?, onQRCode: () -> Unit, onClick: () -> Unit) {
    if (text == null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color = Color.Gray, shape = CircleShape)
                    .placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )

            Spacer(modifier = Modifier.padding(end = 10.dp))
            Column {
                Text(
                    text = "USERNAME", modifier = Modifier.placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
                )
                Box(
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .size(30.dp)
                        .placeholder(
                            visible = true,
                            highlight = PlaceholderHighlight.shimmer(),
                        ),
                )
            }
        }
    } else {
        Row(modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 10.dp)) {

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color = Color.Gray, shape = CircleShape)
                    .border(width = 2.dp, color = Color.White, shape = CircleShape)
            ) {
                Image(
                    painter = rememberImagePainter(data = photo),
                    contentDescription = null
                )
            }

            Spacer(modifier = Modifier.padding(end = 10.dp))
            Column {
                Text(text = text)
                Icon(
                    imageVector = Icons.Outlined.QrCode,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .size(30.dp)
                        .clickable { onQRCode() },
                )
            }
        }
    }
}
