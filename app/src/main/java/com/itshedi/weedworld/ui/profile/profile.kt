package com.itshedi.weedworld.ui.profile

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.itshedi.weedworld.AddPostActivity
import com.itshedi.weedworld.entities.Post
import com.itshedi.weedworld.ui.PostsGrid
import com.itshedi.weedworld.ui.auth.DefaultSnackbar
import com.itshedi.weedworld.ui.dialogs.ProgressDialog
import com.itshedi.weedworld.ui.navigation.Screens
import com.itshedi.weedworld.ui.theme.accentGreen
import com.itshedi.weedworld.utils.pagerTabIndicatorOffset
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


//note: outer navigation
@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = Color.Transparent,
        darkIcons = false
    )

    LaunchedEffect(true) {
        viewModel.reloadUserInfo()
    }


    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    Log.i("kiki", "OK RECEIVED")
                    viewModel.getMyPosts()
                    viewModel.getMyPosts(true)
                }
                Activity.RESULT_CANCELED -> {
                }
            }
        }

    val updateProfilePhoto =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                viewModel.updateProfilePhoto(uri)
            }
        }
    val coverImagePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                viewModel.updateCoverImage(uri)
            }
        }

    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val state = viewModel.eventFlow.collectAsState(ProfileEvent.empty).value
    if (state is ProfileEvent.loading) {
        ProgressDialog()
    }
    LaunchedEffect(state) {
        when (state) {
            is ProfileEvent.showSnackbar -> {
                snackbarMessage = state.message
            }
            is ProfileEvent.empty -> {
                Log.i("cooltag", "empty")
            }
            is ProfileEvent.loading -> {
                Log.i("cooltag", "loading")
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val intent = Intent(context, AddPostActivity::class.java)
                launcher.launch(intent)
            }, modifier = Modifier.systemBarsPadding()) {
                Icon(imageVector = Icons.Outlined.AddAPhoto, contentDescription = null)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize(
            )
        ) {
            viewModel.user?.let { currentUser ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // background cover image
                        Image(
                            painter = rememberImagePainter(data = viewModel.user?.coverPhoto),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                        )
                        Box(
                            modifier = Modifier
                                .height(260.dp)
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
                        Column(modifier = Modifier.fillMaxWidth()) {
                            TopAppBar(
                                elevation = 0.dp,
                                modifier = Modifier.systemBarsPadding(),
                                navigationIcon = {
                                    IconButton(onClick = {
                                        navController.navigateUp()
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = null
                                        )
                                    }
                                }, actions = {
//                                    IconButton(onClick = {
//                                        val intent = Intent(context, ChatActivity::class.java)
//                                        val bundle = Bundle()
//                                        bundle.putString("userId", "CCgBoM6P27Mg9kEkQFcAl2Wxwcr1")
//                                        intent.putExtra("bundle", bundle)
//                                        context.startActivity(intent)
//                                    }) {
//                                        Icon(painter = painterResource(id = R.drawable.bottom_nav_chat), contentDescription = null)
//                                    }
                                    IconButton(onClick = { navController.navigate(Screens.EditProfile.route) }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null
                                        )
                                    }
                                }, title = {},
                                backgroundColor = Color.Transparent,
                                contentColor = Color.White
                            )


                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(color = Color.Gray, shape = CircleShape)
                                            .border(
                                                width = 2.dp,
                                                color = Color.White,
                                                shape = CircleShape
                                            )
                                    ) {
                                        Image(
                                            painter = rememberImagePainter(data = viewModel.user?.profilePhoto),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop
                                        )
                                    }
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
                                Spacer(modifier = Modifier.padding(end = 20.dp))
                                Column(modifier = Modifier.padding(top = 4.dp)) {
                                    Text(
                                        currentUser.username.toString(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        currentUser.uid.toString(),
                                        color = Color.White.copy(0.8f),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        currentUser.birthday.toString(),
                                        color = Color.White.copy(0.8f),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        currentUser.location.toString(),
                                        color = Color.White.copy(0.8f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
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
                            Text("Edit cover", fontSize = 12.sp, color = Color.White)
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

                    ProfileSections(onSectionClick = {})
                    ProfileBio(text = "Creating a Life") {
                    }
                    Spacer(Modifier.padding(top = 8.dp))
                    ProfileContentPager(
                        myposts = viewModel.allMyPosts,
                        privatePosts = viewModel.allMyPrivatePosts,
                        downloadUrl = {
//                            viewModel.pathToDownloadUrl(it) note:old way of doing it
                            it
                        },
                        onLoadMyPosts = {
                            viewModel.getMyPosts()
                        },
                        onLoadPrivatePosts = {
                            viewModel.getMyPosts(private = true)
                        },
                        isLoadingPosts = viewModel.isLoadingPosts,
                        isLoadingPrivatePosts = viewModel.isLoadingPrivatePosts,
                        likes = viewModel.likes,
                        comments = viewModel.comments,
                        privateLikes = viewModel.privateLikes,
                        privateComments = viewModel.privateComments,
                        getLikesAndComments = { index, private ->
                            viewModel.getLikesAndComments(
                                index,
                                private
                            )
                        }
                    )
                }
            }
            if (snackbarMessage != null) {
                DefaultSnackbar(
                    message = snackbarMessage ?: "", onDismiss = {
                        scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .systemBarsPadding()
                )
                LaunchedEffect(true) {
                    delay(2000)
                    snackbarMessage = null
                }
            }
        }


    }

}


@OptIn(ExperimentalPagerApi::class)
@Composable
fun ProfileContentPager(
    myposts: List<Post>,
    privatePosts: List<Post>,
    downloadUrl: suspend (String) -> String,
    onLoadMyPosts: () -> Unit,
    onLoadPrivatePosts: () -> Unit,
    isLoadingPosts: Boolean,
    isLoadingPrivatePosts: Boolean,
    likes: List<Int?>,
    comments: List<Int?>,
    privateLikes: List<Int?>,
    privateComments: List<Int?>,
    getLikesAndComments: (Int, Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxSize()) {
        val pagerState = rememberPagerState()

        TabRow(
            // Our selected tab is our current page
            selectedTabIndex = pagerState.currentPage,
            // Override the indicator, using the provided pagerTabIndicatorOffset modifier
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier
                        .pagerTabIndicatorOffset(pagerState, tabPositions)
                        .padding(horizontal = 34.dp)
                        .align(CenterHorizontally)
                        .clip(RoundedCornerShape(4.dp)),
                    height = 4.dp,
                    color = Color.Red,
                )
            },
            backgroundColor = Color.Transparent,
            contentColor = Color.White,
        ) {
            // Add tabs for all of our pages
            listOf(
                Icons.Outlined.Dashboard, Icons.Outlined.Lock, Icons.Outlined.PersonOutline
            ).forEachIndexed { index, icon ->
                Tab(
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.Black.takeIf { pagerState.currentPage == index }
                                ?: Color.LightGray
                        )
                    },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        if (pagerState.currentPage != index) {
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        }
                    },
                )
            }
        }



        HorizontalPager(
            count = 3,
            state = pagerState,
        ) { page ->
            when (page) {
                0 -> { //Dash
                    LaunchedEffect(Unit) {
                        onLoadMyPosts()
                    }
                    PostsGrid(
                        modifier = Modifier.fillMaxSize(),
                        Posts = myposts,
                        downloadUrl = downloadUrl,
                        isLoading = isLoadingPosts,
                        onRefresh = {
                            onLoadMyPosts()
                        },
                        likes = likes,
                        comments = comments,
                        getLikesAndComments = { getLikesAndComments(it, false) })
                }
                1 -> { //Private
                    LaunchedEffect(Unit) {
                        onLoadPrivatePosts()
                    }
                    PostsGrid(
                        modifier = Modifier.fillMaxSize(),
                        Posts = privatePosts,
                        downloadUrl = downloadUrl,
                        isLoading = isLoadingPrivatePosts,
                        onRefresh = {
                            onLoadPrivatePosts()
                        },
                        likes = privateLikes,
                        comments = privateComments,
                        getLikesAndComments = { getLikesAndComments(it, true) })
                }
                2 -> { //Tagged Posts

                }
            }
        }
    }
}

@Composable
fun ProfileSections(onSectionClick: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        listOf(
            Pair(Icons.Outlined.ChatBubbleOutline, "Groups"),
            Pair(Icons.Outlined.ShoppingCart, "Store"),
            Pair(Icons.Outlined.Diamond, "Paid content"),
        ).forEachIndexed { index, (icon, label) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                    .clickable { onSectionClick(index) }
                    .padding(vertical = 10.dp),
                horizontalAlignment = CenterHorizontally
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = Color.LightGray)
                Spacer(modifier = Modifier.padding(top = 4.dp))
                Text(text = label, color = Color.LightGray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ProfileBio(text: String, onEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 20.dp)
    ) {
        Text(
            text = text, modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            fontSize = 12.sp,
            fontStyle = FontStyle.Italic
        )
        Box(modifier = Modifier) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .border(
                        width = 2.dp,
                        shape = RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp,
                            bottomEnd = 8.dp
                        ),
                        color = accentGreen
                    )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier
                        .align(
                            Alignment.Center
                        )
                        .size(16.dp),
                    tint = accentGreen
                )
            }
        }
    }
}
