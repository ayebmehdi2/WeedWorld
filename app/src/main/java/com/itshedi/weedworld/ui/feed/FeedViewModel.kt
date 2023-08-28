package com.itshedi.weedworld.ui.feed

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itshedi.weedworld.entities.Post
import com.itshedi.weedworld.repository.post_repository.PostRepository
import com.itshedi.weedworld.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class FeedEvent {
    data class showSnackbar(val message: String) : FeedEvent()
    object loading : FeedEvent()
    object empty : FeedEvent()
}

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<FeedEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val posts = mutableStateListOf<Post>()

    val likes = mutableStateListOf<Int?>()
    val comments = mutableStateListOf<Int?>()

    var isLoading by mutableStateOf(false)

    suspend fun pathToDownloadUrl(path: String): String {
        return postRepository.downloadUrl(path)
    }

    fun getLikesAndComments(index:Int){
        //checking if its not loading out of bound
        if (index >= posts.size) return

        val postId = posts[index].postId
        postId?.let {
            CoroutineScope(Dispatchers.IO).launch {
                postRepository.getLikeCount(postId).collect {
                    when (it) {
                        is Resource.Success -> {
                            it.data?.let { data ->
                                likes[index] = data
                            }

                        }
                        is Resource.Error -> {
                            Log.i("zzzz", it.message.toString())
                        }
                        is Resource.Loading -> {

                        }
                    }
                }
                postRepository.getCommentCount(postId).collect {
                    when (it) {
                        is Resource.Success -> {
                            it.data?.let { data ->
                                comments[index] = data
                            }

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

    fun getFeedPosts() {
        isLoading = true
        posts.clear()
        CoroutineScope(Dispatchers.IO).launch {
            delay(400)
            postRepository.getFeedPosts().collect {
                when (it) {
                    is Resource.Success -> {
                        isLoading = false
                        it.data?.let{ d ->
                            likes.clear()
                            likes.addAll(d.map { null })
                            comments.clear()
                            comments.addAll(d.map { null })
                            posts.addAll(d)
                        }

                    }
                    is Resource.Error -> {
                        _eventFlow.emit(FeedEvent.showSnackbar(message = "Couldn't get posts"))
                        isLoading = false
                    }
                    is Resource.Loading -> {
                        isLoading = true
                    }
                }
            }
        }
    }
}