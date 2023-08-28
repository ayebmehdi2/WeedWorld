package com.itshedi.weedworld.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.itshedi.weedworld.ui.theme.accentGreen


@Composable
fun PostLikesCommentsCounter(likes: Int, comments: Int, onLike: (() -> Unit)?=null,onShare: (() -> Unit)?=null, height: Dp, fontSize:TextUnit,
isLiked: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {


        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = when(isLiked) {
                    true -> Icons.Filled.Favorite
                    false -> Icons.Outlined.FavoriteBorder
                                            },
                contentDescription = null,
                tint = Color.Red,
                modifier = when(onLike){
                    null -> Modifier.size(height)
                    else -> Modifier
                        .size(height)
                        .clip(CircleShape)
                        .clickable { onLike() }
                }
            )
            Spacer(modifier = Modifier.padding(end = 4.dp))
            Text(text = likes.toString(), color = Color.LightGray, fontSize = fontSize)
        }

        Spacer(modifier = Modifier.padding(end = 10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
        ) {
            Icon(
                painter = painterResource(id = com.itshedi.weedworld.R.drawable.comment),
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(height)
            )
            Spacer(modifier = Modifier.padding(end = 4.dp))
            Text(text = comments.toString(), color = Color.LightGray, fontSize = fontSize)
        }
        onShare?.let {
            Spacer(modifier = Modifier.padding(end = 10.dp))
            Box(
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = accentGreen,
                    modifier = Modifier.align(Alignment.CenterEnd).size(height)
                        .clip(CircleShape)
                        .clickable {
                            it.invoke()
                        }
                )
                Spacer(modifier = Modifier.padding(end = 4.dp))
            }
        }

    }
}