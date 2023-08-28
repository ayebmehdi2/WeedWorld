package com.itshedi.weedworld.ui.posts.product_post

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itshedi.weedworld.entities.CartItem
import com.itshedi.weedworld.entities.Product
import com.itshedi.weedworld.repository.post_repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named


@HiltViewModel
class ProductViewModel @Inject constructor(
    private val postRepository: PostRepository, //todo: create new repo for shopping
    @Named("preferences_inject") private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : ViewModel() {
    var product by mutableStateOf<Product?>(null)
    var multiplier by mutableStateOf(1)

    var items = mutableStateListOf<CartItem>()

    var isProductAlreadyInCart by mutableStateOf(true)

    fun addItem(){
        Log.i("hedii","product: ${product}")
        items.add(CartItem(productId = product!!.productId!!, storeId = product!!.storeId!!, count = multiplier))
        saveCartItems()
    }

    private fun saveCartItems(){
        CoroutineScope(Dispatchers.IO).launch {
            val stringListObject : String = gson.toJson(items)
            sharedPreferences.edit().putString("CART", stringListObject).apply()
            isProductAlreadyInCart = true
        }
    }

    fun getCartItems(){
        CoroutineScope(Dispatchers.IO).launch {
            val cartItems = sharedPreferences.getString("CART", "[]")
            val listType = object : TypeToken<ArrayList<CartItem>>() {}.type
            val listObjects : ArrayList<CartItem> = gson.fromJson(cartItems,listType)
            items.clear()
            items.addAll(listObjects)

            isProductAlreadyInCart = items.firstOrNull { it.productId == product!!.productId } != null
            if(isProductAlreadyInCart){
                multiplier = items.first { it.productId == product!!.productId }.count
            }
        }
    }
}