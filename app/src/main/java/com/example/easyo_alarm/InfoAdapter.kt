package com.example.easyo_alarm

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.easyo_alarm.databinding.InfoRowBinding

class InfoAdapter(val context : Context) : RecyclerView.Adapter<InfoViewHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoViewHolder {
        return InfoViewHolder(LayoutInflater.from(context).inflate(R.layout.info_row, parent, false))
    }

    override fun getItemCount(): Int {
        return 2
    }

    override fun onBindViewHolder(holder: InfoViewHolder, position: Int) {
        when(position){
            // * 0 : 문의탭
            0 ->{
                holder.binder.infoImage.setImageResource(R.drawable.info_emal)
                holder.binder.infoText.text = context.getString(R.string.infoItem_contact)
                holder.binder.infoSubText.text = context.getString(R.string.infoItem_subContact)
                holder.binder.rowItemView.setOnClickListener {
                    val emailAddress = "ljws93@naver.com"
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.data = Uri.parse("mailto:")
                    intent.type = "text/plain"

                    intent.putExtra(Intent.EXTRA_EMAIL, emailAddress)
                    context.startActivity(intent)
                }
            }
            // * 1 : 오픈소스탭
            1 -> {
                holder.binder.infoImage.setImageResource(R.drawable.info_open)
                holder.binder.infoText.text = context.getString(R.string.infoItem_openSource)
                holder.binder.infoSubText.text = context.getString(R.string.infoItem_subOpenSource)
                holder.binder.rowItemView.setOnClickListener {
                    // 오픈소스 항목 입력
                    val listItem = arrayOf("Apache2.0", "GNU General Public License v3.0", "com.github.iammert:ReadableBottomBar:0.2",
                            "com.github.imtuann:FloatingActionButtonExpandable:1.1.2")
                    // 리스트 다이얼로그 만들기
                    val dialogBuilder = AlertDialog.Builder(context)
                    dialogBuilder.setTitle(context.getString(R.string.infoItem_openSource))
                    dialogBuilder.setNeutralButton(context.getString(R.string.cancelBtn), null)

                    // 각 항목 클릭에 따른 리스너 설정
                    val openSourceListener = object : DialogInterface.OnClickListener{
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            val intent = Intent(context, LicenseActivity::class.java)
                            when(which){
                                // Apache
                                0 -> {
                                    intent.putExtra("content", context.getString(R.string.license_Apache))
                                }
                                // GPL3.0
                                1 -> {
                                    intent.putExtra("content", context.getString(R.string.license_GPL))
                                }
                                // bottomTap
                                2 ->{
                                    intent.putExtra("content", context.getString(R.string.license_bottomTap))
                                }
                                // floatingButton
                                3 -> {
                                    intent.putExtra("content", context.getString(R.string.license_floatingButton))
                                }
                            }
                            context.startActivity(intent)
                        }
                    }
                    dialogBuilder.setItems(listItem, openSourceListener)
                    dialogBuilder.show()
                }
            }
            // * 2 : 후원탭 - 현재 미사용 상태
//            2 ->{
//                holder.binder.infoImage.setImageResource(R.drawable.info_support)
//                holder.binder.infoText.text = context.getString(R.string.infoItem_support)
//                holder.binder.infoSubText.text = context.getString(R.string.infoItem_subSupport)
//                holder.binder.rowItemView.setOnClickListener {
//                }
//            }
        }
    }
}

class InfoViewHolder(view : View) : RecyclerView.ViewHolder(view){
    val binder : InfoRowBinding = InfoRowBinding.bind(view)

    val row_mainText = binder.infoText
    val row_SubText = binder.infoSubText
    val row_image = binder.infoImage
    val row_view = binder.rowItemView
}