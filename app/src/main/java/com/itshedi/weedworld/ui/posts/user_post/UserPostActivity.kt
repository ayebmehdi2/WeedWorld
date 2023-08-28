package com.itshedi.weedworld.ui.posts.user_post

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import coil.compose.rememberImagePainter
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.itshedi.weedworld.entities.Post
import com.itshedi.weedworld.entities.UserInfo
import com.itshedi.weedworld.ui.PostLikesCommentsCounter
import com.itshedi.weedworld.ui.dialogs.ChoiceConfirmDialog
import com.itshedi.weedworld.ui.theme.VeryLightGray
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserPostActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = intent.getBundleExtra("bundle")
        val viewModel: UserPostViewModel = ViewModelProvider(this)[UserPostViewModel::class.java]

        if (bundle!!.containsKey("post")) {
            viewModel.post = bundle.getParcelable("post")
        }

        setContent {

            BackHandler(viewModel.isEditing) {
                viewModel.isEditing = false
            }
//            ViewAllCommentsDialog(
//                showDialog = viewModel.viewAllCommentsDialog,
//                onDismissRequest = { viewModel.viewAllCommentsDialog = false },
//                comments = viewModel.comments,
//                commentators = viewModel.commentators,
//                loadCommentator = { userId, index ->
//                    viewModel.getUserById(userId!!, onResult = {
//                        viewModel.commentators[index] = it
//                    })
//                }
//            )

            ChoiceConfirmDialog(
                showDialog = viewModel.deleteConfirmDialog,
                message = {
                    Text(
                        "Are you sure you want to remove this post ?",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                    )
                },
                okMessage = "Yes",
                cancelMessage = "No",
                onOk = { viewModel.deletePost(onDeleted = {
                    setResult(Activity.RESULT_OK)
                    finish() }) },
                onCancel = { viewModel.deleteConfirmDialog = false },
                onDismissRequest = { viewModel.deleteConfirmDialog = false })

            ChoiceConfirmDialog(
                showDialog = viewModel.repostConfirmDialog != null,
                message = {
                    Text(
                        "Repost this post by ${viewModel.repostConfirmDialog} ?",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                    )
                },
                okMessage = "Yes",
                cancelMessage = "No",
                onOk = {
                    viewModel.repost()
                    viewModel.repostConfirmDialog = null
                },
                onCancel = { viewModel.repostConfirmDialog = null },
                onDismissRequest = { viewModel.repostConfirmDialog = null })




            viewModel.post?.let { post ->
                LaunchedEffect(true) {
                    viewModel.getLikes(post.postId!!)
                    viewModel.getComments(post.postId)
                    viewModel.loadAuthor()
                    viewModel.loadMe()
                    viewModel.checkIfMyPost()
                }
                val scaffoldState = rememberScaffoldState()
                val cs = rememberCoroutineScope()
                val modalSheetState = rememberModalBottomSheetState(
                    initialValue = ModalBottomSheetValue.Hidden,
                    confirmStateChange = { it != ModalBottomSheetValue.HalfExpanded },
                    skipHalfExpanded = true,
                )

                ModalBottomSheetLayout(sheetState = modalSheetState, sheetContent = {
                    BottomSheetActions(onDelete = {
                        cs.launch { modalSheetState.hide() }
                        viewModel.deleteConfirmDialog = true
                    }, onEdit = when (viewModel.post?.image) {
                        null -> {
                            {
                                cs.launch { modalSheetState.hide() }
                                viewModel.editValue = viewModel.post?.text ?: ""
                                viewModel.isEditing = true
                            }
                        }
                        else -> null
                    })
                }, sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)) {

                    Scaffold(
                        scaffoldState = scaffoldState,
                        topBar = {
                            TopNavigation(onBackPressed = {
                                if (viewModel.isEditing) {
                                    viewModel.isEditing = false
                                } else {
                                    setResult(Activity.RESULT_CANCELED)
                                    finish()
                                }
                            },
                                user = viewModel.author,
                                onAction = when (viewModel.isMyPost) {
                                    true -> {
                                        { cs.launch { modalSheetState.show() } }
                                    }
                                    false -> null
                                })
                        },
                        bottomBar = {
                            viewModel.me?.let {
                                CommentBar(
                                    it,
                                    comment = viewModel.myComment,
                                    onValueChange = { value ->
                                        viewModel.myComment = value
                                    },
                                    onConfirmComment = { viewModel.addComment() })
                            }
                        },
                        floatingActionButton = {
                            if (viewModel.isEditing && viewModel.editValue != viewModel.post?.text) {
                                FloatingActionButton(onClick = { viewModel.saveEdit(onUpdated = {
                                    viewModel.isEditing = false
                                }) }) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    ) { paddingValues ->
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                                .padding(paddingValues)
                                .padding(16.dp)
                        ) {
                            Spacer(modifier = Modifier.padding(top = 10.dp))

                            PostSection(
                                post = post,
                                onLike = { viewModel.likePost() },
                                onShare = {
                                    viewModel.repostConfirmDialog = viewModel.author?.name
                                },
                                comments = viewModel.comments.size,
                                likes = viewModel.likes.size,
                                isLiked = viewModel.isLiked,
                                isEditing = viewModel.isEditing,
                                editValue = viewModel.editValue,
                                onEdit = { viewModel.editValue = it }
                            )

                            Spacer(modifier = Modifier.padding(top = 12.dp))

                            UsernameAndDateSection(
                                username = viewModel.author?.name,
                                date = viewModel.dateFormatter(viewModel.post?.timestamp?.toDate())
                            )
                            Spacer(modifier = Modifier.padding(top = 2.dp))

                            if (viewModel.comments.size> 1 && !viewModel.viewAllComments){
                                TextButton(onClick = {
                                    cs.launch {
                                        viewModel.viewAllComments = true
                                    }
                                }) {
                                    Text(
                                        "View all comments (${viewModel.comments.size})",
                                        color = MaterialTheme.colors.onBackground.copy(ContentAlpha.medium)
                                    )
                                }
                            }else{

                                Spacer(modifier = Modifier.padding(top = 8.dp))
                            }
                            Spacer(modifier = Modifier.padding(top = 8.dp))


                             when(viewModel.viewAllComments){
                                true -> viewModel.comments.toList()
                                false -> viewModel.comments.toList()
                                    .take(1)
                            }.forEachIndexed { _, comment ->
                                 comment.userId?.let{ userId ->
                                     LaunchedEffect(true) {
                                         viewModel.loadCommentator(userId)

                                     }
                                     CommentItem(
                                         commentator = viewModel.commentators.getOrDefault(userId, null),
                                         content = comment.content ?: ""
                                     )
                                     Spacer(modifier = Modifier.padding(top = 12.dp))
                                 }
                            }


                        }
                    }
                }

            }
        }
    }


    @Composable
    fun UsernameAndDateSection(username: String?, date: String?) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Text(text = username ?: "", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(
                text = date ?: "",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
                fontSize = 12.sp,
                color = Color.LightGray
            )
        }
    }

    @Composable
    fun CommentBar(
        me: UserInfo, comment: String, onValueChange: (String) -> Unit, onConfirmComment: () -> Unit
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Divider(modifier = Modifier.fillMaxWidth(), color = VeryLightGray)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                ) {
                    me.profilePhoto?.let {
                        Image(
                            painter = rememberImagePainter(data = it),
                            contentScale = ContentScale.Crop,
                            contentDescription = null,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                val focusManager = LocalFocusManager.current

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(shape = RoundedCornerShape(12.dp), color = VeryLightGray)
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                ) {
                    if (comment.isBlank()) {
                        Text("Add comment..", color = Color.Gray)
                    }
                    BasicTextField(
                        value = comment,
                        onValueChange = onValueChange,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            onConfirmComment()
                        }),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

    }

    @Composable
    private fun TopNavigation(
        onBackPressed: () -> Unit,
        user: PostAuthor?,
        onAction: (() -> Unit)? = null
    ) {
        TopAppBar(elevation = 2.dp,
            contentColor = Color.Black,
            backgroundColor = Color.White,
            navigationIcon = {
                IconButton(onClick = {
                    onBackPressed()
                }) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }, actions = {
                onAction?.let {
                    IconButton(onClick = { it.invoke() }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
                    }
                }

            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    ) {
                        user?.photo?.let {
                            Image(
                                painter = rememberImagePainter(data = it),
                                contentScale = ContentScale.Crop,
                                contentDescription = null,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.padding(start = 16.dp))

                    user?.name?.let {
                        Text(it)
                    }
                }
            })
    }

    @Composable
    fun PostSection(
        post: Post,
        onLike: () -> Unit,
        onShare: () -> Unit,
        likes: Int,
        comments: Int,
        isLiked: Boolean,
        isEditing: Boolean,
        editValue: String,
        onEdit: (String) -> Unit,
    ) {
        val editFocusRequester = FocusRequester()
        LaunchedEffect(isEditing) {
            if (isEditing) {
                delay(200)
                editFocusRequester.requestFocus()
            }
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = when (post.image) {
                    null -> Modifier.fillMaxWidth()
                    else -> Modifier
                        .fillMaxWidth()
                        .aspectRatio(post.aspectRatio ?: 1f)
                }
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.LightGray.copy(0.5f))
            ) {
                if (post.image != null) {
                    Image(
                        painter = rememberImagePainter(data = post.image),
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (post.text != null) {
                    if (isEditing) {
                        BasicTextField(
                            value = editValue, onValueChange = onEdit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(editFocusRequester)
                                .padding(horizontal = 16.dp, vertical = 26.dp)
                                .align(Alignment.Center),
                            textStyle = TextStyle(
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    } else {
                        Text(
                            text = post.text,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 16.dp, vertical = 26.dp),
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 20.sp
                        )
                    }

                }

            }
            Spacer(modifier = Modifier.padding(top = 12.dp))

            PostLikesCommentsCounter(
                likes = likes,
                comments = comments,
                onLike = onLike,
                onShare = onShare,
                height = 26.dp,
                fontSize = 14.sp,
                isLiked = isLiked
            )

        }
    }


}



@Composable
fun CommentPlaceholder() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .clip(CircleShape)
                .placeholder(
                    visible = true,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(start = 16.dp), verticalArrangement = Arrangement.SpaceAround
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(14.dp)
                    .placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(14.dp)
                    .placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
        }
    }
}


@Composable
fun CommentItem(commentator: UserInfo?, content: String) {
    Crossfade(targetState = commentator) {
        when (it) {
            null -> CommentPlaceholder()
            else -> Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    ) {
                        commentator?.profilePhoto?.let {
                            Image(
                                painter = rememberImagePainter(data = it),
                                contentDescription = null,
                                modifier = Modifier.size(34.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Spacer(modifier = Modifier.padding(start = 12.dp))
                    Text(
                        text = commentator?.username ?: "",
                        style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    )
                }
                Text(
                    text = content,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(start = 46.dp, top = 0.dp)
                )
            }
        }
    }
}