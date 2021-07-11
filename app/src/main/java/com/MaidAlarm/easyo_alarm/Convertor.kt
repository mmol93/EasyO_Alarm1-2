package com.MaidAlarm.easyo_alarm

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat


object Convertor {
    fun bitmapConvertor(context: Context, drawableId : Int): Bitmap? {
        var drawable = ContextCompat.getDrawable(context, drawableId)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable!!).mutate()
        }
        // 비트맵 전환을 위한 포맷 정의
        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable!!.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        // 비트맵으로 다시 그리기
        val canvas = Canvas(bitmap)
        drawable!!.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        drawable!!.draw(canvas)

        return bitmap
    }
}