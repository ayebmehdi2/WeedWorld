package com.itshedi.weedworld.ui.market

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.itshedi.weedworld.entities.Store
import com.itshedi.weedworld.ui.ProductItem
import com.itshedi.weedworld.ui.StoreItem
import com.itshedi.weedworld.ui.business_page.BusinessPageActivity
import com.itshedi.weedworld.ui.posts.product_post.ProductActivity
import com.itshedi.weedworld.ui.theme.VeryLightGray
import com.itshedi.weedworld.ui.theme.WeedWorldTheme
import com.itshedi.weedworld.ui.theme.accentGreen
import com.itshedi.weedworld.utils.pagerTabIndicatorOffset
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@AndroidEntryPoint
class MarketActivity : ComponentActivity() {
    lateinit var viewModel: MarketViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MarketViewModel::class.java]
        viewModel.findFeaturedStores()
        setContent {
            WeedWorldTheme {
                BackHandler(viewModel.showSearch) {
                    viewModel.dismissSearch()
                }
                Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
                    Column {
                        Crossfade(targetState = viewModel.showSearch) { showSearch ->
                            if (showSearch) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    SearchBar(modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                        value = viewModel.searchQuery,
                                        setValue = {
                                            viewModel.searchQuery = it
                                            viewModel.getProductsAndStores()
                                        },
                                        onSearch = { /* note: dont need this items gets loaded when user stops typing */ },
                                        onDismiss = {
                                            viewModel.dismissSearch()
                                        })
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = {
                                            finish()
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.ChevronLeft,
                                                contentDescription = null
                                            )
                                        }
                                        Text(
                                            text = "Market",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.showSearch = true },
                                        modifier = Modifier.align(Alignment.CenterEnd)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null
                                        )
                                    }
                                }
                            }

                        }
                    }
                }) {
                    if (viewModel.showSearch) {
                        ResultPage()
                    } else {
                        FeaturedStores()
                    }
                }
            }
        }
    }

    @Composable
    fun FeaturedStores() {
        val context = LocalContext.current
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(
                "Featured Stores",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 20.dp, bottom = 16.dp)
            )

            LazyRow(modifier = Modifier.fillMaxWidth()) {
                item { Spacer(modifier = Modifier.padding(start = 4.dp)) }
                itemsIndexed(viewModel.featuredStores) { index, item ->
                    FeaturedStore(item = item, onClick = {
                        item.storeId?.let {
                            val intent = Intent(context, BusinessPageActivity::class.java)
                            val bundle = Bundle()
                            bundle.putString("storeId", it)
                            bundle.putBoolean("isMyStore", false)
                            intent.putExtra("bundle", bundle)
                            context.startActivity(intent)
                        }
                    })
                }
                item { Spacer(modifier = Modifier.padding(start = 4.dp)) }
            }
        }
    }

    @Composable
    private fun FeaturedStore(item: Store, onClick: () -> Unit) {
        Column(modifier = Modifier
            .padding(8.dp)
            .size(168.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(4.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .weight(1f)
                    .background(VeryLightGray)
            ) {
                Image(
                    painter = rememberImagePainter(data = item.coverPhoto),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(
                            VeryLightGray
                        )
                ) {
                    Image(
                        painter = rememberImagePainter(data = item.photo),
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.padding(3.dp))
                Text(
                    item.businessName ?: "",
                    fontWeight = FontWeight.SemiBold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )

            }
        }
    }

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    fun ResultPage() {
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
                divider = { }
            ) {
                // Add tabs for all of our pages
                Tab(
                    text = { Text("Products") },
                    selected = pagerState.currentPage == 0,
                    onClick = {
                        if (pagerState.currentPage != 0) {
                            coroutineScope.launch { pagerState.animateScrollToPage(0) }
                        }
                    },
                )
                Tab(
                    text = { Text("Businesses") },
                    selected = pagerState.currentPage == 1,
                    onClick = {
                        if (pagerState.currentPage != 1) {
                            coroutineScope.launch { pagerState.animateScrollToPage(1) }
                        }
                    },
                )
            }
            HorizontalPager(count = 2, state = pagerState, modifier = Modifier.weight(1f)) { page ->

                Crossfade(
                    targetState = when (page) {
                        0 -> viewModel.isLoadingProducts
                        else -> viewModel.isLoadingStores
                    }
                ) { isLoading ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        when (isLoading) {
                            true -> CircularProgressIndicator(
                                modifier = Modifier.align(
                                    Alignment.Center
                                )
                            )
                            false -> {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    if (page == 0 && viewModel.products.isEmpty() || page == 1 && viewModel.stores.isEmpty()) {
                                        Text(
                                            when (page) {
                                                0 -> "No products found"
                                                else -> "No Businesses found"
                                            },
                                            fontSize = 18.sp,
                                            color = Color.Gray,
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .fillMaxWidth()
                                                .padding(horizontal = 30.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    } else {
                                        when (page) {
                                            0 -> ProductList()
                                            1 -> StoreList()
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun StoreList() {
        val context = LocalContext.current
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(viewModel.stores) { index, item ->
                Box(
                    modifier = Modifier
                        .height(90.dp)
                ) {
                    StoreItem(store = item, onClick = {
                        item.storeId?.let {
                            val intent = Intent(context, BusinessPageActivity::class.java)
                            val bundle = Bundle()
                            bundle.putString("storeId", it)
                            bundle.putBoolean("isMyStore", false)
                            intent.putExtra("bundle", bundle)
                            context.startActivity(intent)
                        }
                    })
                }
            }
        }
    }

    @Composable
    fun ProductList() {
        val context = LocalContext.current
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(viewModel.products) { index, item ->
                //note: dont need store info for products for now
//                LaunchedEffect(true){
//                    item.storeId?.let { viewModel.loadStore(it) }
//                }

                ProductItem(product = item, onClick = {
                    val intent = Intent(context, ProductActivity::class.java)
                    val bundle = Bundle()
                    bundle.putParcelable("product", viewModel.products[index])
                    intent.putExtra("bundle", bundle)
                    context.startActivity(intent)
                })

            }
        }
    }
}


@Composable
fun SearchBar(
    modifier: Modifier,
    value: String,
    setValue: (String) -> Unit,
    onSearch: () -> Unit,
    onDismiss: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(shape = RoundedCornerShape(10.dp), color = VeryLightGray)
            .padding(vertical = 0.dp, horizontal = 8.dp)
            .padding(start = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (value.isBlank()) {
                Text("Search", color = Color.Gray)
            }

            val focusRequester = FocusRequester()
            LaunchedEffect(true) {
                delay(200)
                focusRequester.requestFocus()
            }
            BasicTextField(
                value = value,
                maxLines = 1,
                onValueChange = setValue,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    onSearch()

                }),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .fillMaxWidth()
                    .padding(end = 10.dp)
            )
        }
        IconButton(onClick = {
            onDismiss()
        }) {
            Icon(imageVector = Icons.Default.Close, contentDescription = null)
        }

    }
}
