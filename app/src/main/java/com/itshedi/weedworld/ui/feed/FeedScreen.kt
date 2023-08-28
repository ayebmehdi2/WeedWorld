package com.itshedi.weedworld.ui.feed

import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.itshedi.weedworld.ui.PostsGrid
import com.itshedi.weedworld.ui.auth.DefaultSnackbar
import com.itshedi.weedworld.ui.dialogs.ProgressDialog
import kotlinx.coroutines.delay

@Composable
fun FeedScreen(navController: NavController, viewModel: FeedViewModel) {

    LaunchedEffect(true){
        viewModel.getFeedPosts()
    }
    val scaffoldState = rememberScaffoldState()
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val state = viewModel.eventFlow.collectAsState(FeedEvent.empty).value
    if (state is FeedEvent.loading) {
        ProgressDialog()
    }
    LaunchedEffect(state) {
        when (state) {
            is FeedEvent.showSnackbar -> {
                snackbarMessage = state.message
            }
            else -> {}
        }
    }

    Scaffold(scaffoldState = scaffoldState) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)){
            PostsGrid(Posts = viewModel.posts, downloadUrl = {
//                viewModel.pathToDownloadUrl(it) note :old way of doing it
                                                             it
                                                             }, isLoading = viewModel.isLoading,
                onRefresh = {
                    viewModel.getFeedPosts()
                }, modifier = Modifier.fillMaxSize(),
            likes = viewModel.likes,
            comments = viewModel.comments,
            getLikesAndComments = { viewModel.getLikesAndComments(it)})
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