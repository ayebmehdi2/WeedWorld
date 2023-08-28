package com.itshedi.weedworld

import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.ShortText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.calculateCurrentOffsetForPage
import com.google.accompanist.pager.rememberPagerState
import com.itshedi.weedworld.ui.theme.AddPostOptionColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CameraScreen(
    permissionState: Int, // 0 granted 1 denied once 2 perma denied
    onRequestPermission: () -> Unit,
    onPreviewView: (PreviewView) -> Unit,
    viewModel: AddPostViewModel,
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val cs = rememberCoroutineScope()
    BackHandler(scaffoldState.bottomSheetState.isExpanded) {
        cs.launch { scaffoldState.bottomSheetState.collapse() }
    }

  //  val densityValue = LocalDensity.current
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            Modifier
                .fillMaxSize()
        ) {


            when (permissionState) {
                0 -> { // note: Permission granted
                    AndroidView(modifier = Modifier.matchParentSize(),
                        factory = { context ->
                            val previewView =
                                PreviewView(context).apply {
                                    this.scaleType =
                                        scaleType
                                    layoutParams =
                                        ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                }

                            CoroutineScope(Dispatchers.IO).launch {
                                onPreviewView(previewView)
                            }
                            previewView
                        })


                    LaunchedEffect(viewModel.cameraFocusPoint) {
                        viewModel.cameraFocusPoint?.let {

                            delay(500)
                            viewModel.cameraFocusPoint = null
                        }
                    }
                    CameraCanvas(
                        modifier = Modifier
                            .fillMaxSize(),
                        cameraFocusPoint = viewModel.cameraFocusPoint
                    )
                }
                1 -> { // note: Can request again
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .align(Alignment.Center)
                            .clickable {
                                onRequestPermission()
                            }, horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Text("Camera permission is needed", textAlign = TextAlign.Center)
                    }

                }
                2 -> { // note: Perma denied
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Text(
                            "Permision permanently denied. Enable it in device settings",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

        }
    }
}



@Composable
fun CameraCanvas(
    modifier: Modifier,
    cameraFocusPoint: Offset? = null
) {
    val radius = if (cameraFocusPoint == null) 46f else 56f
    val animatedRadius = animateFloatAsState(
        radius,
        animationSpec = tween(
            durationMillis = 300, easing = LinearEasing
        )
    )
    Canvas(
        modifier = modifier,
    ) {
        cameraFocusPoint?.let {
            drawCircle(
                color = Color.White,
                radius = animatedRadius.value,
                center = it,
                style = Stroke(width = 4f)
            )
        }
    }
}