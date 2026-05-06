//dezactivat
package com.example.mystflowtb

import retrofit2.http.GET

interface MystFlowApi {
    @GET("posts/1")
    suspend fun getTestPost(): Post
}