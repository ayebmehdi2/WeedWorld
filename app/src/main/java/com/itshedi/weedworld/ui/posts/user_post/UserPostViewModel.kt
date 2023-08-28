package com.itshedi.weedworld.ui.posts.user_post

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.itshedi.weedworld.entities.*
import com.itshedi.weedworld.repository.post_repository.PostRepository
import com.itshedi.weedworld.repository.store_repository.StoreRepository
import com.itshedi.weedworld.repository.user_repository.UserRepository
import com.itshedi.weedworld.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class PostAuthor(
    val name:String?,
    val photo:String?
)

@HiltViewModel
class UserPostViewModel @Inject constructor(
    private val userRepository: UserRepository, private val postRepository: PostRepository,
    private val storeRepository: StoreRepository,
) : ViewModel() {

    var isLoadingComments by mutableStateOf(false)

    var isMyPost by mutableStateOf(false)

    var post by mutableStateOf<Post?>(null)

    var author by mutableStateOf<PostAuthor?>(null)

    var store by mutableStateOf<UserInfo?>(null)

    var me by mutableStateOf<UserInfo?>(null)

    var likes = mutableStateListOf<Like>()
    var comments = mutableStateListOf<Comment>()

    var commentators = mutableStateMapOf<String,UserInfo>()

    var isLiked by mutableStateOf(false)

    var myComment by mutableStateOf("")

    var deleteConfirmDialog by mutableStateOf(false)
    var repostConfirmDialog by mutableStateOf<String?>(null)

    var viewAllComments by mutableStateOf(false)

    var editValue by mutableStateOf("")
    var isEditing by mutableStateOf(false)

    var outputDateFormatter = SimpleDateFormat("MMMM dd,yyyy")


    fun loadCommentator(userId:String){
        if (!commentators.containsKey(userId)){
            getUserById(userId, onResult = {
                commentators[userId] = it
            })
        }
    }
    fun saveEdit(onUpdated : () -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            postRepository.updatePost(postId = post!!.postId!!, content = editValue).collect {
                when (it){
                    is Resource.Error -> {
                    }
                    is Resource.Success -> {
                        post = post!!.copy(text = editValue)
                        onUpdated()
                    }
                    is Resource.Loading -> {

                    }
                }
            }
        }
    }

    fun checkIfMyPost(){
        isMyPost = FirebaseAuth.getInstance().currentUser?.uid == post?.userId
    }

    fun dateFormatter(date: Date?):String{
        if (date==null) return ""
        return outputDateFormatter.format(date)
    }
    fun loadMe(){
        CoroutineScope(Dispatchers.IO).launch {
            userRepository.currentUserInfo().collect {
                when (it){
                    is Resource.Error -> {
                        Log.i("yayy", it.message.toString())
                    }
                    is Resource.Success -> {
                        me = it.data
                        Log.i("yayy", it.data.toString())
                    }
                    is Resource.Loading -> {

                    }
                }
            }
        }
    }

    fun deletePost(onDeleted:()-> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            postRepository.deletePost(postId = post!!.postId!!).collect {
                when (it) {
                    is Resource.Loading -> {

                    }
                    is Resource.Success -> {
                        onDeleted()
                    }
                    is Resource.Error -> {

                    }
                }
            }
        }
    }

    fun addComment(){
        CoroutineScope(Dispatchers.IO).launch {
            postRepository.commentPost(content = myComment, postId = post!!.postId!!).collect {
                when (it) {
                    is Resource.Loading -> {

                    }
                    is Resource.Success -> {
                        getComments(post!!.postId!!)
                        myComment = ""
                    }
                    is Resource.Error -> {

                    }
                }
            }
        }
    }

    fun repost(){
        CoroutineScope(Dispatchers.IO).launch {
            postRepository.sharePost(postId = post!!.postId!!).collect {
                when (it) {
                    is Resource.Loading -> {

                    }
                    is Resource.Success -> {
                        getComments(post!!.postId!!)
                        myComment = ""
                    }
                    is Resource.Error -> {

                    }
                }
            }
        }
    }

    fun getLikes(postId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            postRepository.getLikes(postId).collect {
                when (it) {
                    is Resource.Success -> {
                        it.data?.let { data ->
                            likes.clear()
                            likes.addAll(data)

                            isLiked = likes.firstOrNull { like-> like.userId==FirebaseAuth.getInstance().currentUser?.uid } != null
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

    fun getComments(postId: String) {
        isLoadingComments = true
        CoroutineScope(Dispatchers.IO).launch {
            delay(200)
            postRepository.getComments(postId).collect {
                when (it) {
                    is Resource.Success -> {
                        it.data?.let { data ->
                            comments.clear()
                            comments.addAll(data)
                        }
                        isLoadingComments = false
                    }
                    is Resource.Error -> {
                        Log.i("zzzz", it.message.toString())
                        isLoadingComments = false
                    }
                    is Resource.Loading -> {
                        isLoadingComments = false
                    }
                }
            }
        }
    }

    fun loadAuthor() {
        post?.let {
            if(it.storeId!=null){
                getStoreById(id = it.storeId, onResult = {
                    author = PostAuthor(name = it.businessName, photo = it.photo)
                })
            }else{
                getUserById(id = it.userId!!, onResult = {
                    author = PostAuthor(name = it.username, photo = it.profilePhoto)
                })
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


    fun getStoreById(id: String, onResult: (Store) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            storeRepository.getStoreById(id).collect {
                when (it) {
                    is Resource.Success -> {
                        it.data?.let { onResult(it) }
                    }
                    is Resource.Error -> {
                    }
                    is Resource.Loading -> {

                    }
                }
            }
        }
    }

    fun likePost() {
        Log.i("fcb", "likePost")
        CoroutineScope(Dispatchers.IO).launch {
            when (isLiked) {
                true -> postRepository.dislikePost(postId = post!!.postId!!)
                false -> postRepository.likePost(postId = post!!.postId!!)
            }.collect {
                when (it) {
                    is Resource.Loading -> {

                    }
                    is Resource.Success -> {
                        isLiked = !isLiked
                        getLikes(post!!.postId!!)
                    }
                    is Resource.Error -> {

                    }
                }
            }
        }
    }
}