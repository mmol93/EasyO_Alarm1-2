package com.example.easyo_alarm

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.example.easyo_alarm.databinding.MainalarmRawBinding


class RecyclerAdapter(val context : Context, val SQLHelper : SQLHelper, var size : Int) : RecyclerView.Adapter<ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.mainalarm_raw, parent, false))
    }

    // onBindViewHolder의 position은 0부터 시작한다
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // *** SQL의 모든 데이터를 가져와서 어댑터에 등록시킨다
        val sql_select = "select * from MaidAlarm"
        val c1 = SQLHelper.writableDatabase.rawQuery(sql_select, null)

        val idxList = mutableListOf<Int>()
        val hourList = mutableListOf<Int>()
        val minList = mutableListOf<Int>()
        val progressList = mutableListOf<Int>()
        val Sun = mutableListOf<Int>()
        val Mon = mutableListOf<Int>()
        val Tue = mutableListOf<Int>()
        val Wed = mutableListOf<Int>()
        val Thu = mutableListOf<Int>()
        val Fri = mutableListOf<Int>()
        val Sat = mutableListOf<Int>()
        val requestCode = mutableListOf<Int>()
        val quick = mutableListOf<Int>()
        val switch = mutableListOf<Int>()

        // 모든 컬럼, 모든 레코드의 값 가져와서 리스트 안에 넣기
        while (c1.moveToNext()){
            val index1 = c1.getColumnIndex("idx")
            val index2 = c1.getColumnIndex("hourData")
            val index3 = c1.getColumnIndex("minData")
            val index4 = c1.getColumnIndex("progressData")
            val index5 = c1.getColumnIndex("Sun")
            val index6 = c1.getColumnIndex("Mon")
            val index7 = c1.getColumnIndex("Tue")
            val index8 = c1.getColumnIndex("Wed")
            val index9 = c1 .getColumnIndex("Thu")
            val index10 = c1.getColumnIndex("Fri")
            val index11 = c1.getColumnIndex("Sat")
            val index12 = c1.getColumnIndex("requestCode")
            val index13 = c1.getColumnIndex("quick")
            val index14 = c1.getColumnIndex("switch")

            idxList.add(c1.getInt(index1))
            hourList.add(c1.getInt(index2))
            minList.add(c1.getInt(index3))
            progressList.add(c1.getInt(index4))
            Sun.add(c1.getInt(index5))
            Mon.add(c1.getInt(index6))
            Tue.add(c1.getInt(index7))
            Wed.add(c1.getInt(index8))
            Thu.add(c1.getInt(index9))
            Fri.add(c1.getInt(index10))
            Sat.add(c1.getInt(index11))
            requestCode.add(c1.getInt(index12))
            quick.add(c1.getInt(index13))
            switch.add(c1.getInt(index14))
        }

        Log.d("RecyclerAdapter", "idxList in Bind(): $idxList")   // SQL에 있는 idx의 리스트
        Log.d("RecyclerAdapter", "position in Bind(): $position") // 현재 onBindViewHolder에서 참조하고 있는 리스트의 position

        // ** 뷰에 시간을 세팅한다 **
        // 단, 시간과 분은 반드시 2자리로 표현하게 한다.
        if (hourList[position].toString().length < 2 && minList[position].toString().length < 2){
            holder.row_clock.text = "0${hourList[position]} : 0${minList[position]}"

        }else if (hourList[position].toString().length < 2){
            holder.row_clock.text = "0${hourList[position]} : ${minList[position]}"

        }else if (minList[position].toString().length < 2){
            holder.row_clock.text = "${hourList[position]} : 0${minList[position]}"

        }else{
            holder.row_clock.text = "${hourList[position]} : ${minList[position]}"
        }

        // ** 각 요일별로 텍스트에 배경색을 넣어 구분을 하게 한다. **
        if (Sun[position].toString() == "1"){
            holder.row_sun.setBackgroundColor(Color.parseColor("#1ABC9C"))
        }
        if (Mon[position].toString() == "1"){
            holder.row_mon.setBackgroundColor(Color.parseColor("#1ABC9C"))
        }
        if (Tue[position].toString() == "1"){
            holder.row_tues.setBackgroundColor(Color.parseColor("#1ABC9C"))
        }
        if (Wed[position].toString() == "1"){
            holder.row_wed.setBackgroundColor(Color.parseColor("#1ABC9C"))
        }
        if (Thu[position].toString() == "1"){
            holder.row_thur.setBackgroundColor(Color.parseColor("#1ABC9C"))
        }
        if (Fri[position].toString() == "1"){
            holder.row_fri.setBackgroundColor(Color.parseColor("#1ABC9C"))
        }
        if (Sat[position].toString() == "1"){
            holder.row_sat.setBackgroundColor(Color.parseColor("#1ABC9C"))
        }

        // *** switch를 SQL에 저장한 switch 값을 적용한다
        if (switch[position] == 1){
            Log.d("RecyclerAdapter", "switch: ${switch[position]}")
            holder.row_switch.isChecked = true
        }
        else {
            Log.d("RecyclerAdapter", "switch: ${switch[position]}")
            holder.row_switch.isChecked = false
        }

        // *** switch on/off 의 변화에 따른 행동 정의
        holder.row_switch.setOnCheckedChangeListener() { compoundButton: CompoundButton, b: Boolean ->
            // 알람 매니저에 알람 등록하기
            if (holder.row_switch.isChecked){
                val weekList = mutableListOf<Int>(Sun[position], Mon[position], Tue[position], Wed[position], Thu[position], Fri[position], Sat[position])
                val reNewAlarm = makeAlarm(context, hourList[position], minList[position], progressList[position], weekList, requestCode[position])
                // quick인지 normal인지에 따라 알람 매니저의 다른 메서드를 호출하게 한다
                if (quick[position] == 1){
                    // quick == 1 이면 quick 알람임
                    reNewAlarm.addNewAlarm_once()
                    // SQL 데이터에 있는 switch 컬럼도 바꿔줘야 switch의 기록이 유지된다
                    val sql_update = "update MaidAlarm set switch = ? where idx = ?"
                    val arg1 = arrayOf(1, position + 1)
                    SQLHelper.writableDatabase.execSQL(sql_update, arg1)
                    Log.d("RecyclerAdapter", "position: $position")

                }else{  // normal 알람의 경우
                    reNewAlarm.addNewAlarm_normal()
                    // SQL 데이터에 있는 switch 컬럼도 바꿔줘야 switch의 기록이 유지된다
                    val sql_update = "update MaidAlarm set switch = ? where idx = ?"
                    val arg1 = arrayOf(1, position + 1)
                    SQLHelper.writableDatabase.execSQL(sql_update, arg1)
                }
            }
            // 알람 매니저에 해당 알람 캔슬하기
            else{
                val weekList = mutableListOf<Int>(Sun[position], Mon[position], Tue[position], Wed[position], Thu[position], Fri[position], Sat[position])
                val reNewAlarm = makeAlarm(context, hourList[position], minList[position], progressList[position], weekList, requestCode[position])

                reNewAlarm.cancelAlarm(requestCode[position])


                // switch를 off할 경우 SQL 데이터에 있는 switch 컬럼도 바꿔줘야 switch의 기록이 유지된다
                val sql_update = "update MaidAlarm set switch = ? where idx = ?"
                val arg1 = arrayOf(0, position + 1)
                SQLHelper.writableDatabase.execSQL(sql_update, arg1)
            }
        }

        // *** 쓰레기통 아이콘을 눌렀을 때 = 해당 리스트 삭제하기 ***
        holder.row_trash.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(context)

            // dialog의 positive 버튼의 리스너에 대한 정의
            val positiveListener = object : DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    // 데이터를 삭제하기 전에 알람매니저를 먼저 취소한다.
                    // 해당 알람 캔슬하기
                    val weekList = mutableListOf<Int>(Sun[position], Mon[position], Tue[position], Wed[position], Thu[position], Fri[position], Sat[position])
                    val reNewAlarm = makeAlarm(context, hourList[position], minList[position], progressList[position], weekList, requestCode[position])
                    reNewAlarm.cancelAlarm(requestCode[position])

                    // ** RecyclerView를 이루고 있는 리스트도 데이터를 삭제해야한다.
                    Log.d("RecyclerAdapter", "which position delete?: $position")
                    Log.d("RecyclerAdapter", "minList before delete: $minList")
                    idxList.removeAt(position)
                    hourList.removeAt(position)
                    minList.removeAt(position)
                    progressList.removeAt(position)
                    Sun.removeAt(position)
                    Mon.removeAt(position)
                    Tue.removeAt(position)
                    Wed.removeAt(position)
                    Thu.removeAt(position)
                    Fri.removeAt(position)
                    Sat.removeAt(position)
                    Log.d("RecyclerAdapter", "minList after delete: $minList")

                    // ** SQL의 데이터를 삭제한다
                    val SQLHelper = SQLHelper(context)
                    val sql_delete = "delete from MaidAlarm where idx = ?"
                    val arg1 = arrayOf("${position + 1}")   // position은 0부터 시작하고 SQL의 idx는 1부터 시작한다

                    SQLHelper.writableDatabase.execSQL(sql_delete, arg1)

                    // ** SQL 데이터를 삭제 했으니 인덱스 및 항목들을 재정렬 해야 순서대로 배치된다
                    var index = 1
                    val c1 = SQLHelper.writableDatabase.rawQuery(sql_select, null)
                    val sql_modify = "update MaidAlarm set idx = ? where idx = ?"

                    while (c1.moveToNext()){
                        val SQL_index = c1.getColumnIndex("idx")
                        val SQL_index2 = c1.getInt(SQL_index)
                        val arg1 = arrayOf(index, SQL_index2)
                        index += 1

                        SQLHelper.writableDatabase.execSQL(sql_modify, arg1)
                    }

                    val c2 = SQLHelper.writableDatabase.rawQuery(sql_select, null)
                    size = c2.count

                    SQLHelper.close()
                    notifyItemRemoved(position)  // 데이터를 삭제하고 나서 RecyclerView를 업데이트
                    notifyItemRangeChanged(position, size)  // 삭제하고나서 어댑터의 item Range 갱신
                }
            }

            dialogBuilder.setTitle(context.getString(R.string.list_dialog_title))
            dialogBuilder.setMessage(context.getString(R.string.list_delete_dialog))
            dialogBuilder.setPositiveButton(context.getString(R.string.list_dialog_Yes), positiveListener)
            dialogBuilder.setNeutralButton(context.getString(R.string.list_dialog_No), null)

            dialogBuilder.show()
        }
    }

    override fun getItemCount(): Int {
        return size
    }
}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val binder : MainalarmRawBinding = MainalarmRawBinding.bind(view)

    val row_clock = binder.rowClcok
    val row_trash = binder.shortImageTrash
    val row_sun = binder.rowSun
    val row_mon = binder.rowMon
    val row_tues = binder.rowTues
    val row_wed = binder.rowWed
    val row_thur = binder.rowThur
    val row_fri = binder.rowFri
    val row_sat = binder.rowSat
    val row_switch = binder.rowSwitch
}