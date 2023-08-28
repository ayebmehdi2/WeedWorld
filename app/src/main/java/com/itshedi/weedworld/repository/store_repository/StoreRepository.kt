package com.itshedi.weedworld.repository.store_repository

import android.net.Uri
import com.itshedi.weedworld.entities.Product
import com.itshedi.weedworld.entities.Store
import com.itshedi.weedworld.entities.UserInfo
import com.itshedi.weedworld.utils.Resource
import kotlinx.coroutines.flow.Flow

interface StoreRepository {
    fun registerStore(
        businessType: String?,
        businessName: String?,
        useFor: String?,
        phone: String?,
        address: String?,
        licence: String?,
        emailAddress: String?,
        website: String?,
        licencePDF: Uri?,
        lat: Double,
        lng: Double
    ): Flow<Resource<Unit>>

    fun addStore(
        businessType: String?,
        businessName: String?,
        phone: String?,
        licence: String?,
        emailAddress: String?,
        website: String?,
    ): Flow<Resource<Unit>>

    fun myStore(): Flow<Resource<Store>>

    fun updatePicture(store: Store, uri: Uri): Flow<Resource<Uri>>

    fun updateCoverPicture(store: Store, uri: Uri): Flow<Resource<Uri>>

    fun addProduct(
        storeId: String,
        images: List<Uri>,
        name: String,
        brand: String,
        category: String,
        specie: String?=null,
        description: String,
        thc: Double? = null,
        cbd: Double? = null,
        price: Double,
        discountPrice: Double? = null,
        weight: Double? = null,
    ): Flow<Resource<Unit>>

    fun updateProduct(
        productId: String,
        storeId: String,
        images: List<Uri>,
        name: String,
        brand: String,
        category: String,
        specie: String,
        description: String,
        thc: Double? = null,
        cbd: Double? = null,
        price: Double,
        discountPrice: Double? = null,
        weight: Double? = null,
    ): Flow<Resource<Unit>>

    fun deleteProduct(productId: String): Flow<Resource<Unit>>

    fun getProducts(storeId: String?=null, category:String?=null, query:String?=null, limit:Int?=null): Flow<Resource<List<Product>>>


    fun updateStore(
        storeId: String,
        phone: String,
        email: String,
        website: String,
        bio: String,
        fromTime: String,
        toTime: String,
        days: List<Int>
    ): Flow<Resource<Unit>>


    fun getStoreById(id:String): Flow<Resource<Store>>

    fun getProductById(id:String): Flow<Resource<Product>>


    fun findStores(query:String?=null, featured:Boolean=false): Flow<Resource<List<Store>>>
}