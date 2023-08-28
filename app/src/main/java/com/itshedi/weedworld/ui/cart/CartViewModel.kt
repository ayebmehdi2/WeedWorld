package com.itshedi.weedworld.ui.cart

import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itshedi.weedworld.entities.CartItem
import com.itshedi.weedworld.entities.Product
import com.itshedi.weedworld.entities.Store
import com.itshedi.weedworld.repository.store_repository.StoreRepository
import com.itshedi.weedworld.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class CartViewModel @Inject constructor(
    private val storeRepository: StoreRepository, //todo: create new repo for shopping
    @Named("preferences_inject") private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : ViewModel() {
    var items = mutableStateListOf<CartItem>()

    var stores = mutableStateMapOf<String, Store>()

    var products = mutableStateMapOf<String, Product>()

    fun deleteItem(productId: String) {
        items.removeIf { it.productId == productId }
        saveCartItems()
    }

    fun deleteCheckedOutItems(storeId: String) {
        items.removeIf { it.storeId == storeId }
        saveCartItems()
    }



    fun setCount(productId: String, count: Int) {
        val index = items.indexOfFirst { it.productId == productId }
        items[index] = items[index].copy(count = count)
        saveCartItems()
    }

    private fun saveCartItemsAfterDeletion(productIdsToRemove: List<String> ) {
        val updatedItems = items.filterNot { it.productId in productIdsToRemove }
        val stringListObject: String = gson.toJson(updatedItems)
        sharedPreferences.edit().putString("CART", stringListObject).apply()
    }


    private fun saveCartItems() {
        val stringListObject: String = gson.toJson(items)
        sharedPreferences.edit().putString("CART", stringListObject).apply()
    }

    fun getStoreById(storeId: String) {
        if (!stores.keys.contains(storeId)) {
            CoroutineScope(Dispatchers.IO).launch {
                storeRepository.getStoreById(storeId).collect {
                    when (it) {
                        is Resource.Success -> {
                            it.data?.let { store -> stores[storeId] = store }
                        }

                        is Resource.Error -> {

                        }

                        else -> {}
                    }
                }
            }
        }
    }

    fun getProductById(productId: String) {
        if (!products.keys.contains(productId)) {
            CoroutineScope(Dispatchers.IO).launch {
                storeRepository.getProductById(productId).collect {
                    when (it) {
                        is Resource.Success -> {
                            it.data?.let { product -> products[productId] = product }
                        }

                        is Resource.Error -> {

                        }

                        else -> {}
                    }
                }
            }
        }

    }

    fun getCartItems() {
        CoroutineScope(Dispatchers.IO).launch {
            val cartItems = sharedPreferences.getString("CART", "[]")

            val listType = object : TypeToken<ArrayList<CartItem>>() {}.type
            val listObjects: ArrayList<CartItem> = gson.fromJson(cartItems, listType)
            items.clear()
            items.addAll(listObjects)
        }
    }
}

