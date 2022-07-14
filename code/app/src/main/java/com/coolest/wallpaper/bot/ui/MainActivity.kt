package com.coolest.wallpaper.bot.ui

import android.Manifest
import android.R.attr.bitmap
import android.app.ActivityManager
import android.app.WallpaperManager
import android.content.*
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.coolest.wallpaper.bot.MyApplication
import com.coolest.wallpaper.bot.R
import com.coolest.wallpaper.bot.databinding.ActivityMainBinding
import com.coolest.wallpaper.bot.utils.*
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.permissionx.guolindev.PermissionX
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.*
import java.net.URL
import java.util.*
import java.util.regex.Pattern
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding

	private var githubName = "CoolestEnoch"
	private var githubRepo = "WallpaperBot"


	//复制链接相关
	//获取剪贴板管理器：
	lateinit var clipBoardManager: ClipboardManager
	lateinit var wallpaperManager: WallpaperManager


	//当前图片相关信息
/*	private var currentImageBitMap: Bitmap? = null
	private var currentImageFileName = ""
	private var currentImageUri: Uri? = null
	private var alreadySaved = false*/

	private fun getCurrentImageBitmap() = MyApplication.currentImageBitMap
	private fun getCurrentImageFileName() = MyApplication.currentImageFileName
	private fun getCurrentImageUri() = MyApplication.currentImageUri
	private fun getAlreadySaved() = MyApplication.alreadySaved

	private fun setCurrentImageBitmap(bitmap: Bitmap?) {
		MyApplication.currentImageBitMap = bitmap
	}

	private fun setCurrentImageFileName(fileName: String) {
		MyApplication.currentImageFileName = fileName
	}

	private fun setCurrentImageUri(uri: Uri) {
		MyApplication.currentImageUri = uri
	}

	private fun setAlreadySaved(alreadySaved: Boolean) {
		MyApplication.alreadySaved = alreadySaved
	}


	//兼容安卓10以前
	private val pictureFolderPath =
		Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
			.toString() + "/WallpaperBot/"

	private val channels = mapOf(
		"二次元" to "http://api.btstu.cn/sjbz/?lx=dongman",
		"二次元2" to "https://api.maho.cc/random-img/pc.php",
		"二次元3(竖屏)" to "https://api.maho.cc/random-img/mobile.php",
		"二次元风景" to "https://api.ghser.com/random/bg.php",
		"三次元" to "http://api.btstu.cn/sjbz/?lx=meizi",
		"三次元2" to "http://api.btstu.cn/sjbz/?lx=m_meizi",
		"三次元3" to "http://api.btstu.cn/sjbz/?lx=m_dongman",
		"随机" to "http://api.btstu.cn/sjbz/?lx=suiji"
	)

	private var currentChannel = ""//${channels["二次元"]}
	private var currentLink = ""

	//ljc定制代码
	private val lsp_enabled = false
	private var lsp_count = 0
	private val lsp_count_max = 5
	private val setuList = listOf(
		"https://image.coolapk.com/picture/2022/0321/21/2395617_0869_1195_522@2047x1046.jpg",
		"https://tse1-mm.cn.bing.net/th/id/R-C.7a3b1aea8325c2b4a70cf5e8b917ef5b?rik=o3iLuWH6Tx9nuw&riu=http%3a%2f%2fimg.pclady.com.cn%2fimages%2fupload%2fupc%2ftx%2fpclady_capsule%2f1704%2f11%2fc1%2f42741410_1491902113969.jpg&ehk=tiwRUBJZi9%2fdYbTyrW9VShP3OQmg6uHb3%2f2SJKkB2Lo%3d&risl=&pid=ImgRaw&r=0",
		"https://tse4-mm.cn.bing.net/th/id/OIP-C.4NcXykybn9lqHncwK4FbSQHaKm?pid=ImgDet&rs=1",
		"https://tse1-mm.cn.bing.net/th/id/R-C.2678f5cf7e30fbe2b713f5b7fef42af1?rik=6pMopp16TxVV5w&riu=http%3a%2f%2fimg.3dmgame.com%2fuploads%2fallimg%2f150707%2f153_150707133340_2_lit.jpg&ehk=nRnElpArZlPZidtnH%2bh1k8QJQDYceSdjm1Zd%2fmye1Tg%3d&risl=&pid=ImgRaw&r=0",
		"https://tse1-mm.cn.bing.net/th/id/R-C.2caf0125d1564875c5a466978a51bd00?rik=W0PAYoUMoe%2bU%2bw&riu=http%3a%2f%2fhimg.china.cn%2f0%2f4_188_192008_650_580.jpg&ehk=DCChhBKTWAs%2fqJGauyB7GXvWftUfuRj%2fKGZ8bqI2EQA%3d&risl=&pid=ImgRaw&r=0",
		"https://tse3-mm.cn.bing.net/th/id/OIP-C.EVxgpLIJ-aroPlM3ZGE7DAAAAA?pid=ImgDet&rs=1",
		"https://tva2.sinaimg.cn/large/9bd9b167gy1fwrtn9p6p4j21hc0u0qqf.jpg",
		"https://tva2.sinaimg.cn/large/9bd9b167gy1g4li9zkmi6j21hc0xcb29.jpg",
		"https://tva2.sinaimg.cn/large/9bd9b167gy1g4lifhuoeoj21hc0xchdt.jpg",
		"https://tva1.sinaimg.cn/large/9bd9b167gy1g4lhdj5oxaj21hc0xcwhu.jpg",
		"https://tva1.sinaimg.cn/large/0072Vf1pgy1foxlhl2l61j31hc0u0ash.jpg"
	)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)

		adjustScreenOrientation()

		setContentView(binding.root)

		//相关系统服务
		clipBoardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
		wallpaperManager = getSystemService(WALLPAPER_SERVICE) as WallpaperManager

		/**
		 * MIUI小白条沉浸
		 * from https://dev.mi.com/console/doc/detail?pId=2229
		 */
//		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//		window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) ///设置沉浸式状态栏，在MIUI系统中，状态栏背景透明。原生系统中，状态栏背景半透明。
//		window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION) //设置沉浸式虚拟键，在MIUI系统中，虚拟键背景透明。原生系统中，虚拟键背景半透明。

//		window.setFlags(
//			WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
//			WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)


		//获取更新渠道
		currentChannel = getSharedPreferences(
			"config",
			Context.MODE_PRIVATE
		).getString("wallpaperChannel", "").toString()//${channels["二次元"]}

		//拉取开屏第一张图与转屏适配
		when (MyApplication.firstStartUp) {
			//刚启动程序
			false -> {
				MyApplication.firstStartUp = true
				//申请权限
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
					PermissionX.init(this)
						.permissions(
							Manifest.permission.WRITE_EXTERNAL_STORAGE,
							Manifest.permission.READ_EXTERNAL_STORAGE,
						)
						.explainReasonBeforeRequest()
						.onExplainRequestReason { scope, deniedList ->
							scope.showRequestReasonDialog(deniedList, "保存图片所需权限", "好", "取消")
						}
						.onForwardToSettings { scope, deniedList ->
							scope.showForwardToSettingsDialog(
								deniedList,
								"不授予权限则可能出现闪退!",
								"好",
								"取消"
							)
						}
						.request { allGranted, grantedList, deniedList ->
							if (allGranted) {
								Snackbar.make(
									this.window.decorView,
									"权限状态正常",
									Snackbar.LENGTH_LONG
								)
							} else {
								Snackbar.make(
									this.window.decorView,
									"以下权限被拒绝: $deniedList",
									Snackbar.LENGTH_LONG
								).show()
							}
						}
				}
				//获取必应壁纸
				thread {
					try {
						val dis = DataInputStream(URL(getTodayWallpaperURL()).openStream())
						MyApplication.dailyBingWallpaperBitmap = BitmapFactory.decodeStream(dis)
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}
				//启动自动检查更新
				checkUpdate(
					packageManager.getPackageInfo(packageName, 0).versionName,
					binding.swipeRefresh,
					githubName,
					githubRepo,
					true
				)

				//更新版本配置文件设置
				val lastVersion = getSharedPreferences(
					"config",
					Context.MODE_PRIVATE
				).getString("lastVersion", "1.0").toString()

				//版本相同代表版本没发生变化，无需操作
				//版本不同代表版本发生变化，drop掉老版本文件并创建新的配置文件
				when (lastVersion) {
					packageManager.getPackageInfo(packageName, 0).versionName -> Unit
					else -> {
						getSharedPreferences("config", Context.MODE_PRIVATE).edit().putString(
							"lastVersion",
							packageManager.getPackageInfo(packageName, 0).versionName
						).commit()
						ReleaseNotesUtil.showLastReleaseNotes(this)
					}
				}

				binding.swipeRefresh.isRefreshing = true
				getNewPic(this@MainActivity, true)
			}
			//转屏
			true -> try {
				binding.firstPicLoadingTip.visibility = View.INVISIBLE
				Glide.with(this)
					.asBitmap()
					.load(getCurrentImageBitmap())
					.into(binding.mainImageView)
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}


		//下拉刷新获取图片
		val myContext = this
		binding.swipeRefresh.apply {
			setColorSchemeColors(
				resources.getColor(R.color.green),
				resources.getColor(R.color.blue),
				resources.getColor(R.color.yellow),
				resources.getColor(R.color.purple),
				resources.getColor(R.color.orange),
				resources.getColor(R.color.red),
				resources.getColor(R.color.cyan)
			)
			setOnRefreshListener {
				setAlreadySaved(false)
				when (lsp_enabled) {
					false -> getNewPic(this@MainActivity)
					true -> {
						lsp_count++
						when {
							lsp_count < lsp_count_max -> {
								getNewPic(this@MainActivity)
								val description = ActivityManager.TaskDescription("壁纸Bot")
								myContext.setTaskDescription(description)
							}
							else -> {
								lsp_count = 0
								thread {
									val setu = setuList[(setuList.indices).random()]
									val dis = DataInputStream(URL(setu).openStream())
									setCurrentImageBitmap(BitmapFactory.decodeStream(dis))
									setCurrentImageFileName(adjustFileName(setu))

									runOnMainThread {
										val description = ActivityManager.TaskDescription("LSP Bot")
										myContext.setTaskDescription(description)

										Glide.with(context)
											.asBitmap()
											.load(getCurrentImageBitmap())
											.into(binding.mainImageView)
										binding.swipeRefresh.isRefreshing = false
										saveImage(
											"嘿嘿嘿${getCurrentImageFileName()}.jpg",
											getCurrentImageBitmap()!!
										)
										setCustomWallpaper(getCurrentImageBitmap())
										Snackbar.make(
											window.decorView,
											"保存到本地并设置壁纸成功!",
											Snackbar.LENGTH_LONG
										).show()
									}
								}
							}
						}
					}
				}
			}
		}
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.menu_main, menu)
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.saveImage -> {
				when (getCurrentImageBitmap()) {
					null -> Snackbar.make(
						window.decorView,
						"图片没加载完成, 等会儿再试试吧",
						Snackbar.LENGTH_LONG
					).show()
					else -> {
						saveImage("${getCurrentImageFileName()}", getCurrentImageBitmap()!!)
					}
				}
			}
			R.id.aboutAuthor -> startActivity(Intent(this, AboutAuthor::class.java))
			R.id.wallpaperUpdateChannel -> selectChannel(this)
			R.id.sharePic -> share(this, getCurrentImageBitmap(), getCurrentImageFileName())
			R.id.setWallpaper -> when (getCurrentImageBitmap()) {
				null -> Snackbar.make(window.decorView, "图片未加载好, 稍后再试", Snackbar.LENGTH_LONG).show()
				else -> {
					var bottomBar: MaterialDialog? = null
					val linearLayout = LinearLayout(this).apply {
						layoutParams = LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.MATCH_PARENT,
							LinearLayout.LayoutParams.WRAP_CONTENT
						)
						orientation = LinearLayout.VERTICAL
					}
					val btnConfirm = ViewUtils_Kotlin.createBigButton(
						this,
						R.drawable.ic_baseline_double_arrow_24,
						R.color.item_card_bg,
						listOf("确认"),
						MyApplication.dailyBingWallpaperBitmap,
						R.color.black
					) {
						setCustomWallpaper(getCurrentImageBitmap())
						bottomBar?.cancel()
						Unit
					}
					val btnCancel = ViewUtils_Kotlin.createBigButton(
						this,
						R.drawable.ic_baseline_double_arrow_24,
						R.color.item_card_bg,
						listOf("取消"),
						MyApplication.dailyBingWallpaperBitmap,
						R.color.black
					) {
						bottomBar?.cancel()
						Unit
					}

					linearLayout.addView(btnConfirm)
					linearLayout.addView(btnCancel)

					bottomBar = ViewUtils_Kotlin.getBigCardFromBottom(
						this,
						"设置为系统壁纸?",
						linearLayout
					)
					bottomBar.show()
				}
			}
		}
		return super.onOptionsItemSelected(item)
	}


	fun adjustScreenOrientation() {
		//屏幕方向提示
		if (getSharedPreferences(
				"config",
				Context.MODE_PRIVATE
			).getString("wallpaperChannel", "").toString() != channels["二次元3(竖屏)"]
		) {
			if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
			) {
				//竖屏
				setTheme(R.style.Theme_壁纸Bot)
				Snackbar.make(window.decorView, "横屏使用体验更佳~", Snackbar.LENGTH_LONG).show()
			} else {
				//横屏
				window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) ///设置沉浸式状态栏，在MIUI系统中，状态栏背景透明。原生系统中，状态栏背景半透明。
				window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION) //设置沉浸式虚拟键，在MIUI系统中，虚拟键背景透明。原生系统中，虚拟键背景半透明。
			}
		} else {
			if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
			) {
				//竖屏
				window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) ///设置沉浸式状态栏，在MIUI系统中，状态栏背景透明。原生系统中，状态栏背景半透明。
				window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION) //设置沉浸式虚拟键，在MIUI系统中，虚拟键背景透明。原生系统中，虚拟键背景半透明。
			} else {
				//横屏
				setTheme(R.style.Theme_壁纸Bot)
				Snackbar.make(window.decorView, "竖屏使用体验更佳~", Snackbar.LENGTH_LONG).show()
			}
		}
	}

	//	@Deprecated(message = "This method is no longer support and maintain.")
	private fun saveImagePreQ(context: Context, imgFileName: String) {
		Log.e("saveImage", "${pictureFolderPath}$imgFileName")
		try {
			ImageUtils.createFileIfNotExist("${pictureFolderPath}$imgFileName")
			ImageUtils.saveImage("${pictureFolderPath}$imgFileName", getCurrentImageBitmap())
			sendBroadcast(
				Intent(
					Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
					Uri.fromFile(File("${pictureFolderPath}$imgFileName"))
				)
			)
			setAlreadySaved(true)
			Snackbar.make(window.decorView, "保存到本地成功!", Snackbar.LENGTH_LONG).show()
		} catch (e: Exception) {
			e.printStackTrace()
			setAlreadySaved(false)
			Snackbar.make(window.decorView, "保存失败", Snackbar.LENGTH_LONG).show()

			var bottomBar: MaterialDialog? = null
			val linearLayout = LinearLayout(context).apply {
				layoutParams = LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT
				)
				orientation = LinearLayout.VERTICAL
			}
			val btnApp = ViewUtils_Kotlin.createBigButton(
				context,
				R.drawable.ic_baseline_double_arrow_24,
				R.color.item_card_bg,
				listOf("是"),
				MyApplication.dailyBingWallpaperBitmap,
				R.color.black
			) {
				startActivity(Intent().apply {
					action = "android.intent.action.VIEW"
					data = Uri.parse(currentLink)
				})


				bottomBar?.cancel()
				Unit
			}
			linearLayout.addView(btnApp)

			val btnCopy = ViewUtils_Kotlin.createBigButton(
				context,
				R.drawable.ic_baseline_double_arrow_24,
				R.color.item_card_bg,
				listOf("否"),
				MyApplication.dailyBingWallpaperBitmap,
				R.color.black
			) {
				bottomBar?.cancel()
				Unit
			}


			linearLayout.addView(btnCopy)
			bottomBar = ViewUtils_Kotlin.getBigCardFromBottom(
				context,
				"保存失败! 是否到浏览器内下载?",
				linearLayout
			)
			bottomBar.show()
		}
	}

	private fun saveImage(imgFileName: String, imgBitmap: Bitmap) {
		thread {
			when (getAlreadySaved()) {
				true -> Snackbar.make(window.decorView, "图片已存在!", Snackbar.LENGTH_LONG).show()
				false -> {
					when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
						false -> saveImagePreQ(this@MainActivity, imgFileName)
						true -> {
							// Add a media item that other apps shouldn't see until the item is
							// fully written to the media store.
							val resolver = applicationContext.contentResolver

							// Find all audio files on the primary external storage device.
							val imageCollection =
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
									MediaStore.Images.Media.getContentUri(
										MediaStore.VOLUME_EXTERNAL_PRIMARY
									)
								} else {
									MediaStore.Images.Media.EXTERNAL_CONTENT_URI
								}
							val imageDetails = ContentValues().apply {
								put(
									MediaStore.Images.Media.RELATIVE_PATH,
									"${Environment.DIRECTORY_PICTURES}/WallpaperBot"
								)
								put(MediaStore.Images.Media.DISPLAY_NAME, imgFileName)
								put(MediaStore.Images.Media.IS_PENDING, 1)
							}
							val imageContentUri = resolver.insert(imageCollection, imageDetails)
							resolver.openFileDescriptor(imageContentUri!!, "w", null).use { pfd ->
								// Write data into the pending audio file.
								val bos =
									BufferedOutputStream(FileOutputStream(pfd!!.fileDescriptor))
								imgBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos)
								bos.flush()
								bos.close()
							}
// Now that we're finished, release the "pending" status, and allow other apps
// to play the audio track.
							imageDetails.clear()
							imageDetails.put(MediaStore.Images.Media.IS_PENDING, 0)
							resolver.update(imageContentUri, imageDetails, null, null)

							setCurrentImageUri(imageContentUri)
							setAlreadySaved(true)
							Snackbar.make(window.decorView, "保存到本地成功!", Snackbar.LENGTH_LONG).show()
						}
					}
				}
			}
		}
	}

	private fun copyText(text: String) {
		// 创建普通字符型ClipData
		val mClipData = ClipData.newPlainText("Label", text)
		// 将ClipData内容放到系统剪贴板里。
		clipBoardManager.setPrimaryClip(mClipData)
		Snackbar.make(window.decorView, "已复制到剪贴板!", Snackbar.LENGTH_LONG).show()
	}

	private fun adjustFileName(picUrl: String): String = when {
		picUrl.startsWith("http") -> picUrl.substring(picUrl.lastIndexOf('/') + 1)
		picUrl.length > 16 -> picUrl.substring(picUrl.length - 16)
		else -> {
			val newString = StringBuffer()
			val matcher = Pattern.compile("\\w+").matcher(picUrl)
			while (matcher.find()) {
				newString.append(matcher.group())
			}
			var ret = newString.toString()
			when {
				ret.length > 16 -> ret.substring(0, 16)
				else -> ret
			}
		}
	}

	private fun getNewPic(context: Context, startup: Boolean = false) {
		when (TextUtils.isEmpty(currentChannel)) {
			true -> {
				selectChannel(context, "选择你喜欢的渠道:", startup)
			}
			false -> {
				thread {
					runOnMainThread {
						binding.firstPicLoadingTip.visibility = View.VISIBLE
						binding.swipeRefresh.isRefreshing = true
						//加载图的动画
						thread {
							val sleepPeriod = 400L
							val list = listOf(
								"图来啦...\n___(:з 」∠)_",
								"图来啦...\n___(:з」 ∠)_",
								"图来啦...\n__(:з 」∠)__",
								"图来啦...\n__(:з」 ∠)__",
								"图来啦...\n_(:з 」∠)___",
								"图来啦...\n__(:з 」∠)__"
							)
							while (binding.swipeRefresh.isRefreshing) {
								for (str in list) {
									if (binding.swipeRefresh.isRefreshing) {
										runOnUiThread {
											binding.firstPicLoadingTip.text = str
										}
										Thread.sleep(sleepPeriod)
									}
								}
							}

						}
					}
					try {
						//调用API获取重定向的图片URL
//				Log.e("channel", currentChannel)
						var apiResponsePic: String? = ""
						val client = OkHttpClient()
						val request = Request.Builder()
							.url(currentChannel)
							.build()
						val response = client.newCall(request).execute()
						apiResponsePic = response.request.url.toString()
						currentLink = apiResponsePic
						Log.e("okhttp", "$apiResponsePic")

						setCurrentImageFileName(adjustFileName(apiResponsePic))
						Log.e("filename", getCurrentImageFileName())
						val dis = DataInputStream(URL(apiResponsePic).openStream())
						setCurrentImageBitmap(BitmapFactory.decodeStream(dis))

						//设图片
						runOnMainThread {
							try {
								binding.firstPicLoadingTip.visibility = View.INVISIBLE
								Glide.with(context)
									.asBitmap()
									.load(getCurrentImageBitmap())
									.into(binding.mainImageView)
							} catch (e: Exception) {
								e.printStackTrace()
							}
						}
					} catch (e: Exception) {
						Snackbar.make(window.decorView, "无网络", Snackbar.LENGTH_LONG).show()
						runOnUiThread {
							binding.firstPicLoadingTip.text = "无网络\n_(´ཀ`」 ∠)__ \n联网后下拉刷新重试"
						}
						e.printStackTrace()
					} finally {
						runOnUiThread {
							binding.swipeRefresh.isRefreshing = false
						}
					}
				}
			}
		}
	}

	fun setCustomWallpaper(bitmap: Bitmap?) {
		thread { wallpaperManager.setBitmap(bitmap) }
		Snackbar.make(window.decorView, "已设置为壁纸", Snackbar.LENGTH_LONG).show()
	}

	fun selectChannel(context: Context, customTitle: String? = null, startup: Boolean = false) {
		var bottomBar: MaterialDialog? = null
		val linearLayout = LinearLayout(context).apply {
			layoutParams = LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
			)
			orientation = LinearLayout.VERTICAL
		}
		val list = LinkedList<MaterialCardView>()
		for ((tag, channel) in channels) {
			val btnChannel = ViewUtils_Kotlin.createBigButton(
				context, R.drawable.ic_baseline_double_arrow_24,
				R.color.item_card_bg, listOf(tag), MyApplication.dailyBingWallpaperBitmap, R.color.black
			) {
				context.getSharedPreferences("config", Context.MODE_PRIVATE).edit()
					.apply {
						putString("wallpaperChannel", channel)
						commit()
					}
				currentChannel = channel
				getNewPic(context)
				bottomBar?.cancel()
				if (startup) getNewPic(context, startup)
				Unit
			}
			list.add(btnChannel)
		}
		for (view in list) {
			linearLayout.addView(view)
		}
		val title = when (customTitle) {
			null -> "切换更新渠道\t当前渠道: ${getKeyOfMap(currentChannel, channels)}"
			else -> customTitle
		}
		bottomBar = ViewUtils_Kotlin.getBigCardFromBottom(
			context,
			title,
			linearLayout
		)
		bottomBar.show()
	}

	fun share(context: Context, imgBitmap: Bitmap?, imgFileName: String) {
		when (imgBitmap) {
			null -> Snackbar.make(window.decorView, "图片未加载好, 等一下再点分享吧", Snackbar.LENGTH_LONG).show()
			else -> {
				var bottomBar: MaterialDialog? = null
				val linearLayout = LinearLayout(context).apply {
					layoutParams = LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT
					)
					orientation = LinearLayout.VERTICAL
				}
				val btnApp = ViewUtils_Kotlin.createBigButton(
					context,
					R.drawable.ic_baseline_double_arrow_24,
					R.color.item_card_bg,
					listOf("分享到应用"),
					MyApplication.dailyBingWallpaperBitmap,
					R.color.black
				) {
//					val filePath = "$pictureFolderPath$imgFileName"
//					saveImage(imgFileName, imgBitmap)
//					val imgFile = File(filePath)
//					val myIntent = Intent().setAction(Intent.ACTION_SEND)
//					if (imgFile.exists()) {
//						//7.0开始，需通过FileProvider共享文件
//						if (Build.VERSION.SDK_INT >= 24) {
//							val uri = FileProvider.getUriForFile(
//								context,
//								"com.coolest.wallpaper.bot.fileprovider",
//								imgFile
//							)
//							// intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//							myIntent.putExtra(Intent.EXTRA_STREAM, uri)
//						} else {
//							val uri = Uri.fromFile(imgFile)
//							myIntent.putExtra(Intent.EXTRA_STREAM, uri)
//						}
//					}
//					myIntent.type = "image/*"
//					val share = Intent.createChooser(myIntent, "分享图片到...")
//					startActivity(share)

					if (!getAlreadySaved()) saveImage(imgFileName, imgBitmap)
					val myIntent = Intent().setAction(Intent.ACTION_SEND)
					when {
						Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
							myIntent.putExtra(Intent.EXTRA_STREAM, getCurrentImageUri())
						}
						Build.VERSION.SDK_INT >= 24 -> {
							val uri = FileProvider.getUriForFile(
								context,
								"com.coolest.wallpaper.bot.fileprovider",
								File("$pictureFolderPath$imgFileName")
							)
							myIntent.putExtra(Intent.EXTRA_STREAM, uri)
						}
						else -> {
							val uri = Uri.fromFile(File("$pictureFolderPath$imgFileName"))
							myIntent.putExtra(Intent.EXTRA_STREAM, uri)
						}
					}
					myIntent.type = "image/*"
					val share = Intent.createChooser(myIntent, "分享图片到...")
					startActivity(share)


					bottomBar?.cancel()
					Unit
				}
				linearLayout.addView(btnApp)
				val btnCopy = ViewUtils_Kotlin.createBigButton(
					context,
					R.drawable.ic_baseline_double_arrow_24,
					R.color.item_card_bg,
					listOf("复制链接"),
					MyApplication.dailyBingWallpaperBitmap,
					R.color.black
				) {
					copyText(currentLink)
					bottomBar?.cancel()
					Unit
				}


				linearLayout.addView(btnCopy)
				bottomBar = ViewUtils_Kotlin.getBigCardFromBottom(
					context,
					"分享...",
					linearLayout
				)
				bottomBar.show()
			}
		}

	}

	private fun getKeyOfMap(value: String?, map: Map<String, String>): String {
		for ((key, valu) in map) {
			if (value == valu) {
				return key
			}
		}
		return "Unknown"
	}

}