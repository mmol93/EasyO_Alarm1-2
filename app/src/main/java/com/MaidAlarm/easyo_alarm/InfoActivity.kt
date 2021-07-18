package com.MaidAlarm.easyo_alarm

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.MaidAlarm.easyo_alarm.AppClass.Companion.context
import com.MaidAlarm.easyo_alarm.databinding.ActivityInfoBinding
import com.MaidAlarm.easyo_alarm.notification.WeatherNotification
import com.MaidAlarm.easyo_alarm.retrofit.RetrofitManager
import java.util.*

class InfoActivity : AppCompatActivity() {
    lateinit var binder : ActivityInfoBinding
    override fun onResume() {
        super.onResume()
        binder.infoRecyclerView.layoutManager = LinearLayoutManager(this)
        binder.infoRecyclerView.adapter = InfoAdapter(this)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        binder = ActivityInfoBinding.inflate(layoutInflater)

        val function = Function()
        // 현재 버전 이름을 텍스트뷰에 넣기
        binder.infoTextView2.text = "ver" + function.checkAppVersion(this)

        // 그림 길게 누를 시 개발자 화면 띄우기
        binder.infoImageView1.setOnLongClickListener{
            val developerActivity = Intent(this, Developer::class.java)
            startActivityForResult(developerActivity, 100)
            // 내일 날씨 알람은 매일 울리기 때문에 요일 확인을 할 필요가 없음
            // API를 이용해 날씨 정보를 가져온다(기본적으로 WeatherFragment.kt에 있는 방식 활용)
            val locationManager = context!!.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
            // 제일 최근 위치 정보값을 가져온다
            // 권한을 얻었는지 확인(getLastKnownLocation을 사용하기 위해서 반드시 필요한 사전 확인임)
            if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(AppClass.context, context!!.getString(R.string.location_permmision), Toast.LENGTH_LONG).show()
            }
            val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            // GPS가 제일 정확하기 때문에 먼저 발동되게 한다
            if (gpsLocation != null){
                Log.d("location", "gpsLocation: $gpsLocation")
                if (gpsLocation != null){
                    // 위도와 경도를 이용하여 도시 이름 가져오기
                    val geocoder = Geocoder(context, Locale.getDefault())
                    // address에는 GPS 결과에 따른 후보군들이 리스트 형태로 들어간다
                    // maxResults는 해당 후보를 몇 개를 선정할지 결정(숫자가 낮은 것을 권장함)
                    // 즉, address[0]에는 1번 후보가 들어가 있는 것임
                    val addresses: List<Address> = geocoder.getFromLocation(gpsLocation.latitude, gpsLocation.longitude, 1)
                    if (addresses.isNotEmpty()) {
                        // 경도 위도 저장
                        AppClass.lon = gpsLocation.longitude
                        AppClass.lat = gpsLocation.latitude

                        Log.d("location", "provider: ${gpsLocation.provider}")
                        Log.d("location", "countryName: ${addresses[0].countryName}")
                        Log.d("location", "countryCode: ${addresses[0].countryCode}")
                        Log.d("location", "stateCode: ${addresses[0].postalCode}")
                        Log.d("location", "stateName: ${addresses[0].adminArea}")
                        Log.d("location", "cityName: ${addresses[0].locality}")

                        // 경도와 위도를 이용하여 주소 이름 알아내기
                        AppClass.provider = gpsLocation.provider
                        AppClass.countryCode = addresses[0].countryCode
                        AppClass.stateCode = addresses[0].postalCode
                        AppClass.stateName = addresses[0].adminArea
                        AppClass.cityName = addresses[0].locality
                    }
                }
            }else if (networkLocation != null){
                Log.d("location", "networkLocation: $networkLocation")
                // 위도와 경도를 이용하여 도시 이름 가져오기
                val geocoder = Geocoder(context, Locale.getDefault())
                // address에는 GPS 결과에 따른 후보군들이 리스트 형태로 들어간다
                // maxResults는 해당 후보를 몇 개를 선정할지 결정(숫자가 낮은 것을 권장함)
                // 즉, address[0]에는 1번 후보가 들어가 있는 것임
                val addresses: List<Address> = geocoder.getFromLocation(networkLocation.latitude, networkLocation.longitude, 1)
                if (addresses.isNotEmpty()) {
                    // 경도 위도 저장
                    AppClass.lon = networkLocation.longitude
                    AppClass.lat = networkLocation.latitude

                    Log.d("location", "provider: ${networkLocation.provider}")
                    Log.d("location", "countryName: ${addresses[0].countryName}")
                    Log.d("location", "countryCode: ${addresses[0].countryCode}")
                    Log.d("location", "stateCode: ${addresses[0].postalCode}")
                    Log.d("location", "stateName: ${addresses[0].adminArea}")
                    Log.d("location", "cityName: ${addresses[0].locality}")

                    // 경도와 위도를 이용하여 주소 이름 알아내기
                    AppClass.provider = networkLocation.provider
                    AppClass.countryCode = addresses[0].countryCode
                    AppClass.stateCode = addresses[0].postalCode
                    AppClass.stateName = addresses[0].adminArea
                    AppClass.cityName = addresses[0].locality
                }
            }
            RetrofitManager.instance.getForecast("${AppClass.lat}", "${AppClass.lon}", "hourly.temp,daily.temp", API.getID(),
                completion = { hourlyTemp, hourlyPop, hourlyWind, hourlyUvi, hourlyMain, dailyMinTemp, dailyMaxTemp, dailyPop, dailyMain ->
                    val weatherNotification = WeatherNotification(context)
                    val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                    // Notification 생성
                    // 인덱스0이 내일 날씨를 의미(인덱스 1이 내일 날씨인 것은 이미 RetrofitManager.kt에서 거름)
                    weatherNotification.makeWeatherNotification(dailyMain[0], dailyMaxTemp[0], dailyMinTemp[0], dailyPop[0], notificationManager)
                }
            )
            true
        }
        setContentView(binder.root)
    }
}