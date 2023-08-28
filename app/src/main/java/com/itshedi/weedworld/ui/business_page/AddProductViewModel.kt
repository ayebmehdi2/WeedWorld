package com.itshedi.weedworld.ui.business_page

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.itshedi.weedworld.entities.Product
import com.itshedi.weedworld.repository.store_repository.StoreRepository
import com.itshedi.weedworld.utils.ProductCategory
import com.itshedi.weedworld.utils.ProductSpecie
import com.itshedi.weedworld.utils.Resource
import com.itshedi.weedworld.utils.productCategories
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class AddProductEvent {
    data class showSnackbar(val message: String) : AddProductEvent()
    object loading : AddProductEvent()
    object empty : AddProductEvent()

    object productAdded : AddProductEvent()
}



@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val storeRepository: StoreRepository
) : ViewModel() {

    lateinit var storeId:String

    var editableProduct by mutableStateOf<Product?>(null)

    private val _eventFlow = MutableSharedFlow<AddProductEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val photoList = mutableStateListOf<Uri>()
    var primaryPhotoIndex by mutableStateOf(0)


    var productName by mutableStateOf("")
    var brandName by mutableStateOf("")
    var category by mutableStateOf("")
    var specie by mutableStateOf("")
    var thc by mutableStateOf("")
    var cbd by mutableStateOf("")
    var price by mutableStateOf("")
    var discountPrice by mutableStateOf("")
    var weight by mutableStateOf("")
    var description by mutableStateOf("")

    var categoryMenu by mutableStateOf(false)
    var specieMenu by mutableStateOf(false)

    var productNameError by mutableStateOf(false)
    var brandNameError by mutableStateOf(false)
    var categoryError by mutableStateOf(false)
    var specieError by mutableStateOf(false)
    var priceError by mutableStateOf(false)

    var thcError by mutableStateOf(false)
    var cbdError by mutableStateOf(false)
    var discountPriceError by mutableStateOf(false)
    var weightError by mutableStateOf(false)

    private fun String.doubleOrNull():Double? = when(this.isBlank()){ true -> null else -> this.toDouble()}

    fun isSpecieSelectionRequired(category:String):Boolean{
        // only product categories with isWeed = true require selecting specie type
        return productCategories.firstOrNull { it.name==category }?.isWeed ?: false
    }
    fun submit() {
        if (productName.isBlank()) {
            productNameError = true
        } else if (brandName.isBlank()) {
            brandNameError = true
        } else if (category.isBlank()) {
            categoryError = true
        } else if (specie.isBlank() && isSpecieSelectionRequired(category)) {
            specieError = true
        } else if (thc.isNotBlank() && (thc.toDouble()>100)) {
            thcError = true
        } else if (cbd.isNotBlank() && (cbd.toDouble()>100)) {
            cbdError = true
        } else if (price.isBlank()) {
            priceError = true
        } else if (discountPrice.isNotBlank() && discountPrice.toDouble() >= price.toDouble()) {
            discountPriceError = true
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                when(editableProduct){
                    null -> storeRepository.addProduct(
                        images = photoList.toMutableList().apply { this[0] = this[primaryPhotoIndex].also { this[primaryPhotoIndex] = this[0] } },
                        name = productName,
                        brand = brandName,
                        category = category,
                        specie = specie,
                        description = description,
                        price = price.toDouble(),
                        thc = thc.doubleOrNull(),
                        cbd = cbd.doubleOrNull(),
                        discountPrice = discountPrice.doubleOrNull(),
                        weight = weight.doubleOrNull(),
                        storeId = storeId
                    )
                    else -> storeRepository.updateProduct(
                        productId = editableProduct!!.productId!!,
                        images = photoList.toMutableList().apply { this[0] = this[primaryPhotoIndex].also { this[primaryPhotoIndex] = this[0] } },
                        name = productName,
                        brand = brandName,
                        category = category,
                        specie = specie,
                        description = description,
                        price = price.toDouble(),
                        thc = thc.doubleOrNull(),
                        cbd = cbd.doubleOrNull(),
                        discountPrice = discountPrice.doubleOrNull(),
                        weight = weight.doubleOrNull(),
                        storeId = storeId
                    )
                }.collect {
                    when (it) {
                        is Resource.Loading -> {
                            _eventFlow.emit(AddProductEvent.loading)
                        }
                        is Resource.Error -> {
                            _eventFlow.emit(AddProductEvent.showSnackbar(message = "Error adding product"))
                        }
                        is Resource.Success -> {
                            _eventFlow.emit(AddProductEvent.showSnackbar(message = "Product added successfully"))
                            delay(1500)
                            _eventFlow.emit(AddProductEvent.productAdded)
                        }
                    }
                }
            }
        }
    }
}