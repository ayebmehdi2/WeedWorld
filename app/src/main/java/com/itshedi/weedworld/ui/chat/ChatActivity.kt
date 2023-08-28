package com.itshedi.weedworld.ui.chat

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import coil.compose.rememberImagePainter
import com.itshedi.weedworld.R
import com.itshedi.weedworld.entities.Chat
import com.itshedi.weedworld.entities.UserInfo
import com.itshedi.weedworld.ui.posts.user_post.BottomSheetActions
import com.itshedi.weedworld.ui.posts.user_post.BottomSheetChatGroup
import com.itshedi.weedworld.ui.theme.VeryLightGray
import com.itshedi.weedworld.ui.theme.VeryLightGreen
import com.itshedi.weedworld.ui.theme.accentGreen
import com.itshedi.weedworld.ui.theme.dividerColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class ChatActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: ChatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        val bundle = intent.getBundleExtra("bundle")

        if (bundle!!.containsKey("user")) {
            viewModel.user = bundle.getParcelable("user")
        }

        setContent {
            BackHandler(viewModel.showEmojiPanel) {
                viewModel.showEmojiPanel = false
            }
            LaunchedEffect(true) {
                viewModel.loadMe()
                viewModel.loadUser()
                viewModel.loadMessages()
            }

            val lazyListState = rememberLazyListState()

            LaunchedEffect(viewModel.scrollTriggerr){
                if (viewModel.messages.isNotEmpty()){
                    lazyListState.scrollToItem(viewModel.messages.size-1)
                }
            }

            val cs = rememberCoroutineScope()
            val modalSheetState = rememberModalBottomSheetState(
                initialValue = ModalBottomSheetValue.Hidden,
                confirmStateChange = { it != ModalBottomSheetValue.HalfExpanded },
                skipHalfExpanded = true,
            )

            ModalBottomSheetLayout(sheetState = modalSheetState, sheetContent = {
                LaunchedEffect(true){
                    viewModel.loadGroups()
                }
                BottomSheetChatGroup(
                    groups = viewModel.groups.map { it.name?:"Untitled" },
                    onSelect = { index ->
                        cs.launch { modalSheetState.hide() }
                        viewModel.user?.uid?.let {
                            viewModel.addToGroup(it, viewModel.groups[index].name!!)
                        }

                    },
                    name = viewModel.groupName,
                    onNameChange = {viewModel.groupName = it},
                    onCreateGroup = {
                        cs.launch { modalSheetState.hide() }
                        if (viewModel.groupName.isNotBlank()){
                            viewModel.user?.uid?.let {
                                viewModel.addToGroup(it, viewModel.groupName)
                                viewModel.groupName = ""
                            }
                        }
                    }
                )
            }, sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)) {
                Scaffold(topBar = {
                    Column() {
                        ChatTopNavigation(user = viewModel.user,
                            onDeleteConversation = {
                                //todo: del conv
                                finish()
                                viewModel.showMore = false
                            },
                            onAddToGroup = {
                                viewModel.showMore = false
                                cs.launch { modalSheetState.show() }
                            },
                            onShowHideMore = { viewModel.showMore = it },
                            expandMore = viewModel.showMore)
                        Divider(color = dividerColor, thickness = 1.dp)
                    }
                },
                    bottomBar = {
                        Column {
                            MessageBar(message = viewModel.message, onValueChange = {
                                viewModel.message = it
                            }, onConfirmMessage = {viewModel.sendMessage(it)}, onEmotes = {viewModel.showEmojiPanel = !viewModel.showEmojiPanel},
                                onFocused = {
                                    cs.launch {
                                        if (viewModel.messages.isNotEmpty()) {
                                            delay(400)
                                            lazyListState.scrollToItem(viewModel.messages.size - 1)
                                        }
                                    }
                                })
                            if (viewModel.showEmojiPanel){
                                EmojiPanel(onSelectd = { viewModel.sendMessage("", emoji = it)})
                            }
                        }

                    }) { paddingValues ->
                    Column(modifier = Modifier.padding(paddingValues)) {
                        ChatMessages(lazyListState = lazyListState,messages = viewModel.messages, profilePhotoMe = viewModel.me?.profilePhoto, profilePhotoTo = viewModel.user?.profilePhoto,
                            formatDate = { date->
                                viewModel.formatMessageTimestamp(date)
                            })
                    }
                }
            }

        }
    }


    @Composable
    fun EmojiPanel(onSelectd:(Int)->Unit) {
        LazyRow(modifier = Modifier
            .padding(vertical = 16.dp)
            .height(100.dp)){
            for (i in 0..32){
                item {
                    Image(painter = rememberImagePainter(getEmojiPainter(i)), contentDescription = null,
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
                            .padding(10.dp)
                            .clickable { onSelectd(i) }
                    )
                }
            }
        }
    }

    @Composable
    fun MessageBar(
        message: String, onValueChange: (String) -> Unit, onConfirmMessage: (String) -> Unit,
        onEmotes:()->Unit, onFocused:() -> Unit
    ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VeryLightGreen)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val focusManager = LocalFocusManager.current

                Box(modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onEmotes() }
                    .padding(2.dp)){
                    Icon(painter = painterResource(id = R.drawable.emotes), contentDescription = null, tint = accentGreen)
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(shape = RoundedCornerShape(20.dp), color = Color.White)
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)){
                        if (message.isBlank()) {
                            Text("Send a message", color = Color.Gray)
                        }
                        val focusRequester = FocusRequester()
                        BasicTextField(
                            value = message,
                            onValueChange = onValueChange,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                            }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 10.dp)
                                .focusRequester(focusRequester)
                                .onFocusChanged {
                                    if (it.isFocused) {
                                        onFocused()
                                    }
                                }
                        )
                    }
                    Box(modifier = Modifier.clip(CircleShape).clickable(enabled = message.isNotBlank()) {
                        onConfirmMessage(message)
                        onValueChange("")
                    }.padding(4.dp)){
                        Icon(imageVector = Icons.Filled.Send, contentDescription = null, tint = when(message.isBlank()){
                            true -> Color.LightGray
                            false -> Color.Gray
                        }, modifier = Modifier.size(22.dp))
                    }


                }
            }


    }

    @Composable
    fun ChatMessages(messages: List<Chat>, profilePhotoTo:String?, profilePhotoMe: String?, formatDate:(Date?) -> String,
    lazyListState: LazyListState) {
        LazyColumn(state = lazyListState, modifier = Modifier.padding(horizontal = 16.dp)){
            item{
                Spacer(modifier = Modifier.padding(top = 16.dp))
            }
            itemsIndexed(messages){ index, message ->
                Log.i("tits",message.outgoing.toString())
                when(message.outgoing){
                    true ->
                        MessageItemOut(emoji=message.emoji,content = message.content?:"", profilePhoto = profilePhotoMe, time = formatDate(message.timestamp?.toDate()), inqueue = message.inqueue)
                    else ->
                        MessageItemIn(emoji=message.emoji,content = message.content?:"", profilePhoto = profilePhotoTo, time = formatDate(message.timestamp?.toDate()))
                }
                Spacer(modifier = Modifier.padding(top = 10.dp))
            }
        }
    }

    fun getEmojiPainter(
        index:Int
    ):Int{
        return listOf(
            R.drawable.emoji_0,
            R.drawable.emoji_1,
            R.drawable.emoji_2,
            R.drawable.emoji_3,
            R.drawable.emoji_4,
            R.drawable.emoji_5,
            R.drawable.emoji_6,
            R.drawable.emoji_7,
            R.drawable.emoji_8,
            R.drawable.emoji_9,
            R.drawable.emoji_10,
            R.drawable.emoji_11,
            R.drawable.emoji_12,
            R.drawable.emoji_13,
            R.drawable.emoji_14,
            R.drawable.emoji_15,
            R.drawable.emoji_16,
            R.drawable.emoji_17,
            R.drawable.emoji_18,
            R.drawable.emoji_19,
            R.drawable.emoji_20,
            R.drawable.emoji_21,
            R.drawable.emoji_22,
            R.drawable.emoji_23,
            R.drawable.emoji_24,
            R.drawable.emoji_25,
            R.drawable.emoji_26,
            R.drawable.emoji_27,
            R.drawable.emoji_28,
            R.drawable.emoji_29,
            R.drawable.emoji_30,
            R.drawable.emoji_31,
            R.drawable.emoji_32,
        )[index]
    }
    
    @Composable
    fun MessageItemOut(emoji:Int?=null, content: String, profilePhoto:String?, time:String, inqueue:Boolean) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically){
            Text(time, fontSize = 11.sp, color = Color.LightGray)
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {

                Box(modifier = Modifier.weight(1f)){
                    when(emoji){
                        null -> {
                            Box(modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(horizontal = 12.dp)
                                .background(
                                    color = VeryLightGray,
                                    shape = RoundedCornerShape(
                                        topStart = 20.dp,
                                        topEnd = 20.dp,
                                        bottomStart = 20.dp
                                    )
                                )
                                .graphicsLayer {
                                    alpha = if(inqueue) 0.2f else 1f
                                }
                                .padding(10.dp)){
                                Text(text = content, fontSize = 15.sp)
                            }
                        }
                        else-> {
                            Image(painter = rememberImagePainter(getEmojiPainter(emoji)), contentDescription = null,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(horizontal = 12.dp)
                                    .size(100.dp)
                            )
                        }
                    }
                    
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                ) {
                    profilePhoto?.let {
                        Image(
                            painter = rememberImagePainter(data = it),
                            contentScale = ContentScale.Crop,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun MessageItemIn(emoji:Int?=null, content: String, profilePhoto:String?, time:String) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically){
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                ) {
                    profilePhoto?.let {
                        Image(
                            painter = rememberImagePainter(data = it),
                            contentScale = ContentScale.Crop,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Box(modifier = Modifier.weight(1f)){
                    when(emoji){
                        null -> {
                            Box(modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .background(
                                    color = VeryLightGreen,
                                    shape = RoundedCornerShape(
                                        topStart = 20.dp,
                                        topEnd = 20.dp,
                                        bottomEnd = 20.dp
                                    )
                                )
                                .padding(10.dp)){
                                Text(text = content, fontSize = 15.sp)
                            }
                        }
                        else -> {
                            Image(painter = rememberImagePainter(getEmojiPainter(emoji)), contentDescription = null,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .size(100.dp)
                            )
                        }
                    }
                    
                }


            }
            Text(time, fontSize = 11.sp, color = Color.LightGray)
        }
    }
    @Composable
    fun ChatTopNavigation(user: UserInfo?, expandMore:Boolean, onAddToGroup:()->Unit, onShowHideMore:(Boolean)-> Unit,
    onDeleteConversation:()-> Unit) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)) {
            Row(modifier = Modifier, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { finish() }) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = null)
                }
                Text(text = "Chat", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            UserBanner(user = user, modifier = Modifier.align(Alignment.Center))

            IconButton(onClick = { onShowHideMore(true) }, modifier = Modifier.align(Alignment.CenterEnd)) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
            }
            Box(modifier = Modifier.align(
                Alignment.TopEnd)){
                DropdownMenu(expanded = expandMore, onDismissRequest = { onShowHideMore(false) } ) {
//                    DropdownMenuItem(onClick = { onDeleteConversation() }) {
//                        Text("Delete conversation")
//                    }
                    DropdownMenuItem(onClick = { onAddToGroup() }) {
                        Text("Add to the group..")
                    }
                }
            }

        }
    }

    @Composable
    fun UserBanner(user: UserInfo?, modifier: Modifier) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            ) {
                user?.profilePhoto?.let {
                    Image(
                        painter = rememberImagePainter(data = it),
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.padding(start = 8.dp))

            user?.username?.let {
                Text(it, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}