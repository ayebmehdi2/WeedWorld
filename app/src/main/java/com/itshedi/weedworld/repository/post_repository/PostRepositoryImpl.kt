package com.itshedi.weedworld.repository.post_repository

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.itshedi.weedworld.entities.*
import com.itshedi.weedworld.utils.Resource
import com.itshedi.weedworld.utils.randomName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await


class PostRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    private val firebaseFirestore: FirebaseFirestore
) : PostRepository {

    override fun addPost(storeId:String?,uri: Uri?, ratio: Float?, text: String?, private:Boolean): Flow<Resource<String>> {
        return flow {
            emit(Resource.Loading())

            val postCollection = firebaseFirestore.collection("Posts_m")


            val postSender = PostSender(
                image = suspend {
                    if (uri != null && ratio != null) {
                        val imagePath = "images/posts/${randomName()}"

                        val imageRef = firebaseStorage.reference.child(imagePath)

                        imageRef.putFile(uri).await()
                        imageRef.downloadUrl.await()?.toString()
                    } else {
                        null
                    }
                }.invoke(),
                text = text,
                timestamp = FieldValue.serverTimestamp(),
                userId = firebaseAuth.currentUser!!.uid,
                aspectRatio = ratio,
                private = private,
                storeId = storeId,
            )

            postCollection.add(postSender).await()

            emit(Resource.Success("done"))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error adding post"))
        }
    }

    //todo: switch to firestore
    override fun getMyPosts(private: Boolean?): Flow<Resource<List<Post>>> {
        return flow {
            emit(Resource.Loading())
            val posts = ArrayList<Post>()

            val postCollection = firebaseFirestore.collection("Posts_m")


            val querySnapshot = when (private) {
                true -> postCollection
                    .whereEqualTo("userId", firebaseAuth.currentUser?.uid)
                    .whereEqualTo("private", true)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()
                else -> postCollection
                    .whereEqualTo("userId", firebaseAuth.currentUser?.uid)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()
            }
            for (document in querySnapshot.documents) {
                document.toObject<Post>()?.let { posts.add(it.copy(postId = document.id)) }
            }

            emit(Resource.Success(posts.toList()))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error adding post"))
        }
    }

    override fun getFeedPosts(): Flow<Resource<List<Post>>> {
        return flow {
            emit(Resource.Loading())
            val posts = ArrayList<Post>()

            val postCollection = firebaseFirestore.collection("Posts_m")

            val querySnapshot = postCollection
                .whereEqualTo("private", false)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            for (document in querySnapshot.documents) {
                document.toObject<Post>()?.let { posts.add(it.copy(postId = document.id)) }
            }

            emit(Resource.Success(posts.toList()))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error loading posts"))
        }
    }

    override fun getStorePosts(storeId: String): Flow<Resource<List<Post>>> {
        return flow {
            emit(Resource.Loading())
            val posts = ArrayList<Post>()

            val postCollection = firebaseFirestore.collection("Posts_m")

            val querySnapshot = postCollection
                .whereEqualTo("storeId", storeId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            for (document in querySnapshot.documents) {
                document.toObject<Post>()?.let { posts.add(it.copy(postId = document.id)) }
            }

            emit(Resource.Success(posts.toList()))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error loading posts"))
        }
    }

    override suspend fun downloadUrl(path: String): String {
        val url = firebaseStorage.reference.child(path).downloadUrl.await().toString()
        Log.i("zzzz", url)
        return url
    }

    override fun getLikes(postId: String): Flow<Resource<List<Like>>> {
        return flow {
            emit(Resource.Loading())

            val likesCollection = firebaseFirestore.collection("Likes")

            val likes = ArrayList<Like>()

            // get likes
            val querySnapshot = likesCollection
                .whereEqualTo("postId", postId)
                .get()
                .await()

            for (document in querySnapshot.documents) {
                document.toObject<Like>()?.let { likes.add(it) }
            }

            emit(Resource.Success(likes.toList()))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error loading likes"))
        }
    }


    override fun getComments(postId: String): Flow<Resource<List<Comment>>> {
        return flow {
            emit(Resource.Loading())

            val commentCollection = firebaseFirestore.collection("Comments")

            val comments = ArrayList<Comment>()


            //get Comments
            val querySnapshot2 = commentCollection
                .whereEqualTo("postId", postId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            for (document in querySnapshot2.documents) {
                document.toObject<Comment>()?.let { comments.add(it) }
            }

            emit(Resource.Success(comments.toList()))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error loading comments"))
        }
    }

    override fun likePost(postId: String): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading())

            val likeCollection = firebaseFirestore.collection("Likes")

            val commentSender = Like(
                userId = firebaseAuth.currentUser!!.uid,
                postId = postId
            )

            likeCollection.add(commentSender).await()

            emit(Resource.Success(Unit))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error liking post"))
        }
    }


    override fun commentPost(postId: String, content:String): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading())

            val commentCollection = firebaseFirestore.collection("Comments")

            val commentSender = CommentSender(
                timestamp = FieldValue.serverTimestamp(),
                userId = firebaseAuth.currentUser!!.uid,
                content = content,
                postId = postId
            )

            commentCollection.add(commentSender).await()

            emit(Resource.Success(Unit))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error commenting"))
        }
    }

    override fun sharePost(postId: String): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading())

            val shareCollection = firebaseFirestore.collection("Shares")

            val commentSender = ShareSender(
                timestamp = FieldValue.serverTimestamp(),
                userId = firebaseAuth.currentUser!!.uid,
                postId = postId
            )

            shareCollection.add(commentSender).await()

            emit(Resource.Success(Unit))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error commenting"))
        }
    }

    override fun getLikeCount(postId: String): Flow<Resource<Int>> {
        return flow {
            emit(Resource.Loading())
            val likesCollection = firebaseFirestore.collection("Likes")

            // get likes
            val querySnapshot = likesCollection
                .whereEqualTo("postId", postId)
                .count().get(AggregateSource.SERVER).await()

            emit(Resource.Success(querySnapshot.count.toInt()))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error loading likes"))
        }
    }

    override fun getCommentCount(postId: String): Flow<Resource<Int>> {
        return flow {
            emit(Resource.Loading())
            val commentsCollection = firebaseFirestore.collection("Comments")

            // get comments
            val querySnapshot = commentsCollection
                .whereEqualTo("postId", postId)
                .count().get(AggregateSource.SERVER).await()

            emit(Resource.Success(querySnapshot.count.toInt()))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error loading comments"))
        }
    }

    override fun dislikePost(postId: String): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading())

            val likesCollection = firebaseFirestore.collection("Likes")

            // get likes
            val querySnapshot = likesCollection
                .whereEqualTo("postId", postId)
                .whereEqualTo("userId", firebaseAuth.currentUser!!.uid)
                .get()
                .await()

            querySnapshot.documents.forEach { it.reference.delete().await() }

            emit(Resource.Success(Unit))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error ccommentIng"))
        }
    }

    override fun deletePost(postId: String): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading())

            val postsCollection = firebaseFirestore.collection("Posts_m")

            val querySnapshot = postsCollection.document(postId)

            querySnapshot.delete().await()

            emit(Resource.Success(Unit))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error deleting post"))
        }
    }

    override fun updatePost(content: String, postId: String): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading())

            val postsCollection = firebaseFirestore.collection("Posts_m")

            val querySnapshot = postsCollection.document(postId)

            querySnapshot.update(mapOf("text" to content)).await()

            emit(Resource.Success(Unit))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error updating post"))
        }
    }
}