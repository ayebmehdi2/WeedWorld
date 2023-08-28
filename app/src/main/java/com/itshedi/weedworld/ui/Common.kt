package com.itshedi.weedworld.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.itshedi.weedworld.entities.Product
import com.itshedi.weedworld.entities.Store
import com.itshedi.weedworld.ui.business_page.WeightPriceItem
import com.itshedi.weedworld.utils.productSpecies


@Composable
fun ConnectivityError(modifier: Modifier, onReload:()->Unit) {
    Box(modifier = modifier){
        Column(modifier = modifier.align(Alignment.Center)
            .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally){
            Text(text = "Unable to connect to internet", fontSize = 18.sp)
            TextButton(onClick = { onReload() }) {
                Text("Try again")
            }
        }
    }

}

@Composable
fun ProductItem(product:Product, onClick:()->Unit) {
    Box(
        modifier = Modifier
            .height(90.dp)
            .background(Color.White)
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp))
            .background(shape = RoundedCornerShape(10.dp), color = Color.White)
            .clickable { onClick() }) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 3.dp)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                ) {
                    Image(
                        painter = rememberImagePainter(data = product.images.firstOrNull()),
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
                    )

                }
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .weight(1f)
                ) {
                    Text(
                        product.brand ?: "Unknown brand",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.Gray
                    )
                    Text(
                        product.name ?: "Untitled product",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            product.specie ?: "",
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = productSpecies.firstOrNull{ it.name==product.specie}?.color?:Color.DarkGray
                        )
                        Text(
                            when(product.thc==null){true -> "" false -> String.format("THC: %.0f%%",product.thc)} +
                                    when(product.thc!=null && product.cbd!=null){true -> " | " false -> "" }+
                                    when(product.cbd==null){true -> "" false -> String.format("CBD: %.0f%%",product.cbd)},
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End,
                            color = Color.Gray
                        )
                    }
                }

                WeightPriceItem(
                    weight = product.weight,
                    price = product.price!!,
                    discountPrice = product.discountPrice
                )
            }
        }
    }
}

@Composable
fun StoreItem(store: Store, onClick: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxSize()
        .clip(RoundedCornerShape(10.dp))
        .background(shape = RoundedCornerShape(10.dp), color = Color.White)
        .clickable { onClick() }) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 3.dp)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            ) {
                Image(
                    painter = rememberImagePainter(data = store.photo),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                )

            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    store.businessType ?: "",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    store.businessName ?: "",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Gray
                )
                Text(
                    store.address ?: "",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Gray
                )
            }
        }
    }
}