package com.itshedi.weedworld.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.itshedi.weedworld.repository.user_repository.UserRepository
import com.itshedi.weedworld.utils.Resource
import com.itshedi.weedworld.utils.isNumberOnly
import com.itshedi.weedworld.utils.isValidEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthEvent {
    data class showSnackbar(val message: String) : AuthEvent()
    object authenticated : AuthEvent()
    object loading : AuthEvent()
    object empty : AuthEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {


    private val _eventFlow = MutableSharedFlow<AuthEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    //login
    var loginEmail by mutableStateOf("")
    var loginPassword by mutableStateOf("")

    var ageAgreed by mutableStateOf(false)
    var ageAgreedError by mutableStateOf(false)

    //login errors
    var loginEmailError by mutableStateOf(false)
    var loginPasswordError by mutableStateOf(false)


    //register
    var registerEmail by mutableStateOf("")
    var registerPassword by mutableStateOf("")
    var registerUsername by mutableStateOf("")
    var registerPhone by mutableStateOf("")
    var registerBirthDate by mutableStateOf("")
    var registerGender by mutableStateOf("")

    //register errors
    var registerEmailError by mutableStateOf(false)
    var registerPasswordError by mutableStateOf(false)
    var registerUsernameError by mutableStateOf(false)
    var registerPhoneError by mutableStateOf(false)
    var registerBirthDateError by mutableStateOf(false)
    var registerGenderError by mutableStateOf(false)


    fun login(email: String, password: String) = CoroutineScope(Dispatchers.IO).launch {
        if (!email.isValidEmail()) {
            loginEmailError = true
        } else if (password.isBlank()) {
            loginPasswordError = true
        } else {
            userRepository.login(email, password).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _eventFlow.emit(AuthEvent.showSnackbar(message = "Logged in successfully"))
                        delay(500)
                        _eventFlow.emit(AuthEvent.authenticated)
                    }
                    is Resource.Loading -> {
                        _eventFlow.emit(AuthEvent.loading)
                        delay(2000) //todo: remove
                    }
                    is Resource.Error -> {
                        _eventFlow.emit(AuthEvent.showSnackbar(message = "Invalid credentials"))
                    }
                }
            }
        }

    }

    fun clearRegisterForm(){
        registerBirthDate = ""
        registerUsername = ""
        registerEmail =""
        registerPassword = ""
        registerGender =""
        registerPhone =""
    }
    fun register(
        email: String,
        password: String,
        birthdate: String,
        username: String,
        gender: String,
        phone: String
    ) {
        val launch = CoroutineScope(Dispatchers.IO).launch {
            if (username.isBlank()) {
                registerUsernameError = true
            } else if (password.isBlank()) {
                registerPasswordError = true
            } else if (!email.isValidEmail()) {
                registerEmailError = true
            } else if (phone.length < 8 || !phone.isNumberOnly()) {
                registerPhoneError = true
            } else if (birthdate.isBlank()) {
                registerBirthDateError = true
            } else if (gender.isBlank()) {
                registerGenderError = true
            } else if (!ageAgreed) {
                ageAgreedError = true
            } else {
                userRepository.register(
                    email = email,
                    password = password,
                    birthdate = birthdate,
                    username = username,
                    gender = gender,
                    phone = phone
                ).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _eventFlow.emit(AuthEvent.showSnackbar(message = "User registered successfully"))
                            clearRegisterForm()
                            _eventFlow.emit(AuthEvent.authenticated)
                        }
                        is Resource.Loading -> {
                            _eventFlow.emit(AuthEvent.loading)
                            delay(1000) //todo: remove
                        }
                        is Resource.Error -> {
                            _eventFlow.emit(AuthEvent.showSnackbar(message = "Error registring user"))
                        }
                    }
                }
            }
        }
    }
}


