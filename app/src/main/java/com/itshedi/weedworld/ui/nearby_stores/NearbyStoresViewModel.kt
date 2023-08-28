package com.itshedi.weedworld.ui.nearby_stores

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.itshedi.weedworld.entities.Product
import com.itshedi.weedworld.entities.Store
import com.itshedi.weedworld.repository.nearby_repository.NearbyRepository
import com.itshedi.weedworld.repository.store_repository.StoreRepository
import com.itshedi.weedworld.repository.user_repository.UserRepository
import com.itshedi.weedworld.ui.market.MarketEvent
import com.itshedi.weedworld.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.net.URL
import javax.inject.Inject


@HiltViewModel
class NearbyStoresViewModel @Inject constructor(
    private val nearbyRepository: NearbyRepository, private val storeRepository: StoreRepository
) : ViewModel() {

    val stores = mutableStateListOf<Store>()
    val storeBitmaps = mutableStateMapOf<String, BitmapDescriptor?>()

    var myLocation by mutableStateOf<Location?>(null)

    var lat by mutableStateOf<Double?>(null)
    var lng by mutableStateOf<Double?>(null)

    var isLoading by mutableStateOf(false)

    var filter by mutableStateOf("All")

    var searchQuery by mutableStateOf("")

    var mapsRadius by mutableStateOf<Double>(15.0)

    var currentStoreDetails by mutableStateOf<Store?>(null)
    var storeProducts = mutableStateMapOf<String,List<Product>>()

    var job: Job? = null

    var productLoadingJob: Job? = null

    fun loadBitmap(url: String) {
        if (!storeBitmaps.keys.contains(url)) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    withContext(Dispatchers.IO) {
                        storeBitmaps[url] = BitmapDescriptorFactory.fromBitmap(
                            Bitmap.createScaledBitmap(
                                BitmapFactory.decodeStream(
                                    URL(url).openConnection().getInputStream()
                                ), 100, 100, false
                            )
                        )
                    }
                } catch (e: java.lang.Exception) {
                    //todo:handle network error
                }
            }
        }
    }

    fun getSelectedStoreProducts(){
        productLoadingJob?.cancel()
        currentStoreDetails?.storeId?.let { storeId ->
            if (!storeProducts.containsKey(storeId)){
                productLoadingJob = CoroutineScope(Dispatchers.IO).launch {
                    delay(100)
                    storeRepository.getProducts(storeId = storeId, limit = 4
                    ).collect {
                        when (it) {
                            is Resource.Loading -> {
                            }
                            is Resource.Error -> {
                            }
                            is Resource.Success -> {
                                it.data?.let { data ->
                                    storeProducts[storeId] = data
                                }
                            }
                        }
                    }
                }
            }
        }

    }
    fun findStores() {
        lat?.let { lat ->
            lng?.let { lng ->
                job?.cancel()
                isLoading = true
                job = CoroutineScope(Dispatchers.IO).launch {
                    delay(1000)
                    Log.i("zaza", "looking for stores at zoom level: $mapsRadius")
                    when (searchQuery.isNotBlank()) {
                        true -> storeRepository.findStores(query = searchQuery)
                        else -> nearbyRepository.findNearbyStores(lat = lat, lng = lng, mapsRadius)
                    }.collect {
                        when (it) {
                            is Resource.Success -> {
                                Log.i("lstuff", "found ${it.data?.size} nearby stores")
                                it.data?.let { data ->
                                    stores.clear()
                                    stores.addAll(data)
                                }
                                isLoading = false
                            }
                            is Resource.Loading -> {
                                isLoading = true
                            }
                            is Resource.Error -> {
                                isLoading = false
                            }
                        }
                    }
                }
            }
        }
    }
}