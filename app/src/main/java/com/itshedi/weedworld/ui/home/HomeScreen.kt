package com.itshedi.weedworld.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.itshedi.weedworld.R
import com.itshedi.weedworld.ui.navigation.*
import com.itshedi.weedworld.ui.nearby_stores.NearbyShopsTopBar
import com.itshedi.weedworld.ui.nearby_users.NearbySectionsNavigationBar
import kotlinx.coroutines.launch
import org.checkerframework.common.subtyping.qual.Bottom
import rememberQrBitmapPainter

@Composable
fun HomeScreen(viewModel: HomeViewModel, navController: NavController) {

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = Color.Transparent, darkIcons = true
    )

    val scaffoldState = rememberScaffoldState()
    val cs = rememberCoroutineScope()
    val innerNavController = rememberNavController()

    LaunchedEffect(true){
        viewModel.initialize()
    }

    Scaffold(scaffoldState = scaffoldState, topBar = {
        when (innerNavController.currentBackStackEntryAsState().value?.destination?.route){
            BottomNavScreens.Chat.route -> {

            }
            BottomNavScreens.Nearby.route -> {
                NearbySectionsNavigationBar(onMenuClicked = {
                    cs.launch { scaffoldState.drawerState.open() }
                }, selectedIndex = 2)
            }
            BottomNavScreens.Clinic.route -> {

            }
            else -> TopNavigation(onMenuClicked = {
                viewModel.getCartItems()
                cs.launch { scaffoldState.drawerState.open() }
            }, icon = when(innerNavController.currentBackStackEntryAsState().value?.destination?.route){
                DrawerScreens.orders.route -> painterResource(R.drawable.drawer_orders)
                DrawerScreens.cart.route -> painterResource(R.drawable.drawer_cart)
                else -> painterResource(com.itshedi.weedworld.R.drawable.bottom_nav_feed)
            })
        }

    },drawerGesturesEnabled = when(innerNavController.currentBackStackEntryAsState().value?.destination?.route){
        BottomNavScreens.Clinic.route -> scaffoldState.drawerState.isOpen //note: map get buggy when drawer gesture is enabled so we turn it off when drawerstate.isClosed = true
        else -> true
    }, drawerContent = {
        LaunchedEffect(true){
            viewModel.getCartItems()
        }
        MyDrawer(modifier = Modifier.systemBarsPadding(),
            innerNavController = innerNavController,
            navController = navController,
            onCloseDrawer = {
                cs.launch { scaffoldState.drawerState.close() }
            },
            userInfo = viewModel.user,
            businessInfo = viewModel.business,
            profilePhoto = viewModel.user?.profilePhoto,
            onLogout = {
                viewModel.logout()
                navController.navigate(Screens.AuthScreen.route) {
                    popUpTo(Screens.MainScreen.route) {
                        inclusive = true
                    }
                }
            },
            onShowQRCode = {
                viewModel.showQrCodeDialog = true
            },
            onShowBusinessQRCode = {
                viewModel.showBusinessQrCodeDialog = true
            }, isLoadingBusiness = viewModel.isLoadingBusiness,
        cartCount = viewModel.cartCount,
        reloadBusinessPage = {
            viewModel.getMyStore()
        })
    }, drawerShape = RoundedCornerShape(topEnd = 30.dp, bottomEnd = 30.dp), bottomBar = {
        MyBottomNavigation(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxWidth(), navController = innerNavController
        )
    }, modifier = when(innerNavController.currentBackStackEntryAsState().value?.destination?.route){ BottomNavScreens.Nearby.route -> Modifier
        BottomNavScreens.Clinic.route -> Modifier
        else -> Modifier.systemBarsPadding()}
    ) { padding ->


        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)){
            //QR Code dialog
            viewModel.user?.uid?.let {
                if (viewModel.showQrCodeDialog) {
                    Dialog(onDismissRequest = { viewModel.showQrCodeDialog = false }) {
                        Column(
                            modifier = Modifier
                                .background(
                                    color = Color.White, shape = RoundedCornerShape(20.dp)
                                )
                                .padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = rememberQrBitmapPainter(it),
                                contentDescription = "User QR code",
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f),
                            )
                            Spacer(modifier = Modifier.padding(top = 20.dp))
                            OutlinedButton(onClick = { viewModel.showQrCodeDialog = false }) {
                                Text("Close")
                            }
                        }
                    }
                }
            }

            viewModel.business?.storeId?.let {
                if (viewModel.showBusinessQrCodeDialog) {
                    Dialog(onDismissRequest = { viewModel.showBusinessQrCodeDialog = false }) {
                        Column(
                            modifier = Modifier
                                .background(
                                    color = Color.White, shape = RoundedCornerShape(20.dp)
                                )
                                .padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = rememberQrBitmapPainter(it),
                                contentDescription = "${viewModel.business?.businessName} QR code",
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f),
                            )
                            Spacer(modifier = Modifier.padding(top = 20.dp))
                            OutlinedButton(onClick = { viewModel.showBusinessQrCodeDialog = false }) {
                                Text("Close")
                            }
                        }
                    }
                }
            }

            // content
            InnerNavigation(innerNavController = innerNavController, onOpenDrawer = {
                cs.launch { scaffoldState.drawerState.open() }
            })
        }

    }
}