package com.itshedi.weedworld.ui.nearby_users

import android.location.Location
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.itshedi.weedworld.AddPostEvent
import com.itshedi.weedworld.entities.UserInfo
import com.itshedi.weedworld.entities.UserLocation
import com.itshedi.weedworld.repository.nearby_repository.NearbyRepository
import com.itshedi.weedworld.repository.post_repository.PostRepository
import com.itshedi.weedworld.repository.user_repository.UserRepository
import com.itshedi.weedworld.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject


@HiltViewModel
class NearbyUsersViewModel @Inject constructor(
    private val nearbyRepository: NearbyRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val userLocations = mutableStateListOf<UserLocation>()
    val users = mutableStateMapOf<String,UserInfo>()

    var myLocation by mutableStateOf<Location?>(null)
    var isLoading by mutableStateOf(false)

    var job: Job? = null

    fun loadUserInfo(userId:String){
        getUserById(
            userId,
            onResult = { result -> users[userId] = result })
    }
    fun updateLocation(){
        myLocation?.let {
            CoroutineScope(Dispatchers.IO).launch {
                nearbyRepository.updateLocation(lat = it.latitude, lng = it.longitude).collect {
                    when (it) {
                        is Resource.Success -> {
                            Log.i("lstuff", "updated location")
                        }
                        is Resource.Loading -> {

                        }
                        is Resource.Error -> {
                            Log.i("lstuff", "errorrrrrr ${it.message}")
                        }
                    }
                }
            }
        }

    }

    fun getUserById(id: String, onResult: (UserInfo) -> Unit) {
        Log.i("lstuff", "getuserbyid ${id}")
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

    fun findUsers(){
        job?.cancel()
        userLocations.clear()
        myLocation?.let {
            job = CoroutineScope(Dispatchers.IO).launch {
                nearbyRepository.findNearbyUsers(lat = it.latitude, lng = it.longitude).collect {
                    when (it) {
                        is Resource.Success -> {
                            Log.i("lstuff", "found ${it.data?.size} nearby users")
                            it.data?.let { data ->
                                userLocations.addAll(data)
                            }
                            isLoading = false
                        }
                        is Resource.Loading -> {
                            isLoading = true
                        }
                        is Resource.Error -> {
                            Log.i("lstuff", "errorrrrrr ${it.message}")
                            isLoading = false
                        }
                    }
                }
            }
        }
    }
}