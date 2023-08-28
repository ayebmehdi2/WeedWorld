package com.itshedi.weedworld.ui.nearby_users

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.rememberImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.location.LocationServices
import com.itshedi.weedworld.R
import com.itshedi.weedworld.entities.UserInfo
import com.itshedi.weedworld.entities.UserLocation
import com.itshedi.weedworld.ui.chat.ChatActivity
import com.itshedi.weedworld.ui.nearby_stores.calculateDistance
import com.itshedi.weedworld.ui.theme.AccentRed
import com.itshedi.weedworld.ui.theme.NearbyBackground
import com.itshedi.weedworld.ui.theme.NearbyInactiveNavIcon
import com.itshedi.weedworld.ui.theme.accentGreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NearbyUsersScreen(viewmodel: NearbyUsersViewModel) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = Color.Transparent, darkIcons = true
    )
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )
    val lifecycleOwner = LocalLifecycleOwner.current

    val context = LocalContext.current

    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                permissionsState.launchMultiplePermissionRequest()
            }
            if (event == Lifecycle.Event.ON_RESUME){
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val location = LocationServices.getFusedLocationProviderClient(context).lastLocation.await()
                        viewmodel.myLocation = location
                        // Display the user's latitude and longitude
                        Log.i("lstuff", "Lat: ${location.latitude}, Lon: ${location.longitude}")

                        if (viewmodel.userLocations.isEmpty()){
                            viewmodel.updateLocation()
                            viewmodel.findUsers()
                        }
                    } catch (e: Exception) {
                        // Handle exceptions
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    })

    if (permissionsState.permissions.any { it.permission == Manifest.permission.ACCESS_FINE_LOCATION } && permissionsState.permissions.any { it.permission == Manifest.permission.ACCESS_COARSE_LOCATION }) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        LaunchedEffect(Unit) {
            try {
                val location = fusedLocationClient.lastLocation.await()
                viewmodel.myLocation = location
                // Display the user's latitude and longitude
                Log.i("lstuff", "Lat: ${location.latitude}, Lon: ${location.longitude}")

                viewmodel.updateLocation()
                viewmodel.findUsers()
            } catch (e: Exception) {
                // Handle exceptions
            }
        }
    }

    val scaffoldState = rememberScaffoldState()
    Scaffold(scaffoldState = scaffoldState, modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column() {
            NearbyUsersGrid(
                modifier = Modifier.padding(paddingValues),
                users = viewmodel.userLocations,
                userInfos = viewmodel.users,
                onLoadUser = {
                             viewmodel.loadUserInfo(it)
                },
                getDistance = {
                    calculateDistance(
                        lat1 = viewmodel.userLocations[it].lat, lon1 = viewmodel.userLocations[it].lng,
                        lat2 = viewmodel.myLocation?.latitude, lon2 = viewmodel.myLocation?.longitude
                    )
                },
                isRefreshing = viewmodel.isLoading,
                onRefresh = {
                    viewmodel.findUsers()
                })
        }

    }

}

@Composable
fun NearbyUsersGrid(
    modifier: Modifier,
    users: List<UserLocation>,
    userInfos: Map<String,UserInfo>,
    onLoadUser: (String) -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
    getDistance:(Int)->Double?
) {
    val context = LocalContext.current
    SwipeRefresh(
        modifier = modifier,
        state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
        onRefresh = { onRefresh() }) {
        Crossfade(targetState = users.isEmpty()) {
            when(it){
                true -> {}
                false ->LazyVerticalGrid(
                    modifier = Modifier
                        .background(NearbyBackground)
                        .fillMaxSize(),
                    columns = GridCells.Fixed(3)
                ) {
                    itemsIndexed(users) { index, item ->
                        LaunchedEffect(Unit) {
                            item.userId?.let{onLoadUser(item.userId)}
                        }
                        UserItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.8f)
                                .padding(8.dp), user = userInfos.getOrDefault(item.userId,null),
                            onClick = {
                                userInfos.getOrDefault(item.userId,null)?.let {
                                    val intent = Intent(context, ChatActivity::class.java)
                                    val bundle = Bundle()
                                    bundle.putParcelable("user", it)
                                    intent.putExtra("bundle", bundle)
                                    context.startActivity(intent)
                                }
                            },distance = getDistance(index)
                        )
                    }
                }
            }
        }

    }
}

@Composable
fun NearbySectionsNavigationBar(onMenuClicked: () -> Unit, selectedIndex:Int) {
    var bannerHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    Box(modifier = Modifier
        .fillMaxWidth()
        .onGloballyPositioned {
            with(density) {
                bannerHeight = it.size.height.toDp()
            }
        }) {
        Image(
            painter = painterResource(id = R.drawable.nearby_banner),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(bannerHeight),
            contentScale = ContentScale.FillWidth
        )

        Column(modifier = Modifier.systemBarsPadding()) {
            TopAppBar(
                backgroundColor = Color.Transparent,
                contentColor = Color.Black,
                elevation = 0.dp,
                title = {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Nearby", modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(end = 80.dp))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onMenuClicked()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = null,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                })

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                listOf(
                    R.drawable.nearby_nav_feed,
                    R.drawable.nearby_nav_live,
                    R.drawable.nearby_nav_people
                ).forEachIndexed { index,item ->
                    Column(
                        modifier = Modifier, horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(item),
                            contentDescription = null,
                            tint = when(selectedIndex== index){ true -> accentGreen false -> NearbyInactiveNavIcon},
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.padding(top = 2.dp))
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(4.dp)
                                .background(
                                    color = when (selectedIndex == index) {
                                        true -> Color.Black
                                        false -> Color.Transparent
                                    }, shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }

            }
        }

    }

}

@Composable
fun UserItem(modifier: Modifier, user: UserInfo?, onClick:()->Unit, distance:Double?) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth()
                .background(AccentRed)
        )
        Column(
            modifier = modifier
                .align(Alignment.Center)
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            ) {
                user?.profilePhoto?.let {
                    Image(
                        painter = rememberImagePainter(data = it),
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Row(Modifier, verticalAlignment = Alignment.CenterVertically) {

                Text(
                    user?.username ?: "",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.padding(end = 2.dp))
                when (user?.gender) {
                    "Male" -> Image(
                        painter = painterResource(id = com.itshedi.weedworld.R.drawable.nearby_male),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    "Female" -> Image(
                        painter = painterResource(id = com.itshedi.weedworld.R.drawable.nearby_female),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.padding(top = 2.dp))
            Text(
                String.format("%.2fKm", distance),
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = Color.Gray,
            )
        }
    }

}
