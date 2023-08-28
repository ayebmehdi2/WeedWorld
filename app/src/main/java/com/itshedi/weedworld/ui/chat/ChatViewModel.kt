package com.itshedi.weedworld.ui.chat

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.itshedi.weedworld.entities.Chat
import com.itshedi.weedworld.entities.ChatGroup
import com.itshedi.weedworld.entities.UserInfo
import com.itshedi.weedworld.repository.chat_repository.ChatRepository
import com.itshedi.weedworld.repository.post_repository.PostRepository
import com.itshedi.weedworld.repository.user_repository.UserRepository
import com.itshedi.weedworld.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val userRepository: UserRepository, private val postRepository: PostRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    var me by mutableStateOf<UserInfo?>(null)

    var user by mutableStateOf<UserInfo?>(null)

    var showMore by mutableStateOf(false)

    var groups = mutableStateListOf<ChatGroup>()

    var messages = mutableStateListOf<Chat>()

    var message by mutableStateOf("")

    var groupName by mutableStateOf("")

    var showEmojiPanel by mutableStateOf(false)

    var scrollTriggerr by mutableStateOf(false)

    fun addToGroup(to:String, name:String){
        CoroutineScope(Dispatchers.IO).launch {
            chatRepository.addToGroup(to, name).collect {
                when (it) {
                    is Resource.Loading -> {

                    }
                    is Resource.Success -> {
                    }
                    is Resource.Error -> {

                    }
                }
            }

        }
    }
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
    fun loadMe(){
        FirebaseAuth.getInstance().currentUser?.uid?.let {
            getUserById(id = it, onResult = { userInfo ->
                me = userInfo
            })
        }
    }
    fun loadUser() {
        user?.uid?.let {
            getUserById(id = it, onResult = { userInfo ->
                user = userInfo
            })
        }
    }

    var outputDateFormatter = SimpleDateFormat("hh.mm aa")

    fun formatMessageTimestamp(date: Date?):String{
        if (date==null) return ""
        return outputDateFormatter.format(date)
    }

    //load every 1 sec
    fun loadMessages(){
        CoroutineScope(Dispatchers.IO).launch {
            while (true){
                chatRepository.loadMessages(userId = user?.uid!!).collect {
                    when (it) {
                        is Resource.Loading -> {

                        }
                        is Resource.Success -> {
                            it.data?.let {
                                if (it!=messages){
                                    if (messages.size<it.size){
                                        scrollTriggerr = !scrollTriggerr
                                    }
                                    kotlin.run { //note: single transaction to prevent glitching
                                        messages.clear()
                                        messages.addAll(it)
                                    }
                                }
                                Log.i("getget", "got ${messages.size} messages")
                            }
                        }
                        is Resource.Error -> {

                        }
                    }
                }
                delay(2000)
            }

        }
    }

    fun sendMessage(content:String, emoji:Int?=null){
        messages.add(Chat(content = content,emoji = emoji, outgoing = true, inqueue = true))
        scrollTriggerr = !scrollTriggerr
        CoroutineScope(Dispatchers.IO).launch {
            chatRepository.sendMessage(to = user?.uid!!, content = content, emoji = emoji).collect {
                when (it) {
                    is Resource.Loading -> {
                    }
                    is Resource.Success -> {

                    }
                    is Resource.Error -> {

                    }
                }
            }
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