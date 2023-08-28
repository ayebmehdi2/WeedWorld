package com.itshedi.weedworld.repository.chat_repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.itshedi.weedworld.entities.*
import com.itshedi.weedworld.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class ChatRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore
)  : ChatRepository {
    override fun sendMessage(to: String, content: String,emoji:Int?): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading())

            val chatCollection = firebaseFirestore.collection("Chat")

            val chatSender = ChatSender(
                from = firebaseAuth.currentUser!!.uid,
                to = to,
                timestamp = FieldValue.serverTimestamp(),
                content = content,
                emoji = emoji
            )

            chatCollection.add(chatSender).await()

            emit(Resource.Success(Unit))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error sending message"))
        }
    }

    override fun loadMessages(userId: String): Flow<Resource<List<Chat>>> {
        return flow {
            emit(Resource.Loading())
            val chatCollection = firebaseFirestore.collection("Chat")
            val chats = ArrayList<Chat>()
            //recieved Chats
            val querySnapshot1 = chatCollection
                .whereEqualTo("from", userId)
                .whereEqualTo("to", firebaseAuth.currentUser!!.uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
          //sent Chats
            val querySnapshot2 = chatCollection
                .whereEqualTo("from", firebaseAuth.currentUser!!.uid)
                .whereEqualTo("to", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            for (document in querySnapshot1.documents) {
                document.toObject<Chat>()?.let { chats.add(it.copy(outgoing = false)) }
            }
            for (document in querySnapshot2.documents) {
                document.toObject<Chat>()?.let { chats.add(it.copy(outgoing = true)) }
            }
            emit(Resource.Success(chats.toList().sortedBy { it.timestamp }))
        }.catch {
            Log.i("zzzz", " ${it.message} yoyoyo")
            emit(Resource.Error(message = "Error loading chats"))
        }
    }

    override fun loadConversations(): Flow<Resource<List<Chat>>> {
        return flow {
            emit(Resource.Loading())
            val chatCollection = firebaseFirestore.collection("Chat")
            val chats = ArrayList<Chat>()

            //recieved Chats
            val querySnapshot1 = chatCollection
                .whereEqualTo("to", firebaseAuth.currentUser!!.uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            //sent Chats
            val querySnapshot2 = chatCollection
                .whereEqualTo("from", firebaseAuth.currentUser!!.uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            for (document in querySnapshot1.documents) {
                document.toObject<Chat>()?.let { chats.add(it.copy(outgoing = false)) }
            }
            for (document in querySnapshot2.documents) {
                document.toObject<Chat>()?.let { chats.add(it.copy(outgoing = true)) }
            }

            val result = ArrayList<Chat>()
            result.addAll(chats.sortedByDescending { it.timestamp }.distinctBy {
                if (it.to == firebaseAuth.currentUser!!.uid){
                    it.from
                }else{
                    it.to
                }
            })

            emit(Resource.Success(result.toList()))
        }.catch {
            Log.i("zzzz", " ${it.message} yoyoyo")
            emit(Resource.Error(message = "Error loading chats"))
        }
    }

    override fun deleteConversations(userId: String): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading())

            emit(Resource.Loading())
            val chatCollection = firebaseFirestore.collection("Chat")


            //recieved Chats
            val querySnapshot1 = chatCollection
                .whereEqualTo("from", userId)
                .whereEqualTo("to", firebaseAuth.currentUser!!.uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            //sent Chats
            val querySnapshot2 = chatCollection
                .whereEqualTo("from", firebaseAuth.currentUser!!.uid)
                .whereEqualTo("to", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            querySnapshot1.documents.forEach { it.reference.delete().await() }
            querySnapshot2.documents.forEach { it.reference.delete().await() }

            emit(Resource.Success(Unit))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error deleting conversation"))
        }
    }


    override fun setStatus(status: Boolean): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading())

            val statusCollection = firebaseFirestore.collection("Status")
            val querySnapshot = statusCollection
                .whereEqualTo("userId", FirebaseAuth.getInstance().currentUser?.uid)
                .get()
                .await()

            when(status){
                false -> {
                    querySnapshot.documents.forEach { it.reference.delete().await() }
                }
                true -> {
                    if (querySnapshot.documents.isEmpty()){
                        statusCollection.add(
                            StatusSender(
                                timestamp = FieldValue.serverTimestamp(),
                                userId = FirebaseAuth.getInstance().currentUser?.uid!!,
                            )
                        )
                    }
                }
            }
            emit(Resource.Success(Unit))
        }.catch {
            emit(Resource.Error(message = "Error"))
        }
    }

    override fun getOnlineUsers(): Flow<Resource<List<Status>>> {
        return flow {
            emit(Resource.Loading())

            val statusCollection = firebaseFirestore.collection("Status")
            val users = ArrayList<Status>()

            val querySnapshot = statusCollection
                .whereNotEqualTo("userId",FirebaseAuth.getInstance().currentUser?.uid!!)
                .get()
                .await()

            for (document in querySnapshot.documents) {
                document.toObject<Status>()?.let { users.add(it) }
            }

            emit(Resource.Success(users.toList()))
        }.catch {
            Log.i("zzzz", "Error loading online users ${it.message}")
            emit(Resource.Error(message = "Error loading online users"))
        }
    }

    override fun addToGroup(to:String, group:String): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading())
            val chatGroupCollection = firebaseFirestore.collection("ChatGroup")
            val querySnapshot = chatGroupCollection
                .whereEqualTo("userId", FirebaseAuth.getInstance().currentUser?.uid)
                .whereEqualTo("name", group)
                .get()
                .await()

            if (querySnapshot.documents.isEmpty()){
                chatGroupCollection.add(
                    ChatGroup(
                        userId = FirebaseAuth.getInstance().currentUser?.uid!!,
                        to = listOf(to),
                        name = group
                    )
                )
            }else{
                querySnapshot.documents.first().toObject<ChatGroup>()?.let {
                    val _to = ArrayList<String>()
                    it.to?.let{ _to.addAll(it) }
                    _to.add(to)

                    chatGroupCollection.add(
                        ChatGroup(
                            userId = FirebaseAuth.getInstance().currentUser?.uid!!,
                            to = _to.toList(),
                            name = group
                        )
                    )
                }
            }
            emit(Resource.Success(Unit))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error"))
        }
    }

    override fun loadGroups(): Flow<Resource<List<ChatGroup>>> {
        return flow {
            emit(Resource.Loading())

            val chatGroupCollection = firebaseFirestore.collection("ChatGroup")
            val users = ArrayList<ChatGroup>()

            val querySnapshot = chatGroupCollection
                .whereEqualTo("userId",FirebaseAuth.getInstance().currentUser?.uid!!)
                .get()
                .await()

            for (document in querySnapshot.documents) {
                document.toObject<ChatGroup>()?.let { users.add(it) }
            }

            emit(Resource.Success(users.toList()))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error loading chat groups"))
        }
    }
}