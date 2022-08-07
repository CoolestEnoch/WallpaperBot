package com.coolest.wallpaper.bot.utils

import android.content.Context
import android.util.Log
import android.widget.LinearLayout
import android.widget.ScrollView
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.coolest.wallpaper.bot.R
import java.lang.StringBuilder
import java.util.*

object ReleaseNotesUtil {
	var noteList: MutableList<Version?> = ArrayList()
	var alert: AlertDialog? = null
	var builder: AlertDialog.Builder? = null
	const val tab_and_new_line = "\n\t\t\t"
	fun showAlertDialog(title: String?, str: String?, context: Context?) {
		builder = AlertDialog.Builder(context!!)
		alert = builder!!
			.setTitle(title)
			.setMessage(str)
			.setPositiveButton("确定") { dialog, which -> }.create() //创建AlertDialog对象
		alert!!.show() //显示对话框
	}

	fun setReleaseNotes() {
		noteList.apply {
			add(
				Version(
					2.23,
					"[修复]Android9及以上系统的设备概率性刷不出来图片" + tab_and_new_line +
							"[修复]刷新交互逻辑错误" + tab_and_new_line +
							"[修复]部分图标显示问题" + tab_and_new_line +
							"[优化]设置壁纸的速度" + tab_and_new_line +
							"[优化]适配MIUI小白条沉浸" + tab_and_new_line +
							"[优化]根据最小权限原则, 去除在Android11及以上设备上多申请的一个权限" + tab_and_new_line +
							"[优化]全透明的浏览体验" + tab_and_new_line +
							"[新增]图片加载动画" + tab_and_new_line +
							"[新增]按钮高斯模糊"
				)
			)
			add(
				Version(
					2.5,
					"[新增]两个壁纸更新渠道" + tab_and_new_line +
							"[修复]部分设备上图片保存失败导致的闪退" + tab_and_new_line +
							"[修复]部分场景下深色模式显示异常"
				)
			)
			add(
				Version(
					2.51,
					"[修复]部分设备上保存图片闪退的问题" + tab_and_new_line +
							"[修复]转屏后会自动获取新图片" + tab_and_new_line +
							"[修复]转屏后程序重进" + tab_and_new_line +
							"[优化]图片保存逻辑" + tab_and_new_line +
							"[优化]部分场景的性能和稳定性"
				)
			)
			add(
				Version(
					2.6,
					"[适配]Material You 动态配色算法\n" +
							"(需更新手机系统至Android 12或更高版本)"
				)
			)
			add(
				Version(
					2.61,
					"[修复]FydeOS 14.2(83)上保存图片失败的问题"
				)
			)
		}
		Collections.reverse(noteList)
	}

	val releaseNotes: String
		get() {
			if (noteList.isEmpty()) setReleaseNotes()
			val sb = StringBuilder()
			for (v in noteList) {
				sb.append(v)
			}
			return sb.toString()
		}

	fun showReleaseNotes(context: Context) {
//		showAlertDialog("更新日志" + "   " + "\n当前版本" + CoolUtils.getAppVersionName(context), getReleaseNotes(), context);
		releaseNotes
		val linearLayout = LinearLayout(context)
		val params = LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT
		)
		linearLayout.layoutParams = params
		linearLayout.orientation = LinearLayout.VERTICAL
		Log.e("for", "before")
		for (version in noteList) {
			val list = LinkedList<String>().apply {
				add(version!!.version.toString())
				val detail = version.notes.split(tab_and_new_line.toRegex()).toTypedArray()
				for (str in detail) {
					add(str)
				}
			}
			linearLayout.addView(
				ViewUtils_Kotlin.createBigButton(
					context,
					R.drawable.ic_baseline_grass_24,
					R.color.item_card_bg,
					list,
					null,
					R.color.white
				) {}
			)
			Log.e("for", "run in")
		}
		Log.e("for", "after")
		val scrollView = ScrollView(context)
		val scrollParams: ViewGroup.LayoutParams = LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT
		)
		scrollView.layoutParams = scrollParams
		scrollView.addView(linearLayout)
		ViewUtils_Kotlin.getBigCardFromBottom(
			context,
			"更新日志\t当前版本${
				context.packageManager.getPackageInfo(
					context.packageName,
					0
				).versionName
			}",
			scrollView
		).show()
	}

	fun showLastReleaseNotes(context: Context) {
//		showAlertDialog("更新日志" + "   " + "\n当前版本" + CoolUtils.getAppVersionName(context), getReleaseNotes(), context);
		releaseNotes
		val linearLayout = LinearLayout(context)
		val params = LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT
		)
		linearLayout.layoutParams = params
		linearLayout.orientation = LinearLayout.VERTICAL
		val version = noteList[0]
		val list = LinkedList<String>()
		list.add(version!!.version.toString())
		val detail = version.notes.split(tab_and_new_line.toRegex()).toTypedArray()
		for (str in detail) {
			list.add(str)
		}
		linearLayout.addView(
			ViewUtils_Kotlin.createBigButton(
				context,
				R.drawable.ic_baseline_grass_24,
				R.color.item_card_bg,
				list,
				null,
				R.color.white
			) {}
		)
		val scrollView = ScrollView(context)
		val scrollParams: ViewGroup.LayoutParams = LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT
		)
		scrollView.layoutParams = scrollParams
		scrollView.addView(linearLayout)
		ViewUtils_Kotlin.getBigCardFromBottom(context, "这个版本更新了啥", scrollView).show()
	}

	class Version(var version: Double, var notes: String) {
		override fun toString(): String {
			return "$version $notes\n"
		}
	}
}
