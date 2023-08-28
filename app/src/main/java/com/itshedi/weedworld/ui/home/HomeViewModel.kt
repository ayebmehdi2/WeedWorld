package com.itshedi.weedworld.ui.home

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itshedi.weedworld.entities.CartItem
import com.itshedi.weedworld.entities.Store
import com.itshedi.weedworld.entities.UserInfo
import com.itshedi.weedworld.repository.store_repository.StoreRepository
import com.itshedi.weedworld.repository.user_repository.UserRepository
import com.itshedi.weedworld.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val storeRepository: StoreRepository,
    @Named("preferences_inject") private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : ViewModel() {

    var cartCount by mutableStateOf(0)

    var user by mutableStateOf<UserInfo?>(null)
    var business by mutableStateOf<Store?>(null)

    var showQrCodeDialog by mutableStateOf(false)
    var showBusinessQrCodeDialog by mutableStateOf(false)

    var isLoadingBusiness by mutableStateOf(false) // use this so user can't access upgrades menu when we still don't know if he already have registred a business/ store

    fun initialize(){
        CoroutineScope(Dispatchers.IO).launch {
            userRepository.currentUserInfo().collect {
                when (it){
                    is Resource.Error -> {
                        Log.i("yayy", it.message.toString())
                    }
                    is Resource.Success -> {
                        user = it.data
                        Log.i("yayy", it.data.toString())
                    }
                    else -> {}
                }
            }
        }
        getMyStore()
    }

    fun getMyStore(){
        CoroutineScope(Dispatchers.IO).launch {
            storeRepository.myStore().collect {
                when (it){
                    is Resource.Loading -> {
                        isLoadingBusiness = true
                    }
                    is Resource.Error -> {
                        isLoadingBusiness = false
                    }
                    is Resource.Success -> {
                        business = it.data
                        isLoadingBusiness = false
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
            cartCount = listObjects.size
        }
    }

    fun logout() {
        clearCart()
        userRepository.logout()
    }

    fun clearCart(){
        sharedPreferences.edit().putString("CART", "[]").apply()
    }

}