package com.itshedi.weedworld.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Feed
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.itshedi.weedworld.ui.theme.accentGreen

@Composable
fun TopNavigation(onMenuClicked: () -> Unit, icon: Painter) {
    TopAppBar(elevation = 0.dp,contentColor = Color.Black, backgroundColor = Color.White, navigationIcon = {
        IconButton(onClick = {
            onMenuClicked()
        }) {
            Icon(imageVector = Icons.Default.Menu, contentDescription = null,
                modifier = Modifier.size(30.dp))
        }
    }, title = {


        Column(
            modifier = Modifier.fillMaxWidth().padding(end = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = accentGreen,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.padding(top = 3.dp))
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(4.dp)
                    .background(color = Color.Red, shape = RoundedCornerShape(2.dp))
            )
        }
    },
//        actions = {
//            IconButton(onClick = { /*TODO*/ }) {
//                Icon(imageVector = Icons.Outlined.Search, contentDescription = null,
//                    modifier = Modifier.size(30.dp))
//            }
//        }
    )
}