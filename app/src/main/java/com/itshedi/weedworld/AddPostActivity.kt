package com.itshedi.weedworld

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.calculateCurrentOffsetForPage
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.itshedi.weedworld.ui.dialogs.ProgressDialog
import com.itshedi.weedworld.ui.posts.product_post.ProductViewModel
import com.itshedi.weedworld.ui.theme.AddPostOptionColor
import com.itshedi.weedworld.utils.getAspectRatio
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


@AndroidEntryPoint
class AddPostActivity : ComponentActivity() {

    private val imageAnalyzer by lazy {
        ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this)[AddPostViewModel::class.java]

        val bundle = intent.getBundleExtra("bundle")

        //if not null its a business page post
        if (bundle != null && bundle.containsKey("storeId")) {
            viewModel.storeId = bundle.getString("storeId")
        }

        setContent {

            var snackbarMessage by remember { mutableStateOf<String?>(null) }
            var cs = rememberCoroutineScope()
            val state = viewModel.eventFlow.collectAsState(AddPostEvent.empty).value
            if (state is AddPostEvent.loading) {
                ProgressDialog()
            }
            LaunchedEffect(state) {
                when (state) {
                    is AddPostEvent.showSnackbar -> {
                        snackbarMessage = state.message
                    }
                    is AddPostEvent.postAdded -> {
                        cs.launch {
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }

                    else -> {

                    }
                }
            }


            var isPrivate by remember { mutableStateOf(false) }
            var addTextValue by remember { mutableStateOf<String>("") }

            var imageUri by remember { mutableStateOf<Uri?>(null) }
            var imageRatio by remember { mutableStateOf<Float>(1f) }
            val context = LocalContext.current
            val imagePicker =
                rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                    uri?.let {
                        imageUri = it
                        imageRatio = getAspectRatio(uri, context)
                    }
                }

            Box(modifier = Modifier.fillMaxSize()) {

                when (viewModel.addPostMethodIndex) {
//                    0 -> AddFromCamera(viewModel = viewModel)
                    1 -> AddFromGallery(uri = imageUri)
                    2 -> AddText(addTextValue, onValueChange = {
                        addTextValue = it
                    })
                }

                Column(
                    modifier = Modifier
                        .systemBarsPadding()
                        .padding(bottom = 50.dp)
                        .align(Alignment.BottomCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)){
                        if (viewModel.addPostMethodIndex == 1) {
                            PickFromGalleryButton(modifier = Modifier.align(Center)) {
                                imagePicker.launch(arrayOf("*/*"))
                            }
                        }

                        SetPrivateButton(modifier = Modifier.align(CenterEnd), onClick = { isPrivate = !isPrivate }, isPrivate = isPrivate)
                    }
                    

                    Spacer(modifier = Modifier.padding(bottom = 24.dp))

                    AddPostMethodSelector(modifier = Modifier, onOptionChanged = {
                        viewModel.addPostMethodIndex = it
                    })
                }
                Box(
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(0.6f), Color.Transparent
                                )
                            )
                        )
                )
                TopAppBar(elevation = 0.dp,
                    modifier = Modifier.systemBarsPadding(),
                    navigationIcon = {
                        IconButton(onClick = {
                            setResult(Activity.RESULT_CANCELED)
                            finish()
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack, contentDescription = null
                            )
                        }
                    },
                    actions = {
                        if (viewModel.addPostMethodIndex in 1..2) {
                            TextButton(
                                onClick = {
                                    when (viewModel.addPostMethodIndex) {
                                        1 -> imageUri?.let { viewModel.addPost(it, imageRatio, isPrivate) }
                                        2 -> if (addTextValue.isNotBlank()) viewModel.addPost(
                                            addTextValue, isPrivate
                                        )
                                    }
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                )
                                Text("Submit")
                            }

                        }
                    },
                    title = {},
                    backgroundColor = Color.Transparent,
                    contentColor = Color.White
                )
            }
        }
    }


    @SuppressLint("RestrictedApi")
    private fun startCamera(
        cameraPreview: PreviewView,
        onFocus: (Offset) -> Unit,
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val preview = androidx.camera.core.Preview.Builder().build()

        cameraProviderFuture.addListener(
            Runnable {
                preview.setSurfaceProvider(cameraPreview.surfaceProvider)
                cameraProviderFuture.get().bind(preview, imageAnalyzer)
            }, ContextCompat.getMainExecutor(this)
        )

        cameraPreview.setOnTouchListener { view: View, motionEvent: MotionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> return@setOnTouchListener true
                MotionEvent.ACTION_UP -> {
                    val factory = cameraPreview.meteringPointFactory

                    val point = factory.createPoint(
                        motionEvent.x, motionEvent.y
                    )

                    val action = FocusMeteringAction.Builder(
                        point
                    ).build()

                    preview.camera?.cameraControl?.startFocusAndMetering(
                        action
                    )
                    view.performClick()
                    onFocus(Offset(motionEvent.x, motionEvent.y))
                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }

    }

    private fun ProcessCameraProvider.bind(
        preview: androidx.camera.core.Preview, imageAnalyzer: ImageAnalysis
    ) = try {
        unbindAll()
        bindToLifecycle(
            this@AddPostActivity, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer
        )
    } catch (ise: IllegalStateException) {
        // Thrown if binding is not done from the main thread
//        Log.e(TAG, "Binding failed", ise)
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun AddFromCamera(viewModel: AddPostViewModel) {
        val permissionsState = rememberMultiplePermissionsState(
            permissions = listOf(
                Manifest.permission.CAMERA,
            )
        )
        val lifecycleOwner = LocalLifecycleOwner.current

        DisposableEffect(key1 = lifecycleOwner, effect = {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START) {
                    permissionsState.launchMultiplePermissionRequest()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        })
        permissionsState.permissions.forEach { perm ->
            when (perm.permission) {
                Manifest.permission.CAMERA -> {
                    val systemUiController = rememberSystemUiController()
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent, darkIcons = false
                    )
                    CameraScreen(onPreviewView = {
                        startCamera(it, onFocus = { offset ->
                            viewModel.cameraFocusPoint = offset
                        })
                    },
                        viewModel = viewModel,
                        permissionState = (when {
                            perm.status.isGranted -> 0
                            perm.status.shouldShowRationale -> 1
                            !(perm.status.shouldShowRationale) && !(perm.status.isGranted) -> 2
                            else -> 1
                        }),
                        onRequestPermission = { permissionsState.launchMultiplePermissionRequest() })
                }
            }
        }
    }
}

@Composable
fun AddText(value: String, onValueChange: (String) -> Unit) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = Color.Transparent, darkIcons = false
    )

    val focusManager = LocalFocusManager.current

    val interactionSource = remember { MutableInteractionSource() }
    Box(modifier = Modifier
        .fillMaxSize()
        .clickable(
            interactionSource = interactionSource,
            indication = null
        ) {
            focusManager.clearFocus()
        }) {

        Image(
            painter = painterResource(id = R.drawable.addpostbackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (value.isBlank()) {
            Text(
                text = "Type something here..",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .align(Alignment.Center),
                color = Color.Black.copy(0.4f),
                style = TextStyle(
                    fontSize = 22.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold
                )
            )
        }

        BasicTextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .align(Alignment.Center),
            textStyle = TextStyle(
                fontSize = 22.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold
            ),

            )
    }
}


@Composable
fun AddFromGallery(uri: Uri?) {

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = Color.Transparent, darkIcons = false
    )
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.addpostbackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        uri?.let {
            Image(
                painter = rememberImagePainter(data = it),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}


@Composable
fun SetPrivateButton(modifier: Modifier, onClick: () -> Unit, isPrivate:Boolean) {

    Box(modifier = modifier
        .clip(CircleShape)
        .background(Color.Black.copy(0.4f))
        .clickable { onClick() }
        .padding(8.dp)) {
        Icon(
            imageVector = when(isPrivate){
                                         true -> Icons.Outlined.PublicOff
                                         false -> Icons.Outlined.Public
                                         },
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .align(Center)
                .size(20.dp)
        )
    }
}


@Composable
fun PickFromGalleryButton(modifier: Modifier, onClick: () -> Unit) {

    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(0.4f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)) {
        Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .padding(end = 4.dp)
                .size(20.dp)
        )
        Text(text = "Pick from gallery", color = Color.White)
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun AddPostMethodSelector(modifier: Modifier, onOptionChanged: (Int) -> Unit) {

    val cs = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 1)
    HorizontalPager(
        modifier = modifier,
        count = 3,
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 160.dp),
        userScrollEnabled = false
    ) { index ->
        Box(modifier = Modifier
            .graphicsLayer {
                val pageOffset = calculateCurrentOffsetForPage(index).absoluteValue

                lerp(
                    start = 0.70f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f)
                ).also { scale ->
                    scaleX = scale
                    scaleY = scale
                }

                alpha = lerp(
                    start = 0.3f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f)
                )
            }
            .size(80.dp)
            .background(shape = CircleShape, color = AddPostOptionColor)
            .clip(CircleShape)
            .clickable(enabled = index != pagerState.currentPage) {
                cs.launch {
                    onOptionChanged(index)
                    pagerState.animateScrollToPage(index)
                }
            }
            .padding(16.dp)) {
            Icon(
                imageVector = (when (index) {
                    0 -> Icons.Outlined.PhotoCamera
                    1 -> Icons.Outlined.Image
                    else -> Icons.Outlined.ShortText
                }), contentDescription = null, modifier = Modifier.fillMaxSize(), tint = Color.White
            )

        }
    }


}


