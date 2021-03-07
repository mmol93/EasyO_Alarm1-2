package com.example.easyo_alarm

class CheckDpi(dpi : Float){
    val dpi = dpi

    fun getDpiString() : String {
        if (dpi >= 4.0) {
            return "xxxhdpi"
        }
        if (dpi >= 3.0) {
            return "xxhdpi"
        }
        if (dpi >= 2.0) {
            return "xhdpi"
        }
        if (dpi >= 1.5) {
            return "hdpi"
        }
        if (dpi >= 1.0) {
            return "mdpi"
        }
        return "ldpi";
    }
}