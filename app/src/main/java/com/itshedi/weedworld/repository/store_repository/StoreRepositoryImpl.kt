package com.itshedi.weedworld.repository.store_repository

import android.net.Uri
import android.util.Log
import android.webkit.URLUtil
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.itshedi.weedworld.entities.*
import com.itshedi.weedworld.utils.Resource
import com.itshedi.weedworld.utils.randomName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class StoreRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    private val firebaseFirestore: FirebaseFirestore
) : StoreRepository {
    override fun registerStore(
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
    ): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading())

            val storeRegistrationCollection = firebaseFirestore.collection("StoreRegistration")

            val storeRegistration = StoreRegistration(
                licencePDF = suspend {
                    if (licencePDF != null) {
                        val imagePath = "images/storeRegistrationCollection/${randomName()}"
                        val imageRef = firebaseStorage.reference.child(imagePath)
                        imageRef.putFile(licencePDF).await()
                        imageRef.path
                    } else {
                        null
                    }
                }.invoke(),
                businessType = businessType,
                timestamp = FieldValue.serverTimestamp(),
                userId = firebaseAuth.currentUser!!.uid,
                businessName = businessName,
                useFor = useFor,
                phone = phone,
                address = address,
                licence = licence,
                emailAddress = emailAddress,
                website = website,
                geohash = GeoFireUtils.getGeoHashForLocation(GeoLocation(lat, lng)),
                lat = lat,
                lng = lng
            )

            storeRegistrationCollection.add(storeRegistration).await()

            emit(Resource.Success(Unit))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error registring store"))
        }
    }

    override fun addStore(
        businessType: String?,
        businessName: String?,
        phone: String?,
        licence: String?,
        emailAddress: String?,
        website: String?
    ): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading())

            val storeCollection = firebaseFirestore.collection("Store")

            val storeRegistration = StoreSender(
                businessType = businessType,
                timestamp = FieldValue.serverTimestamp(),
                userId = firebaseAuth.currentUser!!.uid,
                businessName = businessName,
                phone = phone,
                licence = licence,
                emailAddress = emailAddress,
                website = website,
            )

            storeCollection.add(storeRegistration).await()

            emit(Resource.Success(Unit))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error adding store"))
        }
    }

    override fun myStore(): Flow<Resource<Store>> {
        return flow {
            emit(Resource.Loading())

            val storeCollection = firebaseFirestore.collection("Store")

            val querySnapshot = storeCollection
                .whereEqualTo("userId", FirebaseAuth.getInstance().currentUser?.uid!!)
                .get()
                .await()

            val store = querySnapshot.documents.first().toObject<Store>()
                ?.copy(storeId = querySnapshot.documents.first().id)

            emit(Resource.Success(store))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error loading online users"))
        }
    }

    override fun updatePicture(store: Store, uri: Uri): Flow<Resource<Uri>> {
        return flow {
            emit(Resource.Loading())
            val photoUrl =
                uploadImage(uri, "images/storePhoto", store.storeId!!).downloadUrl.await()

            val storeCollection = firebaseFirestore.collection("Store")

            val querySnapshot = storeCollection.document(store.storeId)

            querySnapshot.update(mapOf("photo" to photoUrl)).await()

            emit(Resource.Success(photoUrl))
        }.catch {
            Log.i("yayy", it.message.toString())
            emit(Resource.Error("Error updating store data"))
        }
    }

    override fun updateCoverPicture(store: Store, uri: Uri): Flow<Resource<Uri>> {
        return flow {
            emit(Resource.Loading())
            val photoUrl =
                uploadImage(uri, "images/storeCover", store.storeId!!).downloadUrl.await()

            val storeCollection = firebaseFirestore.collection("Store")

            val querySnapshot = storeCollection.document(store.storeId)

            querySnapshot.update(mapOf("coverPhoto" to photoUrl)).await()
            emit(Resource.Success(photoUrl))
        }.catch {
            Log.i("yayy", it.message.toString())
            emit(Resource.Error("Error updating store data"))
        }
    }

    override fun addProduct(
        storeId: String,
        images: List<Uri>,
        name: String,
        brand: String,
        category: String,
        specie: String?,
        description: String,
        thc: Double?,
        cbd: Double?,
        price: Double,
        discountPrice: Double?,
        weight: Double?
    ): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading())

            val productCollection = firebaseFirestore.collection("Product")

            val postSender = ProductSender(
                images = suspend {
                    val downloadUrls = ArrayList<String>()
                    images.forEach {
                        val imagePath = "images/posts/${randomName()}"

                        val imageRef = firebaseStorage.reference.child(imagePath)

                        imageRef.putFile(it).await()
                        imageRef.downloadUrl.await()?.let { downloadUrl ->
                            downloadUrls.add(downloadUrl.toString())
                        }

                    }
                    downloadUrls
                }.invoke(),
                name = name,
                nameLowerCase = name.lowercase(),
                brand = brand,
                description = description,
                category = category,
                specie = specie,
                thc = thc,
                cbd = cbd,
                price = price,
                discountPrice = discountPrice,
                weight = weight,
                timestamp = FieldValue.serverTimestamp(),
                storeId = storeId,
            )

            productCollection.add(postSender).await()


            emit(Resource.Success(Unit))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error adding post"))
        }
    }

    override fun updateProduct(
        productId: String,
        storeId: String,
        images: List<Uri>,
        name: String,
        brand: String,
        category: String,
        specie: String,
        description: String,
        thc: Double?,
        cbd: Double?,
        price: Double,
        discountPrice: Double?,
        weight: Double?
    ): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading())

            val product = firebaseFirestore.collection("Product").document(productId)

            product.update(
                mapOf(
                    "images" to suspend {
                        val downloadUrls = ArrayList<String>()
                        images.forEach {
                            if (URLUtil.isNetworkUrl(it.toString())) {
                                Log.i("yayy", it.toString())
                                downloadUrls.add(it.toString())
                            } else {
                                val imagePath = "images/posts/${randomName()}"

                                val imageRef = firebaseStorage.reference.child(imagePath)

                                imageRef.putFile(it).await()
                                imageRef.downloadUrl.await()?.let { downloadUrl ->
                                    downloadUrls.add(downloadUrl.toString())
                                }
                            }
                        }
                        downloadUrls
                    }.invoke(),
                    "name" to name,
                    "nameLowerCase" to name.lowercase(),
                    "brand" to brand,
                    "description" to description,
                    "category" to category,
                    "specie" to specie,
                    "thc" to thc,
                    "cbd" to cbd,
                    "price" to price,
                    "discountPrice" to discountPrice,
                    "weight" to weight,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "storeId" to storeId
                )
            ).await()


            emit(Resource.Success(Unit))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error adding post"))
        }
    }

    override fun deleteProduct(productId: String): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading())

            val productCollection = firebaseFirestore.collection("Product")

            val querySnapshot = productCollection.document(productId)

            querySnapshot.delete().await()

            emit(Resource.Success(Unit))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error deleting product"))
        }
    }

    override fun getProducts(
        storeId: String?,
        category: String?,
        query: String?,
        limit:Int?
    ): Flow<Resource<List<Product>>> {
        return flow {
            emit(Resource.Loading())
            val products = ArrayList<Product>()

            val productCollection = firebaseFirestore.collection("Product")

            val querySnapshot0 = when (query == null) {
                true -> productCollection
                else -> productCollection
                    .orderBy("nameLowerCase")
                    .whereGreaterThanOrEqualTo("nameLowerCase", query.lowercase())
                    .whereLessThan("nameLowerCase", query.lowercase() + 'z')
            }

            val querySnapshot1 = when (category == null) {
                true -> querySnapshot0
                else -> querySnapshot0.whereEqualTo("category", category)
            }
            val querySnapshot2 = when (storeId == null) {
                true -> querySnapshot1
                false -> querySnapshot1.whereEqualTo("storeId", storeId)
            }
            val querSnapshot3 = when (limit == null) {
                true -> querySnapshot2
                false -> querySnapshot2.limit(limit.toLong())
            }
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            for (document in querSnapshot3.documents) {
                document.toObject<Product>()?.let { products.add(it.copy(productId = document.id)) }
            }

            emit(Resource.Success(products.toList()))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error adding post"))
        }
    }

    override fun updateStore(
        storeId: String,
        phone: String,
        email: String,
        website: String,
        bio: String,
        fromTime: String,
        toTime: String,
        days: List<Int>
    ): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading())

            val storeCollection = firebaseFirestore.collection("Store")

            val querySnapshot = storeCollection.document(storeId)

            querySnapshot.update(
                mapOf(
                    "phone" to phone,
                    "emailAddress" to email,
                    "website" to website,
                    "bio" to bio,
                    "fromTime" to fromTime,
                    "toTime" to toTime,
                    "days" to days,
                )
            ).await()

            emit(Resource.Success(Unit))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error updating store"))
        }
    }

    override fun getStoreById(id: String): Flow<Resource<Store>> {
        return flow {
            emit(Resource.Loading())

            val storeCollection = firebaseFirestore.collection("Store")

            emit(
                Resource.Success(
                    storeCollection.document(id)
                        .get()
                        .await().toObject<Store>()?.copy(storeId = id)
                )
            )
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error loading store"))
        }
    }


    private suspend fun uploadImage(
        uri: Uri,
        path: String,
        userId: String
    ): StorageReference { // todo: update user
        val storageRef = firebaseStorage.reference
        val riversRef = storageRef.child("$path/$userId")
        riversRef.putFile(uri).await()
        return riversRef
    }

    override fun getProductById(id: String): Flow<Resource<Product>> {
        return flow {
            emit(Resource.Loading())

            val productCollection = firebaseFirestore.collection("Product")

            emit(
                Resource.Success(
                    productCollection.document(id)
                        .get()
                        .await().toObject<Product>()?.copy(productId = id)
                )
            )
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error loading product"))
        }
    }


    override fun findStores(query: String?, featured: Boolean): Flow<Resource<List<Store>>> {
        return flow {
            emit(Resource.Loading())

            val storeCollection = firebaseFirestore.collection("Store")

            val stores = ArrayList<Store>()

            val querySnapshot0 = when (query) {
                null -> storeCollection
                else -> storeCollection
                    .orderBy("businessNameLowercase")
                    .whereGreaterThanOrEqualTo("businessNameLowercase", query.lowercase())
                    .whereLessThan("businessNameLowercase", query.lowercase() + 'z')
            }
            when (featured) {
                false -> querySnapshot0
                true -> querySnapshot0.whereEqualTo("isFeatured", true)
            }
                .get().await().documents.forEach { document ->
                    document.toObject<Store>()?.let { stores.add(it.copy(storeId = document.id)) }
                }

            emit(Resource.Success(stores.toList()))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error loading nearby stores"))
        }
    }


}