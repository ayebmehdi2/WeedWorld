package com.itshedi.weedworld.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.itshedi.weedworld.ui.navigation.BottomNavScreens
import com.itshedi.weedworld.ui.navigation.DrawerScreens
import com.itshedi.weedworld.ui.theme.accentGreen
import com.itshedi.weedworld.ui.theme.productAddToCart
import com.itshedi.weedworld.utils.pagerTabIndicatorOffset
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OrdersScreen(viewModel: OrdersViewModel, navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        val pagerState = rememberPagerState()
        val coroutineScope = rememberCoroutineScope()
        TabRow(
            // Our selected tab is our current page
            selectedTabIndex = pagerState.currentPage,
            // Override the indicator, using the provided pagerTabIndicatorOffset modifier
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier
                        .pagerTabIndicatorOffset(pagerState, tabPositions)
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    height = 6.dp,
                    color = accentGreen,
                )
            }, backgroundColor = Color.Transparent,
            contentColor = Color.Black,
            modifier = Modifier
                .padding(bottom = 5.dp),
            divider = {  }
        ) {
            // Add tabs for all of our pages
            Tab(
                text = { Text("Past orders") },
                selected = pagerState.currentPage == 0,
                onClick = {
                    if (pagerState.currentPage != 0) {
                        coroutineScope.launch { pagerState.animateScrollToPage(0) }
                    }
                },
            )
            Tab(
                text = { Text("Pickup orders") },
                selected = pagerState.currentPage == 1,
                onClick = {
                    if (pagerState.currentPage != 1) {
                        coroutineScope.launch { pagerState.animateScrollToPage(1) }
                    }
                },
            )
        }
        HorizontalPager(count = 2, state = pagerState, modifier = Modifier.weight(1f)) { page ->
            when(page){
                0 -> {
                    when (viewModel.pickupOrders.isEmpty()) {
                        true -> EmptyStatePlaceholder(modifier = Modifier.fillMaxSize(),
                            onStartShopping = { navController.navigate(DrawerScreens.markets.route) })
                        false -> {
                            //note: two pages layout with orders
                        }
                    }
                }
                1 ->  {
                    when (viewModel.pastOrders.isEmpty()) {
                        true -> EmptyStatePlaceholder(modifier = Modifier.fillMaxSize(),
                            onStartShopping = { navController.navigate(BottomNavScreens.Clinic.route) })
                        false -> {
                            //note: two pages layout with orders
                        }
                    }
                }
            }
        }

    }

}

@Composable
internal fun EmptyStatePlaceholder(modifier: Modifier, onStartShopping: () -> Unit) {
    Column(modifier = modifier, verticalArrangement = Arrangement.SpaceEvenly) {
        Text(
            "No Orders Yet",
            fontSize = 18.sp,
            color = Color.Gray,
            modifier = Modifier.align(CenterHorizontally)
        )

        Box(
            modifier = Modifier
                .align(CenterHorizontally)
                .clip(RoundedCornerShape(30.dp))
                .background(productAddToCart, shape = RoundedCornerShape(30.dp))
                .clickable { onStartShopping() }
                .padding(horizontal = 30.dp, vertical = 12.dp), contentAlignment = Alignment.Center
        ) {
            Text("Start Shopping", color = Color.White.copy(0.8f), fontWeight = FontWeight.Bold)
        }

    }
}