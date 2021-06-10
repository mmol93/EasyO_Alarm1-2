package com.MaidAlarm.easyo_alarm

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isGone
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.MaidAlarm.easyo_alarm.databinding.FragmentWeatherBinding
import com.MaidAlarm.easyo_alarm.retrofit.RetrofitManager
import com.MaidAlarm.easyo_alarm.weather_adapter.DailyWeatherAdapter
import com.MaidAlarm.easyo_alarm.weather_adapter.HourlyWeatherAdapter
import java.text.SimpleDateFormat
import java.util.*

class WeatherFragment : Fragment() {
    lateinit var binder : FragmentWeatherBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_weather, container, false)
        binder = FragmentWeatherBinding.bind(view)
        return view
    }

    // 프래그먼트를 연 시점에서 권한확인을 하게 한다
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val locationManager = requireContext().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        // 제일 최근 위치 정보값을 가져온다
        // 권한을 얻었는지 확인(getLastKnownLocation을 사용하기 위해서 반드시 필요한 사전 확인임)
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        // GPS가 제일 정확하기 때문에 먼저 발동되게 한다
        if (gpsLocation != null){
            Log.d("location", "gpsLocation: $gpsLocation")
            showInfo(gpsLocation)
        }else if (networkLocation != null){
            Log.d("location", "networkLocation: $networkLocation")
            showInfo(networkLocation)
        }else{
            Toast.makeText(AppClass.context, "GPS 연결 실패", Toast.LENGTH_SHORT).show()
        }

        refreshWeather()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binder.refreshImageView.setOnClickListener {
            binder.progressBar.isGone = false
            binder.refreshImageView.isGone = true
            refreshWeather()
            Log.d("test", "새로고침")
        }
    }

    fun showInfo(location : Location){
        if (location != null){
            // 위도와 경도를 이용하여 도시 이름 가져오기
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            // address에는 GPS 결과에 따른 후보군들이 리스트 형태로 들어간다
            // maxResults는 해당 후보를 몇 개를 선정할지 결정(숫자가 낮은 것을 권장함)
            // 즉, address[0]에는 1번 후보가 들어가 있는 것임
            val addresses: List<Address> = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses.isNotEmpty()) {
                // 경도 위도 저장
                AppClass.lon = location.longitude
                AppClass.lat = location.latitude

                Log.d("location", "provider: ${location.provider}")
                Log.d("location", "countryName: ${addresses[0].countryName}")
                Log.d("location", "countryCode: ${addresses[0].countryCode}")
                Log.d("location", "stateCode: ${addresses[0].postalCode}")
                Log.d("location", "stateName: ${addresses[0].adminArea}")
                Log.d("location", "cityName: ${addresses[0].locality}")

                // 경도와 위도를 이용하여 주소 이름 알아내기
                AppClass.provider = location.provider
                AppClass.countryCode = addresses[0].countryCode
                AppClass.stateCode = addresses[0].postalCode
                AppClass.stateName = addresses[0].adminArea
                AppClass.cityName = addresses[0].locality
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun refreshWeather(){
        // 현재 날짜 및 시간 가져오기
        val sdf = SimpleDateFormat("yyyy-MM-dd, HH:mm", Locale.getDefault())
        val hourFormat = SimpleDateFormat("HH", Locale.getDefault())
        val minFormat = SimpleDateFormat("mm", Locale.getDefault())
        val now = sdf.format(Date())
        // 현재 시간에서 3시간을 더한다
        var hourlyHour = System.currentTimeMillis() + (3*60*60*1000)
        // hourly에 사용될 시간 간격은 3시간이므로 3시간 씩 더한 시간을 리스트로 만든다
        // add 하기 전에 클리어
        AppClass.hour.clear()
        for (i in 1..10){
            AppClass.hour.add(hourFormat.format(hourlyHour))
            hourlyHour += (3 * 60 * 60 * 1000)
        }

        // current weather API 호출
        val locationInfo = "${AppClass.cityName}, ${AppClass.stateCode}, ${AppClass.countryCode}"
        RetrofitManager.instance.getCurrentWeatherData(locationInfo, API.ID,
            completion = {
                // 결과 값 출력하기
                Log.d("retrofit", "locationInfo: $locationInfo")
                Log.d("retrofit", "cityName: ${AppClass.cityName}")
                Log.d("retrofit", "stateCode: ${AppClass.stateCode}")
                Log.d("retrofit", "stateName: ${AppClass.stateName}")
                Log.d("retrofit", "countryCode: ${AppClass.countryCode}")
                Log.d("retrofit", "main: ${Weather.main}")
                Log.d("retrofit", "description: ${Weather.description}")
                Log.d("retrofit", "temp: ${Weather.temp}")
                Log.d("retrofit", "max_temp: ${Weather.max_temp}")
                Log.d("retrofit", "min_temp: ${Weather.min_temp}")
                Log.d("retrofit", "feels: ${Weather.feels}")
                Log.d("retrofit", "humility: ${Weather.humility}")
                Log.d("retrofit", "wind: ${Weather.wind}")
                Log.d("retrofit", "sunRise: ${Weather.sunRise}")
                Log.d("retrofit", "sunSet: ${Weather.sunSet}")
                Log.d("retrofit", "country: ${Weather.country}")

                // 결과를 뷰에 적용하기
                // 지역에 따라 도시 이름이 나오지 않는 곳이 있다.
                if (AppClass.cityName == null){
                    binder.dataTextView.text = "${AppClass.stateName}   "
                }else{
                    binder.dataTextView.text = "${AppClass.cityName}   "
                }
                binder.dataTextView.append(now)
                binder.mainTextView.text = Weather.main
                when(Weather.main){
                    "Thunderstorm" -> binder.weatherImageView.setImageResource(R.drawable.ic_thunder)
                    "Drizzle" -> binder.weatherImageView.setImageResource(R.drawable.ic_little_rain)
                    "Rain" -> binder.weatherImageView.setImageResource(R.drawable.ic_rain)
                    "Snow" -> binder.weatherImageView.setImageResource(R.drawable.ic_snow)
                    "Clear" -> binder.weatherImageView.setImageResource(R.drawable.ic_sunny)
                    "Clouds" -> binder.weatherImageView.setImageResource(R.drawable.ic_clouds)
                    "Mist", "Dust", "Fog", "Haze", "Sand", "Ash" -> binder.weatherImageView.setImageResource(R.drawable.ic_fog)
                    "Tornado", "Squall" -> binder.weatherImageView.setImageResource(R.drawable.ic_tornado)
                }
                binder.tempTextView.text = Weather.temp.toString() + "℃"
                binder.minMaxTextView.text = Weather.min_temp.toString() + "℃ / "
                binder.minMaxTextView.append("${Weather.max_temp}℃")
                binder.humidityPercentTextView.text = Weather.humility.toString() + "%"
            })

        // weather forecast API 데이터 호출
        RetrofitManager.instance.getForecast("${AppClass.lat}", "${AppClass.lon}", "hourly.temp,daily.temp", API.ID,
            completion = { hourlyTemp, hourlyPop, hourlyWind, hourlyUvi, hourlyMain, dailyMinTemp, dailyMaxTemp, dailyPop, dailyMain ->
                // 결과 값 로그 출력
                Log.d("retrofit2", "from UI level, hourlyTemp: $hourlyTemp")
                Log.d("retrofit2", "from UI level, hourlyPop: $hourlyPop")
                Log.d("retrofit2", "from UI level, hourlyWind: $hourlyWind")
                Log.d("retrofit2", "from UI level, hourlyUvi: $hourlyUvi")
                Log.d("retrofit2", "from UI level, hourlyMain: $hourlyMain")

                try{
                    // 시간별 데이터를 리사이클러 어댑터에 보내기
                    val hourlyAdapter = HourlyWeatherAdapter(requireContext(),hourlyTemp, hourlyPop, hourlyMain)
                    binder.hourlyRecycler.layoutManager =
                        GridLayoutManager(requireContext(), 1, GridLayoutManager.HORIZONTAL, false)
                    binder.hourlyRecycler.adapter = hourlyAdapter

                    // 날짜별 데이터를 리사이클러 어댑터로 보내기
                    val dailyAdapter = DailyWeatherAdapter(requireContext(), dailyPop, dailyMain, dailyMinTemp, dailyMaxTemp)
                    binder.dailyRecycler.layoutManager= LinearLayoutManager(requireContext())
                    binder.dailyRecycler.adapter = dailyAdapter
                }catch (e:Exception){
                    binder.loadingErrorTextView.isGone = false
                    binder.loadingProgress.isGone = true
                    binder.loadingTextView.isGone = true
                    binder.weatherContainer.isGone = true
                }


                // OneCall API에서 얻어서 뷰에 넣는 경우 여기에 정의한다
                binder.rainPercentTextView.text = Weather.rainPercent.toString() + "%"
                binder.windPercentTextView.text = "${Weather.wind}m/s"
                binder.feelTextView.text = AppClass.context.getString(R.string.feels_like) + (" ${Weather.feels}℃")
                binder.UVpercentTextView.text = Weather.uvi
                binder.progressBar.isGone = true
                binder.refreshImageView.isGone = false
                if (Weather.rainPercent in 40..50){
                    binder.weatherImageView.setImageResource(R.drawable.ic_little_rain)
                }
                binder.loadingProgress.isGone = true
                binder.loadingTextView.isGone = true
                binder.loadingErrorTextView.isGone = true
                binder.weatherContainer.isGone = false
            })
    }
}