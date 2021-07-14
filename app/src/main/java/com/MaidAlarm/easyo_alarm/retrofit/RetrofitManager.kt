package com.MaidAlarm.easyo_alarm.retrofit

import android.util.Log
import com.MaidAlarm.easyo_alarm.API
import com.MaidAlarm.easyo_alarm.AppClass
import com.MaidAlarm.easyo_alarm.R
import com.MaidAlarm.easyo_alarm.Weather
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToLong

class RetrofitManager {
    companion object{
        val instance = RetrofitManager()
    }
    // 레트로핏 인터페이스 가져오기
    private val iRetrofit : IRetrofit? = RetrofitClient.getClient(API.baseURL)?.create(IRetrofit::class.java)

    // 날씨 데이터를 가져오는 API 호출
    fun getCurrentWeatherData(locationInfo : String, AppId : String,
                              completion:() -> Unit){
        //
//        val call = iRetrofit?.getWeatherData(locationInfo, AppId) ?: return
        val call2 = iRetrofit?.getCurrentWeatherData(AppClass.lat.toString(), AppClass.lon.toString(), AppId) ?: return

        call2.enqueue(object : retrofit2.Callback<JsonElement>{
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                Log.d("retrofit", "접속 성공\n, response: ${response.body()}, code: ${response.code()}")
                when(response.code()){
                    200 ->{
                        // response.body()에 어떤 값이 있을 경우
                        response.body().let {
                            // 1차: JsonObject 분해
                            val body = it!!.asJsonObject
                            // 2차: 원하는 JsonArray 가져와서 JsonObject로 분해
                            val weatherArray = body.getAsJsonArray("weather")
                            // 왜 0번 째 인덱스 참조하는지는 Json 데이터 참조하기
                            val weatherData = weatherArray[0].asJsonObject
                            // 3차 JsonObject 가져왔으면 그 안에서 필요한 요소 다시 가져오기
                            // 간단 날씨 설명
                            Weather.main = weatherData.get("main").asString
                            // 상세 날시 설명
                            Weather.description = weatherData.get("description").asString
                            // main 부분의 JsonObject 가져오기
                            val main = body.getAsJsonObject("main").asJsonObject
                            // 현재 날씨
                            Weather.temp = main.get("temp").asLong
                            // 체감온도
                            Weather.feels = main.get("feels_like").asLong
                            // 최대 온도
                            Weather.max_temp = main.get("temp_max").asLong
                            // 최저 온도
                            Weather.min_temp = main.get("temp_min").asLong
                            // 습도
                            Weather.humility = main.get("humidity").asLong

                            // 위의 온도는 전부 절대온도이기 때문에 섭씨로 변환해준다
                            Weather.temp = (Weather.temp - 273.15).toLong()
                            Weather.max_temp = (Weather.max_temp - 273.15).toLong()
                            Weather.min_temp = (Weather.min_temp - 273.15).toLong()


                            // 바람에 관한 JsonObject 가져오기
                            val wind = body.getAsJsonObject("wind").asJsonObject
                            // 바람 속도 -> OneCall API에서 가져오도록 바꿈

                            // 시스템에 관한 값 가져오기
                            val sys = body.getAsJsonObject("sys").asJsonObject
                            // 해뜨는 시간
                            val sunRise_ori = sys.get("sunrise").asLong
                            Weather.sunRise = SimpleDateFormat("HH", Locale.getDefault()).format(Date(sunRise_ori * 1000))
                            // 해 지는 시간
                            val sunSet_ori = sys.get("sunset").asLong
                            Weather.sunSet = SimpleDateFormat("HH", Locale.getDefault()).format(Date(sunSet_ori * 1000))
                            // 국가 - 이 부분에서 에러 나와서 일단 비활성화
//                            Weather.country = sys.get("country").asString
                            Log.d("retrofit1", "sunRise: ${Weather.sunRise}")
                            Log.d("retrofit1", "sunSet: ${Weather.sunSet}")

                        }
                        completion()
                    }
                }
            }

            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                Log.d("retrofit", "접속 실패, 태그: $t")
            }
        })
    }
    // 날씨 예보에 대한 API 가져오기
    fun getForecast(lat : String, lon : String, part : String, appid : String, completion: (
        ArrayList<Long>, ArrayList<Double>, ArrayList<Int>, ArrayList<Double>, ArrayList<String>,
        ArrayList<Int>, ArrayList<Int>, ArrayList<Int>, ArrayList<String>) -> Unit){
        val call = iRetrofit?.getForecast(lat, lon, part, appid) ?: return

        call.enqueue(object : retrofit2.Callback<JsonElement>{
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                Log.d("retrofit2", "접속 성공\n, response: ${response.body()}, code: ${response.code()}")
                when(response.code()){
                    200 -> {
                        response.body().let {
                            val body = it!!.asJsonObject
                            // hourly JsonArray 가져오기 - 여기에 시간별 날씨 데이터 있음
                            val hourlyForecast = body.getAsJsonArray("hourly")
                            // 각 데이터를 위한 리스트를 만든다
                            val hourlyTemp = ArrayList<Long>()  // 온도
                            val hourlyPop = ArrayList<Double>() // 강수 확률
                            val hourlyWind = ArrayList<Int>() // 바람 속도
                            val hourlyUvi = ArrayList<Double>() // 자외선 지수
                            val hourlyMain = ArrayList<String>() // 날씨 설명

                            val unit = 0..30 step 3    // for를 위한 카운터용 배열

                            // 인덱스는 0부터 하여 3시간 단위로 데이터를 가져온다
                            for (i in unit){
                                val forecastBody = hourlyForecast[i].asJsonObject
                                // i = 0 일 때는 현재 날씨 예보이다
                                // 이 부분은 강수확률 데이터만 필요하다
                                if (i == 0){
                                    val rainPercent = forecastBody.get("pop").asDouble
                                    Weather.rainPercent = (rainPercent * 100).toInt()
                                    Weather.wind = forecastBody.get("wind_speed").asInt
                                    val feelLike = forecastBody.get("feels_like").asLong
                                    Weather.feels = (feelLike - 273.15).roundToLong()
                                    val uvi = forecastBody.get("uvi").asDouble
                                    if (uvi > 2){
                                        Weather.uvi = AppClass.context.getString(R.string.high)
                                    }else if(uvi > 1){
                                        Weather.uvi = AppClass.context.getString(R.string.normal)
                                    }else{
                                        Weather.uvi = AppClass.context.getString(R.string.low)
                                    }

                                    val temp = forecastBody.get("temp").asLong
                                    val wind = forecastBody.get("wind_speed").asInt

                                    val weatherArray = forecastBody.getAsJsonArray("weather")
                                    val weatherBody = weatherArray[0].asJsonObject
                                    val main = weatherBody.get("main").asString


                                    Log.d("retrofit2", "i: $i")
                                    Log.d("retrofit2", "temp: $temp")
                                    Log.d("retrofit2", "pop: $rainPercent")
                                    Log.d("retrofit2", "wind: $wind")
                                    Log.d("retrofit2", "uvi: $uvi")
                                    Log.d("retrofit2", "main: $main")
                                }
                                // i = 0 이후로는 정상적인 동작을 실시
                                else{
                                    val temp = forecastBody.get("temp").asLong
                                    val pop = forecastBody.get("pop").asDouble
                                    val wind = forecastBody.get("wind_speed").asInt
                                    val uvi = forecastBody.get("uvi").asDouble
                                    val weatherArray = forecastBody.getAsJsonArray("weather")
                                    val weatherBody = weatherArray[0].asJsonObject
                                    val main = weatherBody.get("main").asString

                                    Log.d("retrofit2", "temp: $temp")
                                    Log.d("retrofit2", "pop: $pop")
                                    Log.d("retrofit2", "wind: $wind")
                                    Log.d("retrofit2", "uvi: $uvi")
                                    Log.d("retrofit2", "main: $main")

                                    // 온도의 경우 절대 온도로 가져오기 때문에 섭씨는 변환이 필요하다
                                    val temp_trans = (temp - 273.15)

                                    // 각 데이터를 리스트에 넣기
                                    hourlyTemp.add(temp_trans.toLong())
                                    hourlyPop.add((pop * 100))
                                    hourlyWind.add(wind)
                                    hourlyUvi.add(uvi)
                                    hourlyMain.add(main)
                                }
                            }
                            // 여기서 부턴 daily 데이터 얻기
                            val dailyMinTempList = ArrayList<Int>()  // 최저 온도
                            val dailyMaxTempList = ArrayList<Int>() // 최고 온도
                            val dailyPopList = ArrayList<Int>() // 강수 확률
                            val dailyMainList = ArrayList<String>() // 날씨 설명

                            val dailyForecast = body.getAsJsonArray("daily")
                            // 1번 인덱스부터 내일 날씨를 의미한다
                            // 일주일치의 데이터를 저장
                            for (i in 1..7){
                                val dailyBody = dailyForecast[i].asJsonObject
                                val dailyPop = dailyBody.get("pop").asDouble

                                val tempBody = dailyBody.getAsJsonObject("temp")
                                val dailyMinTemp = tempBody.get("min").asLong
                                val dailyMaxTemp = tempBody.get("max").asLong
                                val dailyMinTempTrans = dailyMinTemp - 273.15
                                val dailyMaxTempTrans = dailyMaxTemp - 273.15


                                val weatherJsonArray = dailyBody.getAsJsonArray("weather")
                                val weatherBody = weatherJsonArray[0].asJsonObject
                                val dailyMain = weatherBody.get("main").asString

                                dailyMinTempList.add(dailyMinTempTrans.toInt())
                                dailyMaxTempList.add(dailyMaxTempTrans.toInt())
                                dailyPopList.add((dailyPop * 100).toInt())
                                dailyMainList.add(dailyMain)
                            }
                            Log.d("retrofit3", "dailyMinTempList: $dailyMinTempList")
                            Log.d("retrofit3", "dailyMaxTempList: $dailyMaxTempList")
                            Log.d("retrofit3", "dailyPopList: $dailyPopList")
                            Log.d("retrofit3", "dailyMainList: $dailyMainList")
                            completion(hourlyTemp, hourlyPop, hourlyWind, hourlyUvi, hourlyMain
                                , dailyMinTempList, dailyMaxTempList, dailyPopList, dailyMainList)
                        }
                    }
                }
            }
            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                Log.d("retrofit2", "접속 실패, 태그: $t")
            }
        })
    }
}
