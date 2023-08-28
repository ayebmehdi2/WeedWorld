package com.itshedi.weedworld

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
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



sealed class AddPostEvent {
    data class showSnackbar(val message: String) : AddPostEvent()
    object postAdded : AddPostEvent()
    object loading : AddPostEvent()
    object empty : AddPostEvent()
}


@HiltViewModel
class AddPostViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<AddPostEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    var addPostMethodIndex by mutableStateOf(1)
    var cameraFocusPoint by mutableStateOf<Offset?>(null)

    var storeId by mutableStateOf<String?>(null)

    fun addPost(text:String, private:Boolean){
        addPost(null,null,text,private)
    }
    fun addPost(uri:Uri, ratio:Float, private:Boolean){
        addPost(uri,ratio,null,private)
    }

    private fun addPost(uri: Uri?, ratio: Float?, text: String?, private:Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            postRepository.addPost(storeId = storeId, uri, ratio, text, private).collect {
                when (it) {
                    is Resource.Success -> {
                        _eventFlow.emit(AddPostEvent.showSnackbar("Post added successfully"))
                        delay(1000)
                        _eventFlow.emit(AddPostEvent.postAdded)
                    }
                    is Resource.Loading -> {
                        _eventFlow.emit(AddPostEvent.loading)
                    }
                    is Resource.Error -> _eventFlow.emit(AddPostEvent.showSnackbar("Error adding post"))
                }
            }
        }

    }
}