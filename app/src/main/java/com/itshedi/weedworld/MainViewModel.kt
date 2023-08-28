package com.itshedi.weedworld

import androidx.lifecycle.ViewModel
import com.itshedi.weedworld.entities.Chat
import com.itshedi.weedworld.entities.UserInfo
import com.itshedi.weedworld.repository.chat_repository.ChatRepository
import com.itshedi.weedworld.repository.post_repository.PostRepository
import com.itshedi.weedworld.repository.user_repository.UserRepository
import com.itshedi.weedworld.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    fun setStatus(status:Boolean, onDone:()->Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            chatRepository.setStatus(status = status).collect {
                when (it) {
                    is Resource.Loading -> {
                    }
                    is Resource.Success -> {
                        onDone()
                    }
                    is Resource.Error -> {
                        onDone()
                    }
                }
            }
        }
    }
}