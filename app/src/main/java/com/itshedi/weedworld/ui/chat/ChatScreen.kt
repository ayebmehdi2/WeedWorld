package com.itshedi.weedworld.ui.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.itshedi.weedworld.R
import com.itshedi.weedworld.entities.Chat
import com.itshedi.weedworld.entities.ChatGroup
import com.itshedi.weedworld.entities.UserInfo
import com.itshedi.weedworld.ui.ConnectivityError
import com.itshedi.weedworld.ui.dialogs.ChoiceConfirmDialog
import com.itshedi.weedworld.ui.theme.*

@Composable
fun ChatScreen(viewModel: ChatScreenViewModel, navController: NavController, onOpenDrawer:()->Unit) {

    BackHandler(viewModel.showChatGroups) {
        viewModel.showChatGroups = false
    }
    ChoiceConfirmDialog(
        showDialog = viewModel.deleteConfirmDialog,
        message = {
            Text(
                "Are you sure you want to delete this conversation ?",
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
            viewModel.conversationToDelete?.let {
                viewModel.deleteConversation(it)
            }
            viewModel.deleteConfirmDialog = false
        },
        onCancel = { viewModel.deleteConfirmDialog = false },
        onDismissRequest = { viewModel.deleteConfirmDialog = false })

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            //note: reload conversations after the user leaves the chat screen
            viewModel.loadConversations()
        }



    Scaffold(topBar = {

        TopAppBar(
            elevation = 0.dp,
            backgroundColor = Color.White,
            contentColor = Color.Black,
            title = {
                Text(text = "Chat")
        }, actions =
            {
                if (!viewModel.showChatGroups) {
                    IconButton(
                        onClick = { viewModel.showChatGroups = true }
                    ) {
                        Icon(imageVector = Icons.Default.Group, contentDescription = null)
                    }
                }
            },
            navigationIcon = if (viewModel.showChatGroups) {
                {
                    IconButton(onClick = {
                        viewModel.showChatGroups = false
                    }) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = null)
                    }
                }
            } else {
                {
                    IconButton(onClick = {
                        onOpenDrawer()
                    }) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = null,
                            modifier = Modifier.size(30.dp))
                    }
                }
            }
        )
    }) { paddingValues ->
        LaunchedEffect(true) {
            viewModel.loadConversations()
            viewModel.loadOnlineUsers()
        }
        val context = LocalContext.current
        LaunchedEffect(viewModel.isError) {
            Log.i("hedii", "iserror: ${viewModel.isError}")
        }
        when (viewModel.isError) {
            true -> ConnectivityError(modifier = Modifier.fillMaxSize()) {
                viewModel.loadConversations()
                viewModel.loadOnlineUsers()
            }
            else -> Column(modifier = Modifier.padding(paddingValues)) {
                Spacer(modifier = Modifier.padding(top = 16.dp))
                SearchSection(query = viewModel.query, onQueryChanged = { viewModel.query = it })
                Spacer(modifier = Modifier.padding(top = 16.dp))

                when (viewModel.isLoadingOnlineUsers) {
                    true -> OnlineUsersPlaceholder()
                    false -> OnlineUsersSection(users = viewModel.onlineUsers.map {
                        it.userId ?: ""
                    },
                        photos = viewModel.onlineUsersInfo,
                        onLoad = { userId ->
                            if (!viewModel.onlineUsersInfo.containsKey(userId)) {
                                viewModel.getUserById(userId, onResult = { u ->
                                    viewModel.onlineUsersInfo[userId] = u
                                })
                            }
                        },
                        onClick = {
                            viewModel.onlineUsersInfo[it]?.let { u ->
                                val intent = Intent(context, ChatActivity::class.java)
                                val bundle = Bundle()
                                bundle.putParcelable("user", u)
                                intent.putExtra("bundle", bundle)
                                launcher.launch(intent)
                            }
                        })
                }

                LaunchedEffect(viewModel.chatters) {
                    Log.i("chatters:", "${viewModel.chatters}")
                }

                Spacer(modifier = Modifier.padding(top = 16.dp))
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing = viewModel.isRefreshing),
                    onRefresh = {
                        viewModel.loadConversations()
                        viewModel.loadOnlineUsers()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    if (viewModel.showChatGroups) {
                        LaunchedEffect(true) {
                            viewModel.loadGroups()
                        }
                        ChatGroupSection(
                            groups = viewModel.groups,
                            onSelect = { viewModel.showChatGroups = false /*TODO*/ })
                    } else {
                        when (viewModel.isRefreshing) {
                            true -> Column(modifier = Modifier.fillMaxWidth()) {
                                repeat(4) {
                                    ConversationPlaceholder()
                                }
                            }
                            else -> MessagesSection(chat = viewModel.chats.filterIndexed { index, chat ->
                                if (viewModel.query.isBlank()) {
                                    true
                                } else {
                                    viewModel.chatters[
                                            when (chat.outgoing) {
                                                true -> chat.to
                                                else -> chat.from
                                            }!!
                                    ]?.username?.contains(
                                        viewModel.query,
                                        ignoreCase = true
                                    ) ?: false
                                }
                            }, chatters = viewModel.chatters, onClick = {
                                it?.let {
                                    val intent = Intent(context, ChatActivity::class.java)
                                    val bundle = Bundle()
                                    bundle.putParcelable("user", it)
                                    intent.putExtra("bundle", bundle)
                                    launcher.launch(intent)
                                }
                            }, onDelete = {
                                viewModel.conversationToDelete = it
                                viewModel.deleteConfirmDialog = true
                            }, onLoadChatter = {
                                viewModel.loadChatter(it)
                            })
                        }
                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GrouItem(title: String, onSelect: () -> Unit) {
    Card(
        onClick = onSelect, backgroundColor = VeryLightBlue, modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
        ) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        }
    }

}

@Composable
fun ChatGroupSection(groups: List<ChatGroup>, onSelect: (Int) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        itemsIndexed(groups) { index, item ->
            GrouItem(title = item.name ?: "Untitled") {
                onSelect(index)
            }
        }
    }
}



@Composable
fun ConversationPlaceholder() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp)
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
                    .fillMaxWidth(0.7f)
                    .height(20.dp)
                    .placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(20.dp)
                    .placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun MessagesSection(
    chat: List<Chat>,
    chatters: Map<String, UserInfo>,
    onClick: (UserInfo?) -> Unit,
    onDelete: (Int) -> Unit,
    onLoadChatter: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        itemsIndexed(items = chat) { index, item ->

            LaunchedEffect(true) {
                onLoadChatter(
                    when (item.outgoing) {
                        true -> item.to
                        else -> item.from
                    }!!
                )
            }
            val chatter = chatters.getOrDefault(
                when (item.outgoing) {
                    true -> item.to
                    else -> item.from
                }!!, null
            )

            SwipeToDismiss(modifier = Modifier
                .animateItemPlacement()
                .fillMaxWidth()
                .height(80.dp),
                directions = setOf(DismissDirection.StartToEnd),
                dismissThresholds = { direction ->
                    FractionalThreshold(
                        if (direction == DismissDirection.StartToEnd) 0.66f else 0.50f
                    )
                },
                state = rememberDismissState(confirmStateChange = {
                    when (it) {
                        DismissValue.DismissedToEnd -> {
                            onDelete(index)
                            false
                        }
                        DismissValue.DismissedToStart -> {
                            true
                        }
                        else -> {
                            true
                        }
                    }
                }),
                background = {
                    SwipeDeleteBackground()
                }) {
                if (chatter==null){
                    ConversationPlaceholder()
                }else{
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                    ) {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                            .background(shape = RoundedCornerShape(10.dp), color = Color.White)
                            .clickable { onClick(chatter) }) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .aspectRatio(1f)
                                        .clip(CircleShape)
                                        .background(VeryLightGray)
                                ) {
                                    chatter?.profilePhoto?.let {
                                        Image(
                                            painter = rememberImagePainter(data = it),
                                            contentScale = ContentScale.Crop,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .aspectRatio(1f)
                                        )
                                    }
                                }
                                Column(
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .weight(1f)
                                ) {
                                    Text(
                                        chatter?.username ?: "",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (item.emoji != null) {

                                        Text(
                                            "${
                                                when (item.outgoing) {
                                                    true -> "You"
                                                    else -> chatter?.username ?: ""
                                                }
                                            } sent an emoji", fontSize = 14.sp
                                        )
                                    } else {
                                        Text(
                                            "${
                                                when (item.outgoing) {
                                                    true -> "You"
                                                    else -> chatter?.username ?: ""
                                                }
                                            }: ${item.content ?: ""}", fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun SwipeDeleteBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(accentGreen)
            .padding(horizontal = 20.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.delete),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(28.dp)
                .align(
                    Alignment.CenterStart
                )
        )
    }
}

@Composable
fun OnlineUsersSection(
    photos: Map<String, UserInfo>,
    users: List<String>,
    onClick: (String) -> Unit,
    onLoad: (String) -> Unit
) {
    LazyRow(modifier = Modifier.fillMaxWidth()) {
        item {
            Spacer(modifier = Modifier.padding(end = 16.dp))
        }
        itemsIndexed(users) { _, userId ->
            onLoad(userId)
            OnlineUserItem(photo = photos[userId]?.profilePhoto, onClick = { onClick(userId) })
            Spacer(modifier = Modifier.padding(end = 16.dp))
        }
        item {
            Spacer(modifier = Modifier.padding(end = 16.dp))
        }
    }
}

@Composable
fun SearchSection(query: String, onQueryChanged: (String) -> Unit) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val focusManager = LocalFocusManager.current

        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(22.dp))
                .background(shape = RoundedCornerShape(22.dp), color = VeryLightGreen)
                .padding(vertical = 10.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(28.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
            ) {
                if (query.isBlank()) {
                    Text("Messages", color = Color.Gray)
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChanged,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                    }),
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }

        IconButton(onClick = { /*TODO*/ }) {
            Icon(
                painter = painterResource(id = R.drawable.chat_bubble),
                contentDescription = null,
                tint = accentGreen
            )
        }
        IconButton(onClick = { /*TODO*/ }) {
            Icon(
                painter = painterResource(id = R.drawable.chat_people),
                contentDescription = null,
                tint = Color.DarkGray
            )
        }
    }
}

@Composable
fun OnlineUsersPlaceholder() {
    Row(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.padding(end = 16.dp))
        repeat(10) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
            Spacer(modifier = Modifier.padding(end = 16.dp))
        }
    }
}

@Composable
fun OnlineUserItem(photo: String?, onClick: () -> Unit) {
    Box(modifier = Modifier.size(60.dp)) {
        Box(modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(VeryLightGray)
            .clickable { onClick() }) {
            photo?.let {
                Image(
                    painter = rememberImagePainter(data = it),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Box(
            modifier = Modifier
                .padding(bottom = 0.dp, end = 0.dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(accentGreen)
                .align(Alignment.BottomEnd)
        )
    }
}