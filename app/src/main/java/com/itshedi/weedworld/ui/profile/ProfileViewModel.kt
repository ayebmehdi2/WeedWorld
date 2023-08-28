package com.itshedi.weedworld.ui.profile

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.itshedi.weedworld.entities.Post
import com.itshedi.weedworld.entities.UserInfo
import com.itshedi.weedworld.repository.post_repository.PostRepository
import com.itshedi.weedworld.repository.user_repository.UserRepository
import com.itshedi.weedworld.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileEvent {
    data class showSnackbar(val message: String) : ProfileEvent()
    object loading : ProfileEvent()
    object empty : ProfileEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<ProfileEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    var user by mutableStateOf<UserInfo?>(null)


    var isLoadingProfile by mutableStateOf(false)

    var isLoadingPosts by mutableStateOf(false)
    var isLoadingPrivatePosts by mutableStateOf(false)

    var allMyPosts = mutableStateListOf<Post>()
    var allMyPrivatePosts = mutableStateListOf<Post>()


    val likes = mutableStateListOf<Int?>()
    val privateLikes = mutableStateListOf<Int?>()
    val comments = mutableStateListOf<Int?>()
    val privateComments = mutableStateListOf<Int?>()

    suspend fun pathToDownloadUrl(path: String): String {
        return postRepository.downloadUrl(path)
    }

    fun getLikesAndComments(index:Int, private:Boolean){
        //checking if its not loading out of bound
        if (private && index >= allMyPrivatePosts.size) return
        if (!private && index >= allMyPosts.size) return

        Log.i("yuyu","${index} ${private} ${allMyPrivatePosts.size} ${allMyPosts.size}")

        val postId = when(private){ true -> allMyPrivatePosts[index].postId else -> allMyPosts[index].postId}
        postId?.let {
            CoroutineScope(Dispatchers.IO).launch {
                postRepository.getLikeCount(postId).collect {
                    when (it) {
                        is Resource.Success -> {
                            it.data?.let { data ->
                                if (private){
                                    privateLikes[index] = data
                                }else{
                                    likes[index] = data
                                }
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
                                Log.i("cunter","$data comments")
                                if (private){
                                    privateComments[index] = data
                                }else{
                                    comments[index] = data
                                }
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



    fun getMyPosts(private: Boolean? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            postRepository.getMyPosts(private).collect {
                when (it) {
                    is Resource.Success -> {

                        it.data?.let { d->
                            if (private == true) {
                                privateLikes.clear()
                                privateLikes.addAll(d.map { null })
                                privateComments.clear()
                                privateComments.addAll(d.map { null })

                                isLoadingPrivatePosts = false
                                allMyPrivatePosts.clear()
                                allMyPrivatePosts.addAll(d)
                            } else {
                                likes.clear()
                                likes.addAll(d.map { null })
                                comments.clear()
                                comments.addAll(d.map { null })

                                isLoadingPosts = false
                                allMyPosts.addAll(d)
                            }
                        }


                        Log.i("zzzz", it.data.toString())
                    }
                    is Resource.Error -> {
                        _eventFlow.emit(ProfileEvent.showSnackbar(message = "Couldn't get posts"))
                        if (private == true) {
                            isLoadingPrivatePosts = false
                        } else {
                            isLoadingPosts = false
                        }
                    }
                    is Resource.Loading -> {
                        if (private == true) {
                            allMyPrivatePosts.clear()
                            isLoadingPrivatePosts = true
                        } else {
                            allMyPosts.clear()
                            isLoadingPosts = true
                        }
                    }
                }
            }
        }
    }

    fun updateProfilePhoto(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            user?.let { u ->
                userRepository.updateProfilePicture(user = u, uri = uri).collect {
                    when (it) {
                        is Resource.Error -> {
                            Log.i("yayy", it.message.toString())
                            _eventFlow.emit(ProfileEvent.showSnackbar(message = "Error updating profile photo"))
                        }
                        is Resource.Success -> {
                            user = user?.copy(profilePhoto = it.data.toString())
                            user?.let { newUserInfo ->
                                userRepository.updateUserInfo(newUserInfo).collect()
                            }
                            _eventFlow.emit(ProfileEvent.showSnackbar(message = "Profile photo updated successfully"))
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    fun updateCoverImage(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            user?.let { u ->
                userRepository.updateCoverPicture(user = u, uri = uri).collect {
                    when (it) {
                        is Resource.Error -> {
                            Log.i("yayy", it.message.toString())
                            _eventFlow.emit(ProfileEvent.showSnackbar(message = "Error updating cover photo"))
                        }
                        is Resource.Success -> {
                            user = user?.copy(coverPhoto = it.data.toString())
                            user?.let { newUserInfo ->
                                userRepository.updateUserInfo(newUserInfo).collect()
                            }
                            _eventFlow.emit(ProfileEvent.showSnackbar(message = "Cover photo updated successfully"))
                        }
                        else -> {}
                    }
                }
            }
        }
    }


    fun reloadUserInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            userRepository.currentUserInfo().collect {
                when (it) {
                    is Resource.Error -> {
                        isLoadingProfile = false
                        Log.i("yayy", it.message.toString())
                        _eventFlow.emit(ProfileEvent.showSnackbar(message = "Error loading user infos"))
                    }
                    is Resource.Success -> {
                        isLoadingProfile = false
                        user = it.data
                    }
                    is Resource.Loading -> isLoadingProfile = true
                }
            }
        }
    }


}
