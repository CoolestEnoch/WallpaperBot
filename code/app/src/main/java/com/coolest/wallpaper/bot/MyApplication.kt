package com.coolest.wallpaper.bot

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri

class MyApplication :Application(){

	companion object{
		var dailyBingWallpaperBitmap: Bitmap? = null
		var currentImageBitMap: Bitmap? = null
		var currentImageFileName = ""
		var currentImageURL = ""
		var currentImageUri: Uri? = null
		var alreadySaved = false
		var firstStartUp = false
	}

	override fun onCreate() {
		super.onCreate()

	}
}