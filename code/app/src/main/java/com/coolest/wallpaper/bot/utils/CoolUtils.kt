package com.coolest.wallpaper.bot.utils

import android.os.Build
import android.text.TextUtils
import java.util.*

object CoolUtils {
	fun isMIUI(): Boolean {
		val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
		return !TextUtils.isEmpty(manufacturer) && (manufacturer == "xiaomi" || manufacturer == "blackshark")
	}

	fun isFydeOS():Boolean{
		val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
		return !TextUtils.isEmpty(manufacturer) && (manufacturer == "fydeos")
	}
}