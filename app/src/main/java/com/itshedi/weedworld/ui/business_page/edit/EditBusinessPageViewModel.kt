package com.itshedi.weedworld.ui.business_page.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.itshedi.weedworld.entities.Store
import com.itshedi.weedworld.repository.store_repository.StoreRepository
import com.itshedi.weedworld.ui.business_page.AddProductEvent
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


sealed class EditBusinessPageEvent {
    data class showSnackbar(val message: String) : EditBusinessPageEvent()
    object loading : EditBusinessPageEvent()
    object empty : EditBusinessPageEvent()

    object done : EditBusinessPageEvent()
}


@HiltViewModel
class EditBusinessPageViewModel @Inject constructor(
    private val storeRepository: StoreRepository
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<EditBusinessPageEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val daysList = listOf(
        "Mon","Tue","Wed","Thu","Fri","Sat","Sun"
    )
    var business by mutableStateOf<Store?>(null)

    var phone by mutableStateOf("")
    var address by mutableStateOf("") //dont know if this should be editable
    var emailAddress by mutableStateOf("")
    var website by mutableStateOf("")
    var bio by mutableStateOf("")
    var fromTime by mutableStateOf("")
    var toTime by mutableStateOf("")
    var days = mutableStateListOf<Int>()


    var daysMenu by mutableStateOf(false)
    var fromTimeMenu by mutableStateOf(false)
    var toTimeMenu by mutableStateOf(false)

    var phoneError by mutableStateOf(false)
    var emailAddressError by mutableStateOf(false)
    var websiteError by mutableStateOf(false)


    fun submit() {
        if (phone.isBlank() || !phone.isNumberOnly()) {
            phoneError = true
        } else if (emailAddress.isBlank() || !emailAddress.isValidEmail()) {
            emailAddressError = true
        } else if (website.isBlank()) {
            websiteError = true
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                storeRepository.updateStore(
                    storeId = business!!.storeId!!,
                    phone = phone,
                    email = emailAddress,
                    website = website,
                    bio = bio,
                    fromTime = fromTime,
                    toTime = toTime,
                    days = days
                ).collect {
                    when (it) {
                        is Resource.Loading -> {
                            _eventFlow.emit(EditBusinessPageEvent.loading)
                        }
                        is Resource.Error -> {
                            _eventFlow.emit(EditBusinessPageEvent.showSnackbar(message = "Error updating store information"))
                        }
                        is Resource.Success -> {
                            _eventFlow.emit(EditBusinessPageEvent.showSnackbar(message = "Store information updated successfully"))
                            delay(1500)
                            _eventFlow.emit(EditBusinessPageEvent.done)
                        }
                    }
                }
            }
        }
    }
}