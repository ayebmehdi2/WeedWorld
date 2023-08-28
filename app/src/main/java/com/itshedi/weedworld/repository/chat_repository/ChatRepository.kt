package com.itshedi.weedworld.repository.chat_repository

import com.itshedi.weedworld.entities.Chat
import com.itshedi.weedworld.entities.ChatGroup
import com.itshedi.weedworld.entities.Status
import com.itshedi.weedworld.entities.UserInfo
import com.itshedi.weedworld.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun sendMessage(to:String, content:String, emoji:Int?): Flow<Resource<Unit>>

    fun loadMessages(userId:String): Flow<Resource<List<Chat>>>

    fun loadConversations(): Flow<Resource<List<Chat>>>

    fun deleteConversations(userId: String): Flow<Resource<Unit>>

    fun setStatus(status:Boolean): Flow<Resource<Unit>>

    fun getOnlineUsers(): Flow<Resource<List<Status>>>

    fun addToGroup(to:String,group:String): Flow<Resource<Unit>>

    fun loadGroups(): Flow<Resource<List<ChatGroup>>>
}