package com.itshedi.weedworld.ui.nearby_stores

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.rememberImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.itshedi.weedworld.R
import com.itshedi.weedworld.entities.Product
import com.itshedi.weedworld.entities.Store
import com.itshedi.weedworld.ui.business_page.BusinessPageActivity
import com.itshedi.weedworld.ui.theme.NearbyShopSearchBackground
import com.itshedi.weedworld.ui.theme.VeryLightGray
import com.itshedi.weedworld.ui.theme.accentGreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Math.*

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterialApi::class)
@Composable
fun NearbyStoresScreen(viewModel: NearbyStoresViewModel, onOpenDrawer:()->Unit) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = Color.Transparent, darkIcons = true
    )


    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(
                viewModel.lat ?: 40.0,
                viewModel.lng ?: 20.0
            ), 15f
        )
    }


    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )
    val lifecycleOwner = LocalLifecycleOwner.current

    val context = LocalContext.current

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                permissionsState.launchMultiplePermissionRequest()
            }
            if (event == Lifecycle.Event.ON_RESUME){
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val location = fusedLocationClient.lastLocation.await()
                        viewModel.myLocation = location

                        viewModel.lat = location.latitude
                        viewModel.lng = location.longitude

                        cameraPositionState.position =
                            CameraPosition.fromLatLngZoom(
                                LatLng(
                                    viewModel.lat ?: 40.0,
                                    viewModel.lng ?: 20.0
                                ), 15f
                            )
                        if (viewModel.stores.isEmpty()){
                            viewModel.findStores()
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


    val focusManager = LocalFocusManager.current
    LaunchedEffect(cameraPositionState.position) {
        focusManager.clearFocus()
        viewModel.lat = cameraPositionState.position.target.latitude
        viewModel.lng = cameraPositionState.position.target.longitude

        val visibleRegion = cameraPositionState.projection?.visibleRegion
        visibleRegion?.let {
            val distanceWidth = FloatArray(1)
            Location.distanceBetween(
                visibleRegion.farLeft.latitude,
                visibleRegion.farLeft.longitude,
                visibleRegion.farRight.latitude,
                visibleRegion.farRight.longitude,
                distanceWidth
            )
            val distanceHeight = FloatArray(1)
            Location.distanceBetween(
                visibleRegion.farLeft.latitude,
                visibleRegion.farLeft.longitude,
                visibleRegion.nearLeft.latitude,
                visibleRegion.nearLeft.longitude,
                distanceHeight
            )
            val radius = distanceWidth[0].coerceAtLeast(distanceHeight[0]) / 2
            Log.d("MAP", "Radius of visible area: $radius meters")

            viewModel.mapsRadius = radius.toDouble()
            viewModel.findStores()
        }

    }

    if (permissionsState.permissions.any { it.permission == Manifest.permission.ACCESS_FINE_LOCATION } && permissionsState.permissions.any { it.permission == Manifest.permission.ACCESS_COARSE_LOCATION }) {

        LaunchedEffect(Unit) {
            try {
                val location = fusedLocationClient.lastLocation.await()
                viewModel.myLocation = location

                viewModel.lat = location.latitude
                viewModel.lng = location.longitude

                cameraPositionState.position =
                    CameraPosition.fromLatLngZoom(
                        LatLng(
                            viewModel.lat ?: 40.0,
                            viewModel.lng ?: 20.0
                        ), 15f
                    )
                viewModel.findStores()
            } catch (e: Exception) {
                // Handle exceptions
            }
        }
    }


    val scaffoldState = rememberBottomSheetScaffoldState()
    val cs = rememberCoroutineScope()
    BackHandler(viewModel.currentStoreDetails!=null) {
        viewModel.currentStoreDetails = null
    }
    BackHandler(scaffoldState.bottomSheetState.isExpanded) {
        cs.launch { scaffoldState.bottomSheetState.collapse() }
    }

    var sheetPeekHeight by remember { mutableStateOf<Dp>(0.dp) }
    var mapsContainerHeight by remember { mutableStateOf<Dp>(0.dp) }
    val densityValue = LocalDensity.current

    Box(modifier = Modifier
        .fillMaxSize()
        .onGloballyPositioned {
            with(densityValue) {
                mapsContainerHeight = (it.size.height * 0.6f).toDp()
                sheetPeekHeight = (it.size.height * 0.4f).toDp()
            }
        }) {
        BottomSheetScaffold(scaffoldState = scaffoldState,
            sheetPeekHeight = sheetPeekHeight.plus(20.dp),
            sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            sheetContent = {
                if( viewModel.currentStoreDetails!=null){
                    viewModel.currentStoreDetails?.let{
                        StoreDetailSheetContent(store = it, distance = calculateDistance(
                            lat1 = it.lat, lon1 = it.lng,
                            lat2 = viewModel.myLocation?.latitude, lon2 = viewModel.myLocation?.longitude
                        ), products =
                        viewModel.storeProducts.getOrDefault(it.storeId!!, null))
                    }
                }else{
                   FilterBar(onSelectedFilter = {
                        viewModel.filter = it
                    },selectedFilter = viewModel.filter)
                    Crossfade(targetState = viewModel.isLoading) {

                        when(it) {
                            true -> Column{repeat(3){
                                StoreItemPlaceholder()
                            }}
                                false -> SwipeRefresh(state = rememberSwipeRefreshState(isRefreshing = false), onRefresh = {
                            viewModel.findStores()
                        }) {
                            BottomSheetContent(viewModel = viewModel, onRequestNavigate = { lat,lng->
                                cs.launch {
                                    cameraPositionState.animate(CameraUpdateFactory.newLatLng(LatLng(
                                        lat,
                                        lng
                                    )))
                                }
                            }, onStoreDetails = { viewModel.currentStoreDetails = it
                            viewModel.getSelectedStoreProducts()})
                        }
                        }
                    }


                }

            }) {
            Box(modifier = Modifier.fillMaxSize()) {

                GoogleMap(
                    cameraPositionState = cameraPositionState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(mapsContainerHeight),
                    properties = MapProperties(isMyLocationEnabled = false),
                    uiSettings = MapUiSettings(compassEnabled = false, mapToolbarEnabled = false)
                ) {
                    viewModel.stores.filter {
                        if(viewModel.filter=="All"){
                            true
                        }else{
                            it.businessType==viewModel.filter
                        }
                    }.forEachIndexed { index, store ->
                        if (store.lat != null && store.lng != null) {
                            LaunchedEffect(true){
                                viewModel.stores[index].photo?.let{
                                    viewModel.loadBitmap(it)
                                }
                            }
                            Marker(
                                state = MarkerState(
                                    position = LatLng(
                                        store.lat,
                                        store.lng
                                    )
                                ),
                                title = store.businessName ?: "Unknown store",
                                icon = viewModel.storeBitmaps[store.photo]
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(0.6f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                NearbyShopsTopBar(
                    onMenuClicked = {
                                    if (viewModel.currentStoreDetails!=null){
                                        viewModel.currentStoreDetails = null
                                    }else{
                                        onOpenDrawer()
                                    }
                    },
                    searchValue = viewModel.searchQuery,
                    onValueChange = { viewModel.searchQuery = it
                                    viewModel.findStores()},
                    onConfirmSearch = { viewModel.findStores() },
                    isNavBack = viewModel.currentStoreDetails!=null
                )
            }

        }
    }
}


@Composable
fun ProductDisplay(products: List<Product>?) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp)) {
            repeat(5){
                Box(modifier = Modifier
                    .size(90.dp)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(shape = RoundedCornerShape(10.dp), color = VeryLightGray)){
                    products?.getOrNull(it)?.images?.getOrNull(0)?.let{ image ->
                        Image(painter = rememberImagePainter(data = image), contentDescription = null,
                            contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize())
                    }

            }


        }
    }
}

@Composable
fun StoreDetailSheetContent(store:Store, distance: Double?, products:List<Product>?) {
    val context = LocalContext.current
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        //handle
        Box(
            modifier = Modifier
                .align(CenterHorizontally)
                .background(
                    color = MaterialTheme.colors.onBackground.copy(0.2f),
                    shape = RoundedCornerShape(10.dp)
                )
                .width(100.dp)
                .height(8.dp)
        )

        Spacer(modifier = Modifier.padding(top = 16.dp))
        StoreItem(store = store, onClick = {
            store.storeId?.let {
                val intent = Intent(context, BusinessPageActivity::class.java)
                val bundle = Bundle()
                bundle.putString("storeId", it)
                bundle.putBoolean("isMyStore", false)
                intent.putExtra("bundle", bundle)
                context.startActivity(intent)
            }
        }, distance = distance)
        ProductDisplay(products = products)
        store.phone?.let { StoreDetailItem(icon = Icons.Filled.Phone, text = it) }
        store.emailAddress?.let { StoreDetailItem(icon = Icons.Filled.Email, text = it) }
        store.website?.let { StoreDetailItem(icon = Icons.Filled.Pageview, text = it) }
    }

}

@Composable
fun StoreDetailItem(icon:ImageVector, text:String) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = accentGreen)
        Spacer(modifier = Modifier.padding(end = 10.dp))
        Text(text = text, color = Color.Gray, fontSize = 14.sp)
    }
}

data class StoreSearchFilter(
    val icon:Int,
    val name:String,
    val filter:String
)

/* "Cannabis store",
        "Delivery Service", "CBD/Hemp", "Hydrostore", "Doctor", "other" */
@Composable
fun FilterBar(onSelectedFilter: (String) -> Unit, selectedFilter:String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(0.08f))
            .horizontalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.padding(start = 10.dp))
        listOf(
            StoreSearchFilter(name = "Recreational", icon = R.drawable.filter_recreational, filter= "Recreational"),
            StoreSearchFilter(name = "Medical Store", icon = R.drawable.filter_doctor, filter = "Cannabis store"),
            StoreSearchFilter(name = "Delivery service", icon = R.drawable.filter_delivery, filter = "Delivery Service"),
            StoreSearchFilter(name = "Clinic", icon = R.drawable.filter_shop, filter = "Doctor"),
            StoreSearchFilter(name = "Smoke shop", icon = R.drawable.filter_smoke, filter = "Hydrostore"),
        ).forEach { (icon, name, filter) ->
            Column(modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(
                    when (selectedFilter) {
                        filter -> Color.LightGray
                        else -> Color.Transparent
                    }
                )
                .clickable(enabled = selectedFilter != filter) { onSelectedFilter(filter) }
                .padding(
                    vertical = 12.dp,
                    horizontal = 6.dp
                ),
                horizontalAlignment = CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = icon),
                        contentScale = ContentScale.FillWidth,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )

                }
                Spacer(modifier = Modifier.padding(top = 4.dp))
                Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Column(modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                when (selectedFilter) {
                    "All" -> Color.LightGray
                    else -> Color.Transparent
                }
            )
            .clickable(enabled = selectedFilter != "All") { onSelectedFilter("All") }
            .padding(
                vertical = 12.dp,
                horizontal = 6.dp
            ),
            horizontalAlignment = CenterHorizontally) {
            Box(
                modifier = Modifier
                    .height(50.dp)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(12.dp)
            ) {
                Text(text = "All", modifier = Modifier.align(Center), fontWeight = FontWeight.Bold)
            }
            Text(text = "")
        }

        Spacer(modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
fun BottomSheetContent(viewModel: NearbyStoresViewModel, onRequestNavigate:(Double,Double) -> Unit, onStoreDetails:(Store)->Unit) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(viewModel.stores.filter {
                if(viewModel.filter=="All"){
                    true
                }else{
                    it.businessType==viewModel.filter
                }
            }) { index, item ->
                StoreItem(
                    store = item, onClick = {
                        if (item.lat!=null && item.lng!=null){
                            onRequestNavigate(item.lat,item.lng)
                        }
                        onStoreDetails(item)
                    }, distance = calculateDistance(
                        lat1 = item.lat, lon1 = item.lng,
                        lat2 = viewModel.myLocation?.latitude, lon2 = viewModel.myLocation?.longitude
                    )
                )
            }
        }




}

@Composable
fun StoreItemPlaceholder() {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)) {
        Box(
            modifier = Modifier
                .height(50.dp)
                .aspectRatio(1f)
                .clip(CircleShape)
                .placeholder(
                    visible = true,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )

        Spacer(modifier = Modifier.padding(end = 12.dp))
        Column(modifier = Modifier.height(50.dp), verticalArrangement = Arrangement.SpaceAround) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(20.dp)
                    .placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(20.dp)
                    .placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
        }
    }
}

@Composable
fun StoreItem(store: Store?, onClick: (() -> Unit)?, distance: Double?) {
    Row(modifier = Modifier
        .clip(RoundedCornerShape(10.dp))
        .clickable(enabled = distance != null) { onClick?.invoke() }
        .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .height(50.dp)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            ) {
                store?.photo?.let {
                    Image(
                        painter = rememberImagePainter(data = it),
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.padding(end = 12.dp))
            Column() {
                Spacer(modifier = Modifier.padding(end = 10.dp))

                Text(
                    store?.businessName ?: "WeedWord Store",
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.padding(top = 2.dp))

                Text(
                    store?.businessType ?: "",
                    color = Color.LightGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(modifier = Modifier, verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.location_pink), contentDescription = null,
                modifier = Modifier.size(26.dp), contentScale = ContentScale.FillHeight
            )
            Spacer(modifier = Modifier.padding(end = 6.dp))
            Text(
                String.format("%.2fKm", distance),
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }

    }
}

@Composable
fun NearbyShopsTopBar(
    isNavBack:Boolean,
    onMenuClicked: () -> Unit,
    searchValue: String,
    onValueChange: (String) -> Unit,
    onConfirmSearch: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .systemBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            onMenuClicked()
        }) {
            Icon(
                imageVector = when(isNavBack){ true -> Icons.Default.ChevronLeft false -> Icons.Default.Menu},
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )

        }
        ShopsSearchBar(
            value = searchValue,
            onValueChange = onValueChange,
            onConfirmSearch = onConfirmSearch
        )
    }

}

@Composable
fun ShopsSearchBar(value: String, onValueChange: (String) -> Unit, onConfirmSearch: () -> Unit) {
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                shape = RoundedCornerShape(20.dp),
                color = NearbyShopSearchBackground.copy(0.5f)
            )
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painter = painterResource(id = R.drawable.search), contentDescription = null)
            Spacer(modifier = Modifier.padding(end = 6.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (value.isBlank()) {
                    Text("Search by name or category", color = Color.DarkGray)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        onConfirmSearch()
                    }),
                )
            }
        }

    }
}

fun calculateDistance(lat1: Double?, lon1: Double?, lat2: Double?, lon2: Double?): Double? {
    return if (lat1 == null || lat2 == null || lon1 == null || lon2 == null) {
        null
    } else {
        val theta = lon1 - lon2
        var dist =
            sin(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) + cos(Math.toRadians(lat1)) * cos(
                Math.toRadians(lat2)
            ) * cos(Math.toRadians(theta))
        dist = acos(dist)
        dist = toDegrees(dist)
        dist = dist * 60 * 1.1515
        dist = dist * 1.609344
        dist
    }
}