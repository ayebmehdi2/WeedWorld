package com.itshedi.weedworld.ui.business_page

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.itshedi.weedworld.entities.Post
import com.itshedi.weedworld.entities.Product
import com.itshedi.weedworld.entities.Store
import com.itshedi.weedworld.repository.post_repository.PostRepository
import com.itshedi.weedworld.repository.store_repository.StoreRepository
import com.itshedi.weedworld.ui.feed.FeedEvent
import com.itshedi.weedworld.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject


sealed class BusinessPageEvent {
    data class showSnackbar(val message: String) : BusinessPageEvent()
    object loading : BusinessPageEvent()
    object empty : BusinessPageEvent()
}


@HiltViewModel
class BusinessPageViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<BusinessPageEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    var isMyStore by mutableStateOf(true)

    var storeId by mutableStateOf<String?>(null)

    var business by mutableStateOf<Store?>(null)
    var isLoadingBusiness by mutableStateOf(false)

    var isLoadingProducts by mutableStateOf(false)
    var isLoadingPosts by mutableStateOf(false)

    var showInfoPopup by mutableStateOf(false)

    var deleteConfirmDialog by mutableStateOf(false)

    var productToDelete by mutableStateOf<String?>(null)

    var products = mutableStateListOf<Product>()

    var posts = mutableStateListOf<Post>()
    val likes = mutableStateListOf<Int?>()
    val comments = mutableStateListOf<Int?>()


    var filter by mutableStateOf("All")

    var selectedSection by mutableStateOf(0)

    var job: Job? = null


    fun loadPosts(){
        isLoadingPosts = true
        business?.storeId?.let{
            posts.clear()
            CoroutineScope(Dispatchers.IO).launch {
                postRepository.getStorePosts(it).collect {
                    when (it) {
                        is Resource.Success -> {
                            isLoadingPosts = false
                            it.data?.let{ d ->
                                likes.clear()
                                likes.addAll(d.map { null })
                                comments.clear()
                                comments.addAll(d.map { null })
                                posts.addAll(d)
                            }
                        }
                        is Resource.Error -> {
                            _eventFlow.emit(BusinessPageEvent.showSnackbar(message = "Couldn't get posts"))
                            isLoadingPosts = false
                        }
                        is Resource.Loading -> {
                            isLoadingPosts = true
                        }
                    }
                }
            }
        }
    }

    fun getLikesAndComments(index:Int){
        val postId =  posts[index].postId
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

                        }
                        is Resource.Loading -> {

                        }
                    }
                }
            }
        }
    }

    fun deleteProduct(onDeleted: () -> Unit) {
        productToDelete?.let {
            CoroutineScope(Dispatchers.IO).launch {
                storeRepository.deleteProduct(productId = it).collect {
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
    }

    fun loadProducts() {
        isLoadingProducts = true
        business?.let { b ->
            job?.cancel()
            job = CoroutineScope(Dispatchers.IO).launch {
                delay(400)
                storeRepository.getProducts(
                    storeId = business!!.storeId!!, category = when (filter == "All") {
                        true -> null
                        else -> filter
                    }
                ).collect {
                    when (it) {
                        is Resource.Loading -> {
                            isLoadingProducts = true
                        }
                        is Resource.Error -> {
                            Log.i("yayy", it.message.toString())
                            _eventFlow.emit(BusinessPageEvent.showSnackbar(message = "Error loading products"))
                            isLoadingProducts = false
                        }
                        is Resource.Success -> {
                            it.data?.let { data ->
                                products.clear()
                                products.addAll(data)
                                isLoadingProducts = false
                            }
                        }
                    }
                }
            }
        }
    }

    fun updatePhoto(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            business?.let { b ->
                storeRepository.updatePicture(store = b, uri = uri).collect {
                    when (it) {
                        is Resource.Error -> {
                            Log.i("yayy", it.message.toString())
                            _eventFlow.emit(BusinessPageEvent.showSnackbar(message = "Error updating photo"))
                        }
                        is Resource.Success -> {
                            business = business?.copy(photo = it.data.toString())
                            _eventFlow.emit(BusinessPageEvent.showSnackbar(message = "Photo updated successfully"))
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    fun updateCoverPhoto(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            business?.let { b ->
                storeRepository.updateCoverPicture(store = b, uri = uri).collect {
                    when (it) {
                        is Resource.Error -> {
                            Log.i("yayy", it.message.toString())
                            _eventFlow.emit(BusinessPageEvent.showSnackbar(message = "Error updating cover photo"))
                        }
                        is Resource.Success -> {
                            business = business?.copy(coverPhoto = it.data.toString())
                            _eventFlow.emit(BusinessPageEvent.showSnackbar(message = "Photo updated cover successfully"))
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    fun loadStore() {
        storeId?.let{
            CoroutineScope(Dispatchers.IO).launch {
                storeRepository.getStoreById(id = it).collect {
                    when (it) {
                        is Resource.Loading -> {
                            isLoadingBusiness = true
                        }
                        is Resource.Error -> {
                            isLoadingBusiness = false
                            Log.i("yayy", it.message.toString())
                        }
                        is Resource.Success -> {
                            business = it.data
                            isLoadingBusiness = false
                            Log.i("yayy", it.data.toString())
                        }
                    }
                }
            }
        }
    }
}