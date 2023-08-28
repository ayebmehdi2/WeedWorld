package com.itshedi.weedworld.ui.landing_slides

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoubleArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.itshedi.weedworld.ui.navigation.Screens
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun LandingSlides(navController: NavController) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = Color.Transparent,
        darkIcons = true
    )
    Box(modifier = Modifier.fillMaxSize()) {
        val pagerState = rememberPagerState()
        val coroutineScope = rememberCoroutineScope()
        HorizontalPager(
            count = 3,
            state = pagerState,
            userScrollEnabled = false
        ) { page ->
            when (page) {
                0 -> {
                    LandingSlide(
                        image = painterResource(id = com.itshedi.weedworld.R.drawable.one),
                        text = "Satisfy your cannabis curiosity",
                        textColor = Color.Black,
                        actionColor = Color.Gray,
                        onSkip = {
                            coroutineScope.launch { pagerState.animateScrollToPage(1) }
                        }
                    )

                }
                1 -> {
                    LandingSlide(
                        image = painterResource(id = com.itshedi.weedworld.R.drawable.two),
                        text = "Share your stories and experience",
                        textColor = Color(0xFFCD219D),
                        actionColor = Color.Black,
                        onSkip = {
                            coroutineScope.launch { pagerState.animateScrollToPage(2) }
                        }
                    )
                }
                2 -> {
                    LandingSlide(
                        image = painterResource(id = com.itshedi.weedworld.R.drawable.three),
                        text = "Connect with like-minded people",
                        textColor = Color(0xFF4F0027),
                        actionColor = Color.White,
                        onSkip = {
                            navController.navigate(Screens.AuthScreen.route) {
                                popUpTo(Screens.LandingScreen.route) {
                                    inclusive = true
                                }
                            }
                        }
                    )
                }
            }
        }

        MyIndicator(
            indicatorProgress = (pagerState.currentPage + 1).toFloat() / 3, modifier = Modifier
                .padding(top = 120.dp)
                .align(
                    Alignment.TopCenter
                )
        )
    }

}


@Composable
fun MyIndicator(indicatorProgress: Float, modifier: Modifier) {
    var progress by remember { mutableStateOf(0f) }
    val progressAnimDuration = 350
    val progressAnimation by animateFloatAsState(
        targetValue = indicatorProgress,
        animationSpec = tween(durationMillis = progressAnimDuration, easing = FastOutSlowInEasing)
    )
    LinearProgressIndicator(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp)), // Rounded edges
        progress = progressAnimation,
        color = Color(0xFF1F0A58),
        backgroundColor = Color.White
    )
    LaunchedEffect(indicatorProgress) {
        progress = indicatorProgress
    }
}

@Composable
fun LandingSlide(
    image: Painter,
    text: String,
    textColor: Color,
    actionColor: Color,
    onSkip: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = image, contentDescription = null, modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .padding(start = 30.dp)
                .fillMaxWidth(0.6f)
                .fillMaxHeight(0.5f)
        ) {
            Text(
                text = text, color = textColor, modifier = Modifier
                    .align(Alignment.BottomStart), fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.2f)
                .padding(start = 30.dp)
        ) {
            Row(modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onSkip() }) {
                Text(text = "Skip", color = actionColor)
                Spacer(modifier = Modifier.padding(end = 4.dp))
                Icon(
                    imageVector = Icons.Filled.DoubleArrow,
                    contentDescription = null,
                    tint = actionColor
                )
            }
        }
    }
}