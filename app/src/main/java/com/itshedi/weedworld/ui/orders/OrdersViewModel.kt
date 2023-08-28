package com.itshedi.weedworld.ui.orders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.itshedi.weedworld.repository.post_repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    var currentPage by mutableStateOf(0)

    //note: replace with corresponding model
    val pastOrders = mutableStateListOf<Int>()
    val pickupOrders = mutableStateListOf<Int>()

    fun loadMyOrders(){

    }
}