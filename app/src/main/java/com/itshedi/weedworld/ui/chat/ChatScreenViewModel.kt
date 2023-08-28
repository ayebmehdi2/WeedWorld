package com.itshedi.weedworld.ui.chat

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.itshedi.weedworld.entities.Chat
import com.itshedi.weedworld.entities.ChatGroup
import com.itshedi.weedworld.entities.Status
import com.itshedi.weedworld.entities.UserInfo
import com.itshedi.weedworld.repository.chat_repository.ChatRepository
import com.itshedi.weedworld.repository.user_repository.UserRepository
import com.itshedi.weedworld.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ChatScreenViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    var query by mutableStateOf("")

    //    var onlineUsers = mutableStateListOf<UserInfo>()
    var onlineUsers = mutableStateListOf<Status>()
    var onlineUsersInfo = mutableStateMapOf<String,UserInfo>()

    var chats = mutableStateListOf<Chat>()
    var chatters = mutableStateMapOf<String,UserInfo>()

    var isRefreshing by mutableStateOf(false)

    var isLoadingOnlineUsers by mutableStateOf(false)


    var showMore by mutableStateOf(false)
    var showChatGroups by mutableStateOf(false)


    var groups = mutableStateListOf<ChatGroup>()


    var deleteConfirmDialog by mutableStateOf(false)

    var conversationToDelete :Int? = null

    var isError by mutableStateOf(false)

    fun loadGroups(){
        CoroutineScope(Dispatchers.IO).launch {
            chatRepository.loadGroups().collect {
                when (it) {
                    is Resource.Loading -> {

                    }
                    is Resource.Success -> {
                        it.data?.let {
                            groups.clear()
                            groups.addAll(it)
                        }
                    }
                    is Resource.Error -> {

                    }
                }
            }

        }
    }

    fun loadOnlineUsers(){
        CoroutineScope(Dispatchers.IO).launch {
            isError = false
            isLoadingOnlineUsers = true
            delay(400)
            while (true){
                chatRepository.getOnlineUsers().collect {
                    when (it) {
                        is Resource.Loading -> {
                            isLoadingOnlineUsers = true
                        }
                        is Resource.Success -> {
                            it.data?.let {
                                onlineUsers.clear()
                                onlineUsers.addAll(it)
                                isLoadingOnlineUsers = false
                            }
                        }
                        is Resource.Error -> {
                            isLoadingOnlineUsers = false
                            isError = true
                        }
                    }
                }
                delay(20000)
            }

        }
    }

    fun deleteConversation(index: Int){
        CoroutineScope(Dispatchers.IO).launch {
            delay(400)
            val _chats = ArrayList<Chat>()
            val _chatters = ArrayList<UserInfo?>()

            _chats.addAll(chats)

            chats.removeAt(index)

            chatRepository.deleteConversations(userId = when(_chats[index].outgoing){
                true -> _chats[index].to!!
                else -> _chats[index].from!!
            }).collect {
                when (it) {
                    is Resource.Loading -> {
                    }
                    is Resource.Success -> {
                        it.data?.let {
                        }
                    }
                    is Resource.Error -> {
                        chats.clear()
                        chats.addAll(_chats)

                        chatters.clear()
                    }
                }
            }

        }
    }

    fun loadConversations() {
        CoroutineScope(Dispatchers.IO).launch {
            isError = false
            isRefreshing = true
            delay(400)
            chatRepository.loadConversations().collect {
                when (it) {
                    is Resource.Loading -> {
                        isRefreshing = true
                    }
                    is Resource.Success -> {
                        it.data?.let {
                            chats.clear()
                            chatters.clear()
                            chats.addAll(it)
                            isRefreshing = false
                            Log.i("getget", "got ${chats.size} messages")
                        }
                    }
                    is Resource.Error -> {
                        isRefreshing = false
                        isError = true
                    }
                }
            }

        }
    }

    fun loadChatter(userId:String) {
        if(!chatters.containsKey(userId)){
            getUserById(
                userId, onResult = {
                    Log.i("chatters", "loadchatter result")
                    chatters[userId] = it
                })
        }


    }

    fun getUserById(id: String, onResult: (UserInfo) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            userRepository.getUserById(id).collect {
                when (it) {
                    is Resource.Success -> {
                        it.data?.let { onResult(it) }
                    }
                    is Resource.Error -> {
                        Log.i("zzzz", it.message.toString())
                    }
                    is Resource.Loading -> {

                    }
                }
            }
        }
    }



}
