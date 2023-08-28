package com.itshedi.weedworld.ui.cart

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.itshedi.weedworld.entities.CartItem
import com.itshedi.weedworld.entities.Product
import com.itshedi.weedworld.entities.Store
import com.itshedi.weedworld.ui.checkout.CheckoutActivity
import com.itshedi.weedworld.ui.business_page.BusinessPageActivity
import com.itshedi.weedworld.ui.posts.product_post.ProductActivity
import com.itshedi.weedworld.ui.theme.cardBackground
import com.itshedi.weedworld.ui.theme.cartBackgroundGreen
import com.itshedi.weedworld.ui.theme.productAddToCart


@Composable
fun CartScreen(viewModel: CartViewModel) {


    LaunchedEffect(true) {
        viewModel.getCartItems()
    }
    Box(modifier = Modifier.fillMaxSize()) {
        if (viewModel.items.isEmpty()) {
            Text(
                "Your cart is empty",
                fontSize = 18.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {

            val context = LocalContext.current
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(cartBackgroundGreen)
                    .padding(horizontal = 16.dp)
            ) {

                item { Spacer(modifier = Modifier.padding(top = 8.dp)) }
                itemsIndexed(viewModel.items.groupBy { it.storeId }.keys.toList()) { _, storeId ->
                    viewModel.items.groupBy { it.storeId }[storeId]?.let {
                        LaunchedEffect(true) {
                            viewModel.getStoreById(storeId)
                        }
                        CartOrder(
                            cartItems = it,
                            store = viewModel.stores.getOrDefault(storeId, null),
                            onCheckout = { storeId -> viewModel.deleteCheckedOutItems(storeId)
                            },
                            onRemove = { productId -> viewModel.deleteItem(productId) },
                            onClick = { productId ->
                                viewModel.products.getOrDefault(productId, null)?.let { product ->
                                    val intent = Intent(context, ProductActivity::class.java)
                                    val bundle = Bundle()
                                    bundle.putParcelable("product", product)
                                    intent.putExtra("bundle", bundle)
                                    context.startActivity(intent)
                                }
                            },
                            setCount = { productId, count -> viewModel.setCount(productId, count) },
                            products = viewModel.products,
                            loadProduct = { productId -> viewModel.getProductById(productId) },
                            storeId = storeId, // add store id

                        )
                        Spacer(Modifier.padding(top = 10.dp))
                    }
                }
                item { Spacer(modifier = Modifier.padding(top = 100.dp)) }
            }
        }
    }

}


@Composable
fun CartOrder(
    cartItems: List<CartItem>,
    store: Store?,
    onCheckout: (String) -> Unit,
    onRemove: (String) -> Unit,
    onClick: (String) -> Unit,
    products: Map<String, Product>,
    setCount: (String, Int) -> Unit,
    loadProduct: (String) -> Unit,
    storeId: String?
) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(8.dp)) {
            // store banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .clickable {
                        store?.storeId?.let {
                            val intent = Intent(context, BusinessPageActivity::class.java)
                            val bundle = Bundle()
                            bundle.putString("storeId", it)
                            bundle.putBoolean("isMyStore", false)
                            intent.putExtra("bundle", bundle)
                            context.startActivity(intent)
                        }
                    }
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberImagePainter(data = store?.photo), contentDescription = null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(40.dp)
                )
                Text(
                    store?.businessName ?: "",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    overflow = TextOverflow.Ellipsis
                )
            }

            // products


            cartItems.forEach { item ->
                LaunchedEffect(true) {
                    loadProduct(item.productId)

                }
                Spacer(
                    modifier = Modifier
                        .padding(top = 8.dp)
                )
                CartProduct(
                    product = products.getOrDefault(item.productId, null),
                    count = item.count,
                    onRemove = { onRemove(item.productId) },
                    onClick = {
                        onClick(item.productId)
                    },
                    onSetCount = { setCount(item.productId, it) })
            }
            Spacer(modifier = Modifier.padding(top = 20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)

            ) {
                Text(
                    "Total Price",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (cartItems.all { products[it.productId] != null }) { // if all cart items done loading
                    with(cartItems.sumOf { //get either discount price or price and multiply by count
                        (products[it.productId]!!.discountPrice
                            ?: products[it.productId]!!.price!!) * it.count
                    }) {
                        Text(
                            text = String.format(
                                "$%.2f",
                                this,
                            ),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.padding(top = 20.dp))



            Box(

                modifier = Modifier
                    .align(CenterHorizontally)
                    .fillMaxWidth(0.6f)
                    .padding(bottom = 20.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(productAddToCart, shape = RoundedCornerShape(30.dp))
                    .clickable {

                        // Create a map to store product details (name, weight, price) and total prices by product ID
                        val productDetailsAndPrices: MutableMap<String, Triple<String, Double, Double>> =
                            mutableMapOf()

                        var businessName: String? = ""
                        // Create a list to store product IDs
                        val productIdsList: MutableList<String> = mutableListOf()

                        // Iterate through the cart items and populate the map and list
                        cartItems
                            .filter { it.storeId == storeId }
                            .forEach { item ->
                                val product = products[item.productId] ?: return@forEach
                                val productName = product.name ?: ""
                                val productId = product.productId ?: ""
                                productIdsList.add(productId)
                                businessName = store?.businessName ?: ""
                                val productWeight = product.weight ?: 0.0
                                val productPrice = product.discountPrice ?: product.price ?: 0.0
                                val currentDetailsAndPrice = productDetailsAndPrices[productName]
                                if (currentDetailsAndPrice != null) {
                                    // Update existing entry if it already exists
                                    val updatedDetailsAndPrice = Triple(
                                        productName,
                                        productWeight,
                                        currentDetailsAndPrice.third + (productPrice * item.count)
                                    )
                                    productDetailsAndPrices[productName] = updatedDetailsAndPrice
                                } else {
                                    // Create a new entry if it doesn't exist
                                    productDetailsAndPrices[productName] =
                                        Triple(
                                            productName,
                                            productWeight,
                                            productPrice * item.count
                                        )
                                }
                            }

// Create formatted strings for product names and weights, and for prices
                        val formattedResultNandW =
                            productDetailsAndPrices.entries.joinToString("\n") { (_, detailsAndPrice) ->
                                val (productName, productWeight, _) = detailsAndPrice
                                "$productName  $productWeight g"
                            }

                        val formattedResultP =
                            productDetailsAndPrices.entries.joinToString("\n") { (_, detailsAndPrice) ->
                                val (_, _, totalPrice) = detailsAndPrice
                                "$totalPrice"
                            }
                        // Create a formatted string for product IDs list
                        val formattedProductIdsList = productIdsList.joinToString("\n")


                        // Calculate and display total price for the store
                        val totalPrice = cartItems
                            .filter { it.storeId == storeId }
                            .groupBy { it.productId }
                            .values
                            .sumOf { productItems ->
                                val product = products[productItems.first().productId]!!
                                val productTotalPrice = productItems.sumOf { it.count.toDouble() } *
                                        (product.discountPrice ?: product.price!!)
                                productTotalPrice
                            }

                        //  Text("Total Price for Store $storeId: $totalPrice")

                        // Start the CheckoutActivity
                        val intent = Intent(context, CheckoutActivity::class.java)
                        intent.putExtra("storeId", storeId)
                        intent.putExtra("totalPrice", "$totalPrice")
                        intent.putExtra("formattedResultNandW", formattedResultNandW)
                        intent.putExtra("formattedProductIdsList", formattedProductIdsList)
                        intent.putExtra("formattedResultP", formattedResultP)
                        intent.putExtra("businessName", businessName)
                        context.startActivity(intent)

                        onCheckout(storeId.toString())

                    }
                    .padding(horizontal = 30.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Check Out", color = Color.White.copy(0.9f), fontWeight = FontWeight.Bold)
            }
        }
    }


}


@Composable
fun CartProduct(
    product: Product?,
    count: Int,
    onRemove: () -> Unit,
    onClick: () -> Unit,
    onSetCount: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBackground)
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        // product info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)

        ) {
            // product image
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Gray)
            ) {
                product?.let {
                    Image(
                        painter = rememberImagePainter(data = it.images[0]),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            //product text infos
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        product?.name ?: "",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        color = Color.White,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onRemove() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        product?.category ?: "",
                        fontSize = 14.sp,
                        maxLines = 1,
                        color = Color.White,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        when (product?.thc == null) {
                            true -> ""
                            false -> String.format("THC: %.0f%%", product?.thc)
                        } +
                                when (product?.thc != null && product.cbd != null) {
                                    true -> " | "
                                    false -> ""
                                } +
                                when (product?.cbd == null) {
                                    true -> ""
                                    false -> String.format("CBD: %.0f%%", product?.cbd)
                                },
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
                product?.weight?.let {
                    Text(
                        text = String.format("%.1fg", it),
                        fontSize = 14.sp,
                        maxLines = 1,
                        color = Color.White,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format(
                            "$%.2f",
                            when (product?.discountPrice) {
                                null -> product?.price
                                else -> product.discountPrice
                            },
                        ),
                        color = Color.Red,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    color = when (count == 1) {
                                        true -> Color.LightGray
                                        else -> Color.Red
                                    }, shape = CircleShape
                                )
                                .clickable(enabled = count > 1) { onSetCount(count - 1) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = null,
                                tint = when (count == 1) {
                                    true -> Color.Black.copy(0.9f)
                                    else -> Color.White
                                },
                                modifier = Modifier
                                    .size(22.dp)
                                    .padding(3.dp)
                            )
                        }
                        Text(
                            count.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(color = Color.Red, shape = CircleShape)
                                .clickable { onSetCount(count + 1) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .size(22.dp)
                                    .padding(3.dp)
                            )
                        }
                    }
                }

            }
        }

    }


}