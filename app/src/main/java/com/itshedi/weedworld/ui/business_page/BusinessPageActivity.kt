package com.itshedi.weedworld.ui.business_page

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.DropdownMenu
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.rememberDismissState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import coil.compose.rememberImagePainter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.itshedi.weedworld.AddPostActivity
import com.itshedi.weedworld.R
import com.itshedi.weedworld.entities.Product
import com.itshedi.weedworld.entities.Store
import com.itshedi.weedworld.ui.PostsGrid
import com.itshedi.weedworld.ui.ProductItem
import com.itshedi.weedworld.ui.business_page.edit.EditBusinessPage
import com.itshedi.weedworld.ui.dialogs.ChoiceConfirmDialog
import com.itshedi.weedworld.ui.dialogs.ProgressDialog
import com.itshedi.weedworld.ui.posts.product_post.ProductActivity
import com.itshedi.weedworld.ui.theme.WeedWorldTheme
import com.itshedi.weedworld.ui.theme.accentGreen
import com.itshedi.weedworld.ui.theme.priceBorder
import com.itshedi.weedworld.ui.theme.priceFill
import com.itshedi.weedworld.ui.theme.storeTabColor
import com.itshedi.weedworld.ui.theme.storeTabTextColor
import com.itshedi.weedworld.utils.productCategories
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BusinessPageActivity : ComponentActivity() {
    lateinit var viewModel: BusinessPageViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        viewModel = ViewModelProvider(this)[BusinessPageViewModel::class.java]

        val bundle = intent.getBundleExtra("bundle")
        if (bundle!!.containsKey("storeId")) {
            viewModel.isMyStore = bundle.getBoolean("isMyStore")
            viewModel.storeId = bundle.getString("storeId")
        }

        setContent {
            WeedWorldTheme {
                val systemUiController = rememberSystemUiController()
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = false
                )


                LaunchedEffect(viewModel.storeId) {
                    viewModel.loadStore()

                }

                val updateProfilePhoto =
                    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                        uri?.let {
                            viewModel.updatePhoto(uri)
                        }
                    }
                val coverImagePicker =
                    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                        uri?.let {
                            viewModel.updateCoverPhoto(uri)
                        }
                    }


                val scaffoldState = rememberScaffoldState()
                val context = LocalContext.current

                var snackbarMessage by remember { mutableStateOf<String?>(null) }

                val state = viewModel.eventFlow.collectAsState(BusinessPageEvent.empty).value
                if (state is BusinessPageEvent.loading) {
                    ProgressDialog()
                }

                val launcher =
                    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                        when (result.resultCode) {
                            Activity.RESULT_OK -> {
                                Log.i("kiki", "OK RECEIVED")
                                when (viewModel.selectedSection) {
                                    0 -> viewModel.loadProducts()
                                    1 -> viewModel.loadPosts()
                                }
                            }

                            Activity.RESULT_CANCELED -> {
                            }
                        }
                    }

                ChoiceConfirmDialog(
                    showDialog = viewModel.deleteConfirmDialog,
                    message = {
                        Text(
                            "Are you sure you want to delete this product ?",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                        )
                    },
                    okMessage = "Yes",
                    cancelMessage = "No",
                    onOk = {
                        viewModel.deleteProduct(onDeleted = {
                            viewModel.deleteConfirmDialog = false
                            viewModel.loadProducts()
                        })
                    },
                    onCancel = { viewModel.deleteConfirmDialog = false },
                    onDismissRequest = { viewModel.deleteConfirmDialog = false })

                Scaffold(
                    scaffoldState = scaffoldState,
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        if (viewModel.isMyStore) {
                            FloatingActionButton(onClick = {
                                when (viewModel.selectedSection) {
                                    0 -> {
                                        viewModel.business?.storeId?.let {
                                            val intent =
                                                Intent(context, AddProductActivity::class.java)
                                            val bundle = Bundle()
                                            bundle.putString("storeId", it)
                                            intent.putExtra("bundle", bundle)
                                            launcher.launch(intent)
                                        }
                                    }

                                    1 -> {
                                        viewModel.business?.storeId?.let {
                                            val intent =
                                                Intent(context, AddPostActivity::class.java)
                                            val bundle = Bundle()
                                            bundle.putString("storeId", it)
                                            intent.putExtra("bundle", bundle)
                                            launcher.launch(intent)
                                        }
                                    }
                                }

                            }, modifier = Modifier.systemBarsPadding()) {
                                Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
                            }
                        }
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize(
                            )
                            .padding(padding)
                    ) {
                        viewModel.business?.let { business ->
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        // background cover image
                                        Image(
                                            painter = rememberImagePainter(data = viewModel.business?.coverPhoto),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(220.dp)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .height(220.dp)
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
                                        //content
                                        TopAppBar(
                                            elevation = 0.dp,
                                            modifier = Modifier.systemBarsPadding(),
                                            navigationIcon = {
                                                IconButton(onClick = {
                                                    finish()
                                                }) {
                                                    Icon(
                                                        imageVector = Icons.Default.ArrowBack,
                                                        contentDescription = null
                                                    )
                                                }
                                            }, actions = {
                                                if (viewModel.isMyStore) {
                                                    IconButton(onClick = {
                                                        val intent = Intent(
                                                            context,
                                                            EditBusinessPage::class.java
                                                        )
                                                        val intentBundle = Bundle()
                                                        bundle.putParcelable("business", business)
                                                        intent.putExtra("bundle", intentBundle)
                                                        context.startActivity(intent)
                                                    }) {
                                                        Icon(
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = null
                                                        )
                                                    }

                                                }
                                                /*    IconButton(onClick = {
                                                        val intent1 = Intent(
                                                            context,
                                                            QRCodeScannerActivity::class.java
                                                        )
                                                        context.startActivity(intent1)
                                                    }) {
                                                        Icon(
                                                            imageVector = Icons.Default.QrCodeScanner,
                                                            contentDescription = null
                                                        )
                                                    }
    */
                                            }, title = {},
                                            backgroundColor = Color.Transparent,
                                            contentColor = Color.White
                                        )
                                        if (viewModel.isMyStore) {
                                            Row(
                                                modifier = Modifier
                                                    .padding(16.dp)
                                                    .background(
                                                        color = Color.Black.copy(0.4f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .clickable {
                                                        coverImagePicker.launch(arrayOf("*/*"))
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                                    .align(
                                                        Alignment.BottomEnd
                                                    ),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "Edit cover",
                                                    fontSize = 12.sp,
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.padding(end = 4.dp))
                                                Icon(
                                                    imageVector = Icons.Default.PhotoCamera,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(16.dp),
                                                    tint = Color.White
                                                )
                                            }
                                        }

                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                            .padding(top = 180.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                                    .background(
                                                        color = Color.Gray,
                                                        shape = CircleShape
                                                    )
                                                    .border(
                                                        width = 2.dp,
                                                        color = Color.White,
                                                        shape = CircleShape
                                                    )
                                            ) {
                                                Image(
                                                    painter = rememberImagePainter(data = viewModel.business?.photo),
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                            if (viewModel.isMyStore) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            color = Color.Black.copy(0.4f),
                                                            shape = CircleShape
                                                        )
                                                        .clickable {
                                                            updateProfilePhoto.launch(arrayOf("*/*"))
                                                        }
                                                        .padding(4.dp)
                                                        .align(
                                                            Alignment.BottomEnd
                                                        )
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PhotoCamera,
                                                        contentDescription = null,
                                                        modifier = Modifier
                                                            .size(14.dp),
                                                        tint = Color.White
                                                    )
                                                }
                                            }
                                        }
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(start = 20.dp, top = 46.dp, end = 20.dp)
                                        ) {
                                            Text(
                                                business.businessName.toString(),
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "WW ID: ${business.storeId}",
                                                color = Color.Gray,
                                                fontSize = 12.sp
                                            )
                                        }

                                        Box {
                                            Image(painter = painterResource(id = R.drawable.store_info),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .padding(top = 46.dp)
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .clickable { viewModel.showInfoPopup = true })
                                            DropdownMenu(
                                                modifier = Modifier.align(Alignment.TopEnd),
                                                expanded = viewModel.showInfoPopup,
                                                onDismissRequest = {
                                                    viewModel.showInfoPopup = false
                                                },
                                            ) {
                                                BusinessInfoPopup(store = business)
                                            }
                                        }

                                    }
                                }
                                Spacer(modifier = Modifier.padding(top = 16.dp))
                                Sections(
                                    sections = listOf("Store", "Social"),
                                    selectedIndex = viewModel.selectedSection,
                                    onSelect = {
                                        viewModel.selectedSection = it
                                    })
                                Spacer(modifier = Modifier.padding(top = 16.dp))
                                when (viewModel.selectedSection) {
                                    0 -> {
                                        FilterBar(onSelectedFilter = {
                                            viewModel.filter = it
                                            viewModel.loadProducts()
                                        }, selectedFilter = viewModel.filter)
                                        Spacer(modifier = Modifier.padding(top = 16.dp))
                                        LaunchedEffect(true) {
                                            viewModel.loadProducts()
                                        }
                                        ProductList(products = viewModel.products,
                                            isLoading = viewModel.isLoadingProducts, onClick = {
                                                val intent =
                                                    Intent(context, ProductActivity::class.java)
                                                val bundle = Bundle()
                                                bundle.putParcelable(
                                                    "product",
                                                    viewModel.products[it]
                                                )
                                                intent.putExtra("bundle", bundle)
                                                launcher.launch(intent)
                                            }, onEdit = {
                                                val intent =
                                                    Intent(context, AddProductActivity::class.java)
                                                val bundle = Bundle()
                                                viewModel.business?.storeId?.let {
                                                    bundle.putString("storeId", it)
                                                }
                                                bundle.putParcelable(
                                                    "product",
                                                    viewModel.products[it]
                                                )
                                                intent.putExtra("bundle", bundle)
                                                launcher.launch(intent)
                                            }, onDelete = {
                                                viewModel.productToDelete =
                                                    viewModel.products[it].productId
                                                viewModel.deleteConfirmDialog = true
                                            },
                                            onRefresh = {
                                                viewModel.loadProducts()
                                            }, editable = viewModel.isMyStore
                                        )
                                    }

                                    1 -> {
                                        LaunchedEffect(true) {
                                            viewModel.loadPosts()
                                        }
                                        PostsGrid(
                                            modifier = Modifier.fillMaxWidth(),
                                            Posts = viewModel.posts,
                                            downloadUrl = { it },
                                            isLoading = viewModel.isLoadingPosts,
                                            onRefresh = {
                                                viewModel.loadPosts()
                                            },
                                            likes = viewModel.likes,
                                            comments = viewModel.comments,
                                            getLikesAndComments = { viewModel.getLikesAndComments(it) })
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
internal fun Sections(sections: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        sections.forEachIndexed { index, label ->
            Box(modifier = Modifier
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(
                    when (selectedIndex == index) {
                        true -> accentGreen
                        false -> storeTabColor
                    }
                )
                .clickable { onSelect(index) }
                .padding(horizontal = 26.dp, vertical = 8.dp)) {
                Text(
                    text = label,
                    fontWeight = FontWeight.SemiBold,
                    color = when (selectedIndex == index) {
                        true -> Color.White
                        false -> storeTabTextColor
                    }
                )
            }

            Spacer(Modifier.padding(end = 4.dp))
        }
    }
}

@Composable
internal fun FilterBar(onSelectedFilter: (String) -> Unit, selectedFilter: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(0.03f))
            .horizontalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.padding(start = 10.dp))
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
            horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .height(60.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(12.dp)
            ) {
                Text(
                    text = "All",
                    modifier = Modifier.align(Alignment.Center),
                    fontWeight = FontWeight.Bold
                )
            }
            Text(text = "")
        }
        productCategories.forEach { category ->
            Column(modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(
                    when (selectedFilter) {
                        category.name -> Color.LightGray
                        else -> Color.Transparent
                    }
                )
                .clickable(enabled = selectedFilter != category.name) {
                    onSelectedFilter(
                        category.name
                    )
                }
                .padding(
                    vertical = 12.dp,
                    horizontal = 6.dp
                ),
                horizontalAlignment = Alignment.CenterHorizontally) {

                Image(
                    painter = painterResource(id = category.image),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    modifier = Modifier
                        .height(60.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.padding(top = 4.dp))
                Text(text = category.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }


        Spacer(modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
fun BusinessInfoPopup(store: Store) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(store.businessName ?: "", fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.padding(top = 10.dp))
        listOf(
            Pair(Icons.Filled.Phone, store.phone),
            Pair(Icons.Filled.Email, store.emailAddress),
            Pair(Icons.Filled.Article, store.website),
            Pair(Icons.Filled.PinDrop, store.address),
        ).forEach { (icon, value) ->
            value?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.padding(start = 10.dp))
                    Text(it)
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun ProductList(
    products: List<Product>,
    isLoading: Boolean,
    onClick: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onEdit: (Int) -> Unit,
    onRefresh: () -> Unit,
    editable: Boolean
) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = isLoading),
        onRefresh = onRefresh
    ) {
        Crossfade(targetState = isLoading) {
            when (it) {
                true -> Box(Modifier) // todo: shimmer placeholder
                false -> LazyColumn() {
                    itemsIndexed(products) { index, product ->
                        if (!editable) {
                            ProductItem(product = product, onClick = { onClick(index) })
                        } else {
                            SwipeToDismiss(modifier = Modifier
                                .animateItemPlacement()
                                .fillMaxWidth()
                                .height(90.dp),
                                directions = setOf(
                                    DismissDirection.StartToEnd,
                                    DismissDirection.EndToStart
                                ),
                                dismissThresholds = { direction ->
                                    FractionalThreshold(
                                        0.5f
                                    )
                                },
                                state = rememberDismissState(confirmStateChange = {
                                    when (it) {
                                        DismissValue.DismissedToEnd -> {
                                            onDelete(index)
                                            false
                                        }

                                        DismissValue.DismissedToStart -> {
                                            onEdit(index)
                                            false
                                        }

                                        else -> {
                                            true
                                        }
                                    }
                                }),
                                background = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(accentGreen)
                                            .padding(horizontal = 20.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.delete),
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .align(
                                                    Alignment.CenterStart
                                                )
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .align(
                                                    Alignment.CenterEnd
                                                )
                                        )
                                    }
                                }) {
                                ProductItem(product = product, onClick = { onClick(index) })
                            }
                        }
                    }
                }
            }
        }

    }

}

@Composable
fun WeightPriceItem(weight: Double?, price: Double, discountPrice: Double?) {
    Column(horizontalAlignment = Alignment.End) {
        if (discountPrice != null) {
            Text(
                text = String.format("$%.2f", price),
                style = TextStyle(textDecoration = TextDecoration.LineThrough),
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.padding(top = 8.dp))
        Row(
            Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(color = priceFill)
                .border(color = priceBorder, shape = RoundedCornerShape(10.dp), width = 1.dp)
                .padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            weight?.let {
                Text(
                    text = String.format("%.1fg", it),
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.padding(end = 6.dp))
            Text(
                text = String.format("$%.2f", price),
                fontWeight = FontWeight.SemiBold,
                color = Color.Red,
                fontSize = 14.sp
            )
        }
    }
}