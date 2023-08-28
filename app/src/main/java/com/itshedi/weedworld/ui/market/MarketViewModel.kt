package com.itshedi.weedworld.ui.market

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.itshedi.weedworld.entities.Product
import com.itshedi.weedworld.entities.Store
import com.itshedi.weedworld.repository.nearby_repository.NearbyRepository
import com.itshedi.weedworld.repository.store_repository.StoreRepository
import com.itshedi.weedworld.ui.navigation.BottomNavScreens
import com.itshedi.weedworld.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject


sealed class MarketEvent {
    data class showSnackbar(val message: String) : MarketEvent()
    object loading : MarketEvent()
    object empty : MarketEvent()
}


@HiltViewModel
class MarketViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    private val nearbyRepository: NearbyRepository,
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<MarketEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    var products = mutableStateListOf<Product>()
    var stores = mutableStateListOf<Store>()
    var featuredStores = mutableStateListOf<Store>()

    var job: Job? = null

    var isLoadingProducts by mutableStateOf(false)
    var isLoadingStores by mutableStateOf(false)
    var isLoadingFeaturedStores by mutableStateOf(false)

    var showSearch by mutableStateOf(false)
    var searchQuery by mutableStateOf("")

    var filter by mutableStateOf("All")


    fun dismissSearch(){
        showSearch = false
        if (searchQuery.isNotBlank()){
            searchQuery = ""
            getProductsAndStores()
        }
    }


    fun findFeaturedStores(){
        CoroutineScope(Dispatchers.IO).launch {
            storeRepository.findStores(
                featured = true
            ).collect {
                when (it) {
                    is Resource.Loading -> {
                        isLoadingFeaturedStores = true
                    }
                    is Resource.Error -> {
                        _eventFlow.emit(MarketEvent.showSnackbar(message = "Error loading products"))
                        isLoadingFeaturedStores = false
                    }
                    is Resource.Success -> {
                        it.data?.let { data ->
                            featuredStores.clear()
                            featuredStores.addAll(data)
                            isLoadingFeaturedStores = false
                        }
                    }
                }
            }
        }
    }

    fun getProductsAndStores(){
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO).launch {
            if (searchQuery.isNotBlank()){
                isLoadingProducts = true
                isLoadingStores = true
                delay(800)
                //get products
                storeRepository.getProducts(
                    query = searchQuery,
                    category = when (filter == "All") {
                        true -> null
                        else -> filter
                    }
                ).collect {
                    when (it) {
                        is Resource.Loading -> {
                            isLoadingProducts = true
                        }
                        is Resource.Error -> {
                            _eventFlow.emit(MarketEvent.showSnackbar(message = "Error loading products"))
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
                //get stores
                storeRepository.findStores(
                    query = searchQuery.takeIf { it.isNotEmpty() }
                ).collect {
                    when (it) {
                        is Resource.Loading -> {
                            isLoadingStores = true
                        }
                        is Resource.Error -> {
                            _eventFlow.emit(MarketEvent.showSnackbar(message = "Error loading products"))
                            isLoadingStores = false
                        }
                        is Resource.Success -> {
                            it.data?.let { data ->
                                stores.clear()
                                stores.addAll(data)
                                isLoadingStores = false
                            }
                        }
                    }
                }
            }else{
                isLoadingProducts = false
                isLoadingStores = false
                stores.clear()
                products.clear()
            }

        }
    }
}