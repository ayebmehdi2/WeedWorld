package com.itshedi.weedworld.ui.posts.product_post

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.itshedi.weedworld.R
import com.itshedi.weedworld.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductActivity : ComponentActivity() {
    lateinit var viewModel: ProductViewModel

    @OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class, ExperimentalPagerApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val bundle = intent.getBundleExtra("bundle")
        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        if (bundle!!.containsKey("product")) {
            viewModel.product = bundle.getParcelable("product")
            viewModel.getCartItems()
        }

        setContent {
            WeedWorldTheme {
                val systemUiController = rememberSystemUiController()
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = false
                )

                val cs = rememberCoroutineScope()

                val scaffoldState = rememberBottomSheetScaffoldState()

                var sheetPeekHeight by remember { mutableStateOf<Dp>(0.dp) }
                var pagerContainerHeight by remember { mutableStateOf<Dp>(0.dp) }

                var isImageExpanded by remember { mutableStateOf(false) }
                val densityValue = LocalDensity.current

                BackHandler(scaffoldState.bottomSheetState.isExpanded || isImageExpanded) {
                    if (isImageExpanded){
                        isImageExpanded = false
                    }else {
                        cs.launch { scaffoldState.bottomSheetState.collapse() }
                    }
                }


                Box(modifier = Modifier.fillMaxSize()) {
                    BottomSheetScaffold(modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned {
                            with(densityValue) {
                                pagerContainerHeight = (it.size.height * 0.4f).toDp()
                                sheetPeekHeight = (it.size.height * 0.6f).toDp()
                            }
                        },
                        scaffoldState = scaffoldState,
                        sheetPeekHeight = when(isImageExpanded){ true -> 0.dp false -> sheetPeekHeight.plus(20.dp)},
                        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                        sheetBackgroundColor = productSheetBackground,
                        sheetContentColor = Color.White,
                        sheetContent = {
                            BottomSheet()
                        }
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = when(isImageExpanded){
                                    true -> Modifier.fillMaxSize()
                                    false -> Modifier
                                        .fillMaxWidth()
                                        .height(pagerContainerHeight)
                                }
                            ) {
                                // background cover image
                                HorizontalPager(count = viewModel.product!!.images.size,
                                modifier = Modifier.fillMaxSize()) { index ->
                                    Image(
                                        painter = rememberImagePainter(data = viewModel.product!!.images[index]),
                                        contentDescription = null,
                                        contentScale = when(isImageExpanded){
                                                                            true -> ContentScale.Fit
                                            false -> ContentScale.Crop
                                                                            },
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable {
                                                cs.launch { scaffoldState.bottomSheetState.collapse() }
                                                isImageExpanded = true
                                            }
                                    )
                                }


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
                                            if(isImageExpanded){
                                                isImageExpanded = false
                                            }else{
                                                finish()
                                            }
                                        }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.back_chevron),
                                                contentDescription = null
                                            )
                                        }
                                    }, title = {},
                                    backgroundColor = Color.Transparent,
                                    contentColor = Color.White
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(140.dp)
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(1f),
                                    )
                                )
                            )
                    )
                    if(!isImageExpanded){
                        FloatingTopButtons(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 16.dp, top = 16.dp)
                                .systemBarsPadding(),
                            onCart = { /*TODO*/ },
                            onFavorite = { /*TODO*/ }
                        )
                        when(viewModel.isProductAlreadyInCart){
                            true -> AddedToCart(modifier = Modifier.align(Alignment.BottomCenter))

                            false -> FloatingBottomButtons(modifier = Modifier.align(Alignment.BottomCenter),
                                onAddToCart = {
                                    viewModel.addItem()
                                },
                                onMultiplierChanged = {
                                    if(it in 1..100) viewModel.multiplier = it
                                },
                                multiplier =  viewModel.multiplier)
                        }
                    }

                }

            }
        }
    }

    @Composable
    fun AddedToCart(modifier: Modifier) {
        Box(modifier = modifier
            .background(
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = accentGreen
            )){
            Row(modifier = Modifier
                .navigationBarsPadding()
                .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically){

                Text(text = "x${viewModel.multiplier}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                Text("Added to cart",color = Color.White,fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center,modifier = Modifier.weight(1f))

                Text(viewModel.product?.price.toString(),color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }

    }
    @Composable
    fun FloatingTopButtons(modifier: Modifier, onCart:()-> Unit, onFavorite:()->Unit) {
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color = Color.White, shape = CircleShape)
                    .clickable { onCart() }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.drawer_cart),
                    contentDescription = null,
                    tint = productAddToCart,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp)
                )
            }

            Spacer(Modifier.padding(top = 16.dp))

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color = Color.White, shape = CircleShape)
                    .clickable { onFavorite() }
            ) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp)
                )
            }

        }
    }
    @Composable
    fun FloatingBottomButtons(modifier: Modifier, multiplier:Int, onMultiplierChanged: (Int) -> Unit, onAddToCart:() -> Unit) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(110.dp)
                .padding(26.dp), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(30.dp))
                    .background(productMultiplier, shape = RoundedCornerShape(30.dp))
                    .padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(color = Color.White, shape = CircleShape)
                        .clickable { onMultiplierChanged(multiplier - 1) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = null,
                        tint = Color.Black.copy(0.9f),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    )
                }
                Text(multiplier.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(color = Color.White, shape = CircleShape)
                        .clickable { onMultiplierChanged(multiplier + 1) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.Black.copy(0.9f),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(30.dp))
                    .background(productAddToCart, shape = RoundedCornerShape(30.dp))
                    .clickable { onAddToCart() }
                    .padding(horizontal = 22.dp), contentAlignment = Alignment.Center
            ) {
                Text("Add to cart", color = Color.White.copy(0.8f), fontWeight = FontWeight.Bold)
            }
        }
    }

    @Composable
    private fun BottomSheet() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 30.dp, start = 22.dp, end = 22.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                viewModel.product?.price?.let {
                    Text(
                        String.format("$%.2f", it),
                        fontWeight = FontWeight.Bold,
                        color = Color.Red,
                        fontSize = 20.sp
                    )
                }
                viewModel.product?.category?.let {
                    Text(it, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
            Spacer(modifier = Modifier.padding(top = 10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                viewModel.product?.brand?.let {
                    Text(it, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.padding(end = 12.dp))
                viewModel.product?.weight?.let {
                    Text(String.format("%.2f g", it))
                }
            }

            Spacer(modifier = Modifier.padding(top = 10.dp))
            viewModel.product?.name?.let {
                Text(it, fontWeight = FontWeight.SemiBold, color = Color.Red, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.padding(top = 10.dp))
            Row(modifier = Modifier.fillMaxWidth(0.8f)) {
                viewModel.product?.category?.let { category ->
                    Text(
                        category,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Text(
                    when (viewModel.product?.thc == null) {
                        true -> ""
                        false -> String.format("THC: %.0f%%", viewModel.product?.thc)
                    } +
                            when (viewModel.product?.thc != null && viewModel.product?.cbd != null) {
                                true -> " | "
                                false -> ""
                            } +
                            when (viewModel.product?.cbd == null) {
                                true -> ""
                                false -> String.format("CBD: %.0f%%", viewModel.product?.cbd)
                            },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                    color = Color.White.copy(0.86f)
                )
            }

            Spacer(modifier = Modifier.padding(top = 20.dp))
            viewModel.product?.description?.let {
                Text(it, color = Color.White.copy(0.7f))
            }
            Spacer(modifier = Modifier.padding(top = 140.dp)) //save space for floating buttons (add to cart and multiplier)
        }
    }
}