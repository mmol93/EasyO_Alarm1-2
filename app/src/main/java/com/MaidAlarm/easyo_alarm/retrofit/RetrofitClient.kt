package com.MaidAlarm.easyo_alarm.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var retrofitClient : Retrofit? = null

    fun getClient(baseURL : String) : Retrofit?{
        if (retrofitClient == null){
            // 여기서는 okHttp를 사용하지 않기 때문에 바로 build()를 하면된다
            retrofitClient = Retrofit.Builder().baseUrl(baseURL).addConverterFactory(
                GsonConverterFactory.create()).build()
        }
        return retrofitClient
    }
}