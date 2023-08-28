package com.itshedi.weedworld.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.itshedi.weedworld.entities.Post
import com.itshedi.weedworld.ui.posts.user_post.UserPostActivity


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostsGrid(
    modifier: Modifier,
    Posts: List<Post>,
    downloadUrl: suspend (String) -> String,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    likes: List<Int?>,
    comments: List<Int?>,
    getLikesAndComments: (Int) -> Unit
) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        when(result.resultCode) {
            Activity.RESULT_OK -> {
                Log.i("kiki","OK RECEIVED")
                onRefresh()
            }
            Activity.RESULT_CANCELED -> {
            }
        }
    }

    SwipeRefresh(state = rememberSwipeRefreshState(isLoading), onRefresh = {
            onRefresh()
        },modifier = modifier) {
        Crossfade(targetState = isLoading) {
            when (it){
                true -> Box(Modifier)
                false -> LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2), modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) {
                    itemsIndexed(Posts) { index, post ->
                        var url by remember { mutableStateOf<String?>(null) }

                        PostItem(
                            post = post.copy(image = url),
                            onClick = {
                                val intent = Intent(context, UserPostActivity::class.java)
                                val bundle = Bundle()
                                bundle.putParcelable("post", post)
                                intent.putExtra("bundle", bundle)
                                launcher.launch(intent)
                            },
                            likes = likes[index]?:0,
                            comments = comments[index]?:0,
                        )

                        LaunchedEffect(true) {
                            Log.i("zzzz", url.toString())
                            getLikesAndComments(index)
                            post.image?.let {
                                url = downloadUrl(it)
                            }
                            Log.i("zzzz", url.toString())
                        }
                    }

                }
            }
        }

        }

}

@Composable
fun PostItem(post: Post?, onClick:()-> Unit, likes:Int, comments:Int) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(post?.aspectRatio ?: 1f)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.LightGray.copy(0.5f))
                .clickable { onClick() }
        ) {
            if (post?.image!=null){
                Image(
                    painter = rememberImagePainter(data = post.image),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }else if(post?.text !=null){
                Text(
                    text = post.text,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(8.dp),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )

            }

        }

        Spacer(modifier = Modifier.padding(top = 8.dp))
        post?.let {
            PostLikesCommentsCounter(likes = likes, comments = comments, height = 20.dp, fontSize = 12.sp, isLiked = false) //todo: fix this
        }
    }
}
