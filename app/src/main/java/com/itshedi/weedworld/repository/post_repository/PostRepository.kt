package com.itshedi.weedworld.repository.post_repository

import android.net.Uri
import com.itshedi.weedworld.entities.Comment
import com.itshedi.weedworld.entities.Like
import com.itshedi.weedworld.entities.Post
import com.itshedi.weedworld.entities.PostSender
import com.itshedi.weedworld.utils.Resource
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun addPost(storeId:String?=null, uri: Uri?, ratio:Float?, text:String?, private:Boolean): Flow<Resource<String>>
    fun getMyPosts(private:Boolean?): Flow<Resource<List<Post>>>

    fun getFeedPosts(): Flow<Resource<List<Post>>>

    suspend fun downloadUrl(path:String): String

    fun getLikes(postId:String): Flow<Resource<List<Like>>>

    fun getComments(postId:String): Flow<Resource<List<Comment>>>

    fun likePost(postId: String): Flow<Resource<Unit>>

    fun commentPost(postId: String, content:String): Flow<Resource<Unit>>

    fun sharePost(postId: String): Flow<Resource<Unit>>

    fun getLikeCount(postId: String): Flow<Resource<Int>>
    fun getCommentCount(postId: String): Flow<Resource<Int>>

    fun dislikePost(postId:String): Flow<Resource<Unit>>

    fun deletePost(postId:String): Flow<Resource<Unit>>

    fun updatePost(content:String, postId: String): Flow<Resource<Unit>>

    fun getStorePosts(storeId: String): Flow<Resource<List<Post>>>

}