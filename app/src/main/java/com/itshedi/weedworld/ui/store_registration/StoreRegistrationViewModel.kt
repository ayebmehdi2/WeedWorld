package com.itshedi.weedworld.ui.store_registration

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.itshedi.weedworld.repository.store_repository.StoreRepository
import com.itshedi.weedworld.utils.Resource
import com.itshedi.weedworld.utils.isValidEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject


sealed class StoreRegistrationEvent {
    data class showSnackbar(val message: String) : StoreRegistrationEvent()
    object success : StoreRegistrationEvent()
    object loading : StoreRegistrationEvent()
    object empty : StoreRegistrationEvent()
}

@HiltViewModel
class StoreRegistrationViewModel @Inject constructor(
    private val storeRepository: StoreRepository
) : ViewModel() {
    var currentPage by mutableStateOf<StoreRegistrationPage>(StoreRegistrationPage.StoreType)
    var storeType by mutableStateOf(0)

    var businessTypeMenu by mutableStateOf(false)
    var useForMenu by mutableStateOf(false)

    var lat by mutableStateOf<Double?>(null)
    var lng by mutableStateOf<Double?>(null)

    var businessType by mutableStateOf("")
    var businessName by mutableStateOf("")
    var useFor by mutableStateOf("")
    var phone by mutableStateOf("")
    var address by mutableStateOf("")
    var licence by mutableStateOf("")
    var emailAddress by mutableStateOf("")
    var website by mutableStateOf("")
    var licencePDF by mutableStateOf<Uri?>(null)

    var businessTypeError by mutableStateOf(false)
    var businessNameError by mutableStateOf(false)
    var useForError by mutableStateOf(false)
    var phoneError by mutableStateOf(false)
    var addressError by mutableStateOf(false)
    var licenceError by mutableStateOf(false)
    var emailAddressError by mutableStateOf(false)
    var websiteError by mutableStateOf(false)
    var licencePDFError by mutableStateOf(false)

    var billingInfoError by mutableStateOf(false)

    var tosAgreed by mutableStateOf(false)

    val businessTypes = listOf(
        "Cannabis store",
        "Delivery Service", "CBD/Hemp", "Hydrostore", "Doctor", "other"
    )

    val useForTypes = listOf("Recreational", "Medicinal")


    private val _eventFlow = MutableSharedFlow<StoreRegistrationEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun isTHCRegistration(): Boolean {
        return businessType in listOf("Delivery Service", "Cannabis store")
    }

    fun isLocalStoreRegistration(): Boolean {
        return storeType == 1
    }




    fun registerStore() {
        if (businessType.isBlank()) {
            businessTypeError = true
        } else if (businessName.isBlank()) {
            businessNameError = true
        } else if (phone.isBlank()) {
            phoneError = true
        } else if ((address.isBlank() || lat == null || lng == null) && isLocalStoreRegistration()) {
            addressError = true
        } else if (useFor.isBlank() && isTHCRegistration()) {
            useForError = true
        } else if (licence.isBlank()) {
            licenceError = true
        } else if (emailAddress.isBlank() || !emailAddress.isValidEmail()) {
            emailAddressError = true
        } else if (licencePDF == null && isTHCRegistration()) {
            licencePDFError = true
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                when (isLocalStoreRegistration() || isTHCRegistration()) {
                    true -> storeRepository.registerStore(
                        businessType = businessType,
                        businessName = businessName,
                        useFor = useFor,
                        phone = phone,
                        address = address,
                        licence = licence,
                        emailAddress = emailAddress,
                        website = website,
                        licencePDF = licencePDF,
                        lat = lat!!,
                        lng = lng!!
                    )
                    false -> storeRepository.addStore(
                        businessType = businessType,
                        businessName = businessName,
                        phone = phone,
                        licence = licence,
                        emailAddress = emailAddress,
                        website = website,
                    )
                }.collect {
                    when (it) {
                        is Resource.Loading -> {
                            _eventFlow.emit(StoreRegistrationEvent.loading)
                        }
                        is Resource.Error -> {
                            _eventFlow.emit(StoreRegistrationEvent.showSnackbar(message = "Error registring store"))
                        }
                        is Resource.Success -> {
                            when(isLocalStoreRegistration() || isTHCRegistration()){
                                true ->
                                    _eventFlow.emit(StoreRegistrationEvent.showSnackbar(message = "Store registration request submitted successfully"))
                                false ->
                                    _eventFlow.emit(StoreRegistrationEvent.showSnackbar(message = "Store created successfully"))
                            }
                            delay(2000)
                            _eventFlow.emit(StoreRegistrationEvent.success)
                        }
                    }
                }
            }
        }
    }
}

sealed class StoreRegistrationPage {
    object StoreType : StoreRegistrationPage()
    object Registration : StoreRegistrationPage()
    object BillingInfo : StoreRegistrationPage()
    object LocationMap : StoreRegistrationPage()
}