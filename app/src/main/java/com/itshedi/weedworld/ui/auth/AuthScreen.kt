package com.itshedi.weedworld.ui.auth

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.itshedi.weedworld.ui.navigation.Screens
import com.itshedi.weedworld.ui.dialogs.ProgressDialog
import com.itshedi.weedworld.ui.theme.accentGreen
import com.itshedi.weedworld.utils.pagerTabIndicatorOffset
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalPagerApi::class)
@Composable
fun AuthScreen(navController: NavController,viewModel: AuthViewModel) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = Color.Transparent
    )

    val scaffoldState = rememberScaffoldState() // this contains the `SnackbarHostState`
    val coroutineScope = rememberCoroutineScope()
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    Scaffold(modifier = Modifier,
        scaffoldState = scaffoldState ) {

        Box(modifier = Modifier.fillMaxSize()) {
        val state = viewModel.eventFlow.collectAsState(AuthEvent.empty).value
        if(state is AuthEvent.loading){
            ProgressDialog()
        }
        LaunchedEffect(state){
            when(state){
                is AuthEvent.showSnackbar -> {
                    snackbarMessage = state.message

                }
                is AuthEvent.authenticated -> {
                    Log.i("cooltag", "success")
                    navController.navigate(Screens.MainScreen.route) {
                        popUpTo(Screens.AuthScreen.route) {
                            inclusive = true
                        }
                    }
                }
                is AuthEvent.empty -> {
                    Log.i("cooltag", "empty")
                }
                is AuthEvent.loading -> {
                    Log.i("cooltag", "loading")
                }
            }
        }


        Column(modifier = Modifier.fillMaxSize()) {
            val pagerState = rememberPagerState()

            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = com.itshedi.weedworld.R.drawable.auth_banner),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )
                TabRow(
                    // Our selected tab is our current page
                    selectedTabIndex = pagerState.currentPage,
                    // Override the indicator, using the provided pagerTabIndicatorOffset modifier
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier
                                .pagerTabIndicatorOffset(pagerState, tabPositions)
                                .padding(horizontal = 24.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            height = 6.dp,
                            color = accentGreen,
                        )
                    }, backgroundColor = Color.Transparent,
                    contentColor = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 5.dp)
                ) {
                    // Add tabs for all of our pages
                    Tab(
                        text = { Text("Signup") },
                        selected = pagerState.currentPage == 0,
                        onClick = {
                            if (pagerState.currentPage != 0) {
                                coroutineScope.launch { pagerState.animateScrollToPage(0) }
                            }
                        },
                    )
                    Tab(
                        text = { Text("Login") },
                        selected = pagerState.currentPage == 1,
                        onClick = {
                            if (pagerState.currentPage != 1) {
                                coroutineScope.launch { pagerState.animateScrollToPage(1) }
                            }
                        },
                    )
                }
            }


            HorizontalPager(
                count = 2,
                state = pagerState,
            ) { page ->
                when (page) {
                    0 -> { //register
                        RegisterScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colors.background), viewModel = viewModel,
                            navController = navController
                        )
                    }
                    1 -> { //login
                        LoginScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colors.background), viewModel = viewModel,
                            navController = navController
                        )

                    }
                }
            }
        }
        if(snackbarMessage!=null){
            DefaultSnackbar(
                message = snackbarMessage?:"", onDismiss = {
                    scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .systemBarsPadding()
            )
            LaunchedEffect(true){
                delay(2000)
                snackbarMessage = null
            }
        }
        }
    }
}


@Composable
fun DefaultSnackbar(
    message:String,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit?
) {
    Snackbar(
        modifier = modifier.padding(16.dp),
        content = {
            Text(
                text = message,
                style = MaterialTheme.typography.body2,
            )
        },
        action = {
                TextButton(
                    onClick = {
                        onDismiss()
                    }
                ) {
                    Text(
                        text = "Dismiss",
                        style = MaterialTheme.typography.body2,
                        color = accentGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

        }
    )
}
