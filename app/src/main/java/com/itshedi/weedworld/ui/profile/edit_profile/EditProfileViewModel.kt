package com.itshedi.weedworld.ui.profile.edit_profile

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.itshedi.weedworld.entities.UserInfo
import com.itshedi.weedworld.repository.user_repository.UserRepository
import com.itshedi.weedworld.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class EditProfileEvent {
    data class showSnackbar(val message: String) : EditProfileEvent()
    object loading : EditProfileEvent()
    object empty : EditProfileEvent()
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) :  ViewModel() {

    var user by mutableStateOf<UserInfo?>(null)

    init {
        Log.i("yayy","init EditProfileViewModel")
        reloadUserInfo()
    }

    private val _eventFlow = MutableSharedFlow<EditProfileEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    var username by mutableStateOf("")
    var email by mutableStateOf("")
    var wwID by mutableStateOf("")
    var gender by mutableStateOf("")
    var password by mutableStateOf("******")
    var birthday by mutableStateOf("")
    var location by mutableStateOf("")

    var usernameError by mutableStateOf(false)
    var wwIDError by mutableStateOf(false)
    var genderError by mutableStateOf(false)
    var passwordError by mutableStateOf(false)
    var birthdateError by mutableStateOf(false)
    var localtionError by mutableStateOf(false)

    fun updateProfile(
    ) = CoroutineScope(Dispatchers.IO).launch {
        //todo: what's WWID ?
    }



    fun reloadUserInfo(){
        CoroutineScope(Dispatchers.IO).launch {
            userRepository.currentUserInfo().collect {
                when (it){
                    is Resource.Error -> {
                        Log.i("yayy", it.message.toString())
                    }
                    is Resource.Success -> {
                        user = it.data
                        user?.let { userInfo ->
                            username = userInfo.username?:""
                            email = userInfo.email?:""
                            wwID = userInfo.uid?:""
                            gender = userInfo.gender?:""
                            birthday = userInfo.birthday?:""
                            location = userInfo.location?:""
                        }
                    }
                    else -> {}
                }
            }
        }
    }

}