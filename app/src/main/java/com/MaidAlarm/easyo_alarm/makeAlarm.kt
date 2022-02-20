package com.MaidAlarm.easyo_alarm

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.content.Intent.FLAG_INCLUDE_STOPPED_PACKAGES
import android.content.pm.PackageManager
import android.graphics.LinearGradient
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContentProviderCompat.requireContext
import com.MaidAlarm.easyo_alarm.notification.WeatherNotification
import com.MaidAlarm.easyo_alarm.retrofit.RetrofitManager
import com.MaidAlarm.easyo_alarm.weatherFunction.WeatherAlarm
import java.util.*
import kotlin.collections.ArrayList

class makeAlarm(
    val context: Context,
    val hour: Int,
    val min: Int,
    val progress: Int,
    val weekList : List<Int>,
    val requestCode: Int,
    val bellIndex : Int,
    val alarmMode : Int
){

    private val alarmManager: AlarmManager? =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

    // *** 한번만 울리는 새로운 알람을 알람 매니저에 등록한다
    fun addNewAlarm_once(){
        val quick = 1   // 이 메서드는 once 이므로 반드시 한번만 울린다

        // 휴식 상태인 휴대폰 깨우기
        val wakeLock: PowerManager.WakeLock =
            (context!!.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag").apply {
                    acquire(120*1000L)
                }
            }
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            // 정확한 시간 설정
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, min)
            set(Calendar.SECOND, 0)
        }
        // 지금 울린 알람 기록
        val function = Function()
        function.saveFileAsString("data2.bat", context)

        val intent = Intent("com.maidalarm.easyo.alarm")
        // *** 여기서 intent에 데이터를 넣어서 BroadCast에서 사용할 수 있다
        // pendingIntent 에는 1개의 변수만 넣을 수 있기 때문에 리스트를 임시로 만들어 넣는게 제일 좋다
        // 넘겨줄 항목은 다음과 같다.
        // Sun ~ Sat, progress, quick, hour, min, requestCode, bellIndex, alarmMode => 12개의 항목을 가진 리스트
        // 리스트 인덱스: 0~6 : 요일, 7: progress, 8: quick, 9: hour, 10: min, 11: requestCode, 12: bellIndex, 13: alarmMode
        var ListForPendingIntent = mutableListOf<Int>()
        ListForPendingIntent.add(progress)
        ListForPendingIntent.add(quick)
        ListForPendingIntent.add(hour)
        ListForPendingIntent.add(min)
        ListForPendingIntent.add(requestCode)
        ListForPendingIntent.add(bellIndex)
        ListForPendingIntent.add(alarmMode)

        // weekList 에는 alarmFragment 에서 받아온 alarmWeek에 대한 리스트 정보가 담겨있다
        ListForPendingIntent = (weekList + ListForPendingIntent) as ArrayList<Int>

        // intent에 putIntent를 하기 위해선 List -> array로 변환필요
        Log.d("makeAlarm", "ListForPendingIntent: $ListForPendingIntent")
        intent.putExtra("arrayForPendingIntent", ListForPendingIntent)
        intent.addFlags(FLAG_INCLUDE_STOPPED_PACKAGES)
        intent.action = "com.maidalarm.easyo.alarm"
        intent.component = ComponentName("com.MaidAlarm.easyo_alarm", "com.MaidAlarm.easyo_alarm.Receiver")

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        Log.d("makeAlarm", "Set exact Time: " + Date().toString())
        Log.d("makeAlarm", "hour: $hour")
        Log.d("makeAlarm", "min: $min")
        Log.d("makeAlarm", "requestCode: $requestCode")

        // 위에서 설정한 시간(Calendar.getInstance)에 알람이 울리게 한다
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // API23 이상에서는 setExactAndAllowWhileIdle을 사용해야한다.
            alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }else{
            alarmManager?.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    // *** 매일 울리는 새로운 알람을 알람 매니저에 등록한다
    fun addNewAlarm_normal(){
        addNewAlarm_normal_exact(alarmManager!!, context, weekList, progress, hour, min, requestCode, bellIndex, alarmMode)
    }

    // *** 이미 있는 알람을 취소한다.
    fun cancelAlarm(requestCode: Int){
        val intent = Intent("com.maidalarm.easyo.alarm")
        intent.action = "com.maidalarm.easyo.alarm"
        intent.component = ComponentName("com.MaidAlarm.easyo_alarm", "com.MaidAlarm.easyo_alarm.Receiver")
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        Log.d("makeAlarm", "Cancel(): requestCode: $requestCode")
        // 6. 해당 펜딩인텐트에 있는 알람을 해제(삭제, 취소)한다
        alarmManager?.cancel(pendingIntent)
        // 이거를 해줘야 getBraodcast에서 캔슬된 알람을 확인할 수 있다.
        pendingIntent.cancel()
    }
}

class Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // ** 휴대폰을 재부팅 했을 때
        if (intent!!.action == "android.intent.action.BOOT_COMPLETED" ||
            intent!!.action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d("makeAlarm", "재부팅됨")
            // 휴식 상태인 휴대폰 깨우기
            val wakeLock: PowerManager.WakeLock =
                (context!!.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                    newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag").apply {
                        acquire(120*1000L)
                    }
                }

            // ** SQL에서 모든 데이터를 들고와서 다시 알람 매니저에 등록해준다
            val function = Function()
            function.makeAlarmWithAllSQL(context!!)
            function.renewNotification(context)

            // 날씨 데이터의 경우에도 스위치가 on이라면 재설정하게 해준다
            val pref = context.getSharedPreferences("weatherAlarmData", Context.MODE_PRIVATE)
            val tomorrowWeatherSwitch = pref.getBoolean("weatherSwitch", false)

            if (tomorrowWeatherSwitch){
                val weatherAlarmTime = pref.getString("weatherAlarmTime", "01:00")
                val weatherAlarm = WeatherAlarm(context)
                weatherAlarm.setTomorrowWeatherAlarm(weatherAlarmTime!!)
            }
        }

        // 앱을 업데이트 했을 때
        else if(intent!!.action == "android.intent.action.MY_PACKAGE_REPLACED"){
            Log.d("makeAlarm", "앱 어데이트됨")
            // ** SQL에서 모든 데이터를 들고와서 다시 알람 매니저에 등록해준다
            val function = Function()
            function.makeAlarmWithAllSQL(context!!)
        }

        // ** 그 이외(기타)의 모든 알람에 대한 Receiver() 호출에 대한 행동
        else{
            // 액션 버튼 클릭했을 때 행동 구현
            val actionCommand = intent.getStringExtra("action")
            val function = Function()
            when(actionCommand){
                // 알림바에서 action 버튼1을 눌렀을 때
                "action1" -> {
                    Toast.makeText(context, context!!.getString(R.string.alarmToast_Action1), Toast.LENGTH_LONG).show()
                    function.makeSQLSetSimpleAlarm(context!!, 10)
                }
                // 알림바에서 action 버튼2을 눌렀을 때
                "action2" -> {
                    Toast.makeText(context, context!!.getString(R.string.alarmToast_Action2), Toast.LENGTH_LONG).show()
                    function.makeSQLSetSimpleAlarm(context!!, 15)
                }
                // 알림바에서 action 버튼3을 눌렀을 때
                "action3" -> {
                    Toast.makeText(context, context!!.getString(R.string.alarmToast_Action3), Toast.LENGTH_LONG).show()
                    function.makeSQLSetSimpleAlarm(context!!, 30)
                }
                // 내일 날씨 알려주기 알람이 울렸을 때
                "weather" -> {
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
                        return
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
                    // 다시 알람 설정
                    val pref = context.getSharedPreferences("weatherAlarmData", Context.MODE_PRIVATE)
                    val weatherAlarmTime = pref.getString("weatherAlarmTime", "01:00")
                    val weatherAlarm = WeatherAlarm(context)
                    weatherAlarm.setTomorrowWeatherAlarm(weatherAlarmTime!!)
                }

                // 10분 연장 버튼을 클릭했을
                "POSTPHONETIME" -> {
                    Log.d("makeAlarm", "알람 연장됨")

                    // 휴식 상태인 휴대폰 깨우기
                    val wakeLock: PowerManager.WakeLock =
                        (context!!.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag").apply {

                            }
                        }

                    // 60초만 지속되게 하기
                    wakeLock.acquire(60*1000L )

                    // 넘어온 intent에서 progress 데이터를 가져온다
                    val progress = intent.getIntExtra("progress", -1)
                    val bellIndex = intent.getIntExtra("bellIndex",0)
                    val alarmMode = intent.getIntExtra("alarmMode", 0)
                    Log.d("makeAlarm", "progress: $progress")

                    // 지금 울린 알람 기록
                    val function = Function()
                    function.saveFileAsString("data2.bat", context)

                    // 볼륨 강제 설정(else에 있는 볼륨 강제 설정이랑 다름)
                    val audioManager = context!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    // arrayFromMakeAlarm[7] = progress
                    val factor = progress.toFloat() / 100
                    val targetVolume = (maxVolume * factor).toInt()
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, AudioManager.FLAG_PLAY_SOUND)

                    // 10분 지났으니 다시 FrontAlarmActivity를 띄워야한다
                    val frontAlarmActivity = Intent(context, FrontAlarmActivity::class.java)
                    frontAlarmActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    // FrontAlarmActivity 를 띄위기 위해선 progress 데이터를 intent로 넘겨줘야한다.
                    frontAlarmActivity.putExtra("progress", progress)
                    frontAlarmActivity.putExtra("bellIndex", bellIndex)
                    frontAlarmActivity.putExtra("alarmMode", alarmMode)
                    context.startActivity(frontAlarmActivity)
                }
                // 위 상황 이외의 것으로 일반적인 알람이 울렸을 때 발동하는 것들
                else -> {
                    val function = Function()
                    function.saveFileAsString("history2.bat", AppClass.context, " BroadCast Called")

                    Log.d("makeAlarm", "onReceive() 호출 - else부분")
                    // 오늘이 알람에서 설정한 요일과 맞는지 확인하기 위해 오늘 날짜의 요일을 가져온다
                    val calendar = Calendar.getInstance()
                    // 밑에서 사용될 arrayFromMakeAlarm의 경우 인덱스가 0부터 시작
                    // 하지만 일요일 = 1 ~ 토요일 = 7이기 때문에 1부터 시작해서 -1을 해줘야 해당 요일과 인덱스가 매칭된다
                    val present_week = calendar.get(Calendar.DAY_OF_WEEK) - 1
                    val presentHour = calendar.get(Calendar.HOUR_OF_DAY)
                    val presentMin = calendar.get(Calendar.MINUTE)
                    val arrayFromMakeAlarm = intent!!.getIntegerArrayListExtra("arrayForPendingIntent")

                    // 순서대로 일 ~ 토, progress, quick, hour, min, requestCode = 12개 항목 들어있음
                    // index 0~6 : 일 ~ 토  /  7: progress / 8: quick / 9: hour / 10: min / 11: requestCode / 12 : bellIndex
                    Log.d("makeAlarm", "arrayFromMakeAlarm form onReceive(): $arrayFromMakeAlarm")
                    Log.d("makeAlarm", "present_week: $present_week")

                    // 알람에서 설정한 요일일 때만 액티비티 띄워서 알람 울리게 설정
                    // 설정 알람 시간이랑 동일할 때만 울리게 한다.
                    // arrayFromMakeAlarm!![present_week]: 설정한 알람의 요일
                    // arrayFromMakeAlarm[9]: 설정한 알람의 시각
                    // arrayFromMakeAlarm[10]: 설정한 알람의 분
                    if (arrayFromMakeAlarm!![present_week] == 1){
                        Log.d("makeAlarm", "지금 울릴 알람 맞음")

                        // 볼륨 강제 설정(10분뒤 울리는 알람이랑 설정 방법 조금 다름)
                        val audioManager = context!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                        // arrayFromMakeAlarm[7] = progress
                        val factor = arrayFromMakeAlarm[7].toFloat() / 100
                        val targetVolume = (maxVolume * factor).toInt()
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, AudioManager.FLAG_PLAY_SOUND)

                        Log.d("FrontActivity", "targetVolume: $targetVolume")
                        Log.d("FrontActivity", "currentVolume: ${currentVolume}")
                        val frontAlarmActivity = Intent(context, FrontAlarmActivity::class.java)
                        frontAlarmActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        frontAlarmActivity.putExtra("progress", arrayFromMakeAlarm[7])
                        frontAlarmActivity.putExtra("currentVolume", currentVolume)
                        frontAlarmActivity.putExtra("bellIndex", arrayFromMakeAlarm[12])
                        frontAlarmActivity.putExtra("alarmMode", arrayFromMakeAlarm[13])
                        context?.startActivity(frontAlarmActivity)
                    }else{
                        Log.d("makeAlarm", "지금 울릴 알람 아님")
                    }

                    // quick 알람은 울린 후 자동으로 해당 알람을 삭제한다
                    if (arrayFromMakeAlarm!![8] == 1){
                        // SQL의 requestCode 컬럼의 모든 데이터를 검색한다 -> 해당 requestCode가 있는 row 인덱스를 추출한다
                        // -> 해당 인덱스의 데이터를 삭제한다 -> 해당 알람을 cancel한다
                        val requestCode = arrayFromMakeAlarm[11]

                        val SQLHelper = SQLHelper(context!!)
                        val sql = "select * from MaidAlarm"
                        val c1 = SQLHelper.writableDatabase.rawQuery(sql, null)
                        val size = c1.count

                        // SQL에서 해당 데이터 row를 삭제하기 -> 자동으로 RecyclerView에서도 사라질 예정임
                        // Quick의 경우 원래 1번만 울리게 설정되어 있기 때문에 굳이 알람을 Cancel할 필요는 없다
                        while (c1.moveToNext()){
                            // 해당 row를 삭제하기 위해 idx의 값을 가져온다
                            val index1 = c1.getColumnIndex("idx")
                            // 위에 정의되어 있는 requestCode과 같은 reqeustCoed를 찾기 위해 가져온다
                            val index2 = c1.getColumnIndex("requestCode")

                            val SQLKeyIndex = c1.getInt(index1)
                            val SQLRequestCode = c1.getInt(index2)
                            // 위에 정의되어 있는 requestCode과 같은 requestCode를 찾아서 해당 row를 삭제
                            if (requestCode == SQLRequestCode){
                                val sqlDelete = "delete from MaidAlarm Where idx = ?"
                                val arg1 = arrayOf(SQLKeyIndex.toString())

                                SQLHelper.writableDatabase.execSQL(sqlDelete, arg1)
                                Log.d("makeAlarm", "Quick 알람 자동 삭제됨")
                                break
                            }
                        }

                        // 모든 인덱스를 고침
                        // sql2랑 c2를 한 번더 정의해주지 않으면 while 부분을 실시하지 않는다...
                        val sql2 = "select * from MaidAlarm"
                        val sql_update = "update MaidAlarm set idx = ? where idx = ? "
                        val c2 = SQLHelper.writableDatabase.rawQuery(sql2, null)
                        var i = 1
                        Log.d("makeAlarm", "SQL 데이터 삭제 후 인덱스 갱신")
                        while (c2.moveToNext()){
                            val index1 = c2.getColumnIndex("idx")
                            val idx = c2.getInt(index1)
                            val arg1 = arrayOf(i, idx)

                            SQLHelper.writableDatabase.execSQL(sql_update, arg1)
                            i += 1
                        }

                        SQLHelper.close()   // 삭제를 한 이후는 필요가 없으니 SQL을 닫는다
                    }
                    // quick 알람이 아닐 경우 -> 24시간 뒤 다시 울리게 설정한다
                    else{
                        val alarmManager: AlarmManager? = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

                        // 0 ~ 6까지 = 일요~토요
                        val weekList = mutableListOf<Int>()
                        weekList.add(arrayFromMakeAlarm[0])
                        weekList.add(arrayFromMakeAlarm[1])
                        weekList.add(arrayFromMakeAlarm[2])
                        weekList.add(arrayFromMakeAlarm[3])
                        weekList.add(arrayFromMakeAlarm[4])
                        weekList.add(arrayFromMakeAlarm[5])
                        weekList.add(arrayFromMakeAlarm[6])

                        addNewAlarm_normal_exact(alarmManager!!,
                            context,
                            weekList,
                            arrayFromMakeAlarm[7],
                            arrayFromMakeAlarm[9],
                            arrayFromMakeAlarm[10],
                            arrayFromMakeAlarm[11],
                            arrayFromMakeAlarm[12],
                            arrayFromMakeAlarm[13])
                    }
                }
            }
            // 지금 울린 알람 기록
            function.saveFileAsString("history.bat", AppClass.context, " BroadCast is ended")
        }
    }
}

// 일반 알람 만들기
fun addNewAlarm_normal_exact(alarmManager: AlarmManager,
                             context: Context,
                             weekList: List<Int>,
                             progress: Int,
                             hour: Int,
                             min: Int,
                             requestCode: Int,
                             bellIndex: Int,
                             alarmMode: Int){
    val quick = 0   // 이 메서드는 normal 이므로 반복해서 울린다.

    val calendar: Calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        // 정확한 시간 설정
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, min)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    var calendarMillis = calendar.timeInMillis

    // 지정한 시간이 현재 시간보다 과거일 경우 + interval을 해줘야한다
    val curCalendar = Calendar.getInstance()
    if (curCalendar.timeInMillis > calendarMillis){
        val intervalDay = (24 * 60 * 60 * 1000).toLong() // 24시간
        calendarMillis += intervalDay
    }

    val intent = Intent("com.maidalarm.easyo.alarm")
    // *** 여기서 intent에 데이터를 넣어서 BroadCast에서 사용할 수 있다
    // pendingIntent 에는 1개의 변수만 넣을 수 있기 때문에 리스트를 임시로 만들어 넣는게 제일 좋다
    // 넘겨줄 항목은 다음과 같다.
    // Sun ~ Sat, progress, quick, hour, min, requestCode => 12개의 항목을 가진 리스트
    var ListForPendingIntent = mutableListOf<Int>()
    ListForPendingIntent.add(progress)
    ListForPendingIntent.add(quick)
    ListForPendingIntent.add(hour)
    ListForPendingIntent.add(min)
    ListForPendingIntent.add(requestCode)
    ListForPendingIntent.add(bellIndex)
    ListForPendingIntent.add(alarmMode)

    // weekList 에는 alarmFragment 에서 받아온 alarmWeek에 대한 리스트 정보가 담겨있다
    ListForPendingIntent = (weekList + ListForPendingIntent) as ArrayList<Int>

    // intent에 putIntent를 하기 위해선 List -> array로 변환필요
    Log.d("makeAlarm", "ListForPendingIntent: $ListForPendingIntent")
    intent.putExtra("arrayForPendingIntent", ListForPendingIntent)
    intent.addFlags(FLAG_INCLUDE_STOPPED_PACKAGES)
    intent.action = "com.maidalarm.easyo.alarm"
    intent.component = ComponentName("com.MaidAlarm.easyo_alarm", "com.MaidAlarm.easyo_alarm.Receiver")

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )
    Log.d("makeAlarm", "알람 등록한 시간: " + Date().toString() + " / " +curCalendar.timeInMillis)
    Log.d("makeAlarm", "설정된 시간: ${hour}시 ${min}분")
    Log.d("makeAlarm", "requestCode: $requestCode")

    // 다시 지정한 시간에 알람 울리게 설정
    alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendarMillis, pendingIntent)
}



