package com.MaidAlarm.easyo_alarm

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.*
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.MaidAlarm.easyo_alarm.databinding.FragmentAlarmBinding
import com.MaidAlarm.easyo_alarm.notification.notification
import java.lang.Exception
import java.util.*

class alarmFragment : Fragment() {
    val doneAlarmActivity = 100         // 알람 액티비티
    val doneShortAlarmActivity = 200    // 퀵 알람 액티비티 반환값용 변수
    lateinit var binder: FragmentAlarmBinding   // 데이터 바인더용 변수
    lateinit var app : AppClass

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_alarm, null)
        binder = FragmentAlarmBinding.bind(view)
        return view
    }

    // alarmFragment에 있는 모든 View 정보를 갱신한다(textView, notification, recyclerView)
    // RecyclerView에서 갱신될 때 사용된다
    fun renewDisplay(SQLHelper2 : SQLHelper, binder : FragmentAlarmBinding, app : AppClass){
        // 어댑터에 SQL 객체 정보와 레코드의 size를 보낸다
        val context = app.context_alarmFragent
        var SQLHelper : SQLHelper
        try {
            SQLHelper = SQLHelper(activity!!)
        }catch (e:Exception){
            SQLHelper = SQLHelper2
        }

        val sql = "select * from MaidAlarm"
        val c1 = SQLHelper.writableDatabase.rawQuery(sql, null)
        val size = c1.count
        SQLHelper.close()

        // 어댑터에 데이터 넣기
        try {
            binder.alarmListRecycle.layoutManager = LinearLayoutManager(requireContext())
            binder.alarmListRecycle.adapter = RecyclerAdapter(requireContext(), SQLHelper, size)
        }catch (e:Exception){

        }

        if (size > 0){
            // RecentAlarm 갱신하기
            val recentAlarm = RecentAlarm()
            val recentTimeList = recentAlarm.checkSQL(SQLHelper)
            // 알림은 있지만 모든 토글이 off 일 떄
            if (recentTimeList[0] == -1){
                binder.RecentTimeTextview.text = context.getString(R.string.alarmSetFragment_noAlarm)
                val notification = notification()
                notification.cancelNotification(context)
            }
            else{
                var textForWeek = ""     // notification에 사용하기 위한 텍스트를 정의1
                binder.RecentTimeTextview.text = ""
                // 시간 부분 입력
                var recentHour = ""
                var recentMin = ""
                if (recentTimeList[7] < 10){
                    recentHour = "0${recentTimeList[7]}"
                }else{
                    recentHour = "${recentTimeList[7]}"
                }
                if (recentTimeList[8] < 10){
                    recentMin = "0${recentTimeList[8]}"
                }else{
                    recentMin = "${recentTimeList[8]}"
                }
                binder.RecentTimeTextview.append(context.getString(R.string.alarmSetFragment_nextAlarm) + " $recentHour : $recentMin \n")
                app.recentTime = "$recentHour : $recentMin"

                // 월요일에 알람 있을 때 ~ 일요일에 알람 있을 때 -> 요일 부분 입력
                if (recentTimeList[1] == 1){
                    binder.RecentTimeTextview.append(context.getString(R.string.week_mon) + ", ")
                    textForWeek = textForWeek + context.getString(R.string.week_mon) + ", "
                }
                if (recentTimeList[2] == 1){
                    binder.RecentTimeTextview.append(context.getString(R.string.week_tue) + ", ")
                    textForWeek = textForWeek + context.getString(R.string.week_tue) + ", "
                }
                if (recentTimeList[3] == 1){
                    binder.RecentTimeTextview.append(context.getString(R.string.week_wed) + ", ")
                    textForWeek = textForWeek + context.getString(R.string.week_wed) + ", "
                }
                if (recentTimeList[4] == 1){
                    binder.RecentTimeTextview.append(context.getString(R.string.week_thur) + ", ")
                    textForWeek = textForWeek + context.getString(R.string.week_thur) + ", "
                }
                if (recentTimeList[5] == 1){
                    binder.RecentTimeTextview.append(context.getString(R.string.week_fri) + ", ")
                    textForWeek = textForWeek + context.getString(R.string.week_fri) + ", "
                }
                if (recentTimeList[6] == 1){
                    binder.RecentTimeTextview.append(context.getString(R.string.week_sat) + ", ")
                    textForWeek = textForWeek + context.getString(R.string.week_sat) + ", "
                }
                if (recentTimeList[0] == 1){
                    binder.RecentTimeTextview.append(context.getString(R.string.week_sun) + ", ")
                    textForWeek = textForWeek + context.getString(R.string.week_sun) + ", "
                }
                // 체크된 요일을 문자로 표시한다
                if (recentTimeList[0] == 1 || recentTimeList[1] == 1 || recentTimeList[2] == 1 || recentTimeList[3] == 1 || recentTimeList[4] == 1
                        || recentTimeList[5] == 1 || recentTimeList[6] == 1){
                    var text = binder.RecentTimeTextview.text
                    // 텍스트의 제일 마지막 문자(콤마)를 삭제
                    text = text.removeRange(text.length -2, text.length-1)
                    binder.RecentTimeTextview.text = text

                    // textForWeek에서 마지막 부분 콤마 제거하기
                    if (textForWeek.length > 2){
                        textForWeek = textForWeek.removeRange(textForWeek.length -2, textForWeek.length-1)
                    }
                    app.recentWeek = textForWeek    // notification에 사용하기 위한 텍스트 정의2
                }
                // notification 갱신
                if (app.recentTime.length > 0 && app.recentWeek.length > 0 && app.notificationSwitch == 1 && size > 0){
                    val notification = notification()
                    val notificationManager =context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notification.getNotification(context!!, "chanel1", "첫 번째 채널", notificationManager)
                    notification.makeNotification(app, context!!, notificationManager)
                }
            }
        }
        else{
            binder.RecentTimeTextview.text = getString(R.string.alarmSetFragment_noAlarm)
        }
    }

    // 21.03.29 - 기능 실현이 안되서 일단 숨김
//    private val dragListener = View.OnDragListener { v, event ->
//        Log.d("alarmFragment", "드래그중")
//        when(event.action){
//            DragEvent.ACTION_DRAG_STARTED -> {
//                // 리사이클로뷰를 스크롤 하면 floating 버튼이 안보이도록 한다
//                binder.fab.isGone = true
//                // 2초 뒤 다시 floating 버튼이 보이게 설정한다
//                val thread = object : Thread(){
//                    override fun run() {
//                        super.run()
//                        // 2초
//                        SystemClock.sleep(2 * 1000)
//                        // 2초가 지난 후 floating 버튼을 되돌리려고 할 때 프로그램을 종료할 가능성 있음
//                        // 종료되고 실시하면 에러 발생
//                        try {
//                            binder.fab.isGone = false
//                        }
//                        catch (e:Exception){
//
//                        }
//                    }
//                }
//                thread.start()
//            }
//        }
//        true
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 21.03.29 - 기능 실현이 안되서 일단 숨김
//        binder.fab.setOnDragListener(dragListener)

        // 플로팅 버튼 한 번 누름: 일반 알람 설정창
        binder.fab.setOnClickListener {
            val alarmActivity = Intent(activity, AlarmSetActivity::class.java)
            // 알람 세팅을 위한 액티비티 소환
            startActivityForResult(alarmActivity, doneAlarmActivity)
        }
        // 플로팅 버튼을 길게 눌렀을 때: 퀵알람
        binder.fab.setOnLongClickListener {
            val shortAlarmSetActivity = Intent(activity, ShortAlarmSetActivity::class.java)
            startActivityForResult(shortAlarmSetActivity, doneShortAlarmActivity)
            true
        }
        app = context!!.applicationContext as AppClass
        app.binder_alarmFragent = binder
        app.context_alarmFragent = context!!

        binder.alarmListRecycle.setOnDragListener { v, event ->
            when(event.action){
                DragEvent.ACTION_DRAG_STARTED -> {
                    // 리사이클로뷰를 스크롤 하면 floating 버튼이 안보이도록 한다
                    binder.fab.isGone = true
                    Log.d("alarmFragment", "드래그중")
                    // 2초 뒤 다시 floating 버튼이 보이게 설정한다
                    val thread = object : Thread(){
                        override fun run() {
                            super.run()
                            // 2초
                            SystemClock.sleep(2 * 1000)
                            // 2초가 지난 후 floating 버튼을 되돌리려고 할 때 프로그램을 종료할 가능성 있음
                            // 종료되고 실시하면 에러 발생
                            try {
                                binder.fab.isGone = false
                            }
                            catch (e:Exception){

                            }
                        }
                    }
                    thread.start()
                }
            }
            true
        }
    }
    // alarmFragment에 있는 모든 View 정보를 갱신한다(textView, notification, recyclerView)
    // onResume()에서 갱신될 때 사용된다
    fun renewDisplay(SQLHelper2 : SQLHelper){
        // 어댑터에 SQL 객체 정보와 레코드의 size를 보낸다
        var SQLHelper : SQLHelper
        try {
            SQLHelper = SQLHelper(activity!!)
        }catch (e:Exception){
            SQLHelper = SQLHelper2
        }

        val sql = "select * from MaidAlarm"
        val c1 = SQLHelper.writableDatabase.rawQuery(sql, null)
        val size = c1.count
        SQLHelper.close()

        // 어댑터에 데이터 넣기
        try {
            binder.alarmListRecycle.layoutManager = LinearLayoutManager(requireContext())
            binder.alarmListRecycle.adapter = RecyclerAdapter(requireContext(), SQLHelper, size)
        }catch (e:Exception){

        }

        if (size > 0){
            // RecentAlarm 갱신하기
            val recentAlarm = RecentAlarm()
            val recentTimeList = recentAlarm.checkSQL(SQLHelper)
            // 알림은 있지만 모든 토글이 off 일 떄
            if (recentTimeList[0] == -1){
                binder.RecentTimeTextview.text = getString(R.string.alarmSetFragment_noAlarm)
                val notification = notification()
                notification.cancelNotification(context!!)
            }
            else{
                var textForWeek = ""     // notification에 사용하기 위한 텍스트를 정의1
                binder.RecentTimeTextview.text = ""
                // 시간 부분 입력
                var recentHour = ""
                var recentMin = ""
                if (recentTimeList[7] < 10){
                    recentHour = "0${recentTimeList[7]}"
                }else{
                    recentHour = "${recentTimeList[7]}"
                }
                if (recentTimeList[8] < 10){
                    recentMin = "0${recentTimeList[8]}"
                }else{
                    recentMin = "${recentTimeList[8]}"
                }
                binder.RecentTimeTextview.append(getString(R.string.alarmSetFragment_nextAlarm) + " $recentHour : $recentMin \n")
                app.recentTime = "$recentHour : $recentMin"

                // 월요일에 알람 있을 때 ~ 일요일에 알람 있을 때 -> 요일 부분 입력
                if (recentTimeList[1] == 1){
                    binder.RecentTimeTextview.append(getString(R.string.week_mon) + ", ")
                    textForWeek = textForWeek + getString(R.string.week_mon) + ", "
                }
                if (recentTimeList[2] == 1){
                    binder.RecentTimeTextview.append(getString(R.string.week_tue) + ", ")
                    textForWeek = textForWeek + getString(R.string.week_tue) + ", "
                }
                if (recentTimeList[3] == 1){
                    binder.RecentTimeTextview.append(getString(R.string.week_wed) + ", ")
                    textForWeek = textForWeek + getString(R.string.week_wed) + ", "
                }
                if (recentTimeList[4] == 1){
                    binder.RecentTimeTextview.append(getString(R.string.week_thur) + ", ")
                    textForWeek = textForWeek + getString(R.string.week_thur) + ", "
                }
                if (recentTimeList[5] == 1){
                    binder.RecentTimeTextview.append(getString(R.string.week_fri) + ", ")
                    textForWeek = textForWeek + getString(R.string.week_fri) + ", "
                }
                if (recentTimeList[6] == 1){
                    binder.RecentTimeTextview.append(getString(R.string.week_sat) + ", ")
                    textForWeek = textForWeek + getString(R.string.week_sat) + ", "
                }
                if (recentTimeList[0] == 1){
                    binder.RecentTimeTextview.append(getString(R.string.week_sun) + ", ")
                    textForWeek = textForWeek + getString(R.string.week_sun) + ", "
                }
                if (recentTimeList[0] == 1 || recentTimeList[1] == 1 || recentTimeList[2] == 1 || recentTimeList[3] == 1 || recentTimeList[4] == 1
                        || recentTimeList[5] == 1 || recentTimeList[6] == 1){
                    var text = binder.RecentTimeTextview.text
                    // 텍스트의 제일 마지막 문자(콤마)를 삭제
                    text = text.removeRange(text.length -2, text.length-1)
                    binder.RecentTimeTextview.text = text

                    // textForWeek에서 마지막 부분 콤마 제거하기
                    if (textForWeek.length > 2){
                        textForWeek = textForWeek.removeRange(textForWeek.length -2, textForWeek.length-1)
                    }
                    app.recentWeek = textForWeek    // notification에 사용하기 위한 텍스트 정의2
                }
                if (app.recentTime.length > 0 && app.recentWeek.length > 0 && app.notificationSwitch == 1 && size > 0){
                    val notification = notification()
                    val notificationManager =context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notification.getNotification(context!!, "chanel1", "첫 번째 채널", notificationManager)
                    notification.makeNotification(app, context!!, notificationManager)
                }
            }
        }
        else{
            binder.RecentTimeTextview.text = getString(R.string.alarmSetFragment_noAlarm)
        }
    }


    // *** SQL 데이터에서 값을 다 가져와서 RecyclerAdapter에 보내기만 한다 ***
    // *** SQL 데이터 갱신에 따른 index 및 데이터 재배열은 리스트에서 삭제할 때만 한다 ***
    override fun onResume() {
        super.onResume()
        // alarmFragment에 있는 모든 정보를 갱신한다(textView, notification, recyclerView)
        val SQLHelper = SQLHelper(activity!!)
        renewDisplay(SQLHelper)
    }

    // *** 액티비티에서 돌아왔을 때 - SQL 데이터에 설정한 알람의 값을 갱신 + 알람 매니저에 보내기만 하면된다 ***
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 퀵 알람에서 돌아왔을 때
        if (requestCode == doneShortAlarmActivity){
            // 정상적으로 save 버튼을 클릭하여 돌아왔을 때
            if (resultCode == 100){
                // 시간, 분, progress 정보 얻어옴
                val hour = data?.getIntExtra("hour", 0)
                val min = data?.getIntExtra("min", 0)
                val progress = data?.getIntExtra("progress", 0)

                // 요일 계산에 사용하기 위해 현재 날짜 가져옴
                val present_Time = Calendar.getInstance()
                val presentDay = present_Time.get(Calendar.DAY_OF_YEAR)
                val presentHour = present_Time.get(Calendar.HOUR_OF_DAY)
                val presentMin = present_Time.get(Calendar.MINUTE)
                val presentSecond = present_Time.get(Calendar.SECOND)
                // 요일 데이터는 Int형으로 가져온다 (일요일:1 ~ 토요일:7)
                val presentWeek = present_Time.get(Calendar.DAY_OF_WEEK)

                // 알람 데이터에 넘겨줄 요일 리스트를 만든다
                // 각 요일에 0, 1을 넣어 어느 요일에 울리는 알람인지 구분해준다
                // 일요 ~ 토요
                var weekList = mutableListOf<Int>(0, 0, 0, 0, 0, 0, 0)

                // 실제로 설정에 참조할 시간 관련 변수들
                var alarmHour = presentHour + hour!!
                var alarmMin = presentMin + min!!
                var alarmWeek = presentWeek

                // 설정한 시간의 분 + 현재 시간의 분이 = 60분을 넘을 때
                if (alarmMin >= 60){
                    alarmMin = alarmMin - 60
                    alarmHour += 1
                }

                // 설정한 시간의 시간 + 현재 시간의 시간 = 24시간을 넘을 때
                if (alarmHour >= 24){
                    val dayCounter : Int = alarmHour / 24
                    alarmHour = alarmHour - (24 * dayCounter)
                    alarmWeek += dayCounter
                    // alarmWeek는 1~7사이에 있어야한다
                    if (alarmWeek > 7){
                        alarmWeek = alarmWeek - 7
                    }
                }

                // SQL에 넣기 위해선 alarmWeek를 String형의 요일요 변경 필요함
                // + weekList에도 내용을 갱신해준다
                var alarmWeek_String = ""
                when(alarmWeek){
                    1 -> {alarmWeek_String = "Sun"
                        weekList[0] = 1}
                    2 -> {alarmWeek_String = "Mon"
                        weekList[1] = 1}
                    3 -> {alarmWeek_String = "Tue"
                        weekList[2] = 1}
                    4 -> {alarmWeek_String = "Wed"
                        weekList[3] = 1}
                    5 -> {alarmWeek_String = "Thu"
                        weekList[4] = 1}
                    6 -> {alarmWeek_String = "Fri"
                        weekList[5] = 1}
                    7 -> {alarmWeek_String = "Sat"
                        weekList[6] = 1}
                }
                Log.d("alarmFragment", "weekList: $weekList")

                // *** 먼저 SQL에 위 데이터를 넣는다 ***
                val SQLHelper = SQLHelper(activity!!)

                val sql_insert = """
                insert into MaidAlarm (hourData, minData, progressData, $alarmWeek_String, requestCode, quick)
                values(?, ?, ?, ?, ?, ?)
                """.trimIndent()

                // requestCode는 알람을 설정한 현재의 일+시간+분+초로 이루어진다
                val requestCode = presentDay.toString() + presentHour.toString() +
                        presentMin.toString() + presentSecond.toString()
                Log.d("alarmFragment", "requestCode: $requestCode")

                val arg1 = arrayOf(alarmHour, alarmMin, progress, 1, requestCode.toInt(), 1)

                SQLHelper.writableDatabase.execSQL(sql_insert, arg1)

                // 알람 매니저에도 해당 알람 정보를 보내준다
                // 필요 매개변수: context, 알람시간, 알람분, progress, 알람요일, requestCode(Int), quick 여부
                val newAlarm = makeAlarm(requireContext(), alarmHour, alarmMin, progress!!, weekList, requestCode.toInt())
                newAlarm.addNewAlarm_once()

                SQLHelper.close()

                Log.d("alarmFragment", "hour: $alarmHour")
                Log.d("alarmFragment", "transMin: $alarmMin")
                Log.d("alarmFragment", "progress: $progress")
                Log.d("alarmFragment", "alarmWeek_String: $alarmWeek_String")
            }
        }

        if (requestCode == doneAlarmActivity){
            if (resultCode == 200){
                // 액티비티에서 데이터 가져오기
                val hour = data?.getIntExtra("hour", 0)
                val min = data?.getIntExtra("min", 0)
                val progress = data?.getIntExtra("progress", 0)
                val weekList = data?.getIntegerArrayListExtra("weekList")
                Log.d("alarmFragment", "weekList: $weekList")

                // 각 요일별로 변수를 만들어서 weekList의 데이터를 반영하기
                var Sun = 0
                var Mon = 0
                var Tue = 0
                var Wed = 0
                var Thu = 0
                var Fri = 0
                var Sat = 0

                if (weekList!![0] == 1){
                    Sun = 1
                }
                if (weekList!![1] == 1){
                    Mon = 1
                }
                if (weekList!![2] == 1){
                    Tue = 1
                }
                if (weekList!![3] == 1){
                    Wed = 1
                }
                if (weekList!![4] == 1){
                    Thu = 1
                }
                if (weekList!![5] == 1){
                    Fri = 1
                }
                if (weekList!![6] == 1){
                    Sat = 1
                }

                // requestCode 생성을 위해 현재 날짜 가져옴
                val present_Time = Calendar.getInstance()
                val presentDay = present_Time.get(Calendar.DAY_OF_YEAR)
                val presentHour = present_Time.get(Calendar.HOUR_OF_DAY)
                val presentMin = present_Time.get(Calendar.MINUTE)
                val presentSecond = present_Time.get(Calendar.SECOND)

                // *** SQL에 데이터 입력하기 ***
                val SQLHelper = SQLHelper(activity!!)

                val sql_insert = """
                insert into MaidAlarm (hourData, minData, progressData, "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat",
                 requestCode, quick)
                values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent()

                // requestCode는 알람을 설정한 현재의 일+시간+분+초로 이루어진다
                val requestCode = presentDay.toString() + presentHour.toString() +
                        presentMin.toString() + presentSecond.toString()

                val arg1 = arrayOf(hour, min, progress, Sun, Mon, Tue, Wed, Thu, Fri, Sat, requestCode.toInt(), 0)

                SQLHelper.writableDatabase.execSQL(sql_insert, arg1)

                // 알람 매니저에도 해당 알람 정보를 보내준다
                // 필요 매개변수: context, 알람시간, 알람분, progress, 알람요일, requestCode(Int), quick 여부
                val newAlarm = makeAlarm(requireContext(), hour!!, min!!, progress!!, weekList, requestCode.toInt())
                newAlarm.addNewAlarm_normal()

                SQLHelper.close()
            }
        }
    }
}