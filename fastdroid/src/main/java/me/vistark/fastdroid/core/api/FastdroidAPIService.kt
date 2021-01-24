package me.vistark.fastdroid.core.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import me.vistark.fastdroid.core.api.AuthIntercepter.Companion.addToken
import me.vistark.fastdroid.core.api.JwtAuth.BaseUrl
import me.vistark.fastdroid.core.api.interfaces.IAPIService
import me.vistark.fastdroid.core.api.interfaces.IFastdroidAPI

abstract class FastdroidAPIService<T>(val baseUrl: String, val apiServiceType: Class<T>) :
    IAPIService<T> where T : IFastdroidAPI {
    init {
        BaseUrl = baseUrl
    }

    val client: Retrofit
        get() {
            return Retrofit.Builder().apply {
                baseUrl(BaseUrl)
                addToken()
                addConverterFactory(GsonConverterFactory.create())
            }.build()
        }

    var APIs: T = client.create(apiServiceType)
}